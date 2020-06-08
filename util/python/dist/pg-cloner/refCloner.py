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

	return result

################################################
# main
################################################

if __name__ == '__main__':

	argparser = createJobArgParser()

	args = argparser.parse_args()

	propFile = args.props

	config = configparser.ConfigParser()
	config.read(propFile)

	fileSysConf = ConfigUtil.getFileSysConfig(config['FILE-SYS'])

	jobConfig = JobUtil.initJob("REF CLONER", "manual", fileSysConf)
	jobLogger = jobConfig.logger

	typeConverter = PostgresSchema.getTypeConverter()

	trgConf = ConfigUtil.getPostgresConfig(config['DB-TRG'])
	trgStruct = DbStructure(trgConf, jobConfig, typeConverter)
	trgConnResult = trgStruct.connect()

	if not trgConnResult:
		sys.exit(1)

	trgStruct.loadStructure()

	trgStruct.cleanReferences()


	srcConf = ConfigUtil.getPostgresConfig(config['DB-SRC'])
	srcStruct = DbStructure(srcConf, jobConfig, typeConverter)
	srcConnResult = srcStruct.connect()

	if not srcConnResult:
		sys.exit(1)



	srcStruct.loadStructure()
	#targStruct.loadStructure()


	PostgresSchema.cloneReferences(srcStruct, trgStruct, jobConfig)

	JobUtil.endJob(jobConfig)