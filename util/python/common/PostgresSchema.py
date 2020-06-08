#!/usr/bin/python3

import sys
import os
import time
import re
import psycopg2

import CommonUtil
#import LogUtil
import PostgresUtil
import PostgresAdmin

from LogUtil import LogDump
from PostgresAdmin import SysInstall

################################################
# class DbStructure
################################################

class DbStructure(object):
    def __init__(self, argConnConfig, argJobConfig, argTypeConverter):

        #self.name = argName

        self.tblLookup = None
        self.seqLookup = None
        self.viewLookup = None

        self.resetLookups()

        self.connConfig = argConnConfig
        self.jobConfig = argJobConfig
        self.typeConverter = argTypeConverter

        self.cxn = None

    ################################################
    # DbStructure.getLabel
    ################################################

    def getLabel(self):
        result = self.connConfig.label
        return result

    ################################################
    # DbStructure.resetLookups
    ################################################

    def resetLookups(self):

        self.tblLookup = dict()
        self.seqLookup = dict()
        self.viewLookup = dict()

    ################################################
    # DbStructure.connect
    ################################################

    def connect(self):

        self.cxn = PostgresUtil.getConnection(self.connConfig, self.jobConfig.logger)

        if self.cxn != None:
            result = True
        else:
            result = False

        return result

    ################################################
    # DbStructure.executeStatement
    ################################################

    def executeStatement(self, argTemp, argVals=None, argExitIfFailed=True):

        result = False
        cur = self.cxn.cursor()

        try:

            cur.execute(argTemp, argVals);

            self.cxn.commit()
            result = True

        except psycopg2.Error as e:

            stmtSql = cur.mogrify(argTemp, argVals);
            self.jobConfig.logger.dumpLine("Unable to execute: %s" % stmtSql)
            self.jobConfig.logger.dumpLine(e.pgerror)
            result = False

            if argExitIfFailed:
                sys.exit(1)
            else:
                self.cxn.rollback()

        cur.close()

        return result

    ################################################
    # DbStructure.addTable
    ################################################

    def addTable(self, argTable):
        self.tblLookup[argTable.name] = argTable

    ################################################
    # DbStructure.addSequence
    ################################################

    def addSequence(self, argSeq):
        self.seqLookup[argSeq.name] = argSeq

    ################################################
    # DbStructure.addView
    ################################################

    def addView(self, argView):
        self.viewLookup[argView.name] = argView

    ################################################
    # DbStructure.iterateObjects
    ################################################

    def iterateObjects(self):
        for tblName in sorted(self.tblLookup.keys()):
            print("%s -- %s" % (self.name, tblName))

            table = self.tblLookup[tblName]

    ################################################
    # DbStructure.getTableByName
    ################################################

    def getTableByName(self, argTblName):
        result = self.tblLookup.get(argTblName)
        return result

    ################################################
    # DbStructure.loadBasicStructure()
    ################################################

    def loadBasicStructure(self):

        adminSkip = PostgresAdmin.getAdminTableNames()

        try:
            cur = self.cxn.cursor()

            schema = self.connConfig.schema

            sql = "SELECT table_name, column_name, ordinal_position, is_nullable, data_type, " \
                  "character_maximum_length, column_default " \
                  "FROM information_schema.columns " \
                  "WHERE table_schema = '%s' " \
                  "AND is_updatable = 'YES' " \
                  "ORDER BY table_name, ordinal_position;" % schema

            cur.execute(sql)
            rows = cur.fetchall()

            for row in rows:

                tblName = row[0]

                if tblName in adminSkip:
                    continue

                table = self.getTableByName(tblName)

                if table is None:
                    table = Table(self, tblName)
                    # result[tblName] = table
                    self.addTable(table)

                colName = row[1]
                ordPos = row[2]
                nullOrig = row[3]
                nullable = (nullOrig == 'YES')
                dataTypeOrig = row[4]
                maxLen = row[5]
                default = row[6]

                dataType = self.typeConverter.get(dataTypeOrig)

                if dataType is not None:

                    col = Column(colName, tblName, ordPos, nullable, dataType, maxLen, default)
                    table.addColumn(col)

                else:
                    # print("Unknown data type: %s (%s -- %s)" % (self.typeOrig, self.table, self.name))
                    logLine = "Unknown data type: %s (%s -- %s)" % (self.typeOrig, self.table, self.name)
                    self.jobConfig.logger.dumpLine(logLine)
                    sys.exit(1)

        finally:
            cur.close()

    ################################################
    # DbStructure.getSequenceLastValue
    ################################################

    def getSequenceLastValue(self, argSeqName):

        try:
            cur = self.cxn.cursor()

            sql = "SELECT last_value FROM \"%s\";" % argSeqName

            cur.execute(sql)
            row = cur.fetchone()

            result = row[0]

        finally:
            cur.close()

        return result

    ################################################
    # DbStructure.loadSequences
    ################################################

    def loadSequences(self):

        adminSkip = PostgresAdmin.getAdminSequenceNames()

        try:
            cur = self.cxn.cursor()

            schema = self.connConfig.schema

            sql = "SELECT sequence_name, minimum_value, maximum_value, increment " \
                  "FROM information_schema.sequences " \
                  "WHERE sequence_schema = '%s' " \
                  "ORDER BY sequence_name;" % schema

            cur.execute(sql)
            rows = cur.fetchall()

            for row in rows:
                seqName = row[0]

                if seqName in adminSkip:
                    continue

                minVal = row[1]
                maxVal = row[2]
                incr = row[3]

                lastVal = self.getSequenceLastValue(seqName)
                seqStartVal = lastVal + 1
                #seqStartVal = lastVal

                seqnc = Sequence(seqName, seqStartVal, minVal, maxVal, incr)

                self.addSequence(seqnc)

        finally:
            cur.close()

    ################################################
    # DbStructure.loadViews
    ################################################

    def loadViews(self):

        #adminSkip = PostgresAdmin.getAdminSequenceNames()

        try:
            cur = self.cxn.cursor()

            schema = self.connConfig.schema

            sql = "SELECT table_name, view_definition FROM information_schema.views " \
                "WHERE table_schema = '%s';" % schema

            cur.execute(sql)
            rows = cur.fetchall()

            for row in rows:
                viewName = row[0]

                #if seqName in adminSkip:
                #    continue

                viewDef = row[1]


                view = View(viewName, viewDef)

                self.addView(view)

        finally:
            cur.close()

    ################################################
    # DbStructure.loadConstraints
    ################################################

    def loadConstraints(self):

        adminSkip = PostgresAdmin.getAdminTableNames()

        try:

            cur = self.cxn.cursor()

            schema = self.connConfig.schema

            sql = "SELECT t.relname as table_name, c.conname as constraint_name,  c.contype, " \
                  "pg_get_constraintdef(c.oid) as constraint_definition " \
                  "FROM pg_class t JOIN pg_constraint c ON t.oid = c.conrelid " \
                  "JOIN pg_namespace nsp ON t.relnamespace = nsp.oid " \
                  "JOIN pg_namespace ts ON t.relnamespace = ts.oid " \
                  "WHERE nsp.nspname = '%s' ORDER BY t.relname, c.conname;" % schema

            cur.execute(sql)
            rows = cur.fetchall()

            for row in rows:

                tblName = row[0]

                if tblName in adminSkip:
                    continue

                constName = row[1]
                constType = row[2]
                constDef = row[3]

                table = self.getTableByName(tblName)
                const = Constraint(constName, table, constDef)

                if constType == 'f':
                    table.addForeignKey(const)
                elif constType == 'p':
                    table.setPrimaryKey(const)
                elif constType == 'u':
                    table.addUniqueKey(const)

        finally:
            cur.close()

    ################################################
    # DbStructure.loadPrimaryKeyColumns
    ################################################

    def loadPrimaryKeyColumns(self):

        adminSkip = PostgresAdmin.getAdminTableNames()

        try:
            cur = self.cxn.cursor()

            schema = self.connConfig.schema

            sql = "SELECT k.table_name, k.constraint_name, k.column_name, k.ordinal_position " \
                  "FROM information_schema.table_constraints c " \
                  "JOIN information_schema.key_column_usage k ON c.table_name = k.table_name " \
                  "AND c.constraint_name = k.constraint_name AND c.table_schema = k.table_schema " \
                  "WHERE c.table_schema = '%s' AND c.constraint_type = 'PRIMARY KEY' " \
                  "ORDER BY k.table_name, k.constraint_name, k.ordinal_position;" % schema

            cur.execute(sql)
            rows = cur.fetchall()

            for row in rows:
                tblName = row[0]

                if tblName in adminSkip:
                    continue

                # constName = row[1]
                colName = row[2]

                table = self.tblLookup.get(tblName)

                constr = table.primKey

                constr.addColumn(colName)

        finally:

            cur.close()

    ################################################
    # DbStructure.cleanSequences
    ################################################

    def cleanSequences(self):

        logger = self.jobConfig.logger

        label = self.getLabel()

        if label == 'TARGET':

            logger.printDivider()

            for seqnc in self.seqLookup.values():

                seqName = seqnc.name

                if self.jobConfig.doDump():
                    createStmt = seqnc.getCreateStatement()
                    dumpFileName = "SEQ." + seqName + ".TRG"
                    dumpFilePath = os.path.join(self.jobConfig.workDir, dumpFileName)
                    CommonUtil.createFileWithContent(dumpFilePath, createStmt)

                dropStmt = seqnc.getDropStatement()
                logger.dumpLine("DROPPING SEQUENCE: %s" % seqName)
                self.executeStatement(dropStmt)
        else:
            logger.dumpLine("You're only allowed to clean TARGET")

    ################################################
    # DbStructure.cleanViews
    ################################################

    def cleanViews(self):

        logger = self.jobConfig.logger

        label = self.getLabel()

        if label == 'TARGET':

            logger.printDivider()

            for view in self.viewLookup.values():

                viewName = view.name

                if self.jobConfig.doDump():
                    createStmt = view.getCreateStatement()
                    dumpFileName = "VIEW." + viewName + ".TRG"
                    dumpFilePath = os.path.join(self.jobConfig.workDir, dumpFileName)
                    CommonUtil.createFileWithContent(dumpFilePath, createStmt)

                dropStmt = view.getDropStatement()
                logger.dumpLine("DROPPING VIEW: %s" % viewName)
                self.executeStatement(dropStmt)
        else:
            logger.dumpLine("You're only allowed to clean TARGET")


    ################################################
    # DbStructure.cleanReferences
    ################################################

    def cleanReferences(self):

        logger = self.jobConfig.logger

        label = self.getLabel()

        if label == 'TARGET':

            logger.printDivider()

            for tblName in sorted(self.tblLookup.keys()):
                table = self.tblLookup[tblName]

                for fkey in table.fkeys:

                    keyName = fkey.name

                    if self.jobConfig.doDump():
                        createStmt = fkey.getCreateStatement()
                        dumpFileName = "KEY." + keyName + ".TRG"
                        dumpFilePath = os.path.join(self.jobConfig.workDir, dumpFileName)
                        CommonUtil.createFileWithContent(dumpFilePath, createStmt)

                    dropStmt = fkey.getDropStatement()
                    logger.dumpLine("DROPPING FOREIGN KEY: %s" % keyName)
                    self.executeStatement(dropStmt)
        else:
            logger.dumpLine("You're only allowed to clean TARGET")

    ################################################
    # DbStructure.cleanTables
    ################################################

    def cleanTables(self):

        logger = self.jobConfig.logger

        label = self.getLabel()

        if label == 'TARGET':

            logger.printDivider()

            for table in self.tblLookup.values():

                tblName = table.name

                if self.jobConfig.doDump():
                    dumpDir = self.jobConfig.workDir
                    createStmt = table.getCreateStatement(True)
                    dumpFileName = "TBL." + tblName + ".TRG"
                    dumpFilePath = os.path.join(dumpDir, dumpFileName)
                    CommonUtil.createFileWithContent(dumpFilePath, createStmt)

                dropStmt = table.getDropStatement()
                logger.dumpLine("DROPPING TABLE: %s" % tblName)
                self.executeStatement(dropStmt)
        else:
            logger.dumpLine("You're only allowed to clean TARGET")

    ################################################
    # DbStructure.cleanTables
    ################################################

    def cleanDatabase(self):
        self.cleanReferences()
        self.cleanViews()
        self.cleanTables()
        self.cleanSequences()

    ################################################
    # DbStructure.loadStructure
    ################################################
    def loadStructure(self):

        self.resetLookups()

        self.loadSequences()
        self.loadBasicStructure()
        self.loadConstraints()
        self.loadViews()
        self.loadPrimaryKeyColumns()

    ################################################
    # DbStructure.getTableRowCount
    ################################################

    def getTableRowCount(self, argTblName):

        result = 0

        try:
            cur = self.cxn.cursor()

            rowCountStmt = "SELECT COUNT(*) FROM %s;" % argTblName

            cur.execute(rowCountStmt)
            row = cur.fetchone()

            result = row[0]

        finally:
            cur.close()

        return result

    ################################################
    # DbStructure.getTableSummary
    ################################################

    def getTableSummary(self, argTblName):

        table = self.tblLookup.get(argTblName)
        rowCount = self.getTableRowCount(argTblName)
        pkStatus = table.getPrimaryKeyStatus()

        readMode = 'BULK' if pkStatus else 'SEQUENTIAL'

        self.jobConfig.logger.dumpLine("Table <%s> (%d rows). Read Mode: %s\n" % (argTblName, rowCount, readMode))

        return rowCount, pkStatus

    ################################################
    # DbStructure.displayDatabaseSummary
    ################################################

    def displayDatabaseSummary(self):

        for tblName in sorted(self.tblLookup.keys()):
            self.getTableSummary(tblName)

    ################################################
    # DbStructure.copyDatabaseData
    ################################################

    def persistTableCachedData(self, argTbl):

        for dataRow in argTbl.cachedData:
            insTemp, vals = argTbl.generateInsert(dataRow)
            self.executeStatement(insTemp, vals)

    ################################################
    # DbStructure.compareTables
    ################################################

    def compareTables(self, argOther, argDeltaList):

        compared = set()

        thisLabel = self.getLabel()
        twoLabel = argOther.getLabel()

        for tblName in sorted(self.tblLookup.keys()):
            tblOne = self.tblLookup[tblName]

            tblTwo = argOther.tblLookup.get(tblName)

            if tblTwo is not None:
                tblOne.compare(tblTwo, argDeltaList)
            else:
                dropStmt = tblOne.getDropStatement()
                deltaDesc = "Table <%s> does not exist in <%s>" % (tblName, twoLabel)

                delta = SchemaDelta(deltaDesc, dropStmt)
                argDeltaList.append(delta)

                #print("Table <%s> does not exist in <%s>" % (tblName, twoLabel))
                crtStmt = tblOne.getCreateStatement(True)
                #print(crtStmt)

                for fkey in tblOne.fkeys:
                    fkeyCrtStmt = fkey.getCreateStatement()
                    #print(fkeyCrtStmt)

            compared.add(tblName)

        for tblName in sorted(argOther.tblLookup.keys()):

            if tblName not in compared:
                tblTwo = argOther.tblLookup.get(tblName)
                crtStmt = tblTwo.getCreateStatement(True)
                deltaDesc = "Table <%s> does not exist in <%s>" % (tblName, thisLabel)

                delta = SchemaDelta(deltaDesc, crtStmt)
                argDeltaList.append(delta)

                #print("Table <%s> does not exist in <%s>" % (tblName, thisLabel))

    ################################################
    # DbStructure.compareSequences
    ################################################

    def compareSequences(self, argOther, argDeltaList):

        compared = set()

        thisLabel = self.getLabel()
        twoLabel = argOther.getLabel()

        for seqName in sorted(self.seqLookup.keys()):
            seqOne = self.seqLookup[seqName]

            seqTwo = argOther.seqLookup.get(seqName)

            if seqTwo is not None:
                skip = 1
                #tblOne.compare(tblTwo)
            else:
                deltaDesc = "Sequence <%s> does not exist in <%s>" % (seqName, twoLabel)
                dropStmt = seqOne.getDropStatement()
                delta = SchemaDelta(deltaDesc, dropStmt)

                argDeltaList.append(delta)

                #print("Sequence <%s> does not exist in <%s>" % (seqName, twoLabel))
                #crtStmt = seqOne.getCreateStatement()
                #print(crtStmt)

            compared.add(seqName)

        for seqName in sorted(argOther.seqLookup.keys()):

            if seqName not in compared:
                #print("Sequence <%s> does not exist in <%s>" % (seqName, thisLabel))

                deltaDesc = "Sequence <%s> does not exist in <%s>" % (seqName, thisLabel)

                seqOth = argOther.seqLookup.get(seqName)
                dropStmt = seqOth.getCreateStatement()
                delta = SchemaDelta(deltaDesc, dropStmt)

                argDeltaList.append(delta)

    ################################################
    # DbStructure.compareForeignKeys
    ################################################

    def compareForeignKeys(self, argOther):

        compared = set()

        thisLabel = self.getLabel()
        twoLabel = argOther.getLabel()

        for tblName in sorted(self.tblLookup.keys()):
            tblOne = self.tblLookup[tblName]

            tblTwo = argOther.tblLookup.get(tblName)

            if tblTwo is not None:
                tblOne.compare(tblTwo)
            else:
                print("Table <%s> does not exist in <%s>" % (tblName, twoLabel))

            compared.add(tblName)

        for tblName in sorted(argOther.tblLookup.keys()):

            if tblName not in compared:
                print("Table <%s> does not exist in <%s>" % (tblName, thisLabel))

