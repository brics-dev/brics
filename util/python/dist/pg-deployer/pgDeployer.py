#!/usr/bin/python3

import argparse
import configparser

import os
import sys

import CommonUtil
import JobUtil
import PostgresAdmin
import PostgresUtil
import ConfigUtil

from LogUtil import LogDump
from PostgresAdmin import SysInstall



################################################
# createJobArgParser
################################################

def createJobArgParser():

    result = argparse.ArgumentParser()

    result.add_argument('-p', '--props', help='Properties file path', required=True)
    result.add_argument('-m', '--mode', help='Execution mode (optional)', required=False, default="manual")
    result.add_argument('-r', '--reset', help='Reset', action="store_true")
    result.add_argument('-i', '--init', help='Init file', required=False)

    result.add_argument('-l', '--logDir', help='Log dir', required=False)
    result.add_argument('-w', '--workDir', help='Work dir', required=False)

    return result
    
def executeSqlFile(conn, workDir, sqlFile, logger):
    '''
    Executes the given SQL file on the passed in database connection.
    
    @param conn: The connection to the database to run the statements in the SQL file.
    @param workDir: The path to the working directory where all of the SQL files are stored.
    @param sqlFile: The name of the SQL file that will be executed.
    @param logger: The logging object used to send messages to the log file.
    '''
    
    filePath = os.path.join(workDir, sqlFile)
    
    with open(filePath, 'r', encoding='UTF-8') as file:
        with conn:
            with conn.cursor() as curs:
                jobLogger.dumpLine("Executing SQL statements in <%s>..." % sqlFile)
                curs.execute(file.read())

################################################
# main
################################################

