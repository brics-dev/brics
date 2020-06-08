#!/usr/bin/python3

import hashlib
import os
import shutil
import time

from subprocess import Popen, PIPE

################################################
# createFileWithContent
################################################

def createFileWithContent(argFilePath, argContent):

    file = open(argFilePath, 'w', encoding='UTF-8')

    file.write(argContent)
    file.close()

################################################
# getTimeSignature
################################################

def getTimeSignature(argNativeTime):

    result = time.strftime("%Y%m%d%H%M%S", argNativeTime)

    return result

################################################
# getTimeDisplay
################################################

def getTimeDisplay(argNativeTime):

    result = time.strftime("%d-%b-%Y %H:%M:%S", argNativeTime)

    return result

################################################
# getDirectoryFileList
################################################

def getDirectoryFileList(argDir, argFileExt=None, argMaxTravelLevel=-1):
    result = []
    argDirOffset = len(argDir) + 1
    currLevel = 1

    for dirPath, subDirs, fileNames in os.walk(argDir):
        for workFile in fileNames:
            if (argFileExt is None) or (workFile.lower().endswith('.' + argFileExt.lower())):
                result.append({'relDirPath': dirPath[argDirOffset:], 'fileName': workFile})
        
        # Check if the max level caluclations need to be performed.
        if argMaxTravelLevel > 0:
            # Check if the max traversal level has been meet.
            if currLevel < argMaxTravelLevel:
                currLevel += 1
            else:
                break

    return result


################################################
# createArchive
################################################

def createArchive(argDataDir):

    archSubdirName = getTimeSignature(time.localtime())

    result = argDataDir + '//archive//' + archSubdirName

    if not os.path.exists(result):
        os.makedirs(result)

    return result

################################################
# archiveBatchFiles
################################################

def archiveBatchFiles(argDir):

    archDirPath = createArchive(argDir)

    for mirrorDirName, workDirSubdirs, workDirFiles in os.walk(argDir):

        for workDirFile in workDirFiles:
            print("ARCHIVING %s" % workDirFile)
            workDirFilePath = argDir + '//' + workDirFile
            shutil.move(workDirFilePath, archDirPath + '//' + workDirFile)

        break

################################################
# purgeDirectory
################################################

def purgeDirectory(argDirPath):

    for file in os.listdir(argDirPath):
        file_path = os.path.join(argDirPath, file)
        try:
            if os.path.isfile(file_path):
                os.unlink(file_path)
            elif os.path.isdir(file_path):
                shutil.rmtree(file_path)
        except Exception as e:
            print(e)

################################################
# getFileShortName
################################################

def getFileShortName(argFileNameFull):

    result = os.path.basename(argFileNameFull)
    return result

################################################
# getFileExtension
################################################

def getFileExtension(argFilePath):

    shortName = getFileShortName(argFilePath)
    nameElements = shortName.split('.')
    result = nameElements.pop()

    return result

################################################
# getFileChecksum
################################################

def getFileChecksum(argFilePath):

    handle = open(argFilePath, 'rb')
    cont = handle.read()
    md5 = hashlib.md5(cont)
    result = md5.hexdigest()

    return result

################################################
# makeDirWithParents
################################################

def makeDirWithParents(argDirPath):

    try:
        os.makedirs(argDirPath)
        return True
    except:
        return False

################################################
# denullifyString
################################################

def denullifyString(argStr):

    if argStr is None:
        return ''
    return str(argStr)

################################################
# getFileContentString
################################################

def getFileContentString(argFilePath):

    with open(argFilePath, 'r') as fileHandle:
        result = fileHandle.read()

    return result

################################################
# getResourceFilePath
################################################

def getResourceFilePath(argFileName):

    pythonpath = os.environ.get("PYTHONPATH")
    if pythonpath:
        for d in pythonpath.split(os.pathsep):
            filepath = os.path.join(d, argFileName)
            if os.path.isfile(filepath):
                return filepath
    return None

################################################
# getPathDelimiter
################################################

def getPathDelimiter(argPath):

    forSlashPos = argPath.find("/")
    backSlashPos = argPath.find("\\")

    result = None

    if forSlashPos != -1 and backSlashPos == -1:
        result = '/'
    elif forSlashPos == -1 and backSlashPos != -1:
        result = "\\"

    return result

################################################
# def replaceFileVariable
################################################

def replaceFileVariable(argFilePath, argVar, argReplVal):


    try:
        with open(argFilePath, 'r', encoding='UTF-8') as myfile:
            input = myfile.read()

            result = input.replace(argVar, argReplVal)

            createFileWithContent(argFilePath, result)

            result = True
    except:
        result = False

    return result

################################################
# runSystemCommand
################################################

def runSystemCommand(argConmmand):

    subproc = Popen(argConmmand, shell=True, stdin=PIPE, stdout=PIPE, stderr=PIPE)
    stdOutByt, stdErrByt = subproc.communicate()
    stdOut = stdOutByt.decode()
    stdErr = stdErrByt.decode()

    return stdOut, stdErr
