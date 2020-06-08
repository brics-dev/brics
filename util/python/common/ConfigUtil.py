#!/usr/bin/python3

import os
import sys
from LogUtil import LogDump, Logger

################################################
# JobConfig
################################################

class JobConfig(object):

    def __init__(self, argJobName, argUserID, argBeginTime, argWorkDir, \
                 argLogFile, argStopFile, argContFile):

        self.name = argJobName
        self.userID = argUserID
        self.beginTime = argBeginTime
        self.isContinued = False
        self.startOffset = None

        self.logFile = argLogFile
        self.stopFile = argStopFile
        self.contFile = argContFile

        #self.queueDir = argQueueDir
        self.workDir = argWorkDir

        self.keyValCache = {}

        self.logger = Logger(argLogFile)

    def addCategoryCache(self, argCategLabel, argCache):
        self.keyValCache[argCategLabel] = argCache

    def getCategoryCache(self, argCategLabel):
        result = self.keyValCache.get(argCategLabel)
        return result


    def doDump(self):
        result = self.workDir != None
        return result

################################################
# VirtuosoConfig
################################################

class VirtuosoConfig(object):

    def __init__(self, argLabel, argServiceURL, argGraph, argIsqlHost, argIsqlPort,
                 argIsqlUser, argIsqlPass, argBatchMax):
        self.label = argLabel
        self.serviceURL = argServiceURL
        self.graph = argGraph
        self.isqlHost = argIsqlHost
        self.isqlPort = argIsqlPort
        self.isqlUser = argIsqlUser
        self.isqlPass = argIsqlPass
        self.batchMax = argBatchMax

    def dumpPropertyLog(self, argLogger):
        propLogDump = LogDump(False)
        propLogDump.addEntry("%s SERVICE URL" % self.label, self.serviceURL)
        propLogDump.addEntry("%s GRAPH" % self.label, self.graph)
        propLogDump.addEntry("%s ISQL HOST" % self.label, self.isqlPort)
        propLogDump.addEntry("%s ISQL PORT" % self.label, self.isqlPort)
        propLogDump.addEntry("%s ISQL USER" % self.label, self.isqlUser)
        propLogDump.addEntry("%s BATCH MAX" % self.label, self.batchMax)
        argLogger.dumpLog(propLogDump)


################################################
# PostgresConfig
################################################

class PostgresConfig(object):

    def __init__(self, argLabel, argHost, argDatabase, argUser, argSchema, argInitFile):

        self.label = argLabel
        self.host = argHost
        self.database = argDatabase
        self.user = argUser
        self.schema = argSchema
        self.initFile = argInitFile

    def dumpPropertyLog(self, argConnResult, argLogger):
        propLogDump = LogDump(False)
        propLogDump.addEntry("%s HOST" % self.label, self.host)
        propLogDump.addEntry("%s DABABASE" % self.label, self.database)
        propLogDump.addEntry("%s DB USER" % self.label, self.user)
        propLogDump.addEntry("%s DB SCHEMA" % self.label, self.schema)

        if self.initFile is not None:
            propLogDump.addEntry("%s INIT FILE" % self.label, self.initFile)

        propLogDump.addEntry("%s DB CONNECT" % self.label, argConnResult)
        argLogger.dumpLog(propLogDump)

################################################
# SshConfig
################################################

class SshConfig(object):

    def __init__(self, argHost, argUser):
        self.host = argHost
        self.user = argUser
        #self.keyFile = argKeyFile

    def dumpPropertyLog(self, argConnResult, argLogger):
        propLogDump = LogDump(False)
        propLogDump.addEntry("SSH HOST", self.host)
        propLogDump.addEntry("SSH USER", self.user)
        #propLogDump.addEntry("SSH KEY FILE", self.keyFile)
        propLogDump.addEntry("SSH CONNECT", argConnResult)
        argLogger.dumpLog(propLogDump)

################################################
# FileSysConfig
################################################

class FileSysConfig(object):

    def __init__(self, argLogDir, argWorkDir=None,
            argSecFile=None, argInstFile=None, argImpDir=None):

        self.logDir = argLogDir
        self.workDir = argWorkDir
        self.instFile = argInstFile
        self.secFile = argSecFile

        if argImpDir is not None:
            self.impDir = argImpDir
        else:
            self.impDir = self.workDir

################################################
# DeployConfig
################################################

