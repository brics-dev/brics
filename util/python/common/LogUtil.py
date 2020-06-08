#!/bin/python3



################################################
# LogEntry
################################################

class LogEntry(object):

	def __init__(self, argKey, argVal):

		self.key = argKey
		self.val = argVal

	def __repr__(self):
		keyLen = len(self.key)

		if keyLen < 29:
			spaceSize = 29 - keyLen
		else:
			spaceSize = 1

		spaces = "." * spaceSize
		result = "%s:%s%s" % (self.key, spaces, str(self.val))
		return result

################################################
# SimpleLogEntry
################################################

class SimpleLogEntry(object):

	def __init__(self, argLine):

		self.line = argLine

	def __repr__(self):
		return self.line

################################################
# LogDump
################################################

class LogDump(object):

	def __init__(self, argUseDivider=True):
		self.entries = []
		self.useDivider = argUseDivider

	def addEntry(self, argKey, argVal):
		entry = LogEntry(argKey, argVal)
		self.entries.append(entry)

	def addSimpleEntry(self, argLine):
		entry = SimpleLogEntry(argLine)
		self.entries.append(entry)

################################################
# Logger
################################################

class Logger(object):

	def __init__(self, argLogFile):
		self.logFile = argLogFile
		self.divider = "-" * 81

	################################################
	# dumpLog
	################################################

	def dumpLog(self, argLogDump, argInit=False):

		openMode = 'w' if argInit else 'a'

		useDivider = argLogDump.useDivider

		rptFile = open(self.logFile, openMode, encoding="utf-8")

		if useDivider:
			print(self.divider, file=rptFile)
			print(self.divider)

		for logEntry in argLogDump.entries:
			print(logEntry, file=rptFile)
			print(logEntry)

		rptFile.close()

	################################################
	# dumpLine
	################################################

	def dumpLine(self, argLine, argInit=False):

		openMode = 'w' if argInit else 'a'

		rptFile = open(self.logFile, openMode)

		print(argLine, file=rptFile)
		print(argLine)

		rptFile.close()

	################################################
	# dumpError
	################################################

	def dumpError(self, argMsg, argInit=False):

		openMode = 'w' if argInit else 'a'

		#rptFilePath = argJobConfig.logFile
		rptFile = open(self.logFile, openMode)

		argLine = "ERROR: %s" % argMsg
		print(argLine, file=rptFile)
		print(argLine)

		rptFile.close()

	################################################
	# printDivider
	################################################

	def printDivider(self, argInit=False):

		self.dumpLine(self.divider, argInit)