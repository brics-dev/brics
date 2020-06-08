#!/bin/python3


import argparse
import configparser
import sys
import time
#import xml.etree.ElementTree as ET
#from subprocess import call, Popen, PIPE
import os
#import shutil
#import re


import CommonUtil
import VirtuosoUtil
import ConfigUtil
import JobUtil

from LogUtil import LogDump
from VirtuosoUtil import RdfDataType, SubjectCluster, Triple

################################################
# createJobArgParser
################################################

def createJobArgParser():

	result = argparse.ArgumentParser()

	result.add_argument('--props', help='Properties file path', required=True)
	result.add_argument('-m', '--mode', help='Execution mode (optional)', required=False, default="manual")

	result.add_argument('-l', '--logDir', help='Log directory', required=True)
	result.add_argument('-w', '--workDir', help='Work directory', required=True)
	result.add_argument('-i', '--impDir', help='Import directory (if different from workDir)', required=False)
	result.add_argument('-s', '--secFile', help='Security (password) file', required=True)

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

	#fileSysConf = ConfigUtil.getFileSysConfig(config['FILE-SYS'])

	fileSysConfHash = {}
	fileSysConfHash['logDir'] = args.logDir
	fileSysConfHash['workDir'] = args.workDir
	fileSysConfHash['secFile'] = args.secFile
	fileSysConfHash['impDir'] = args.impDir

	if args.impDir is not None:
		fileSysConfHash['impDir'] = args.impDir

	fileSysConf = ConfigUtil.getFileSysConfig(fileSysConfHash)

	jobConfig = JobUtil.initJob("RDF CLONER", execMode, fileSysConf)

	jobLogger = jobConfig.logger

	passCache = jobConfig.getCategoryCache('VIRTUOSO')

	srcConf = ConfigUtil.getVirtuosoConfig('SRC', config['RDF-SRC'])
	srcConf.dumpPropertyLog(jobLogger)
	trgConf = ConfigUtil.getVirtuosoConfig('TRG', config['RDF-TRG'], passCache)
	trgConf.dumpPropertyLog(jobLogger)

	if not jobConfig.isContinued:

		clearResult = VirtuosoUtil.clearGraph(trgConf, jobConfig.workDir, jobLogger)

		if not clearResult:
			sys.exit(1)

	sourceCount = VirtuosoUtil.getTripleCount(srcConf.serviceURL, jobConfig.startOffset, srcConf.graph)

	#sourceCount = getTripleCount(srcHost, offset, graph)

	totalSubjects = 0
	firstBatch = True

	totalRows = 0
	batchID = 0
	stopFileExists = False

	typeMap = VirtuosoUtil.getTypeMap()

	offset = jobConfig.startOffset
	procOffset = offset
	statProcBeginTS = time.localtime()

	while firstBatch or payloadRows == srcConf.batchMax:

		firstBatch = False

		if offset is not None:
			#globContFilePath = logBase + '//' + procName + '.cont'
			CommonUtil.createFileWithContent(jobConfig.contFile, offset)

		if stopFileExists:

			#exitProcess(True)
			JobUtil.endJob(jobConfig, True)
			sys.exit()

		batchID += 1

		expFileName = 'rdf_%d.ttl' % batchID
		expFilePath = os.path.join(jobConfig.workDir, expFileName)
		#impFilePath = os.path.join(fileSysConf.impDir, expFileName)
		#expFilePath = globDataDir + '//' + expFileName

		batchBeginTS = time.localtime()

		expFile = open(expFilePath, 'w', encoding='UTF-8')



		startLogDump = LogDump()
		startLogDump.addEntry("BATCH NO", batchID)
		startLogDump.addEntry("FILE", expFileName)
		startLogDump.addEntry("FIRST SUBJECT", offset)

		jobLogger.dumpLog(startLogDump)

		payload =  VirtuosoUtil.getData(srcConf.serviceURL, srcConf.batchMax, offset, typeMap, srcConf.graph)

		payloadNumClusters = len(payload)
		batchNumClusters = 0
		batchNumPrcdClusters = 0
		payloadRows = 0
		payloadPrcdRows = 0

		for cluster in payload:

			batchNumClusters += 1
			clusterSize = len(cluster.triples)
			payloadRows += clusterSize

			if batchNumClusters < payloadNumClusters:

				batchNumPrcdClusters += 1
				payloadPrcdRows += clusterSize

				offset = cluster.subjectURI

				subjStr = str(cluster)

				expFile.write("\n\n" + subjStr)

		totalRows += payloadPrcdRows
		totalSubjects += batchNumPrcdClusters

		expFile.close()

		impStdOut, impStdErr, cmdFilePath = VirtuosoUtil.doImport(expFilePath, trgConf, fileSysConf.impDir)

		impMsg = None

		if not impStdErr:
			impMsg = "SUCCESS"
		else:
			impMsg = impStdErr

		procEndTS = time.localtime()
		batchTime = time.mktime(procEndTS) - time.mktime(batchBeginTS)
		procTime = int(time.mktime(procEndTS) - time.mktime(statProcBeginTS))
		procTimeDisp = JobUtil.getElapsedTimeDisplay(procTime)

		kiloTripleTime = procTime * 1000 / totalRows
		speedDisp = "%s sec / 1000 triples" % format(kiloTripleTime, '.2f')

		remaining = sourceCount - totalRows
		remainTimeEst = (remaining / 1000) * kiloTripleTime
		remainTimeEstDisp = JobUtil.getElapsedTimeDisplay(remainTimeEst)

		#triplesInStore = getTripleCount(targHost, jobConfig.startOffset, graph)
		triplesInStore = VirtuosoUtil.getTripleCount(trgConf.serviceURL, procOffset, trgConf.graph)
		triplesInStoreDisp = "{:,}".format(triplesInStore) if triplesInStore else "N/A"

		endLogDump = LogDump(False)
		endLogDump.addEntry("BATCH SUBJECTS", "{:,}".format(batchNumPrcdClusters))
		endLogDump.addEntry("BATCH TRIPLES", "{:,}".format(payloadPrcdRows))
		endLogDump.addEntry("BATCH TIME (sec)", int(batchTime))
		endLogDump.addEntry("TOTAL SUBJECTS", "{:,}".format(totalSubjects))
		endLogDump.addEntry("TOTAL TRIPLES SUBMITTED", "{:,}".format(totalRows))
		endLogDump.addEntry("TOTAL TRIPLES IN STORE", triplesInStoreDisp)
		endLogDump.addEntry("TOTAL TIME", procTimeDisp)
		endLogDump.addEntry("LAST SUBJECT", offset)
		endLogDump.addEntry("TRANSFER SPEED", speedDisp)
		endLogDump.addEntry("TRIPLES REMAINING", "{:,}".format(remaining))
		endLogDump.addEntry("ESTIMATED TIME LEFT", remainTimeEstDisp)
		endLogDump.addEntry("IMPORT", impMsg)

		jobLogger.dumpLog(endLogDump)


		stopFileExists = os.path.exists(jobConfig.stopFile)

	#archiveBatchFiles(dataDir)
	#exitProcess()
	JobUtil.endJob(jobConfig, False)

	print("Total rows = %d" % totalRows)