if __name__ == '__main__':

    argparser = createJobArgParser()
    args = argparser.parse_args()
    propFile = args.props
    execMode = args.mode
    doReset = args.reset
    initFile = args.init

    config = configparser.ConfigParser()
    config.read(propFile)

    if config.has_section('FILE-SYS'):

        fileSysConfHash = config['FILE-SYS']
    else:
        fileSysConfHash = {}
        fileSysConfHash['logDir'] = args.logDir
        fileSysConfHash['workDir'] = args.workDir

    fileSysConf = ConfigUtil.getFileSysConfig(fileSysConfHash)
    jobConfig = JobUtil.initJob("PG DEPLOYER", execMode, fileSysConf)

    if jobConfig is None:
        sys.exit(1)

    errorMsg = None
    jobLogger = jobConfig.logger

    connConf = ConfigUtil.getPostgresConfig(config['DB-TRG'])
    dbCxn = PostgresUtil.getConnection(connConf, jobLogger)

    if dbCxn is None:
        sys.exit(1)

    if doReset:
        resetResult = PostgresAdmin.resetVersionModule(dbCxn, jobConfig, initFile)
        sys.exit(resetResult)

    userMap = None

    try:
        userMap = PostgresAdmin.getUsers(dbCxn)
    except:
        resetResult = PostgresAdmin.resetVersionModule(dbCxn, jobConfig, initFile)
        userMap = PostgresAdmin.getUsers(dbCxn)

    procUserID = jobConfig.userID
    procUser = userMap.get(procUserID)

    if procUser is None:
        errorMsg = "Invalid user: <%s>" % procUserID
        jobLogger.dumpError(errorMsg)
        sys.exit(1)

    crntBranch = PostgresAdmin.getCurrentBranch(dbCxn)
    baseVersion = crntBranch.version
    baseRevisionId = None
    baseOrd = None

    if baseVersion is not None:
        baseRevision = PostgresAdmin.getRevisionById(dbCxn, baseVersion)
        baseOrd = baseRevision.ordinal
        baseRevisionId = baseRevision.revisionId

    execOrderMap = {}
    failRevId = crntBranch.failRevId
    failRev = None
    failFix = False
    fileError = False

    if failRevId is not None:
        failFix = True
        failRev = PostgresAdmin.getRevisionById(dbCxn, failRevId)
        baseOrd = failRev.ordinal

    workDir = fileSysConf.workDir
    fwdFileDocList = CommonUtil.getDirectoryFileList(workDir, "FWD", 1)
    queueScanInitDump = LogDump(jobConfig)
    queueScanInitDump.addSimpleEntry("Queue scan:\n")
    jobLogger.dumpLog(queueScanInitDump)
    installStatus = 0
    queueNonEmpty = False

    for fwdFileDoc in fwdFileDocList:

        queueNonEmpty = True
        fwdFilePath = os.path.join(workDir, fwdFileDoc['relDirPath'], fwdFileDoc['fileName'])
        
        # Uncomment line below for debugging.
        # jobLogger.dumpLine("Processing input file: <%s>" % fwdFilePath)

        try:
            revision = PostgresAdmin.createTentativeRevision(fwdFilePath)
            tentatBranchId = revision.branchId

            if tentatBranchId != crntBranch.branchId:
                tentatOrd = revision.ordinal

                if tentatOrd == 999:
                    continue
                else:
                    errorMsg = "Bad branch ID: <%s> in file <%s>. Current branch is <%s>" % \
                               (tentatBranchId, fwdFileDoc['fileName'], crntBranch.branchId)
                    jobLogger.dumpError(errorMsg)
                    break

            tentatUserId = revision.userId
            tentatUser = userMap.get(tentatUserId)

            if tentatUser is None:
                errorMsg = "Bad user ID: <%s> in file <%s>." % (tentatUserId, fwdFileDoc['fileName'])
                jobLogger.dumpError(errorMsg)
                break

            tentatRevId = revision.revisionId
            tentatRevOrd = revision.ordinal
            failFixRev = False

            if failFix and tentatRevId == failRevId:
                failFixRev = True
                revision = failRev
                revision.deleteCommands(dbCxn)

            if (baseOrd is None) or (tentatRevOrd > baseOrd) or failFixRev:
                jobLogger.dumpLine("Queing input file <%s>" % fwdFileDoc['fileName'])
                execOrderMap[tentatRevOrd] = revision
                
                # Apply database changes directly from the file.
                try:
                    executeSqlFile(dbCxn, workDir, fwdFileDoc['fileName'], jobLogger)
                    revision.status = 0
                    revision.errorMsg = None
                except psycopg2.Error as err:
                    jobLogger.dumpError(err.pgerror)
                    revision.errorMsg = err.pgerror
                    revision.status = -1
                    break

            else:
                jobLogger.dumpLine("Skipping input file <%s>: Already processed" % fwdFileDoc['fileName'])
        except:

            fileError = True
            installStatus = 5
            execOrderMap.clear()
            errorMsg = "Badly formatted file name: <%s>" % fwdFileDoc['fileName']
            jobLogger.dumpError(errorMsg)
            break

    hasChanges = False
    changeExecDump = LogDump()

    if not queueNonEmpty:
        changeExecDump.addSimpleEntry("Queue empty")

    if bool(execOrderMap):

        hasChanges = True
        changeExecDump.addSimpleEntry("Changes found")

    else:
        if queueNonEmpty:
            changeExecDump.addSimpleEntry("No changes to deploy")

    jobLogger.dumpLog(changeExecDump)
    recordInstall = hasChanges or fileError
    postVersion = crntBranch.version

    if recordInstall:
        install = SysInstall(crntBranch.branchId, 'FWD', baseVersion, procUserID)
        install.persist(dbCxn)

        for execOrd, revision in sorted(execOrderMap.items()):
            revision.baseId = baseRevisionId
            revision.installId = install.installId
            revision.applyAndPersist(dbCxn, jobLogger)
            installStatus = revision.status

            if revision.status == 0:
                postVersion = revision.revisionId
                baseRevisionId = revision.revisionId
                failRevId = None
            else:
                failRevId = revision.revisionId
                errorMsg = revision.errorMsg
                break

        install.postVersion = postVersion
        install.status = installStatus
        install.failRevId = failRevId
        install.errorMsg = errorMsg
        install.update(dbCxn)

        crntBranch.version = postVersion
        crntBranch.status = installStatus
        crntBranch.failRevId = failRevId
        crntBranch.update(dbCxn)

    procEndDump = LogDump(jobConfig)
    procEndDump.addEntry("PROCESS COMPLETED", installStatus)
    procEndDump.addEntry("VERSION", postVersion)
    jobLogger.dumpLog(procEndDump)
    JobUtil.endJob(jobConfig)

    sys.exit(installStatus)
