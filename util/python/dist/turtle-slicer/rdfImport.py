#!/bin/python3

import argparse
import configparser
import re
import sys
import shutil
import os

import CommonUtil
import VirtuosoUtil

################################################
# main
################################################

if __name__ == '__main__':

    print("Welcome to RDF Import !")

    argparser = argparse.ArgumentParser()

    argparser.add_argument('--props', help='Properties file path', required=False)

    args = argparser.parse_args()

    propFile = args.props

    if propFile is None:
        propFile = './/conf//local.ini'

    config = configparser.ConfigParser()
    config.read(propFile)

    defConf = config['DEFAULT']

    outDir = defConf['outDir']
    graph = defConf['graph']
    isqlPortStr = defConf.get('isqlPort')
    targHost = defConf['target']
    logBase = defConf['logBase']
    procName = defConf['procName']
    impMaxStr = defConf.get('impMax')

    if isqlPortStr is not None:
        isqlPort = int(isqlPortStr)
    else:
        isqlPort = 1111

    if impMaxStr is not None:
        impMax = int(impMaxStr)
    else:
        impMax = 100

    contFilePath = logBase + '//' + procName + '.cont'
    isCont = os.path.exists(contFilePath)

    timeSign = CommonUtil.getTimeSignature()
    globLogFilePath = logBase + '//' + procName + '.' + timeSign + '.log'

    archDirPath = CommonUtil.createArchive(outDir)

    if not isCont:

        clearStdOut, clearStdErr, clearGraphFile = VirtuosoUtil.clearGraph(outDir, isqlPort, graph)

        if clearStdErr == '':
            clearResult = "SUCCESS"
        else:
            clearResult = "FAILED"
            print(clearResult)
            sys.exit(1)

        CommonUtil.createFileWithContent(contFilePath, "X")
        shutil.move(clearGraphFile, archDirPath)


    ttlFileDocList = CommonUtil.getDirectoryFileList(outDir, 'ttl')

    ttlFileIndx = 0

    for ttlFileDoc in ttlFileDocList:

        if ttlFileIndx > impMax:
            sys.exit(0)

        ttlFilePath = os.path.join(outDir, ttlFileDoc['relDirPath'], ttlFileDoc['fileName'])

        regex = re.match(r"rdf\_(.+)\.ttl", ttlFileDoc['fileName'])
        rqstID = int(regex.group(1))

        print("File: %s, Request ID: %d" % (ttlFileDoc['fileName'], rqstID))

        impStdOut, impStdErr, cmdFilePath = VirtuosoUtil.doImport(rqstID, outDir, graph, isqlPort)

        shutil.move(cmdFilePath, archDirPath)
        shutil.move(ttlFilePath, archDirPath)

        ttlFileIndx += 1

    os.remove(contFilePath)
