#!/usr/bin/python3

import sys
import configparser
import argparse

import ConfigUtil
import JobUtil
import PostgresSchema
from PostgresSchema import DbStructure

################################################
# createJobArgParser
################################################

def createJobArgParser():

    result = argparse.ArgumentParser()

    result.add_argument('--props', help='Properties file path', required=True)
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

    fileSysConf = ConfigUtil.getFileSysConfig(config['FILE-SYS'])

    jobConfig = JobUtil.initJob("PG DELTA", execMode, fileSysConf)
    jobLogger = jobConfig.logger

    typeConverter = PostgresSchema.getTypeConverter()

    srcConf = ConfigUtil.getPostgresConfig(config['DB-SRC'])
    srcStruct = DbStructure(srcConf, jobConfig, typeConverter)
    srcConnResult = srcStruct.connect()

    if not srcConnResult:
        sys.exit(1)

    trgConf = ConfigUtil.getPostgresConfig(config['DB-TRG'])
    trgStruct = DbStructure(trgConf, jobConfig, typeConverter)
    trgConnResult = trgStruct.connect()

    if not trgConnResult:
        sys.exit(1)

    srcStruct.loadStructure()
    trgStruct.loadStructure()

    #oneStruct.iterateObjects()

    deltaList = []

    PostgresSchema.compareSchemas(srcStruct, trgStruct, deltaList)

    jobLogger.dumpLine("\nRESULTS:\n")

    deltaCount = len(deltaList)

    if deltaCount == 0:
        jobLogger.dumpLine("No deltas. The DBs are identical.")
    else:

        deltaIndx = 0

        for delta in deltaList:
            deltaIndx = deltaIndx +1

            deltaDesc = delta.deltaDesc
            jobLogger.dumpLine("%d. %s" % (deltaIndx, deltaDesc))

            fixFwd = delta.fixFwd
            jobLogger.printDivider()
            jobLogger.dumpLine("> %s" % fixFwd)
            jobLogger.printDivider()

    JobUtil.endJob(jobConfig)
    
    sys.exit(0)