class DeployConfig(object):

    def __init__(self, argInstFile, argTrgRoot, argSrcRoot=None, argReplVal=None):

        self.instFile = argInstFile
        self.trgRoot = argTrgRoot
        self.replVal = argReplVal

        if argSrcRoot is not None:
            self.srcRoot = argSrcRoot
        else:
            self.srcRoot = os.path.dirname(argInstFile)

    def dumpPropertyLog(self, argLogger):

        propLogDump = LogDump(False)

        if self.instFile is None:
            propLogDump.addEntry("INSTRUCTION FILE", self.instFile)

        propLogDump.addEntry("SOURCE ROOT", self.srcRoot)
        propLogDump.addEntry("TARGET ROOT", self.trgRoot)

        if not self.replVal is None:
            propLogDump.addEntry("REPLACEMENT VALUE", self.replVal)

        argLogger.dumpLog(propLogDump)

    def setSshClient(self, argSshClient):
        self.sshClient = argSshClient

################################################
# PasswordCache
################################################

class PasswordCache(object):

    def __init__(self):
        self.cache = {}

    def addCategory(self, argLabel, argProps):
        self.cache[argLabel] = argProps

    def getValue(self, argCategLabel, argKey):
        result = None
        categProps = self.cache[argCategLabel]

        if categProps is not None:
            result = categProps.get(argKey)

        return result

################################################
# getVirtuosoConfig
################################################

def getVirtuosoConfig(argLabel, argProps, argPassCache=None):

    serviceURL = argProps['serviceURL']
    graph = argProps['graph']
    isqlHostStr = argProps.get('isqlHost')
    isqlPortStr = argProps.get('isqlPort')
    isqlUser = argProps.get('isqlUser')
    batchMaxStr = argProps.get('batchMax')
    isqlPass = None

    if isqlPortStr is not None:
        isqlPort = int(isqlPortStr)
    else:
        isqlPort = 1111

    if isqlHostStr is not None:
        isqlHost = isqlHostStr
    else:
        isqlHost = "localhost"

    if batchMaxStr is not None:
        batchMax = int(batchMaxStr)
    else:
        batchMax = 5000

    if argPassCache is not None:
        passwdPropKey = "%s:%d:VAD:%s" % (isqlHost, isqlPort, isqlUser)
        isqlPass = argPassCache.get(passwdPropKey)

    result = VirtuosoConfig(argLabel, serviceURL, graph, isqlHost, isqlPort,
                            isqlUser, isqlPass, batchMax)

    return result

################################################
# getPostgresConfig
################################################

def getPostgresConfig(argProps):

    host = argProps['host']
    database = argProps['database']
    user = argProps['user']
    schema = argProps['schema']
    label = argProps['label']
    initFile = argProps.get('initFile')

    result = PostgresConfig(label, host, database, user, schema, initFile)

    return result

################################################
# getSshConfig
################################################

def getSshConfig(argProps):

    host = argProps['host']
    user = argProps['user']
    #keyFile = argProps['keyFile']

    result = SshConfig(host, user)

    return result

################################################
# getFileSysConfig
################################################

def getFileSysConfig(argProps):

    logDir = argProps['logDir']
    workDir = argProps.get('workDir')
    impDir = argProps.get('impDir')
    secFile = argProps.get('secFile')
    instFile = argProps.get('instFile')

    result = FileSysConfig(logDir, workDir, secFile, instFile, impDir)

    return result

################################################
# getDeployConfig
################################################

def getDeployConfig(argProps):

    trgRoot = argProps['trgRoot']

    instFile = argProps.get('instFile')
    srcRoot = argProps.get('srcRoot')
    replVal = argProps.get('replVal')

    result = DeployConfig(instFile, trgRoot, srcRoot, replVal)

    return result

################################################
# loadPasswordFile
################################################

def loadPasswordFile(argFilePath):
    passFileRhndl = open(argFilePath, 'r')

    passFileLines = passFileRhndl.readlines()

    result = {}

    lineNo = 0

    for passFileEntry in passFileLines:

        lineNo = lineNo + 1

        if passFileEntry.startswith('#'):
            continue

        passFileEntryClean = passFileEntry.rstrip()
        lineList = passFileEntryClean.split(':')
        cxnParamList = lineList[0:4]
        cxnKey = ":".join(cxnParamList)

        try:
            passwd = lineList[4]
            result[cxnKey] = passwd
        except IndexError:
            print("Error loading password from line %d, file %s" % (lineNo, argFilePath))
            sys.exit(1)




    return result

################################################
# getSshCommandList
################################################

def getSshCommandList(argProps):

    result = []

    cmdIndx = 0

    cmdVar = None

    while (cmdVar is not None) or (cmdIndx == 0):
        cmdIndx = cmdIndx + 1
        cmdVar = "cmd.%d" % cmdIndx