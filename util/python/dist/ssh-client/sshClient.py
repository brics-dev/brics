#!/usr/bin/python3

import argparse
import configparser

import os
import sys
import shutil

import CommonUtil
import JobUtil
import ConfigUtil
import SshUtil

################################################
# createJobArgParser
################################################

def createJobArgParser():

    result = argparse.ArgumentParser()

    result.add_argument('-p', '--props', help='Properties file path', required=False)
    result.add_argument('-m', '--mode', help='Execution mode (optional)', required=False, default="manual")

    return result

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

    sshConfHash = config['SSH-CXN']
    sshConf = ConfigUtil.getSshConfig(sshConfHash)

    fileSysConf = ConfigUtil.getFileSysConfig(fileSysConfHash)

    jobConfig = JobUtil.initJob("SSH CLIENT", execMode, fileSysConf)

    if jobConfig is None:
        sys.exit(1)

    jobLogger = jobConfig.logger

    sshClient = SshUtil.getSshClient(sshConf, jobLogger)

    sshExecHash = config['SSH-EXEC']


