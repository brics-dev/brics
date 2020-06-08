#!/usr/bin/python3

import argparse
import configparser

import re
import os
import sys
import shutil
from collections import OrderedDict

import CommonUtil
import JobUtil
import ConfigUtil
import SshUtil

from LogUtil import LogDump

################################################
# ProcessorFactory
################################################

class ProcessorFactory(object):
    def __init__(self, argConfig, argLogger):
        self.config = argConfig
        self.logger = argLogger

    def getProcessor(self, argCmdId):
        if argCmdId == 'cp':
            return CopyProcessor(self.config)
        elif argCmdId == 'mv':
            return MoveProcessor(self.config)
        elif argCmdId == 'mkdir':
            return MkdirProcessor(self.config)
        elif argCmdId == 'rmdir':
            return RmdirProcessor(self.config)
        elif argCmdId == 'rmfile':
            return RmfileProcessor(self.config)
        elif argCmdId == 'cpdir':
            return CopyDirProcessor(self.config)
        elif argCmdId == 'fcpdir':
            return ForceCopyDirProcessor(self.config)
        elif argCmdId == 'repl':
            return ReplaceProcessor(self.config)
        elif argCmdId == 'fmkdir':
            return ForceMkdirProcessor(self.config)
        elif argCmdId == 'sput':
            return SftpPutProcessor(self.config)
        elif argCmdId == 'sexec':
            return SshExecProcessor(self.config)
        else:
            raise ValueError("Invalid command ID %s" % argCmdId)

################################################
# AbstractProcessor
################################################

class AbstractProcessor(object):
    def __init__(self, argConfig):
        self.config = argConfig

################################################
# SshExecProcessor
################################################

class SshExecProcessor(object):
    def __init__(self, argConfig):
        self.config = argConfig

    def setArgs(self, argArgList):
        execDirArg = argArgList[0]
        trgRoot = self.config.trgRoot
        self.trgPath = trgRoot + '/' + execDirArg

        self.execCmd = argArgList[1]

        return True

    def processRequest(self):
        print("SSH executing '%s' in dir '%s'" % (self.execCmd, self.trgPath))
        sshClient = self.config.sshClient

        retCode, retMsg = SshUtil.sshExec(sshClient, self.trgPath, self.execCmd)
        result = False

        if retCode == 0:
            result = True
            print("Stdout: <%s>" % retMsg)
        else:
            print("Remote SSH command error: <%s>" % retMsg)

        return result

################################################
# SshCopyProcessor
################################################

class SftpPutProcessor(object):
    def __init__(self, argConfig):
        self.config = argConfig

    def setArgs(self, argArgList):
        srcPathArg = argArgList[0]
        srcRoot = self.config.srcRoot
        self.srcPath = os.path.join(srcRoot, srcPathArg)
        srcPathExist = os.path.exists(self.srcPath)
        print("Path %s exists: %s" % (self.srcPath, srcPathExist))

        trgPathArg = argArgList[1]
        trgRoot = self.config.trgRoot
        self.trgPath = trgRoot + '/' + trgPathArg

        result = srcPathExist
        #result = True

        return result

    def processRequest(self):
        print("SSH copying %s to %s" % (self.srcPath, self.trgPath))
        #resultPath = shutil.copy(self.srcPath, self.trgPath)
        #result = os.path.exists(resultPath)
        sshClient = self.config.sshClient
        SshUtil.sftpPutFile(sshClient, self.srcPath, self.trgPath)
        result = True
        return result

################################################
# CopyProcessor
################################################

class CopyProcessor(object):
    def __init__(self, argConfig):
        self.config = argConfig

    def setArgs(self, argArgList):
        srcPathArg = argArgList[0]
        srcRoot = self.config.srcRoot
        self.srcPath = os.path.join(srcRoot, srcPathArg)
        srcPathExist = os.path.exists(self.srcPath)
        print("Path %s exists: %s" % (self.srcPath, srcPathExist))

        trgPathArg = argArgList[1]
        trgRoot = self.config.trgRoot
        self.trgPath = os.path.join(trgRoot, trgPathArg)
        trgPathExist = os.path.exists(self.trgPath)
        print("Path %s exists: %s" % (self.trgPath, trgPathExist))

        result = srcPathExist and trgPathExist

        return result

    def processRequest(self):
        print("Copying %s to %s" % (self.srcPath, self.trgPath))
        resultPath = shutil.copy(self.srcPath, self.trgPath)
        result = os.path.exists(resultPath)
        return result

################################################
# MoveProcessor
################################################