################################################
# class Constraint
################################################

class Constraint(object):
    def __init__(self, argName, argTbl, argDefn):
        self.name = argName
        # self.type = argType
        self.table = argTbl
        self.defn = argDefn
        self.cols = []

    def addColumn(self, argColName):
        self.cols.append(argColName)

    def getDropStatement(self):
        result = "ALTER TABLE %s DROP CONSTRAINT %s;" % (self.table.name, self.name)
        return result

    def getCreateStatement(self):
        result = "ALTER TABLE %s \nADD CONSTRAINT %s \n%s;" % (self.table.name, self.name, self.defn)
        return result

    def __repr__(self):
        result = self.getCreateStatement()
        return result


################################################
# class Sequence
################################################

class Sequence(object):
    def __init__(self, argName, argStart, argMinVal, argMaxVal, argIncr):
        self.name = argName
        self.start = argStart
        self.minVal = argMinVal
        self.maxVal = argMaxVal
        self.incr = argIncr

    def getCreateStatement(self):
        result = "CREATE SEQUENCE %s START %d;" % (self.name, self.start)
        return result

    def getAlterStatement(self):
        result = "ALTER SEQUENCE %s RESTART %d;" % (self.name, self.start)
        return result

    def getDropStatement(self):
        result = "DROP SEQUENCE \"%s\";" % self.name
        return result

