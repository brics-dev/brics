#!/usr/bin/python3

import requests
import argparse
import configparser
import sys
import xml.etree.ElementTree as ET
import xml.dom.minidom

#create PYTHONPATH env var and set it
#to the dir where CommonUtil.py is
import CommonUtil

print("Welcome to Dictionary Loader !")



################################################
# def createElementTag
################################################

def createElementTag(argGrpName, argElemID, argPos, argReqType):

	result = ET.Element("mapElements")
	repeatableGroup = ET.Element("repeatableGroup")
	repeatableGroup.text = argGrpName
	result.append(repeatableGroup)
	dataElement = ET.Element("dataElement")
	elemID = ET.Element("id")
	elemID.text = str(argElemID)
	dataElement.append(elemID)
	result.append(dataElement)
	position = ET.Element("position")
	position.text = str(argPos)
	result.append(position)
	requiredTypeId = ET.Element("requiredTypeId")
	requiredTypeId.text = argReqType
	result.append(requiredTypeId)

	return result

################################################
# def findElement
################################################

def findElement(argServiceHost, argName):

	serviceURL = argServiceHost + '/dictionary-service/api/v1/element/' + argName

	result = findEntity(serviceURL)

	return result

################################################
# def findStructureByID
################################################

def findStructureByID(argServiceHost, argID):

	serviceURL = argServiceHost + '/dictionary-service/api/v1/structureByID/' + str(argID)

	result = findEntity(serviceURL)

	return result

################################################
# def findStructure
################################################

def findStructure(argServiceHost, argName):

	serviceURL = argServiceHost + '/dictionary-service/api/v1/structure/' + argName

	result = findEntity(serviceURL)

	return result

################################################
# def findEntity
################################################

def findEntity(argServiceURL):

	print("\nEntity GET URL: %s" % argServiceURL)

	resp = requests.get(argServiceURL)

	respCode = resp.status_code

	if respCode == 200:

		result = resp.content.decode("utf-8")

	else:
		result = None

	return result

################################################
# def createMessage
################################################

def createMessage(argTemplFile, argName):

	with open(argTemplFile, 'r', encoding='UTF-8') as myfile:
		templ = myfile.read()

	if argName:
		result = templ.replace('@NAME@', argName)
	else:
		result = templ

	return result

################################################
# def postEntity
################################################

def postEntity(argServiceHost, argType, argTemplFile, argName=None):

	data = createMessage(argTemplFile, argName)


	serviceURL = argServiceHost + '/dictionary-service/api/v1/' + argType + '/create'

	headers = {'Content-Type': 'application/xml'}

	if argType == 'FormStructure':

		xmlRoot = ET.fromstring(data)
		repGrps = xmlRoot.findall("./StructuralFormStructure/repeatableGroups")

		for repGrp in repGrps:

			grpNameTag = repGrp.find("name")
			grpName = grpNameTag.text

			elemGenList = repGrp.findall("elemGen")

			for elemGen in elemGenList:
				template = elemGen.get('template')
				position = int(elemGen.get('position'))
				namebase = elemGen.get('namebase')

				elemName = namebase
				elemID = postEntity(argServiceHost, 'DataElement', template, elemName)
				elemTag = createElementTag(grpName, elemID, position, 'RECOMMENDED')

				repGrp.remove(elemGen)
				repGrp.append(elemTag)

			elemFindList = repGrp.findall("elemFind")

			for elemFind in elemFindList:
				findName = elemFind.get('name')
				position = int(elemFind.get('position'))

				foundElem = findElement(argServiceHost, findName)
				foundElemRoot = ET.fromstring(foundElem)
				idTag = foundElemRoot.find("./StructuralDataElement/id")
				foundElemID = int(idTag.text)

				elemTag = createElementTag(grpName, foundElemID, position, 'RECOMMENDED')

				versionTag = foundElemRoot.find("./StructuralDataElement/version")
				version = versionTag.text

				print("Obtained element: name = <%s>, id = <%d>, version = <%s>" % (findName, foundElemID, version))

				repGrp.remove(elemFind)
				repGrp.append(elemTag)

		data = ET.tostring(xmlRoot, method="xml").decode("utf-8")

	print("Posting message:\n%s" % prettifyXML(data))

	resp = requests.post(serviceURL, data=data, headers=headers)
	respCode = resp.status_code

	respXML = resp.content.decode("utf-8")

	if respCode != 200:

		print("Error creating %s using template %s" % (argType, argTemplFile))
		print("Error Service URL: %s" % serviceURL)
		sys.exit(1)

	respRoot = ET.fromstring(respXML)

	if argType == 'FormStructure':
		idTag = respRoot.find("./StructuralFormStructure/id")
		versionTag = respRoot.find("./StructuralFormStructure/version")
	elif argType == 'DataElement':
		idTag = respRoot.find("./StructuralDataElement/id")
		versionTag = respRoot.find("./StructuralDataElement/version")

	entityID = int(idTag.text)
	version = versionTag.text

	print("Created %s: id = <%d>, version = <%s>" % (argType, entityID, version))

	return entityID

