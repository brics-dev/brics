#!/bin/python3

import os
import re
import psycopg2
import inspect

import PostgresUtil
import CommonUtil

from LogUtil import LogDump

################################################
# class SysUser
################################################

class SysUser(object):

    def __init__(self, argUserId, argFirstName, argLastName, argEmail):

        self.branchId = argUserId
        self.firstName = argFirstName
        self.lastName = argLastName
        self.email = argEmail

################################################
# class SysBranch
################################################

class SysBranch(object):

    def __init__(self, argBranchId, argMajor, argMinor, argVersion, argIsTrunk, \
                 argReleaseDate, argStatus, argFailRevId):

        self.branchId = argBranchId
        self.major = argMajor
        self.minor = argMinor
        self.version = argVersion
        self.isTrunk = argIsTrunk
        self.releaseDate = argReleaseDate
        self.status = argStatus
        self.failRevId = argFailRevId

    ################################################
    # SysBranch.update
    ################################################

    def update(self, argDbCxn):

        sql = "UPDATE sys_branch SET version = %(version)s, status = %(status)s, " \
                "fail_revision_id = %(fail_revision_id)s WHERE id = %(id)s"

        vals = { 'version': self.version, 'status': self.status, 'fail_revision_id': self.failRevId, \
                 'id': self.branchId}

        cmdResult = PostgresUtil.executeStatement(argDbCxn, sql, vals)

        result = cmdResult.success

        return result

################################################
# class SysInstall
################################################

class SysInstall(object):

    def __init__(self, argBranchId, argInstallType, argPriorVersion, argUserId,
                 argInstallId=None, argPostVersion=None, argStatus=1, argFailRevId=None,
                 argErrorMsg=None, argCreateTime=None):

        self.branchId = argBranchId
        self.installType = argInstallType
        self.priorVersion = argPriorVersion
        self.userId = argUserId

        self.installId = argInstallId
        self.postVersion = argPostVersion
        self.status = argStatus
        self.failRevId = argFailRevId
        self.errorMsg = argErrorMsg
        self.createTime = argCreateTime

    ################################################
    # SysInstall.persist
    ################################################

    def persist(self, argDbCxn):

        joinStr = ', '.join(['%s'] * 5)

        template = "INSERT INTO sys_install (branch_id, install_type, prior_version, user_id, status) " \
                "VALUES ({}) RETURNING id".format(joinStr)

        insVals = [ self.branchId, self.installType, self.priorVersion, self.userId, self.status ]

        cmdResult = PostgresUtil.executeStatement(argDbCxn, template, insVals, 0)

        if cmdResult.success:
            self.installId = cmdResult.data
            return 0
        else:
            return 6

    ################################################
    # SysInstall.update
    ################################################

    def update(self, argDbCxn):

        sql = "UPDATE sys_install SET post_version = %(post_version)s, status = %(status)s, fail_revision_id = %(fail_revision_id)s, " \
                    "error_msg = %(error_msg)s, update_time = now() WHERE id = %(id)s;"

        vals = { 'post_version':self.postVersion, 'status':self.status, \
                 'fail_revision_id':self.failRevId, 'error_msg':self.errorMsg, 'id':self.installId}

        cmdResult = PostgresUtil.executeStatement(argDbCxn, sql, vals)

        result = cmdResult.success

        return result

################################################
# class SysCommand
################################################

class SysCommand(object):

    def __init__(self, argRevisionId, argOrdinal, argCommand, argStatus):

        self.revisionId = argRevisionId
        self.ordinal = argOrdinal
        self.command = argCommand

        self.commandId = "%s-%d" % (argRevisionId, self.ordinal)
        self.status = argStatus
        self.errorMsg = None

    ################################################
    # SysCommand.apply
    ################################################

    def apply(self, argDbCxn, argLogger):

        cmdApplyLogDump = LogDump()
        cmdApplyLogDump.addSimpleEntry("EXECUTING command %s:\n\n%s" % (self.commandId, self.command))

        cmdResult = PostgresUtil.executeStatement(argDbCxn, self.command)
        logResult = cmdResult.success

        if not logResult:
            cmdApplyLogDump.addSimpleEntry("\n" + cmdResult.error)

        cmdApplyLogDump.addEntry("\nRESULT", logResult)

        argLogger.dumpLog(cmdApplyLogDump)
        return cmdResult

    ################################################
    # SysCommand.applyAndPersist
    ################################################

    def applyAndPersist(self, argDbCxn, argLogger):

        applyResult = self.apply(argDbCxn, argLogger)

        if applyResult.success:
            self.status = 0
        else:
            self.errorMsg = applyResult.error
            self.status = 2

        joinStr = ', '.join(['%s'] * 5)

        template = 'INSERT INTO sys_command (id, revision_id, ordinal, command, status) VALUES ({})'.format(joinStr)

        insVals = [ self.commandId, self.revisionId, self.ordinal, self.command, self.status ]

        persistResult = PostgresUtil.executeStatement(argDbCxn, template, insVals)
        result = persistResult.success

        return result

