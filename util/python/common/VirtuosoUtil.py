#!/usr/bin/python3



import requests
import sys
import re
import os
import xml.etree.ElementTree as ET

from subprocess import Popen, PIPE

import CommonUtil

from LogUtil import LogDump

################################################
# RdfDataType
################################################

class RdfDataType(object):
	#object.typeNameSpace = 'http://www.w3.org/2001/XMLSchema#'
	TYPE_NAME_SPACE = 'http://www.w3.org/2001/XMLSchema#'

	def __init__(self, argShorthand, argOpenMark, argCloseMark, argSpecExplicit):
		self.shorthand = argShorthand
		self.openMark = argOpenMark
		self.closeMark = argCloseMark
		self.specExplicit = argSpecExplicit

	def __repr__(self):

		result = RdfDataType.TYPE_NAME_SPACE + self.shorthand
		return result

################################################
# SubjectCluster
################################################

class SubjectCluster(object):

	def __init__(self, argSubj):
		self.subjectURI = argSubj
		self.triples = []

	def addTriple(self, argTriple):

		predVal = argTriple.predicate.value

		if predVal == 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type':
			argTriple.predicate.value = 'a'

			#aType = globDataTypeMap.get('a')
			aType = RdfDataType('a', None, None, False)
			argTriple.predicate.type = aType

			self.triples = [argTriple] + self.triples
		else:
			self.triples.append(argTriple)

	def __repr__(self):

		result = ""

		clustOrd = 0
		clustSize = len(self.triples)

		for regTriple in self.triples:

			clustOrd += 1

			if clustOrd == 1:
				result += regTriple.getSubjectHead()
			else:
				result += regTriple.getSubjectTriple()

			objType = regTriple.object.type
			#typeSpec = ' # ' + str(objType) if objType.specExplicit else ''

			if clustOrd == clustSize:
				#result += " .%s" % typeSpec
				result += " ."
			else:
				#result += " ;%s\n" % typeSpec
				result += " ;\n"


		return result

################################################
# Triple
################################################

class Triple(object):

	def __init__(self, argSubj):
		#uriType = globDataTypeMap.get('anyURI')
		uriType = RdfDataType('anyURI', '<', '>', False)
		self.subject = TripleElement(argSubj, uriType)

	def setPredicate(self, argVal, argType):
		self.predicate = TripleElement(argVal, argType)

	def setObject(self, argVal, argType):
		self.object = TripleElement(argVal, argType)

	def getTypeSpec(self):
		objType = self.object.type
		result = '^^<%s>' % str(objType) if objType.specExplicit else ''
		return result

	def getSubjectHead(self):

		typeSpec = self.getTypeSpec()
		result = "%s\n\t%s\t%s%s" % (self.subject, self.predicate, self.object, typeSpec)

		#if self.object.type == 'L':
		#	result += " # %s"
		return result

	def getSubjectTriple(self):

		typeSpec = self.getTypeSpec()
		result = "\t%s\n\t\t%s%s" % (self.predicate, self.object, typeSpec)

		return result

################################################
# TripleElement
################################################

class TripleElement(object):

	def __init__(self, argVal, argType):

		self.value = argVal
		self.type = argType
		#self.dataType = argDataType

	def __repr__(self):

		outVal = CommonUtil.denullifyString(self.value)

		if self.type.shorthand == 'string':
			insVal = outVal.replace('\'', '\\\'')
		else:
			insVal = outVal

		result = "%s%s%s" % (CommonUtil.denullifyString(self.type.openMark), insVal, CommonUtil.denullifyString(self.type.closeMark))

		return result

################################################
# runIsqlBatch
################################################

def runIsqlBatch(argInpFilePath, argHost, argPort, argUser, argPasswd):

	impCmd = "isql %s:%d %s %s < %s" % (argHost, argPort, argUser, argPasswd, argInpFilePath)

	#print("impCmd = %s" % impCmd)
	subproc = Popen(impCmd, shell=True, stdin=PIPE, stdout=PIPE, stderr=PIPE)
	stdOutByt, stdErrByt = subproc.communicate()
	stdOut = stdOutByt.decode()
	stdErr = stdErrByt.decode()

	# perhaps do something if below is other than 0
	# retCode = subproc.returncode

	return stdOut, stdErr

################################################
# doImport
################################################