################################################
# def showConfigGroup
################################################

def showConfigGroup(argConf, argGrpTkn):

	print("[%s]" % argGrpTkn);

	confGroup = argConf[argGrpTkn]

	for confVar in confGroup.keys():
		confVal = confGroup[confVar]
		print("%s=%s" % (confVar, confVal))

################################################
# def parseStructureList
################################################

def parseStructureList(argStructListResp, argOrg=None):
	respRoot = ET.fromstring(argStructListResp)

	resultElems = respRoot.findall("formStructureItem")

	structCount = len(resultElems)

	#print("structCount = %d" % structCount)

	result = []

	for structElem in resultElems:
		idElem = structElem.find("id")
		idVal = int(idElem.text)

		incl = True
		if argOrg:
			orgElem = structElem.find("organization")
			org = orgElem.text

			if argOrg != org:
				incl = False

		#print("id = %d" % idVal)
		if incl:
			result.append(idVal)

	return result

################################################
# def processStructureXML
################################################

def processStructureXML(argStructXML):

	xmlRoot = ET.fromstring(argStructXML)

	sqlStructElem = xmlRoot.find("StructuralFormStructure")
	structIdElem = sqlStructElem.find("id")
	structId = structIdElem.text
	sqlStructElem.remove(structIdElem)
	print("structId = %s" % structId)

	shadowIdElem = sqlStructElem.find("shadowId")

	if shadowIdElem is not None:
		sqlStructElem.remove(shadowIdElem)

	shadowIdElem = ET.Element("shadowId")
	shadowIdElem.text = structId
	sqlStructElem.append(shadowIdElem)

	shortNameElem = sqlStructElem.find("shortName")
	shortName = shortNameElem.text
	shortNameSuffix = "_clone_" + structId
	shortNameSuffixLength = len(shortNameSuffix)
	newShortNameBase = shortName[:(max(len(shortName) - shortNameSuffixLength, 26 - shortNameSuffixLength))]
	newShortName = newShortNameBase + shortNameSuffix
	shortNameElem.text = newShortName

	sqlOrgElem = sqlStructElem.find("organization")
	sqlOrgElem.text = "DataDouble"

	titleElem = sqlStructElem.find("title")
	title = titleElem.text
	titleSuffix = " (clone of id %s)" % structId
	titleSuffixLength = len(titleSuffix)
	newTitleBase = title[:(max(len(title) - titleSuffixLength, 100 - titleSuffixLength))]
	newTitle = newTitleBase + titleSuffix
	titleElem.text = newTitle

	dataElemsElem = xmlRoot.find("dataElements")
	xmlRoot.remove(dataElemsElem)

	repGrpElems = sqlStructElem.findall("repeatableGroups")

	for repGrpElem in repGrpElems:

		repGrpIdElem = repGrpElem.find("id")
		repGrpId = repGrpIdElem.text
		print("repGrpId = %s" % repGrpId)

		newRepGrpIdElem = ET.Element("id")
		newRepGrpIdElem.text = "-1"
		repGrpElem.remove(repGrpIdElem)
		repGrpElem.append(newRepGrpIdElem)

		repGrpNameElem = repGrpElem.find("name")
		repGrpName = repGrpNameElem.text
		print("repGrpName = %s" % repGrpName)

		#elemTag = createElementTag("X", "1", 'RECOMMENDED')
		mapElems = repGrpElem.findall("mapElements")

		for mapElem in mapElems:
			mapElemRepGrpElem = mapElem.find("repeatableGroup")
			mapElemRepGrp = mapElemRepGrpElem.text
			mapElemReqdElem = mapElem.find("requiredTypeId")
			mapElemReqd = mapElemReqdElem.text
			mapElemPostnElem = mapElem.find("position")
			mapElemPostn = mapElemPostnElem.text
			mapElemDataElemElem = mapElem.find("dataElement")
			mapElemDataElemIdElem = mapElemDataElemElem.find("id")
			mapElemDataElemId = mapElemDataElemIdElem.text
			newMapElem = createElementTag(mapElemRepGrp, mapElemDataElemId, mapElemPostn, mapElemReqd)
			repGrpElem.remove(mapElem)
			repGrpElem.append(newMapElem)

	diseaseListElem = sqlStructElem.find("diseaseList")
	diseaseListIdElem = diseaseListElem.find("id")
	diseaseListElem.remove(diseaseListIdElem)
	diseaseListStructElem = diseaseListElem.find("formStructure")
	diseaseListStructElem.text = newShortName

	diseaseElems = diseaseListElem.findall("disease")

	for diseaseElem in diseaseElems:
		diseaseIdElem = diseaseElem.find("id")
		diseaseId = diseaseIdElem.text

		newDiseaseElem = ET.Element("disease")
		newDiseaseIdElem = ET.Element("id")
		newDiseaseIdElem.text = diseaseId
		newDiseaseElem.append(newDiseaseIdElem)

		diseaseListElem.remove(diseaseElem)
		diseaseListElem.append(newDiseaseElem)

	sqlDateModElem = sqlStructElem.find("modifiedDate")

	if sqlDateModElem is not None:
		sqlStructElem.remove(sqlDateModElem)

	modifiedUserIdElem = sqlStructElem.find("modifiedUserId")

	if modifiedUserIdElem is not None:
		sqlStructElem.remove(modifiedUserIdElem)

	semanticStructElem = xmlRoot.find("SemanticFormStructure")
	semanticUriElem = semanticStructElem.find("uri")
	semanticStructElem.remove(semanticUriElem)
	semanticDateModElem = semanticStructElem.find("modifiedDate")
	semanticStructElem.remove(semanticDateModElem)
	semanticCreatnDateElem = semanticStructElem.find("dateCreated")
	semanticStructElem.remove(semanticCreatnDateElem)

	semanticShortNameElem = semanticStructElem.find("shortName")
	semanticShortNameElem.text = newShortName
	semanticTitleElem = semanticStructElem.find("title")
	semanticTitleElem.text = newTitle
	semanticOrgElem = semanticStructElem.find("organization")
	semanticOrgElem.text = "DataDouble"
	semanticCreatedByElem = semanticStructElem.find("createdBy")

	if semanticCreatedByElem is not None:
		semanticStructElem.remove(semanticCreatedByElem)

	result = ET.tostring(xmlRoot, method="xml").decode("utf-8")
	prettyResult = prettifyXML(result)

	structXmlFilePath = "conf/" + newShortName + ".xml"

	structXmlFilePath = CommonUtil.createFileWithContent(structXmlFilePath, prettyResult)

	return structXmlFilePath

