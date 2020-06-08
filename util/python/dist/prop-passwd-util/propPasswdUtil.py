#!/usr/bin/python3

#import configparser
import argparse
import os
import re
import sys

import CommonUtil
import JobUtil
import ConfigUtil

################################################
# createJobArgParser
################################################

def createJobArgParser():

    result = argparse.ArgumentParser()

    #result.add_argument('-p', '--props', help='Properties file path', required=True)
    result.add_argument('-m', '--mode', help='Execution mode (optional)', required=False, default="manual")
    #result.add_argument('-o', '--out', help='Out directory to dump ready files', required=False)

    result.add_argument('-s', '--srcDir', help='Source dir for property templates', required=False)
    result.add_argument('-t', '--trgDir', help='Target dir for ready properties', required=False)

    result.add_argument('-l', '--logDir', help='Directory to output log files', required=False)
    result.add_argument('-k', '--secFile', help='Security (key) file', required=False)

    return result


################################################
# main
################################################

if __name__ == '__main__':

    argparser = createJobArgParser()

    args = argparser.parse_args()

    #propFile = args.props
    execMode = args.mode
    #outDir = args.out

    #config = configparser.ConfigParser()
    #config.read(propFile)

    #fileSysConfHash = config['FILE-SYS']
    #deployConfHash = config['DEPLOY']

    secFile = args.secFile

    fileSysConfHash = {}
    fileSysConfHash['logDir'] = args.logDir
    fileSysConfHash['secFile'] = secFile

    deployConfHash = {}
    deployConfHash['srcRoot'] = args.srcDir
    deployConfHash['trgRoot'] = args.trgDir

    #if outDir != None:
    #    deployConfHash['trgRoot'] = outDir



    fileSysConf = ConfigUtil.getFileSysConfig(fileSysConfHash)

    jobConfig = JobUtil.initJob("PROPERTY PASSWORD UTIL", execMode, fileSysConf)

    if jobConfig is None:
        sys.exit(1)

    jobLogger = jobConfig.logger
    passCache = jobConfig.getCategoryCache('VIRTUOSO')

    deployConf = ConfigUtil.getDeployConfig(deployConfHash)
    deployConf.dumpPropertyLog(jobLogger)

    trgRootDir = deployConf.trgRoot

    if not os.path.isdir(trgRootDir):
        if not CommonUtil.makeDirWithParents(trgRootDir):
            errorMsg = "Unable to target log dir: <%s>. Aborting." % trgRootDir
            jobLogger.dumpError(errorMsg)
            result = None

    srcDir = deployConf.srcRoot

    propFileDocList = CommonUtil.getDirectoryFileList(srcDir, "properties")
    propFileDocList.extend(CommonUtil.getDirectoryFileList(srcDir, "json"))

    for propFileDoc in propFileDocList:
        propFilePath = os.path.join(srcDir, propFileDoc['relDirPath'], propFileDoc['fileName'])
        print(propFilePath)
        fileStr = CommonUtil.getFileContentString(propFilePath)
        regexFinds = re.findall(r"\${(.+:\d+:.*:.+)}", fileStr)

        for regexFind in regexFinds:
            passwdToken = "${%s}" % regexFind
            passwd = passCache.get(regexFind)

            if passwd != None:
                jobLogger.dumpLine("%s password FOUND" % passwdToken)
                fileStr = fileStr.replace(passwdToken, passwd)
            else:
                errorMsg = "Unable to find password for %s in <%s>" % (passwdToken, secFile)
                jobLogger.dumpError(errorMsg)
                sys.exit(1)

        trgPropDir = trgRootDir

        # Create the target parent directories for the pending prop file, if needed.
        if len(propFileDoc['relDirPath']) > 0:
            trgPropDir = os.path.join(trgRootDir, propFileDoc['relDirPath'])
            
            if (not os.path.isdir(trgPropDir)) and (not CommonUtil.makeDirWithParents(trgPropDir)):
                errorMsg = "Unable to create target property dir: <%s>. Aborting." % trgPropDir
                jobLogger.dumpError(errorMsg)
                sys.exit(1)
        
        outFile = os.path.join(trgPropDir, propFileDoc['fileName'])

        CommonUtil.createFileWithContent(outFile, fileStr)
