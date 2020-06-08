#!/usr/bin/python3

import psycopg2

################################################
# CommandResult
################################################

class CommandResult(object):
    def __init__(self, argSuccess, argRowCount, argData, argError=None):

        self.success = argSuccess
        self.rowCount = argRowCount
        self.data = argData
        self.error = argError


################################################
# getConnection
################################################

def getConnection(argDbConnConfig, argLogger):

    try:

        cxnStr = "host='%s' port=5432 dbname='%s' user='%s'" % \
                 (argDbConnConfig.host, argDbConnConfig.database, argDbConnConfig.user)
        result = psycopg2.connect(cxnStr)

    except:

        result = None

    logResult = True if result is not None else False

    argDbConnConfig.dumpPropertyLog(logResult, argLogger)

    return result


################################################
# executeStatement
################################################

def executeStatement(argDbCxn, argTemp, argVals=None, argRetValColIndx=None):

    cur = argDbCxn.cursor()

    try:

        cur.execute(argTemp, argVals)

        rowCount = cur.rowcount

        retVal = None

        if argRetValColIndx is not None:
            oneFetched = cur.fetchone()
            retVal = oneFetched[argRetValColIndx]

        argDbCxn.commit()

        result = CommandResult(True, rowCount, retVal)

    except psycopg2.Error as err:

        errArg = err.args[0]

        if errArg == "can't execute an empty query":
            result = CommandResult(True, 0, None)
        else:
            cmdError = err.pgerror
            stmtSql = cur.mogrify(argTemp, argVals);
            result = CommandResult(False, 0, stmtSql, cmdError)

            argDbCxn.rollback()

        pass

    cur.close()

    return result

################################################
# confirmTableExistence
################################################

def confirmTableExistence(argDbCxn, argSchema, argTblName):

    result = False

    try:
        cur = argDbCxn.cursor()

        #schema = self.connConfig.schema

        sql = "SELECT table_catalog FROM information_schema.tables " \
              "WHERE table_schema = '%s' AND table_name = '%s'" % (argSchema, argTblName)

        cur.execute(sql)
        rows = cur.fetchall()

        result = len(rows) > 0

    finally:
        cur.close()

    return result