class MoveProcessor(object):
    def __init__(self, argConfig):
        self.config = argConfig

    def setArgs(self, argArgList):
        srcPathArg = argArgList[0]
        srcRoot = self.config.srcRoot
        self.srcPath = os.path.join(srcRoot, srcPathArg)
        srcPathExist = os.path.exists(self.srcPath)
        print("Path %s exists: %s" % (self.srcPath, srcPathExist))

        trgPathArg = argArgList[1]
        trgRoot = self.config.trgRoot
        self.trgPath = os.path.join(trgRoot, trgPathArg)
        trgPathExist = os.path.exists(self.trgPath)
        print("Path %s exists: %s" % (self.trgPath, trgPathExist))

        result = srcPathExist and trgPathExist

        return result

    def processRequest(self):
        print("Copying %s to %s" % (self.srcPath, self.trgPath))
        resultPath = shutil.move(self.srcPath, self.trgPath)
        result = os.path.exists(resultPath)
        return result

################################################
# MkdirProcessor
################################################

class MkdirProcessor(object):
    def __init__(self, argConfig):
        self.config = argConfig

    def setArgs(self, argArgList):
        trgRoot = self.config.trgRoot
        trgPathArg = argArgList[0]
        self.trgPath = os.path.join(trgRoot, trgPathArg)

        return True

    def processRequest(self):

        if os.path.exists(self.trgPath):
            print("Dir %s already exists. Skipping." % self.trgPath)
        else:
            print("Creating dir %s" % self.trgPath)
            os.makedirs(self.trgPath)

        result = os.path.exists(self.trgPath)

        return result

################################################
# ForceMkdirProcessor
################################################

class ForceMkdirProcessor(object):
    def __init__(self, argConfig):
        self.config = argConfig

    def setArgs(self, argArgList):
        trgRoot = self.config.trgRoot
        trgPathArg = argArgList[0]
        self.trgPath = os.path.join(trgRoot, trgPathArg)

        return True

    def processRequest(self):

        if  os.path.exists(self.trgPath):
            print("Removing dir %s" % self.trgPath)
            shutil.rmtree(self.trgPath)

        print("Creating dir %s" % self.trgPath)
        os.makedirs(self.trgPath)

        result = os.path.exists(self.trgPath)

        return result


################################################
# RmdirProcessor
################################################

class RmdirProcessor(object):
    def __init__(self, argConfig):
        self.config = argConfig

    def setArgs(self, argArgList):
        trgRoot = self.config.trgRoot
        trgPathArg = argArgList[0]
        self.trgPath = os.path.join(trgRoot, trgPathArg)

        result = os.path.exists(self.trgPath)

        return result

    def processRequest(self):

        if os.path.exists(self.trgPath):
            print("Removing dir %s" % self.trgPath)
            shutil.rmtree(self.trgPath)

        result = not os.path.exists(self.trgPath)

        return result

################################################
# CopyDirProcessor
################################################

class CopyDirProcessor(object):
    def __init__(self, argConfig):
        self.config = argConfig

    def setArgs(self, argArgList):
        srcPathArg = argArgList[0]
        srcRoot = self.config.srcRoot
        self.srcPath = os.path.join(srcRoot, srcPathArg)
        srcPathExist = os.path.exists(self.srcPath)
        print("Path %s exists: %s" % (self.srcPath, srcPathExist))

        trgPathArg = argArgList[1]
        trgRoot = self.config.trgRoot
        self.trgPath = os.path.join(trgRoot, trgPathArg)
        trgPathExist = os.path.exists(self.trgPath)
        print("Path %s exists: %s" % (self.trgPath, trgPathExist))

        result = srcPathExist and trgPathExist

        return result

    def processRequest(self):

        if  os.path.exists(self.trgPath):
            print("Target dir %s exists. Skipping" % self.srcPath)
        else:
            print("Copying dir %s to %s" % (self.srcPath, self.trgPath))
            resultPath = shutil.copytree(self.srcPath, self.trgPath)
            result = os.path.exists(resultPath)
            return result

################################################
# ForceCopyDirProcessor
################################################

class ForceCopyDirProcessor(object):
    def __init__(self, argConfig):
        self.config = argConfig

    def setArgs(self, argArgList):
        srcPathArg = argArgList[0]
        srcRoot = self.config.srcRoot
        self.srcPath = os.path.join(srcRoot, srcPathArg)
        srcPathExist = os.path.exists(self.srcPath)
        print("Path %s exists: %s" % (self.srcPath, srcPathExist))

        trgPathArg = argArgList[1]
        trgRoot = self.config.trgRoot
        self.trgPath = os.path.join(trgRoot, trgPathArg)
        trgPathExist = os.path.exists(self.trgPath)
        print("Path %s exists: %s" % (self.trgPath, trgPathExist))

        result = srcPathExist and trgPathExist

        return result

    def processRequest(self):

        if  os.path.exists(self.trgPath):
            print("Removing dir %s" % self.trgPath)
            shutil.rmtree(self.trgPath)

        print("Copying dir %s to %s" % (self.srcPath, self.trgPath))
        resultPath = shutil.copytree(self.srcPath, self.trgPath)
        result = os.path.exists(resultPath)
        return result