################################################
# class View
################################################

class View(object):
    def __init__(self, argName, argDef):
        self.name = argName
        self.defn = argDef

    def getCreateStatement(self):
        result = "CREATE VIEW %s AS %s;" % (self.name, self.defn)
        return result

    def getDropStatement(self):
        result = "DROP VIEW %s;" % self.name
        return result

################################################
# class DataType
################################################

class DataType(object):
    def __init__(self, argDispName, argSqlName, argUseQuote, argBin):
        self.dispName = argDispName
        self.sqlName = argSqlName
        self.useQuote = argUseQuote
        self.binary = argBin


################################################
# class Table
################################################

class Table(object):
    def __init__(self, argStruct, argName):
        self.struct = argStruct
        self.name = argName
        self.colLookup = dict()
        self.colOrdLookup = dict()
        self.fkeys = []
        self.ukeys = []
        self.primKey = None
        self.buildOrd = None
        self.cachedData = None

    def getColumnDataType(self, argColName):
        col = self.colLookup.get(argColName)
        result = col.dataType
        return result

    def getColumnNameByOrdinal(self, argOrd):
        result = self.colOrdLookup.get(argOrd)
        return result

    ################################################
    # Table.generateInsert
    ################################################

    def generateInsert(self, argCachedRow):

        colOrd = 0;

        valClauseList = []

        for colData in argCachedRow:
            colOrd += 1
            colName = self.colOrdLookup.get(colOrd)
            col = self.colLookup.get(colName)
            dataType = col.dataType

            insVal = None

            if colData is not None:

                strVal = str(colData)

                if dataType.useQuote:
                    insVal = strVal
                else:
                    if dataType.binary:
                        insVal = bytes(colData)
                    else:
                        insVal = strVal
            else:
                insVal = None

            valClauseList.append(insVal)

        joinStr = ', '.join(['%s'] * len(valClauseList))

        result = 'INSERT INTO ' + self.name + ' VALUES ({})'.format(joinStr)

        return result, valClauseList

    def addCachedDataRow(self, row):
        self.cachedData.append(row)

    def getCachedDataRowCount(self):
        result = len(self.cachedData)
        return result

    def purgeCachedData(self):

        if self.cachedData is not None:
            result = len(self.cachedData)
        else:
            result = 0

        self.cachedData = []

        return result

    def addColumn(self, argCol):
        self.colLookup[argCol.name] = argCol
        self.colOrdLookup[argCol.ordinal] = argCol.name

    def setPrimaryKey(self, argConst):
        self.primKey = argConst

    def addForeignKey(self, argConst):
        self.fkeys.append(argConst)

    def addUniqueKey(self, argConst):
        self.ukeys.append(argConst)

    def getColumnByName(self, argConst):
        result = self.colLookup.get(argConst)
        return result

    def getCreateStatement(self, crtUnqKeys):

        result = "CREATE TABLE %s (" % self.name;

        firstCol = True;

        for colOrd in self.colOrdLookup.keys():

            if not firstCol:
                result += ","

            colName = self.colOrdLookup.get(colOrd)
            col = self.colLookup.get(colName)

            result += "\n\t%s" % col

            firstCol = False

        result += "\n);"

        if crtUnqKeys:
            pkeyStmt = self.getPrimaryKeyStatement()
            result += "\n%s" % pkeyStmt

            ukeyClause = self.getUniqueConstraintClause()
            result += ukeyClause

        return result

    def getPrimaryKeyStatement(self):

        result = ''

        if self.primKey is not None:
            result += "%s" % self.primKey.getCreateStatement()
        else:
            result += "-- No PK in table %s" % self.name

        return result

    def getDeleteStatement(self):
        result = "DELETE FROM %s;" % self.name
        return result

    def getDropStatement(self):
        result = "DROP TABLE %s;" % self.name
        return result

    def getUniqueConstraintClause(self):

        result = ''

        for ukey in self.ukeys:
            result += "\n%s" % ukey.getCreateStatement()

        return result

    def getCreateConstraintClause(self):

        result = ''

        pkeyStmt = self.getPrimaryKeyStatement()
        result += "\n%s" % pkeyStmt

        for fkey in self.fkeys:
            result += "\n%s" % fkey.getCreateStatement()

        return result

    def getPrimaryKeyStatus(self):

        result = None

        if self.primKey is not None:

            pkColCount = len(self.primKey.cols)

            if pkColCount == 1:

                pkColName = self.primKey.cols[0]
                pkCol = self.getColumnByName(pkColName)
                pkColTypeSql = pkCol.dataType.sqlName

                if pkColTypeSql == "int4" or pkColTypeSql == "int8":
                    result = 0
                else:
                    result = 1
            else:
                result = 2
        else:
            result = 3

        return result

    def compare(self, argOther, argDeltaList):

        result = []

        compared = set()

        thisLabel = self.struct.getLabel()
        othLabel = argOther.struct.getLabel()

        for colName in self.colLookup.keys():
            thisCol = self.getColumnByName(colName)
            othCol = argOther.getColumnByName(colName)

            compared.add(colName)

            if othCol is not None:
                colDiffs = thisCol.compare(othCol, argDeltaList)
                #colDiffCnt = len(colDiffs)

                #if colDiffCnt > 0:
                #    print("Column <%s> in table <%s> is different" % (colName, self.name))

                #    for colDiff in colDiffs:
                #       print("\t%s" % colDiff)
            else:
                deltaDesc = "Column <%s> does not exist in table <%s> in <%s>" % (colName, self.name, othLabel)
                dropStmt = thisCol.getDropStatement()

                delta = SchemaDelta(deltaDesc, dropStmt)
                argDeltaList.append(delta)

                #print("Column <%s> does not exist in table <%s> in <%s>" % (colName, self.name, othLabel))

        for colName in argOther.colLookup.keys():

            if colName not in compared:
                #print("Column <%s> does not exist in table <%s> in <%s>" % (colName, self.name, thisLabel))

                othCol = argOther.getColumnByName(colName)
                deltaDesc = "Column <%s> does not exist in table <%s> in <%s>" % (colName, self.name, thisLabel)
                crtStmt = othCol.getCreateStatement()

                delta = SchemaDelta(deltaDesc, crtStmt)
                argDeltaList.append(delta)

    def __repr__(self):

        result = self.getCreateStatement()

        constrClause = self.getCreateConstraintClause()
        result += "\n%s" % constrClause

        return result