def doImport(argDumpFilePath, argVirtConfig, argImpDir=None):

	dumpFilePath = os.path.normpath(argDumpFilePath)
	dumpFileName = os.path.basename(dumpFilePath)

	rqstIDRegex = re.match(r"^rdf_(\d+).ttl", dumpFileName)

	rqstIDstr = rqstIDRegex.group(1)
	rqstID = int(rqstIDstr)

	#expFileName = 'rdf_%d.ttl' % argRqstID
	#expFilePath = argDataDir + '//' + expFileName

	dumpDir = os.path.dirname(argDumpFilePath)

	#cmdFilePath = argDataDir + '/import_%d.sparql' % rqstID
	cmdFilePath = os.path.join(dumpDir, 'import_%d.sparql' % rqstID)

	if argImpDir is not None:
		impDir = argImpDir
	else:
		impDir = dumpDir

	impDirDlmtr = CommonUtil.getPathDelimiter(impDir)
	#dumpFileDlmtr = CommonUtil.getPathDelimiter(dumpFilePath)

	impFilePath = impDir + impDirDlmtr + dumpFileName

	graph = argVirtConfig.graph
	isqlHost = argVirtConfig.isqlHost
	isqlPort = argVirtConfig.isqlPort
	isqlUser = argVirtConfig.isqlUser
	isqlPass = argVirtConfig.isqlPass

	impFileCont = 'DB.DBA.TTLP_MT(file_to_string_output(\'%s\'), \'\', \'%s\', 255);' % (impFilePath, graph)

	createSparqlInputFile(cmdFilePath, impFileCont)



	stdOut, stdErr = runIsqlBatch(cmdFilePath, isqlHost, isqlPort, isqlUser, isqlPass)

	return stdOut, stdErr, cmdFilePath


################################################
# createSparqlInputFile
################################################

def createSparqlInputFile(argFilePath, argCommand):

	#r"{}".format(argCommand)
	content = argCommand.replace("\\", "\\\\") + "\n\ncheckpoint;"
	#content = r"{}".format(argCommand) + "\n\ncheckpoint;"

	#result = argDir + '/' + argFileName

	#CommonUtil.createFileWithContent(argFilePath, "D:\\parent\\child")

	result = CommonUtil.createFileWithContent(argFilePath, content)

    #return result

################################################
# clearGraph
################################################

def clearGraph(argVirtConfig, argDumpDir, argLogger):

	clearLogDumpStart = LogDump()
	clearLogDumpStart.addSimpleEntry("CLEARING GRAPH")
	#clearLogDumpStart.addEntry("TARGET PORT", isqlPort)

	serviceURL = argVirtConfig.serviceURL
	graph = argVirtConfig.graph
	isqlHost = argVirtConfig.isqlHost
	isqlPort = argVirtConfig.isqlPort
	isqlUser = argVirtConfig.isqlUser
	isqlPass = argVirtConfig.isqlPass

	triplesBeforeClear = getTripleCount(serviceURL, None, graph)
	clearLogDumpStart.addEntry("TRIPLES BEFORE CLEAR", triplesBeforeClear)

	argLogger.dumpLog(clearLogDumpStart)

	inpFileCont = "SPARQL CLEAR GRAPH <%s>;" % graph

	#clearGraphFile = argDumpDir + "/clearGraph.sparql"
	clearGraphFile = os.path.join(argDumpDir, 'clearGraph.sparql')

	createSparqlInputFile(clearGraphFile, inpFileCont)

	stdOut, stdErr = runIsqlBatch(clearGraphFile, isqlHost, isqlPort, isqlUser, isqlPass)

	if stdErr == '':

		triplesAfterClear = getTripleCount(serviceURL, None, graph)

		if triplesAfterClear == 0:
			result = True
			logResult = "SUCCESS"
		else:
			result = False
			logResult = "FAILED"
	else:
		triplesAfterClear = "N/A"
		result = False
		logResult = "FAILED"

	clearLogDumpEnd = LogDump(False)


	# print("triplesAfterClear = %s" % triplesAfterClear)
	clearLogDumpEnd.addEntry("TRIPLES AFTER CLEAR", triplesAfterClear)

	clearLogDumpEnd.addEntry("CLEARING RESULT", logResult)
	argLogger.dumpLog(clearLogDumpEnd)

	return result

################################################
# executeSparql
################################################