################################################
# RmfileProcessor
################################################

class RmfileProcessor(object):
    def __init__(self, argConfig):
        self.config = argConfig

    def setArgs(self, argArgList):
        trgRoot = self.config.trgRoot
        trgPathArg = argArgList[0]
        self.trgPath = os.path.join(trgRoot, trgPathArg)

        result = os.path.exists(self.trgPath)

        return result

    def processRequest(self):

        if os.path.exists(self.trgPath):
            print("Removing file %s" % self.trgPath)
            os.remove(self.trgPath)

        result = not os.path.exists(self.trgPath)

        return result

################################################
# ReplaceProcessor
################################################

class ReplaceProcessor(object):
    def __init__(self, argConfig):
        self.config = argConfig

    def setArgs(self, argArgList):
        trgRoot = self.config.trgRoot
        trgPathArg = argArgList[0]
        self.trgPath = os.path.join(trgRoot, trgPathArg)

        result = os.path.exists(self.trgPath)

        self.varName = argArgList[1]

        return result

    def processRequest(self):

        CommonUtil.replaceFileVariable(self.trgPath, self.varName, self.config.replVal)

################################################
# createJobArgParser
################################################

def createJobArgParser():

    result = argparse.ArgumentParser()

    result.add_argument('-p', '--props', help='Properties file path', required=False)
    result.add_argument('-m', '--mode', help='Execution mode (optional)', required=False, default="manual")
    #result.add_argument('-r', '--reset', help='Reset', action="store_true")

    result.add_argument('-l', '--logDir', help='Directory to output log files', required=False)
    result.add_argument('-i', '--instFile', help='Instructions file', required=False)
    result.add_argument('-t', '--trgRoot', help='Target root', required=False)
    result.add_argument('-s', '--srcRoot', help='Source root', required=False)
    result.add_argument('-r', '--replVal', help='Replacement value', required=False)
    result.add_argument('-k', '--secFile', help='Security (key) file', required=False)

    return result

################################################
# main
################################################

if __name__ == '__main__':

    argparser = createJobArgParser()

    args = argparser.parse_args()

    propFile = args.props
    execMode = args.mode

    fileSysConfHash = {}
    fileSysConfHash['logDir'] = args.logDir
    fileSysConfHash['secFile'] = args.secFile

    deployConfHash  = {}
    deployConfHash['instFile'] = args.instFile
    deployConfHash['trgRoot'] = args.trgRoot
    deployConfHash['srcRoot'] = args.srcRoot
    deployConfHash['replVal'] = args.replVal

    sshConf = None

    if not propFile is None:
        config = configparser.ConfigParser()
        config.read(propFile)

        if config.has_section('SSH-CXN'):
            sshConfHash = config['SSH-CXN']
            sshConf = ConfigUtil.getSshConfig(sshConfHash)

    fileSysConf = ConfigUtil.getFileSysConfig(fileSysConfHash)

    jobConfig = JobUtil.initJob("FILE DEPLOYER", execMode, fileSysConf)

    if jobConfig is None:
        sys.exit(1)

    jobLogger = jobConfig.logger

    deployConf = ConfigUtil.getDeployConfig(deployConfHash)
    deployConf.dumpPropertyLog(jobLogger)

    if sshConf is not None:
        sshClient = SshUtil.getSshClient(sshConf, fileSysConf.secFile, jobLogger)
        deployConf.setSshClient(sshClient)

    instFile = deployConf.instFile
    instFileRhndl = open(instFile, 'r')

    instFileLines = instFileRhndl.readlines()

    procFactory = ProcessorFactory(deployConf, jobLogger)

    for instLine in instFileLines:
        if instLine.startswith('#'):
            continue

        instLineClean = instLine.rstrip()

        lineSplitRegex = re.match(r"^(\d*?)\|(.*?)\|(.*?)\|(.*?)$", instLineClean)

        lineNoStr = lineSplitRegex.group(1)
        command = lineSplitRegex.group(2)
        argOne = lineSplitRegex.group(3)
        argTwo = lineSplitRegex.group(4)
        args = [ argOne, argTwo ]

        lineNo = int(lineNoStr)

        #print("%d. command = <%s>" % (lineNo, command))
        #print("%d. argOne = <%s>" % (lineNo, argOne))
        #print("%d. argTwo = <%s>" % (lineNo, argTwo))

        #lineList = instLineClean.split('|')
        #command = lineList[0]
        #args = lineList[1:]

        processor = procFactory.getProcessor(command)
        processor.setArgs(args)

        try:
            result = processor.processRequest()
            print("%s --> %s" % (instLineClean, result))

            if not result:
                sys.exit(1)
        except:
            print("FAILURE: %s" % instLineClean)
            sys.exit(1)