################################################
# def prettifyXML
################################################

def prettifyXML(argOrigXML):
	unmarshalled = xml.dom.minidom.parseString(argOrigXML)
	result = unmarshalled.toprettyxml()
	return result

################################################
# def deleteClonedStructures
################################################

def deleteClonedStructures(argServiceHost):
	print("In runBatch for service host %s" % argServiceHost)

	allStructURL = argServiceHost + "/dictionary-service/api/v1/allSqlFormStructures"

	allStructResp = findEntity(allStructURL)

	clonedList = parseStructureList(allStructResp, "DataDouble")

	clonedListSize = len(clonedList)

	print("Presently %d cloned form structures in DB" % clonedListSize)

	#sys.exit(1)

	for clonedId in clonedList:
		print("Deleting clonedId = %d" % clonedId)

		delUrl = argServiceHost + "/dictionary-service/api/v1/structureById/" + str(clonedId)
		print("Deletion URL: %s" % delUrl)
		delResp = requests.delete(delUrl)
		delRespCode = delResp.status_code

		if delRespCode == 200:
			print("Succesfully deleted clonedId %d" % clonedId)
		else:
			print("Failed to delete cloned ID %d" % clonedId)
			sys.exit(1)

################################################
# def runBatch
################################################

def runBatch(argServiceHost):
	deleteClonedStructures(argServiceHost)
	#sys.exit(1)

	print("In runBatch for service host %s" % argServiceHost)

	allStructURL = argServiceHost + "/dictionary-service/api/v1/allSqlFormStructures"

	allStructResp = findEntity(allStructURL)

	structIdList = parseStructureList(allStructResp)

	for structId in structIdList:
		print("structId = %d" % structId)

		structXML = findStructureByID(argServiceHost, structId)

		structXmlFilePath = processStructureXML(structXML)

		postEntity(argServiceHost, "FormStructure", structXmlFilePath)

		#sys.exit(1)

