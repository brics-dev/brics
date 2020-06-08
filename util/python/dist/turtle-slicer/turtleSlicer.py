#!/bin/python3

from itertools import islice

import argparse
import configparser
import os

import CommonUtil



################################################
# main
################################################

if __name__ == '__main__':

    print("Welcome to Turtle Slicer !")

    argparser = argparse.ArgumentParser()

    argparser.add_argument('--props', help='Properties file path', required=False)

    args = argparser.parse_args()

    propFile = args.props

    if propFile is None:
        propFile = './/conf//local.ini'

    config = configparser.ConfigParser()
    config.read(propFile)

    defConf = config['DEFAULT']
    filePath = defConf['filePath']
    outDir = defConf['outDir']
    batchInitStr = defConf['batchInit']

    if batchInitStr is not None:
        batchInit = int(batchInitStr)
    else:
        batchInit = 5000

    lineIndx = 0
    fileIndx = 0

    hasContent = True
    prevExportShort = None

    while(hasContent):

        fileIndx += 1

        print("lineIndx = %d" % lineIndx)
        line_slice = (lineIndx, lineIndx + batchInit)

        srcFileRhndl = open(filePath, 'r')

        content = islice(srcFileRhndl, *line_slice)
        #print(content)

        #hasContent = content is not None

        slicedFileShortName = 'rdf_' + str(fileIndx)

        if fileIndx == 1:
            slicedDraftExt = 'prun'
        else:
            slicedDraftExt = 'init'

        slicedDraftPathShort = outDir + '/' + slicedFileShortName
        slicedDraftPath = slicedDraftPathShort + '.' + slicedDraftExt

        slicedDraftWhndl = open(slicedDraftPath, 'w')

        slicedDraftWhndl.writelines(content)
        slicedDraftWhndl.close()



        if fileIndx == 1:
            prevExportShort = slicedDraftPathShort
            slicedPrunedPath = slicedDraftPath
        else:
            #spillLines = []

            slicedDraftRhndl = open(slicedDraftPath, 'r')

            #with open(slicedDraftPath, 'r') as ofr:
            initOutLines = slicedDraftRhndl.readlines()

            outFileLine = 0

            spill = True

            #with open(prevExport, 'a') as pef:
            prevExport = prevExportShort + '.prun'
            prevExportAhandle = open(prevExport, 'a')
            slicedPrunedShortPath = outDir + '/' + slicedFileShortName
            slicedPrunedPath = slicedPrunedShortPath + '.prun'
            slicedPrunedWhandle = open(slicedPrunedPath, 'w')

            for initOutLine in initOutLines:

                outFileLine += 1

                if initOutLine.startswith('<http://ninds.nih.gov'):
                    spill = False

                cleanLine = initOutLine.rstrip()

                if spill:
                    print(cleanLine, file=prevExportAhandle)
                else:
                    print(cleanLine, file=slicedPrunedWhandle)

            slicedPrunedWhandle.close()
            prevExportAhandle.close()
            slicedDraftRhndl.close()

            finalExportPath = prevExportShort + '.ttl'

            os.rename(prevExport, finalExportPath)

            prevExportShort = slicedPrunedShortPath

            os.remove(slicedDraftPath)


        if os.stat(slicedPrunedPath).st_size == 0:
            #os.remove(slicedDraftPath)
            os.remove(slicedPrunedPath)
            #finalExportPath = slicedPrunedShortPath + '.ttl'
            #os.rename(slicedPrunedPath, finalExportPath)
            hasContent = False

        lineIndx += batchInit

    ttlFileDocList = CommonUtil.getDirectoryFileList(outDir, 'ttl')

    for ttlFileDoc in ttlFileDocList:

        print(ttlFileDoc)