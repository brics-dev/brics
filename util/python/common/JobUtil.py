#!/usr/bin/python3


import os
import time
#import configparser
#import sys

import CommonUtil
import ConfigUtil
#import Logging
from ConfigUtil import JobConfig
from LogUtil import LogDump




################################################
# initJob
################################################

def initJob(argJobName, argExecMode, argFileSysConfig, argArchiveWorkDir=False):

    beginTime = time.localtime()

    workDir = argFileSysConfig.workDir
    logDir = argFileSysConfig.logDir
    secFile = argFileSysConfig.secFile



    procLabel = os.path.basename(logDir)
    timeSign = CommonUtil.getTimeSignature(beginTime)
    procTimeLabel = procLabel + '.' + timeSign
    logFilePath = os.path.join(logDir, '%s.log' % procTimeLabel)
    stopFilePath = os.path.join(logDir, '%s.stop' % procLabel)
    contFilePath = os.path.join(logDir, '%s.cont' % procLabel)

    procUserID = getProcessUserID()

    result = JobConfig(argJobName, procUserID, beginTime, workDir, \
                       logFilePath, stopFilePath, contFilePath)

    jobLogger = result.logger

    startTimeDisp = CommonUtil.getTimeDisplay(beginTime)

    headLogDump = LogDump(True)
    headLogDump.addSimpleEntry("| %s" % argJobName)
    headLogDump.addSimpleEntry(result.logger.divider)
    headLogDump.addEntry("PROCESS USER", procUserID)
    headLogDump.addEntry("START", startTimeDisp)

    headLogDump.addEntry("LOG", logFilePath)
    headLogDump.addEntry("MODE", argExecMode)

    if secFile is not None:

        secFileExt = CommonUtil.getFileExtension(secFile)

        if secFileExt == "pass":
            virtPassConf = ConfigUtil.loadPasswordFile(secFile)
            result.addCategoryCache('VIRTUOSO', virtPassConf)
        elif secFileExt != "ppk":
            errorMsg = "Security file <%s> has unsupported extension. Only ppk and pass are allowed." % secFile
            headLogDump.addSimpleEntry(errorMsg)
            result = None

        headLogDump.addEntry("SECURITY FILE", secFile)

    if workDir is not None:

        if os.path.isdir(workDir):
            headLogDump.addEntry("WORK DIR", workDir)
        else:
            errorMsg = "Work dir <%s> is not valid. Aborting." % workDir
            headLogDump.addSimpleEntry(errorMsg)
            result = None

    if not os.path.isdir(logDir):
        if not CommonUtil.makeDirWithParents(logDir):
            errorMsg = "Unable to create log dir: <%s>. Aborting." % logDir
            headLogDump.addSimpleEntry(errorMsg)
            result = None

    if os.path.exists(stopFilePath):
        os.remove(stopFilePath)

    if os.path.exists(contFilePath):
        result.isContinued = True
        result.startOffset = CommonUtil.getFileContentString(contFilePath)
        headLogDump.addEntry("OFFSET", result.startOffset)

    if workDir is not None:
        if not os.path.isdir(workDir):
            os.makedirs(workDir)

        else:
            if argArchiveWorkDir:
                CommonUtil.archiveBatchFiles(workDir)

        #headLogDump.addEntry("DUMP", dumpDir)

    jobLogger.dumpLog(headLogDump, True)

    return result

################################################
# def endJob
################################################

def endJob(argJobConfig, argAbort=False, argArchiveWorkDir=False):

    logger = argJobConfig.logger

    endDump = LogDump(True)

    if argAbort:
        stopFile = argJobConfig.stopFile
        endDump.addSimpleEntry("| Exiting gracefully")
        os.remove(stopFile)

    else:
        contFile = argJobConfig.contFile
        workDir = argJobConfig.workDir

        if os.path.exists(contFile):
            os.remove(contFile)

        if workDir != None and argArchiveWorkDir:
            CommonUtil.archiveBatchFiles(workDir)

    #procEndTS = time.time()
    endTime = time.localtime()

    proctTime = time.mktime(endTime) - time.mktime(argJobConfig.beginTime)
    endTimeDisp = CommonUtil.getTimeDisplay(endTime)
    endDump.addEntry("| END TIME", endTimeDisp)
    endDump.addEntry("| PROCESS TIME (sec)", proctTime)
    endDump.addEntry("| LOG FILE", logger.logFile)
    endDump.addSimpleEntry("| Thank you for using %s" % argJobConfig.name)
    endDump.addSimpleEntry(logger.divider)

    logger.dumpLog(endDump)

################################################
# getProcessUserID
################################################

def getProcessUserID():

    # Windows
    result = os.environ.get('USERNAME')

    if result is None:
        # Linux
        result = os.environ.get('LOGNAME')

    return result

################################################
# getElapsedTimeDisplay
################################################

def getElapsedTimeDisplay(argTimeSec):

	m, s = divmod(argTimeSec, 60)
	h, m = divmod(m, 60)
	result = ("%d:%02d:%02d" % (h, m, s))

	return result