################################################
# class Column
################################################

class Column(object):
    def __init__(self, argName, argTable, argOrdinal, argNullable,
                 argType, argMaxLen, argDefault):

        self.name = argName
        self.table = argTable
        self.ordinal = argOrdinal
        self.dataType = argType
        self.nullable = argNullable
        self.maxLen = argMaxLen
        self.default = argDefault

    def getCreateStatement(self):

        crtClause = self.getCreateClause()
        result = "ALTER TABLE %s ADD COLUMN %s;" % (self.table, crtClause)
        return result

    def getCreateClause(self):

        type = self.getType()
        result = "%s %s" % (self.name, type)

        #if self.maxLen is not None:
        #    maxLenClause = "(" + str(self.maxLen) + ")"
        #    result += maxLenClause

        result += " NULL" if self.nullable else " NOT NULL"

        if self.default is not None:
            defClause = " DEFAULT %s" % self.default
            result += defClause

        return result

    def getDefault(self):
        return self.default

    def getType(self):
        result = self.dataType.sqlName

        if self.maxLen is not None:
            maxLenClause = "(" + str(self.maxLen) + ")"
            result += maxLenClause

        return result

    def getNullable(self):
        result = "NULL" if self.nullable else "NOT NULL"
        return result


    def getDropStatement(self):
        result = "ALTER TABLE %s DROP COLUMN %s;" % (self.table, self.name)
        return result

    def compare(self, argOther, argDeltaList):

        descList = []

        if self.dataType != argOther.dataType:

            deltaDesc = "Column <%s> dataType is different" % (self.name)
            type = argOther.getType()
            fixStmt = "ALTER TABLE %s ALTER COLUMN %s TYPE %s;" % (self.table, self.name, type)
            delta = SchemaDelta(deltaDesc, fixStmt)
            argDeltaList.append(delta)

        if self.nullable != argOther.nullable:

            deltaDesc = "Column <%s> nullable is different (%s vs %s)" % (self.name, self.nullable, argOther.nullable)
            nullable = argOther.getNullable()
            fixStmt = "ALTER TABLE %s ALTER COLUMN %s SET %s;" % (self.table, self.name, nullable)
            delta = SchemaDelta(deltaDesc, fixStmt)
            argDeltaList.append(delta)

        if self.maxLen != argOther.maxLen:

            deltaDesc = "Column <%s> maxLen is different (%s vs %s)" % (self.name, self.maxLen, argOther.maxLen)
            type = argOther.getType()
            fixStmt = "ALTER TABLE %s ALTER COLUMN %s TYPE %s;" % (self.table, self.name, type)
            delta = SchemaDelta(deltaDesc, fixStmt)
            argDeltaList.append(delta)

        if self.default != argOther.default:

            deltaDesc = "Column <%s> default is different (%s vs %s)" % (self.name, self.default, argOther.default)
            default = argOther.getDefault()
            fixStmt = "ALTER TABLE %s ALTER COLUMN %s SET DEFAULT %s;" % (self.table, self.name, default)
            delta = SchemaDelta(deltaDesc, fixStmt)
            argDeltaList.append(delta)

    def __repr__(self):
        result = self.getCreateClause()
        return result

