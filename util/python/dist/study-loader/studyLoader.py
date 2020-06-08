#!/usr/bin/python3

import requests
import argparse
import configparser
import sys
import xml.etree.ElementTree as ET
import xml.dom.minidom
import os

print("Welcome to Study Loader !")


################################################
# def findStudy
################################################

def findStudy(argServiceHost):

	serviceURL = argServiceHost + '/portal/ws/repository/repository/Study/createStudy'

#	result = findEntity(serviceURL)

	print("\nEntity GET URL: %s" % serviceURL)

	resp = requests.get(serviceURL)

	respCode = resp.status_code

	if respCode == 200:

		result = resp.content.decode("utf-8")

	else:
		result = None

	return result

################################################
# def readXML
################################################

def readXML(argTemplFile):

	with open(argTemplFile, 'r') as myfile:
		templ = myfile.read()

	result = templ

	return result

################################################
# def postStudy
################################################

def postStudy(argServiceHost, argTemplFile):

	data = readXML(argTemplFile)

	serviceURL = argServiceHost + '/portal/ws/repository/repository/Study/createStudy'

	headers = {'Content-Type': 'application/xml'}

	resp = requests.post(serviceURL, data=data, headers=headers)
	respCode = resp.status_code

	respXML = resp.content.decode("utf-8")

	if respCode != 200:

		print("Error creating study using template %s" % argTemplFile)
		print("Error Service URL: %s" % serviceURL)
		sys.exit(1)

	respRoot = ET.fromstring(respXML)


	idTag = respRoot.find("./id")
	prefixedIdTag = respRoot.find("./prefixedId")
	titleTag = respRoot.find("./title")

	studyID = idTag.text
	prefixedId = prefixedIdTag.text
	title = titleTag.text

	print("Created study: id = <%s>, prefixed = <%s> title = <%s>" % (studyID, prefixedId, title))

	return studyID

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
# def postStudies
################################################

def postStudies(argServiceHost):

	serviceURL = argServiceHost + '/portal/ws/repository/repository/Study/createStudies'

	resp = requests.post(serviceURL)
	respCode = resp.status_code

	studyListRespXML = resp.content.decode("utf-8")

	if respCode == 200:
		print("Succesfully duplicate all studies")

	else:
		print("Error duplicating studies")
		print("Error Service URL: %s" % serviceURL)

	respRoot = ET.fromstring(studyListRespXML)

	resultElems = respRoot.findall("study")

	studyCount = len(resultElems)

	print("studyCount = %d" % studyCount)

	result = []

	for studyItem in resultElems:
		idTag = studyItem.find("./id")
		prefixedIdTag = studyItem.find("./prefixedId")
		titleTag = studyItem.find("./title")

		studyID = idTag.text
		prefixedId = prefixedIdTag.text
		title = titleTag.text

		print("Created study: id = <%s>, prefixed = <%s> title = <%s>" % (studyID, prefixedId, title))

		result.append(studyID)

	return result

################################################
# def deleteDupStudies
################################################

def deleteDupStudies(argServiceHost):

	serviceURL = argServiceHost + '/portal/ws/repository/repository/Study/deleteDupStudies'

	resp = requests.delete(serviceURL)
	delRespCode = resp.status_code

	if delRespCode == 200:
		print("Succesfully deleted all duplicate studies")
	else:
		print("Failed to deleted all duplicate studies")


################################################
# main
################################################

if __name__ == '__main__':

	argparser = argparse.ArgumentParser()

	argparser.add_argument('-c', '--config', help='Configuration')
	argparser.add_argument('-p', '--props', help='Properties file path')
	argparser.add_argument('-l', '--list', help='List configurations', action="store_true")
	argparser.add_argument('-s', '--show', help='Show study', action="store_true")
	argparser.add_argument('-t', '--template', help='Load from template')

	args = argparser.parse_args();

	propFile = args.props
	confKey = args.config
	list = args.list
	show = args.show
	template = args.template

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

	serviceHost = confSec['serviceHost']

	if template: 
		templFile = confSec['studyTempl']

		if show:
			showXML = readXML(templFile)
			print("\nStudy xml: \n%s" % showXML)

		postStudy(serviceHost, templFile)
	
	else:
		deleteDupStudies(serviceHost)
		
		postStudies(serviceHost)
