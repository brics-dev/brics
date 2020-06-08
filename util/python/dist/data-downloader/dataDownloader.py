#!/usr/bin/python3

import requests
import argparse
import configparser
import sys
import xml.etree.ElementTree as ET
import xml.dom.minidom


print("Welcome to Data Downloader !")

################################################
# def downloadDataByStudy
################################################

def downloadDataByStudy(argServiceHost, argStudyPrefixedId):
	
#	print("downloadDataByStudy() prefixedId = %s" % argStudyPrefixedId)

	serviceURL = argServiceHost + '/portal/ws/repository/repository/Download/downloadDataByStudyId/' + argStudyPrefixedId

	headers = {'Content-Type': 'application/xml'}

	resp = requests.get(serviceURL, headers=headers)
	respCode = resp.status_code

	respXML = resp.content.decode("utf-8")
#	print("downloadDataByStudy() respXML = %s" % respXML)
	if respCode != 200 and respCode != 204 :
		print("Error download data for study %s" % argStudyPrefixedId)
		print("Error Service URL: %s" % serviceURL)
		sys.exit(1)
	
	elif respCode == 204 :
		print("\nNo data downloaded for study = %s" %argStudyPrefixedId)

	downloadFileCount = 0

	if respCode == 200 :
		respRoot = ET.fromstring(respXML)

		resultElems = respRoot.findall("./Download_file/list/userFile")

		downloadFileCount = len(resultElems)

		print("\nstudy with prefixedId %s has downloadFileCount = %d" % (argStudyPrefixedId, downloadFileCount))

		for downloadFileElem in resultElems:
			idTag = downloadFileElem.find("id")
			idVal = idTag.text
			nameTag = downloadFileElem.find("name")
			fileName = nameTag.text

			print("Download data, download file id = <%s>, name = <%s>" % (idVal, fileName))

	return downloadFileCount

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
# def parseStudyList
################################################

def parseStudyList(argServiceHost):

	allStudyURL = argServiceHost + "/portal/ws/repository/repository/Study/getStudies"

	argStudyListResp = findEntity(allStudyURL)
#	print("parseStudyList() argStudyListResp = %s" % argStudyListResp)
	respRoot = ET.fromstring(argStudyListResp)
	
	resultElems = respRoot.findall("study")

	studyCount = len(resultElems)

	print("studyCount = %d" % studyCount)

	studyPrefixedIdList = []

	for studyElem in resultElems:
		prefixedIdElem = studyElem.find("prefixedId")
		prefixedId = prefixedIdElem.text
		
		if not prefixedId.endswith("_DD"): 
			studyPrefixedIdList.append(prefixedId)
#			print("prefixed id :"+prefixedId)

	return studyPrefixedIdList

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
# def downloadDataFromAllStudy
################################################

def downloadDataFromAllStudy(argServiceHost):

	studyPrefixedIdList = parseStudyList(argServiceHost)

	downloadFileCount = 0

	for prefixedId in studyPrefixedIdList:

		downloadFileCount += downloadDataByStudy(argServiceHost, prefixedId)

	print("\nTotal downloadFileCount = %d" % downloadFileCount)

	return downloadFileCount

################################################
# main
################################################

if __name__ == '__main__':

	argparser = argparse.ArgumentParser()

	argparser.add_argument('-c', '--config', help='Configuration')
	argparser.add_argument('-p', '--props', help='Properties file path')
	argparser.add_argument('-l', '--list', help='List configurations', action="store_true")
	argparser.add_argument('-s', '--study', help='Study prefixed id', action="store_true")
	argparser.add_argument('-a', '--all', help='All studies')

	args = argparser.parse_args();

	propFile = args.props
	confKey = args.config
	list = args.list
	study = args.study
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

	serviceHost = confSec['portalServiceHost']

	if study: 
		fileCount = downloadDataByStudy(serviceHost, study)

		if fileCount == 0 :
			print("No data downloaded")

	if all:
		fileCount = downloadDataFromAllStudy(serviceHost)

		if fileCount == 0 :
			print("No data downloaded")
