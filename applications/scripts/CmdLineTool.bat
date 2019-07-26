@ECHO off

SET dir= 
SET env=env_variable

:Loop
IF "%1"=="" GOTO Continue
	IF "%1"=="-d" GOTO SetDir
	
	
SHIFT
GOTO Loop
:Continue
java -jar CmdLineTool.jar -r %env% %dir%
PAUSE
GOTO End

:SetDir
	SHIFT
	SET dir=-d %1
	SHIFT
GOTO Loop

:End