################################################
# class SysRevision
################################################

class SysRevision(object):

    def __init__(self, argRevisionId, argOrdinal, argLabel, argUserId, argChecksum,
                 argInstallId=None, argDesc=None, argBaseId=None, argStatus=1,
                 argCreateTime=None, argPersisted=False):

        self.revisionId = argRevisionId
        self.ordinal = argOrdinal
        self.label = argLabel
        self.userId = argUserId
        self.installId = argInstallId
        self.description = argDesc
        self.baseId = argBaseId
        self.status = argStatus
        self.checksum = argChecksum
        self.createTime = argCreateTime
        self.errorMsg = None

        self.branchId = None

        branchIdRegex = re.match(r"^(\d+\.\d+)\.\d+", self.revisionId)

        if branchIdRegex is not None:
            self.branchId = branchIdRegex.group(1)

        self.persisted = argPersisted

        self.commands = []

    ################################################
    # SysRevision.getFileName
    ################################################

    def getFileName(self):

        elems = (self.revisionId, self.label, self.userId, "fwd")

        result = ".".join(elems)

        return result

    ################################################
    # SysRevision.update
    ################################################

    def update(self, argDbCxn):

        template = "UPDATE sys_revision SET status = %(status)s, file_md5sum = %(checksum)s WHERE id = %(id)s"

        vals = { 'status':self.status, 'checksum':self.checksum, 'id':self.revisionId }

        cmdResult = PostgresUtil.executeStatement(argDbCxn, template, vals)

        result = cmdResult.success

        return result

    ################################################
    # SysRevision.apply
    ################################################

    def apply(self, argDbCxn, argLogger):

        revFileName = self.getFileName()

        argLogger.dumpLine("APPLYING: %s" % revFileName)
        result = True

        for command in self.commands:
            # unit.apply(argDbCxn)
            cmdApplyResult = command.apply(argDbCxn, argLogger)

            if not cmdApplyResult.success:
                result = False
                break

        revEndLogDump = LogDump(True)
        revEndLogDump.addSimpleEntry("%s RESULT: %s" % (revFileName, result))
        argLogger.dumpLog(revEndLogDump)

        return result

    ################################################
    # SysRevision.applyAndPersist
    ################################################

    def applyAndPersist(self, argDbCxn, argLogger):

        if not self.persisted:
            self.persist(argDbCxn)

        '''
        for command in self.commands:
            # unit.apply(argDbCxn)
            command.applyAndPersist(argDbCxn, argLogger)

            self.status = command.status

            if self.status != 0:
                self.errorMsg = command.errorMsg
                break
        '''

        self.update(argDbCxn)

        return self.status

    ################################################
    # SysRevision.persist
    ################################################

    def persist(self, argDbCxn):

        joinStr = ', '.join(['%s'] * 9)

        template = "INSERT INTO sys_revision (id, ordinal, label, user_id, install_id, " \
                "description, base_id, status, file_md5sum) " \
                "VALUES ({})".format(joinStr)

        insVals = [ self.revisionId, self.ordinal, self.label, self.userId, self.installId, \
                   self.description, self.baseId, self.status, self.checksum ]

        cmdResult = PostgresUtil.executeStatement(argDbCxn, template, insVals)

        result = cmdResult.success
        return result

    ################################################
    # SysRevision.deleteCommands
    ################################################

    def deleteCommands(self, argDbCxn):

        template = "DELETE FROM sys_command WHERE revision_id = '%s'" % (self.revisionId)

        cmdResult = PostgresUtil.executeStatement(argDbCxn, template)

        result = cmdResult.success

        return result

    ################################################
    # SysRevision.loadCommands
    ################################################

    def loadCommands(self, argRevFileDir):

        fileName = self.getFileName()

        #revFileDir = argRevFileDir if argRevFileDir is not None else globQueuePath

        filePath = os.path.join(argRevFileDir, fileName)

        with open(filePath, 'r', encoding='UTF-8') as fileHandle:
            fileCont = fileHandle.read()

            crudeCommands = fileCont.split(";")

            cmdOrdinal = 0

            for crudeCommand in crudeCommands:

                commandStr = crudeCommand.strip()

                if not cmdOrdinal:
                    commentRegex = re.match(r"\/\*(.+)\*\/", commandStr)

                    if commentRegex is not None:
                        crudeComment = commentRegex.group(1)

                        if crudeComment is not None:
                            self.description = crudeComment.strip()

                if commandStr:
                    cmdOrdinal += 1
                    rvsnCmd = SysCommand(self.revisionId, cmdOrdinal, commandStr, 1)
                    self.commands.append(rvsnCmd)

