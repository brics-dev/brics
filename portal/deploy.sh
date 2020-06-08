#!/bin/bash

############# VARS
env=$1
project=$2
iteration_number=$3
build_number=$4
if [ "$5" == "" ]
then
	branch_name="trunk"
else
	branch_name="branches/$5"
fi
tag_name="brics-$iteration_number.$build_number"

. ~/config.properties # load configuration properties

### DEFINED METHODS

function runAndTail
{
        script_name=$5

        # run process in background while storing output into file
        ./subscripts/$script_name.sh $1 $2 $3 $4 & >> "$folder_name/$script_name.txt"

        pid=$!

        # tail the output file until the process completes
        tail -f --pid=$pid "$folder_name/$script_name.txt"

        # wait until the 'last' background process completes
        wait $pid

        # check the return status of the process that just completed
        if [ $? != 0 ]
        then
                cleanup 1
        fi

}

function perlAndTail
{
        script_name=$5

        # run process in background while storing output into file
        perl subscripts/$script_name.pl $1 $2 $3 $4  & >> "$folder_name/$script_name.txt"

        pid=$!

        # tail the output file until the process completes
        tail -f --pid=$pid "$folder_name/$script_name.txt"

        # wait until the 'last' background process completes
        wait $pid

        # check the return status of the process that just completed
        if [ $? != 0 ]
        then
                cleanup 1
        fi

}

function createProjectArray
{
	local raw_string=$1
	
	local arr=$(echo $raw_string | tr "+" "\n")
	echo "$arr"
}

function cleanup
{
	msg=$1
	exit $msg
}

echo ""
echo ""
echo ""
echo ""
echo ""
echo ""
echo ""
echo ""
echo "********************************************************************************"
echo "********************************************************************************"
echo "********************************************************************************"
echo "********************************************************************************"
echo "**                                                                            **"
echo "**                                                                            **"
echo "**                                                                            **"
echo "** DEPLOY SCRIPT                                                              **"
echo "**                                                                            **"
echo "**                                                                            **"
echo "**                                                                            **"
echo "********************************************************************************"
echo "********************************************************************************"
echo "********************************************************************************"
echo "********************************************************************************"

if [ "$1" == "help" ] || [ "$env" == "" ] || [ "$project" == "" ] || [ "$iteration_number" == "" ] || [ "$build_number" == "" ]
then
        echo "help"
		echo "usage: ./deploy.sh [env] [project] [iteration_number] [build_number]"
	cleanup 1
else
	if [ "$env" == "dev" ] || [ "$env" == "stage" ] || [ "$env" == "demo" ] || [ "$env" == "prod" ]
	then
		project_array=$(createProjectArray $project)
		for current_project in $project_array
		do
			# get the property for the array of servers
			echo "======= Load Server Locations From config.properies ======="
			servers_prop=\$${current_project}_${env}
			echo "Using property $servers_prop to get list of servers"
			servers_string="$(eval "echo $servers_prop")"	
			servers=$( echo $servers_string | tr ";" "\n")
			echo "List of servers..."
			
			for x in $servers
			do
				echo "> $x"
			done
			
			# create log folder
			time_stamp=$(date +$time_stamp_format)
			folder_name=~/logs/build/$time_stamp
	
			mkdir $folder_name || cleanup 1
	
			# stop tomcat
			#ssh tomcat@$server service tomcat stop


#####################
##################### GRDR - portal, guid, guidApp only
#####################
	
			# run deploy
			runAndTail $env $current_project $iteration_number $build_number "deployPortal"
	
			if [ "$current_project" != "dictionary" ]
			then
				# run deploy Applications
				perlAndTail $env $current_project $tag_name $branch_name "deployApplications"
	
				# run deploy Command Line
				perlAndTail $env $current_project $tag_name $branch_name "deployCmdLineApp"

				# run guid_deploy
				runAndTail $env $current_project $iteration_number $build_number "deployGuid"
	
				# run guid_deploy Client
				perlAndTail $env $current_project $tag_name $branch_name "deployGuidApp"
				
				if [ "$current_project" == "fitbir" ] || [ "$current_project" == "pd" ]
				then
					:
					# run querydeploy
					runAndTail $env $current_project $iteration_number $build_number "deployQueryApp"
					
					# run schedulerDeploy
					# runAndTail $env $current_project $iteration_number $build_number "deployScheduler"
				fi

				# run deploy JavaDoc
				# runAndTail $env $current_project $iteration_number $build_number "deployApiDocs"
			fi
		done
	cleanup 0
	fi
fi
