#! /bin/bash
#  Executes the Command Line Validation Tool for FITBIR

dir= 
env=env_variable

for((;;))
do
	if($1=="")
	then
		break
	fi
	if($1=="-d")
	then
		shift 1
		dir=$1
	fi
	shift 1
done

java -jar CmdLineTool.jar -r $env $dir