################################################
# class SchemaDelta
################################################

class SchemaDelta(object):
    def __init__(self, argDeltaDesc, argFixFwd, argFixRev=None):

        self.deltaDesc = argDeltaDesc
        self.fixFwd = argFixFwd
        self.fixRev = argFixRev

################################################
# getTypeConverter
################################################

def getTypeConverter():
    result = dict()

    result['bigint'] = DataType('bigint', 'int8', False, False)
    result['integer'] = DataType('integer', 'int4', False, False)
    result['character varying'] = DataType('character varying', 'varchar', True, False)
    result['timestamp without time zone'] = DataType('timestamp without time zone', 'timestamp', True, False)
    result['boolean'] = DataType('boolean', 'bool', False, False)
    result['double precision'] = DataType('double precision', 'float8', False, False)
    result['timestamp with time zone'] = DataType('timestamp with time zone', 'timestamptz', True, False)
    result['text'] = DataType('text', 'text', True, False)
    result['numeric'] = DataType('numeric', 'numeric', False, False)
    result['bytea'] = DataType('bytea', 'bytea', False, True)
    result['character'] = DataType('character', 'bpchar', True, False)
    result['date'] = DataType('date', 'date', True, False)

    return result

################################################
# def compareSchemas
################################################

def compareSchemas(argSrc, argTrg, argDeltaList):
    argTrg.compareTables(argSrc, argDeltaList)
    argTrg.compareSequences(argSrc, argDeltaList)