def executeSparql(baseURL, query, graph="", abortOnFail=True):

	params={

		"default-graph-uri": graph,
		"should-sponge": "soft",
		"query": query,
		"debug": "on",
		"timeout": "",
		"format": "application/xml",
		"save": "display",
		"fname": "",
	}

	response = requests.get(baseURL, params=params, headers = {'Content-Type': 'text/xml'});

	retCode = response.status_code

	if retCode != 200:
		print("ERROR:")
		print("Unable to get response from <%s>. Return code=%s" % (baseURL, retCode))
		print("Query: %s" % query)

		if abortOnFail:
			sys.exit(1)
		else:
			return None

	return response

################################################
# getTripleCount
################################################

def getTripleCount(baseURL, offset, graph=""):

	filterClause = "FILTER(STR(?s) > \"%s\")" % offset if offset is not None else ""

	query = "SELECT COUNT(*) { ?s ?p ?o . %s }" % filterClause

	response = executeSparql(baseURL, query, graph, False)

	if response != None:
		respXML = response.content.decode("utf-8")

		respRoot = ET.fromstring(respXML)

		resultsElem = respRoot.find("{http://www.w3.org/2005/sparql-results#}results")
		resultElem = resultsElem.find("{http://www.w3.org/2005/sparql-results#}result")
		bindingElem = resultElem.find("{http://www.w3.org/2005/sparql-results#}binding")
		literalElem = bindingElem.find("{http://www.w3.org/2005/sparql-results#}literal")
		countStr = literalElem.text

		result = int(countStr)
	else:
		result = None

	return result


################################################
# getData
################################################

def getData(argBaseURL, argBatchMax, argOffset, argTypeMap, argGraph=""):

	filterClause = "FILTER(STR(?s) > \"%s\")" % argOffset if argOffset is not None else ""

	query = "SELECT * { ?s ?p ?o . %s } ORDER BY ?s LIMIT %d" % (filterClause, argBatchMax)

	response = executeSparql(argBaseURL, query, argGraph, True)

	respXML = response.content.decode("utf-8")

	scrubbedXML = re.sub('&.+[0-9]+;', '', respXML)

	respRoot = ET.fromstring(scrubbedXML)

	resultsElem = respRoot.find("{http://www.w3.org/2005/sparql-results#}results")
	resultElems = resultsElem.findall("{http://www.w3.org/2005/sparql-results#}result")

	result = []

	prevSubjURI = None

	for resultElem in resultElems:

		bindingElems = resultElem.findall("{http://www.w3.org/2005/sparql-results#}binding")

		#triple = Triple(subject)
		predicate = None

		for bindingElem in bindingElems:
			bindType = bindingElem.get('name')

			uriElem = bindingElem.find("{http://www.w3.org/2005/sparql-results#}uri")

			if uriElem is not None:
				val = uriElem.text
				dataTypeURI = 'http://www.w3.org/2001/XMLSchema#anyURI'

			else:
				literalElem = bindingElem.find("{http://www.w3.org/2005/sparql-results#}literal")
				val = literalElem.text

				dataTypeURI = literalElem.get('datatype')

				if dataTypeURI is None:
					dataTypeURI = 'http://www.w3.org/2001/XMLSchema#string'

			typeShorthand = dataTypeURI.replace(RdfDataType.TYPE_NAME_SPACE, '')

			dataType = argTypeMap.get(typeShorthand)

			if dataType is None:
				print("Shorthand <%s> unknown" % typeShorthand)
				sys.exit()

			if bindType == 's':


				subjURI = val

				if prevSubjURI is None or subjURI != prevSubjURI:
					subject = SubjectCluster(subjURI)
					result.append(subject)

				prevSubjURI = subjURI
				triple = Triple(subjURI)
			elif bindType == 'p':

				triple.setPredicate(val, dataType)

			elif bindType == 'o':
				triple.setObject(val, dataType)
				subject.addTriple(triple)

	return result

################################################
# getTypeMap
################################################

def getTypeMap():

	result = {'string': RdfDataType('string', '\'', '\'', False),
					   'anyURI': RdfDataType('anyURI', '<', '>', False),
					   'integer': RdfDataType('integer', '\'', '\'', True),
					   'int': RdfDataType('integer', '\'', '\'', True),
					   'long': RdfDataType('long', '\'', '\'', True),
					   'a': RdfDataType('a', None, None, False),
					   'dateTime': RdfDataType('dateTime', '\'', '\'', True),
					   'java:java.sql.Timestamp': RdfDataType('java:java.sql.Timestamp', '\'', '\'', True)}

	return result