################################################
# main
################################################

if __name__ == '__main__':

	argparser = argparse.ArgumentParser()

	argparser.add_argument('-c', '--config', help='Configuration')
	argparser.add_argument('-n', '--name', help='Base form structure name')
	argparser.add_argument('-t', '--type', help='Entity type [(S)tructure or (E)lement]')
	argparser.add_argument('-f', '--factor', help='Number of instances', type=int)
	argparser.add_argument('-p', '--props', help='Properties file path')
	argparser.add_argument('-d', '--dry', help='Dry run', action="store_true")
	argparser.add_argument('-l', '--list', help='List configurations', action="store_true")
	argparser.add_argument('-s', '--show', help='Show entity', action="store_true")
	argparser.add_argument('-b', '--batch', help='Batch run', action="store_true")

	args = argparser.parse_args();

	propFile = args.props
	baseName = args.name
	factor = args.factor
	type = args.type
	dry = args.dry
	confKey = args.config
	list = args.list
	show = args.show
	batch = args.batch

	if propFile is None:
		propFile = './/conf//config.ini'

	config = configparser.ConfigParser()
	config.read(propFile)

	if list:

		if confKey is not None:
			showConfigGroup(config, confKey)
		else:

			for confKeyItem in config.keys():

				showConfigGroup(config, confKeyItem)

		sys.exit(1)

	confSec = config[confKey]

	serviceHost = confSec['serviceHost']

	templFile = None
	serviceURL = None
	typeLong = None

	if batch:
		print("Hi, you're in batch mode")
		runBatch(serviceHost)
		sys.exit(0)

	if type == 'S':

		templFile = confSec['structureTempl']
		typeLong = 'FormStructure'

	elif type == 'E':

		templFile = confSec['elementTempl']
		typeLong = 'DataElement'

	else:

		print("ERROR: Entity type must be (S)tructure or (E)lement. Provided: %s" % type)
		sys.exit(1)

	serviceURL = serviceHost + '/dictionary-service/api/v1/' + typeLong + '/create'
	print("Execution URL: %s" % serviceURL)

	if dry:

		message = createMessage(templFile, baseName)
		print("\n%s" % message)
		sys.exit(0)

	if show:
		if type == 'S':

			showXML = findStructure(serviceHost, baseName)

		elif type == 'E':

			showXML = findElement(serviceHost, baseName)

		if showXML is not None:
			xml = xml.dom.minidom.parseString(showXML)  # or xml.dom.minidom.parseString(xml_string)
			pretty_xml_as_string = xml.toprettyxml()
			print("\n%s" % pretty_xml_as_string)
		else:
			print("\nRequested entity not found")

		sys.exit(0)

	if factor is None:
		postEntity(serviceHost, typeLong, templFile, baseName)

	else:

		for i in range(factor):
			entName = "%s_%d" % (baseName, i)

			postEntity(serviceHost, typeLong, templFile, entName)