################################################
# def cloneSchema
################################################

def cloneSchema(argSource, argTarget, argJobConfig, argInitFile=None):

    argTarget.cleanDatabase()

    cloneSequences(argSource, argTarget, argJobConfig)

    cloneTables(argSource, argTarget, argJobConfig)

    cloneViews(argSource, argTarget, argJobConfig)

    #recordClone(argSource, argTarget, argJobConfig, argInitFile)


################################################
# def cloneReferences
################################################

def cloneReferences(argSource, argTarget, argJobConfig):

    logger = argJobConfig.logger
    logger.printDivider()

    for tblName in sorted(argSource.tblLookup.keys()):

        table = argSource.tblLookup[tblName]

        for fkey in table.fkeys:
            createStmt = fkey.getCreateStatement()
            keyName = fkey.name

            if argJobConfig.doDump():
                dumpFileName = "KEY." + keyName + ".SRC"
                dumpFilePath = os.path.join(argJobConfig.workDir, dumpFileName)
                CommonUtil.createFileWithContent(dumpFilePath, createStmt)

            logger.dumpLine("CREATING FOREIGN KEY: %s" % keyName)

            crtResult = argTarget.executeStatement(createStmt, None, False)

            if not crtResult:
                logger.dumpLine("Unable to create FK: %s" % keyName)
                keyDefn = fkey.defn
                #logger.dumpLine("Problem with definition: %s" % keyDefn)

                regex = re.match(r"FOREIGN KEY \((.+)\) REFERENCES (.+)\((.+)\)", keyDefn)
                localCol = regex.group(1)
                #logger.dumpLine("localCol: %s" % localCol)
                refTblName = regex.group(2)
                #logger.dumpLine("refTblName: %s" % refTblName)
                refCol = regex.group(3)
                #logger.dumpLine("refCol: %s" % refCol)

                cleanupSql = "DELETE FROM %s WHERE %s NOT IN (SELECT %s FROM %s);" % (tblName, localCol, refCol, refTblName)

                logger.dumpLine("cleanupSql: %s" % cleanupSql)

                argTarget.executeStatement(cleanupSql)

                crtResult = argTarget.executeStatement(createStmt)

                if crtResult:
                    logger.dumpLine("RETRY SUCCESS IMPOSING FK: %s" % keyName)

