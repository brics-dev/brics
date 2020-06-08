#!/bin/python3

###################################################################################################
# This is a utility script used to copy entire Postgresql database schemas,
# with or without data, from a source to a target, which can be separate servers.
#
# Usage: pgCloner.py --props=.//conf//stage-local.meta
#
# RE:  PS-2492: Utility to revert data simulation (https://dcb-jira.cit.nih.gov/browse/PS-2492)
###################################################################################################

import sys
import configparser
import argparse

import CommonUtil
import ConfigUtil
import JobUtil
import PostgresSchema
import PostgresAdmin
#import Logging

#from LogUtil import LogDump
from PostgresSchema import DbStructure

################################################
# createJobArgParser
################################################

def createJobArgParser():

	result = argparse.ArgumentParser()

	result.add_argument('--props', help='Properties file path', required=True)
	result.add_argument('-c', '--clean', help='Clean target data AND schema', action="store_true")
	result.add_argument('-s', '--schema', help='Schema only, no data', action="store_true")
	result.add_argument('-t', '--test', help='Test connection', action="store_true")
	result.add_argument('-l', '--logDir', help='Log file', required=True)
	result.add_argument('-w', '--workDir', help='Work directory', required=False)

	return result

################################################
# main
################################################

if __name__ == '__main__':

	argparser = createJobArgParser()

	args = argparser.parse_args()

	propFile = args.props
	clean = args.clean
	schemaOnly = args.schema
	testCxn = args.test

	if clean and schemaOnly:
		print("Invalid input arguments. --clean and  --schema cannot go together.")
		sys.exit(1)

	config = configparser.ConfigParser()
	config.read(propFile)

	#fileSysConf = ConfigUtil.getFileSysConfig(config['FILE-SYS'])

	fileSysConfHash = {}
	fileSysConfHash['logDir'] = args.logDir
	fileSysConfHash['workDir'] = args.workDir

	fileSysConf = ConfigUtil.getFileSysConfig(fileSysConfHash)

	jobConfig = JobUtil.initJob("PG CLONER", "manual", fileSysConf)
	jobLogger = jobConfig.logger

	typeConverter = PostgresSchema.getTypeConverter()

	trgConf = ConfigUtil.getPostgresConfig(config['DB-TRG'])
	trgStruct = DbStructure(trgConf, jobConfig, typeConverter)
	trgConnResult = trgStruct.connect()

	if not trgConnResult:
		sys.exit(1)

	if not testCxn:
		trgStruct.loadStructure()

	if clean:
		trgStruct.cleanDatabase()
		sys.exit()

	srcConf = ConfigUtil.getPostgresConfig(config['DB-SRC'])
	srcStruct = DbStructure(srcConf, jobConfig, typeConverter)
	srcConnResult = srcStruct.connect()

	if not srcConnResult:
		sys.exit(1)

	if testCxn:
		sys.exit()

	srcStruct.loadStructure()
	#targStruct.loadStructure()

	initFile = trgConf.initFile

	PostgresSchema.cloneSchema(srcStruct, trgStruct, jobConfig, initFile)

	#we need to do this again after cloning the schema
	trgStruct.loadStructure()

	if not schemaOnly:
		PostgresSchema.copyDatabaseData(srcStruct, trgStruct, jobLogger)

	PostgresSchema.cloneReferences(srcStruct, trgStruct, jobConfig)

	#proceed = input("Proceed?")

	srcStruct.loadSequences()
	#trgStruct.loadSequences()
	PostgresSchema.updateSequences(srcStruct, trgStruct, jobConfig)

	JobUtil.endJob(jobConfig)