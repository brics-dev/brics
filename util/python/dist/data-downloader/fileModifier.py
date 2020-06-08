#!/usr/bin/python3

import requests
import argparse
import configparser
import sys
import xml.etree.ElementTree as ET
import xml.dom.minidom
import re

print("Welcome to File Modifier !")

CLONED_FORM_PATTERN = '\_clone\_'

################################################
# def modifyFileByForm
################################################

def modifyFileByForm(argPortalServHost, argFormShortName):

	serviceURL = argPortalServHost + '/portal/ws/repository/repository/Download/modifyFileByForm/' + argFormShortName
	
	#print("\nModify file POST URL: %s" % serviceURL)

	resp = requests.post(serviceURL)
	respCode = resp.status_code

	respTxt = resp.content.decode("utf-8")

	if respCode != 200 and respCode != 204 :
		print("Error response %s" % respTxt)
		print("Error modify downloaded files for form name %s" % argFormShortName)
		print("Error Service URL: %s" % serviceURL)
		sys.exit(1)

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
# def downloadDataFromAllStudy
################################################

def modifyAllFiles(argPortalServHost, argDictServHost):

	formShortNameList = parseFormList(argDictServHost)

	#print("Original form count = %d" % len(formShortNameList))

	for shortName in formShortNameList:
		result = re.search(r'%s' % CLONED_FORM_PATTERN, shortName)
		
		if not result:
			modifyFileByForm(argPortalServHost, shortName)

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
# main
################################################

if __name__ == '__main__':

	argparser = argparse.ArgumentParser()

	argparser.add_argument('-c', '--config', help='Configuration')
	argparser.add_argument('-p', '--props', help='Properties file path')
	argparser.add_argument('-l', '--list', help='List configurations', action="store_true")
	argparser.add_argument('-n', '--name', help='Form short name', action="store_true")
	argparser.add_argument('-a', '--all', help='All files')

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
		modifyFileByForm(portalServHost, name)

	if all:	
		modifyAllFiles(portalServHost, dictServHost)
