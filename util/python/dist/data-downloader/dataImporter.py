#!/usr/bin/python3

import requests
import argparse
import configparser
import sys
import xml.etree.ElementTree as ET
import xml.dom.minidom
import re

print("Welcome to Data Importer !")

CLONED_FORM_PATTERN = '\_clone\_'

################################################
# def importDataByForm
################################################

def importDataByForm(argPortalServHost, argFormShortName):

	serviceURL = argPortalServHost + '/import-RESTful/rest/DataSubmission/postDownloadedData/' + argFormShortName

	resp = requests.post(serviceURL)
	respCode = resp.status_code

	if respCode == 200:
		print("Succesfully import data for form <%s>" % argFormShortName)
	elif respCode == 204:
		print("No data to submit for form <%s>" % argFormShortName)
	else:
		print("Error import data for form name: " + argFormShortName)
		print("Error Service URL: %s" % serviceURL)

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
# def parseFormList
################################################

def parseFormList(argDictServHost):

	allFormURL = argDictServHost + "/portal/ws/ddt/dictionary/allStructuralFormStructures"

	argFormListResp = findEntity(allFormURL)

	respRoot = ET.fromstring(argFormListResp)

	resultElems = respRoot.findall("formStructureItem")

	formCount = len(resultElems)

	print("Total form count = %d" % formCount)

	shortNameList = []

	for formElem in resultElems:
		shortNameElem = formElem.find("name")
		shortName = shortNameElem.text

		shortNameList.append(shortName)

	return shortNameList

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
# def importAllData
################################################

def importAllData(argPortalServHost, argDictServHost):

	formShortNameList = parseFormList(argDictServHost)

	for shortName in formShortNameList :
		result = re.search(r'%s' % CLONED_FORM_PATTERN, shortName)
		
		if not result and shortName.startswith("LD_Demographics"):
			print("shortName is "+shortName)
			importDataByForm(argPortalServHost, shortName)


################################################
# main
################################################

if __name__ == '__main__':

	argparser = argparse.ArgumentParser()

	argparser.add_argument('-c', '--config', help='Configuration')
	argparser.add_argument('-p', '--props', help='Properties file path')
	argparser.add_argument('-l', '--list', help='List configurations', action="store_true")
	argparser.add_argument('-n', '--name', help='Form short name', action="store_true")
	argparser.add_argument('-a', '--all', help='All data')

	args = argparser.parse_args();

	propFile = args.props
	confKey = args.config
	list = args.list
	name = args.name
	all = args.all

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
	#print("confKey: %s" % confKey)
	confSec = config[confKey]

	portalServHost = confSec['portalServiceHost']
	dictServHost = confSec['dictServiceHost']

	if name: 
		importDataByForm(portalServHost, name)

	if all:
		importAllData(portalServHost, dictServHost)