################################################
# def cloneViews
################################################

def cloneViews(argSource, argTarget, argJobConfig):

    jobLogger = argJobConfig.logger
    jobLogger.printDivider()

    for viewName in sorted(argSource.viewLookup.keys()):

        view = argSource.viewLookup[viewName]
        createStmt = view.getCreateStatement()

        if argJobConfig.doDump():
            dumpFileName = "KEY." + viewName + ".SRC"
            dumpFilePath = os.path.join(argJobConfig.workDir, dumpFileName)
            CommonUtil.createFileWithContent(dumpFilePath, createStmt)

            jobLogger.dumpLine("CREATING VIEW: %s" % viewName)

        argTarget.executeStatement(createStmt)

################################################
# def cloneSequences
################################################

def cloneSequences(argSource, argTarget, argJobConfig):

    #argTarget.cleanSequences()

    jobLogger = argJobConfig.logger
    jobLogger.printDivider()

    dumpDir = argJobConfig.workDir

    for seqName in sorted(argSource.seqLookup.keys()):
        seqnc = argSource.seqLookup[seqName]
        createStmt = seqnc.getCreateStatement()

        if argJobConfig.doDump():
            dumpFileName = "SEQ." + seqName + ".SRC"
            dumpFilePath = os.path.join(dumpDir, dumpFileName)
            CommonUtil.createFileWithContent(dumpFilePath, createStmt)

        jobLogger.dumpLine("CREATING SEQUENCE: %s" % seqName)
        argTarget.executeStatement(createStmt)

################################################
# def updateSequences
################################################

def updateSequences(argSource, argTarget, argJobConfig):

    #argTarget.cleanSequences()

    jobLogger = argJobConfig.logger
    jobLogger.printDivider()

    dumpDir = argJobConfig.workDir

    for seqName in sorted(argTarget.seqLookup.keys()):
        srcSeqnc = argSource.seqLookup[seqName]
        trgSeqnc = argTarget.seqLookup[seqName]

        if srcSeqnc.start > trgSeqnc.start:

            altStmt = srcSeqnc.getAlterStatement()

            if argJobConfig.doDump():
                dumpFileName = "SEQ-ALT." + seqName + ".TRG"
                dumpFilePath = os.path.join(dumpDir, dumpFileName)
                CommonUtil.createFileWithContent(dumpFilePath, altStmt)

            jobLogger.dumpLine("ALTERING SEQUENCE: %s" % seqName)
            argTarget.executeStatement(altStmt)

################################################
# def cloneTables
################################################

def cloneTables(argSource, argTarget, argJobConfig):

    jobLogger = argJobConfig.logger
    jobLogger.printDivider()

    dumpDir = argJobConfig.workDir

    for tblName in sorted(argSource.tblLookup.keys()):
        table = argSource.tblLookup[tblName]
        createStmt = table.getCreateStatement(True)

        if argJobConfig.doDump():
            dumpFileName = "TBL." + tblName + ".SRC"
            dumpFilePath = os.path.join(dumpDir, dumpFileName)
            CommonUtil.createFileWithContent(dumpFilePath, createStmt)

        jobLogger.dumpLine("CREATING TABLE: %s" % tblName)
        argTarget.executeStatement(createStmt)

################################################
# def recordClone
################################################

def recordClone(argSource, argTarget, argJobConfig, argInitFile):

    #jobLogger = argJobConfig.logger
    srcSchema = argSource.connConfig.schema
    trgSchema = argTarget.connConfig.schema
    srcSysTableExist = PostgresUtil.confirmTableExistence(argSource.cxn, srcSchema, "sys_install")
    trgSysTableExist = PostgresUtil.confirmTableExistence(argTarget.cxn, trgSchema, "sys_install")

    if not trgSysTableExist:
        PostgresAdmin.resetVersionModule(argTarget.cxn, argJobConfig, argInitFile)

    trgBranch = PostgresAdmin.getCurrentBranch(argTarget.cxn)

    procUserID = argJobConfig.userID
    baseVersion = trgBranch.version
    install = SysInstall(trgBranch.branchId, 'CLN', baseVersion, procUserID)

    install.persist(argTarget.cxn)

    if srcSysTableExist:
        srcBranch = PostgresAdmin.getCurrentBranch(argSource.cxn)
        srcVersion = srcBranch.version

        crntRev = PostgresAdmin.getRevisionById(argSource.cxn, srcVersion)
        crntRev.installId = install.installId
        crntRev.persist(argTarget.cxn)

        trgBranch.version = srcVersion
        trgBranch.update(argTarget.cxn)