################################################
# createTentativeRevision
################################################

def createTentativeRevision(argFwdFilePath):

    #filePath = os.path.join(globQueuePath, argFwdFileName)

    fileChecksum = CommonUtil.getFileChecksum(argFwdFilePath)

    fwdFileName = os.path.basename(argFwdFilePath)

    fileShortName = fwdFileName[0:-4]

    fileNameTokens = fileShortName.split(".")

    fileTokenSize = len(fileNameTokens)

    if fileTokenSize == 5:

        branchId = "%s.%s" % (fileNameTokens[0], fileNameTokens[1])
        ordinalStr = fileNameTokens[2]

        changeLabel = fileNameTokens[3]
        userId = fileNameTokens[4]

        try:
            ordinal = int(ordinalStr)
            changeId = "%s.%s" % (branchId, ordinalStr)
        except ValueError:
            print("Invalid ordinal: %s" % ordinalStr)
            return

        result = SysRevision(changeId, ordinal, changeLabel, userId, fileChecksum)

        return result

    else:
        raise ValueError("Invalid change unit: %s" % fwdFileName)


################################################
# resetVersionModule
################################################

def resetVersionModule(argDbCxn, argJobConfig, argInitFile=None):

    logger = argJobConfig.logger
    procUserID = argJobConfig.userID
    initFilePath = getInitFilePath(argInitFile)
    initRev = createTentativeRevision(initFilePath)
    #initRev.userId = procUserID
    scriptPathDir = os.path.dirname(initFilePath)
    initRev.loadCommands(scriptPathDir)
    initRevResult = initRev.apply(argDbCxn, logger)

    #crntBranch = getCurrentBranch(argDbCxn)

    install = SysInstall('0.0', 'INI', None, procUserID)

    install.persist(argDbCxn)

    initRev.installId = install.installId
    initRev.persist(argDbCxn)

    result = 0 if initRevResult else 1

    return result

################################################
# getUsers
################################################

def getUsers(argDbCxn):

    result = {}

    try:
        cur = argDbCxn.cursor()

        sql = "SELECT id, first_name, last_name, email FROM sys_user WHERE is_active = true;"

        cur.execute(sql)
        rows = cur.fetchall()

        for row in rows:

            userId = row[0]
            firstName = row[1]
            lastName = row[2]
            email = row[3]

            user = SysUser(userId, firstName, lastName, email)
            result[userId] = user
    except psycopg2.Error as err:
        argDbCxn.rollback()
        raise err
    finally:
        cur.close()

    return result

################################################
# getCurrentBranchId
################################################

def getCurrentBranch(argDbCxn):

    try:
        cur = argDbCxn.cursor()

        sql = "SELECT id, major, minor, version, release_date, status, fail_revision_id " \
                "FROM sys_branch WHERE is_trunk = true;"

        cur.execute(sql)
        rows = cur.fetchall()

        for row in rows:

            branchId = row[0]
            major = row[1]
            minor = row[2]
            version = row[3]
            releaseDate = row[4]
            status = row[5]
            failRevId = row[6]

            result = SysBranch(branchId, major, minor, version, True, releaseDate, status, failRevId)

    finally:
        cur.close()

    return result

################################################
# getRevisionById
################################################

def getRevisionById(argDbCxn, argRevisionId):

    try:
        cur = argDbCxn.cursor()

        sql = "SELECT ordinal, label, user_id, install_id, description, " \
                "base_id, status, file_md5sum, create_time " \
                "FROM sys_revision r WHERE id = '%s'" % argRevisionId

        cur.execute(sql)
        rows = cur.fetchall()

        for row in rows:

            ordinal = row[0]
            label = row[1]
            userId = row[2]
            installId = row[3]
            description = row[4]
            baseId = row[5]
            status = row[6]
            checksum = row[7]
            createTime = row[8]

            result = SysRevision(argRevisionId, ordinal, label, userId, checksum, installId, \
                                 description, baseId, status, createTime, True)

    finally:
        cur.close()

    return result

################################################
# getAdminTableNames
################################################

def getAdminTableNames():

    #result = ('sys_branch', 'sys_command', 'sys_install', 'sys_revision', 'sys_user')
    result = ()

    return result

################################################
# getAdminSequenceNames
################################################

def getAdminSequenceNames():

    #result = ('sys_install_seq')
    result = ()

    return result

################################################
# getInitFilePath
################################################

def getInitFilePath(argInitFile=None):

    initFile = argInitFile

    if initFile is None:
        initFile = "0.0.000.init.buildman.fwd"

    #scriptPath = inspect.stack()[0][1]
    #scriptPathDir = os.path.dirname(scriptPath)
    #result = os.path.join(scriptPathDir, "0.0.000.init.system.fwd")
    result = CommonUtil.getResourceFilePath(initFile)

    return result