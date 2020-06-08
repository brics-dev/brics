#!/usr/bin/python3

import configparser
import argparse
import os
import re
import sys
import shutil

import CommonUtil
import JobUtil
import ConfigUtil

################################################
# createJobArgParser
################################################

def createJobArgParser():

    result = argparse.ArgumentParser()

    result.add_argument('-p', '--props', help='Properties file path', required=True)
    result.add_argument('-m', '--mode', help='Execution mode (optional)', required=False, default="manual")

    return result

################################################
# walkExtractedDirs
################################################

def walkRootDir(argTopLevelDir, argExt, argLogger):

    for root, subdirs, files in os.walk(argTopLevelDir):

        for filename in files:

            if filename.endswith(argExt):
                file_path = os.path.join(root, filename)

                relPath = file_path.replace(argTopLevelDir + "\\", "")

                relPathList = relPath.split("\\")
                jarName = relPathList[0]
                classList = relPathList[1:]
                classPath = ".".join(classList)

                classPathShort = re.findall(r"(.+)\.%s" % argExt, classPath)[0]
                #print("%s|%s" % (jarName, classPathShort))
                jobLogger.dumpLine("%s|%s" % (jarName, classPathShort))

################################################
# extractJars
################################################

def extractJars(argSrcDir, argTrgDir):
    CommonUtil.purgeDirectory(argTrgDir)

    srcFileDocList = CommonUtil.getDirectoryFileList(argSrcDir, "jar")

    for srcFileDoc in srcFileDocList:
        srcFilePath = os.path.join(argSrcDir, srcFileDoc['relDirPath'], srcFileDoc['fileName'])
        # print(srcFilePath)

        jarName = re.findall(r"(.+)\.jar", srcFileDoc['fileName'])[0]

        print("%s -- %s" % (srcFilePath, jarName))

        jarTrgDir = os.path.join(argTrgDir, jarName)

        if not CommonUtil.makeDirWithParents(jarTrgDir):
            errorMsg = "Unable to create target jar dir: <%s>. Aborting." % jarTrgDir
            jobLogger.dumpError(errorMsg)
            sys.exit(1)

        shutil.copy(srcFilePath, jarTrgDir)

        jarFinalPath = os.path.join(jarTrgDir, srcFileDoc['relDirPath'], srcFileDoc['fileName'])

        unzipCmd = "unzip %s -d %s" % (jarFinalPath, jarTrgDir)

        print(unzipCmd)

        CommonUtil.runSystemCommand(unzipCmd)

################################################
# main
################################################

if __name__ == '__main__':

    argparser = createJobArgParser()

    args = argparser.parse_args()

    propFile = args.props
    execMode = args.mode

    config = configparser.ConfigParser()
    config.read(propFile)

    fileSysConfHash = config['FILE-SYS']
    deployConfHash = config['DEPLOY']

    fileSysConf = ConfigUtil.getFileSysConfig(fileSysConfHash)

    jobConfig = JobUtil.initJob("DEPENDENCY ANALYZER", execMode, fileSysConf)

    jobLogger = jobConfig.logger

    deployConf = ConfigUtil.getDeployConfig(deployConfHash)
    deployConf.dumpPropertyLog(jobLogger)


    srcDir = deployConf.srcRoot
    trgDir = deployConf.trgRoot
    codeRoot = fileSysConf.workDir

    if not os.path.isdir(trgDir):
        if not CommonUtil.makeDirWithParents(trgDir):
            errorMsg = "Unable to target log dir: <%s>. Aborting." % trgDir
            jobLogger.dumpError(errorMsg)
            result = None

    extractJars(srcDir, trgDir)

    walkRootDir(trgDir, "class", jobLogger)

    walkRootDir(codeRoot, "java")