################################################
# def copyDatabaseData
################################################

def copyDatabaseData(argSrcStruct, argTargStruct, jobLogger):

	beginTS = time.time()

	dataCount = 0
	noDataCount = 0

	for tblName in sorted(argSrcStruct.tblLookup.keys()):

		rowCount = copyTableData(tblName, argSrcStruct, argTargStruct, jobLogger)

		if rowCount:
			dataCount += 1
		else:
			noDataCount += 1

	endTS = time.time()

	copyTime = endTS - beginTS

	copySummaryDump = LogDump()

	copySummaryDump.addSimpleEntry(jobLogger.divider)
	copySummaryDump.addSimpleEntry("Database Copy Summary")
	copySummaryDump.addSimpleEntry("\nTables with data: \t\t%d" % dataCount)
	copySummaryDump.addSimpleEntry("Tables with no data: \t\t%d" % noDataCount)
	copySummaryDump.addSimpleEntry("Time (sec): \t\t\t%d" % copyTime)
	copySummaryDump.addSimpleEntry(jobLogger.divider)

	jobLogger.dumpLog(copySummaryDump)

################################################
# def copyTableData
################################################

def copyTableData(argTblName, argSrcStruct, argTargStruct, argLogger):

	srcTbl = argSrcStruct.getTableByName(argTblName)
	targTbl = argTargStruct.getTableByName(argTblName)

	deleteStmt = targTbl.getDeleteStatement()
	argTargStruct.executeStatement(deleteStmt, None)

	argLogger.printDivider()
	argLogger.dumpLine("Copying data for:")
	rowCount, pkStatus = argSrcStruct.getTableSummary(argTblName)

	if rowCount == 0: return 0

	rowLimit = 1000 if rowCount <= 10000 else 5000

	beginTS = time.time()

	# if the table has a simple numeric, incremented PK
	# we should load the data and copy it in segments divided by the PK range
	# for performance. otherwise load at once but copy still in segments
	if pkStatus == 0:

		idColName = srcTbl.primKey.cols[0]

		dataLoadInit = True
		complRowId = None

		totalLoaded = 0

		while(dataLoadInit or complRowId is not None):

			dataLoadInit = False

			segCount, complRowId = copyTableDataSegment(srcTbl, argSrcStruct, argTargStruct, argLogger, rowLimit, idColName, complRowId)

			totalLoaded += segCount

			displayDataCopyStatus(totalLoaded, rowCount, argTblName, argLogger)
	else:

		totalLoaded, ignore = copyTableDataSegment(srcTbl, argSrcStruct, argTargStruct, argLogger, rowLimit)

	endTS = time.time()
	copyTime = endTS - beginTS

	argLogger.dumpLine("\nTime (sec): %d" % copyTime)

	return totalLoaded


################################################
# def copyTableDataSegment
################################################

def copyTableDataSegment(argTbl, argSrcStruct, argTargStruct, argLogger, argLimit=None, argIdCol=None, argID=None):

	tblName = argTbl.name
	targTbl = argTargStruct.getTableByName(tblName)

	targTbl.purgeCachedData()

	purgedCount = 0

	try:

		cur = argSrcStruct.cxn.cursor()

		dataStmt = "SELECT * FROM %s" % argTbl.name

		rowIdColInd = None

		if argIdCol is not None:

			if argID is not None:
				dataStmt += " WHERE %s > %d" % (argIdCol, argID)

			dataStmt += " ORDER BY %s LIMIT %d;" % (argIdCol, argLimit)

			rowIdColInd = argTbl.getColumnByName(argIdCol).ordinal - 1

		cur.execute(dataStmt)
		rows = cur.fetchall()

		rowID = None

		rowCounter = 0
		segTotal = 0

		rowSetCount = len(rows)

		for row in rows:

			rowCounter += 1

			if rowIdColInd is not None:
				rowID = row[rowIdColInd]

			targTbl.addCachedDataRow(row)

			if rowCounter == argLimit:

				purgedCount = persistAndPurgeCache(tblName, argTargStruct)
				rowCounter = 0
				segTotal += purgedCount

				if argIdCol is None:
					displayDataCopyStatus(segTotal, rowSetCount, tblName, argLogger)

		if len(targTbl.cachedData) > 0:

			purgedCount = persistAndPurgeCache(tblName, argTargStruct)
			rowID = None
			segTotal += purgedCount

			if argIdCol is None:
				displayDataCopyStatus(segTotal, rowSetCount, tblName, argLogger)

	finally:

		cur.close()

	return purgedCount, rowID

################################################
# def persistAndPurgeCache
################################################

def persistAndPurgeCache(argTblName, argStruct):

	table = argStruct.getTableByName(argTblName)
	argStruct.persistTableCachedData(table)
	result = table.purgeCachedData()

	return result

################################################
# def displayDataCopyStatus
################################################

def displayDataCopyStatus(argProg, argRowsTotal, argTblName, argLogger):
	argLogger.dumpLine("Loaded %d / %d rows in <%s>" % (argProg, argRowsTotal, argTblName))
