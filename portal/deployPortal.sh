#!/bin/bash

################# VARS

env=$1
project=$2
iteration_number=$3
build_number=$4
tag_name="brics-$iteration_number.$build_number"

. ./config.properties # This should load all the properties from file

################# DEFINED METHODS
function createProjectArray
{
	local raw_string=$1
	
	local arr=$(echo $raw_string | tr "+" "\n")
	echo "$arr"
}

function cleanup
{
	msg=$1
	rm -rf $co_location
	exit $msg
}

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
echo "** DEPLOY WEBAPP SCRIPT                                                       **"
echo "**                                                                            **"
echo "**                                                                            **"
echo "**                                                                            **"
echo "********************************************************************************"
echo "********************************************************************************"
echo "********************************************************************************"
echo "********************************************************************************"

if [ "$1" == "help" ] || [ "$env" == "" ] || [ "$project" == "" ]
then
        echo "help"
		echo "usage: deploy [env] [project] "
	cleanup 1
else
		project_array=$(createProjectArray $project)
		for current_project in $project_array
		do
			# get the property for the array of servers
			server_prop=\$${current_project}_${env}
			echo "======================================="
			echo "Using property $server_prop to get list of servers"
			servers_string="$(eval "echo $server_prop")"
			servers=$(echo $servers_string | tr ";" "\n")
			echo "List of servers..."
		
			for x in $servers
			do
				echo "> $x"
			done
			
			echo "================================================="
			echo "Deploying Build to $env environment..."
		
		

			echo "================================================="
			echo "Checking out tags/$tag_name..."
			#CHECK OUT TAG
			#svn co "$svn_location/tbi_dev/source/tags/$tag_name/" $co_location || cleanup 1

			echo "================================================="
			echo "Making Build"
			##BUILD DEPLOYMENT
			mvn clean package  -P replace -Denv=$env -Dproj=$current_project || cleanup 1
			

			##REMOVE OLD WAR
			echo "================================================="
			echo "Removing Old War"
		
			for server in $servers
			do
				echo "] ssh tomcat@$server rm $webapp_location/$webapp.war"
				ssh tomcat@$server rm $webapp_location/$webapp.war
				
				#Remove Scheduler
				if [ "$project" != "dictionary" ]
				then
					echo "] ssh tomcat@$server rm $webapp_location/$schedulerWebapp.war"
					ssh tomcat@$server rm $webapp_location/$schedulerWebapp.war
				fi

							#Remove import-RESTful 
							if [ "$project" != "dictionary" ]
							then
									echo "] ssh tomcat@$server rm $webapp_location/$importRestfulWebapp.war"
									ssh tomcat@$server rm $webapp_location/$importRestfulWebapp.war
							fi

			done

			##COPY NEW WAR OVER
			echo "================================================="
			echo "Copy New War"
			for server in $servers 
			do
				echo "] scp $co_location/portal/target/$webapp.war tomcat@$server_location:$webapp_location/"
				scp $co_location/$webapp.war tomcat@$server:$webapp_location/ || cleanup 1
				
				#Copy _
				if [ "$project" == "DO_NOT_DEPLOY" ]
				then
					echo "] scp $co_location/brics-scheduler/target/$schedulerWebapp.war tomcat@$server_location:$webapp_location/"
					scp $co_location/brics-scheduler/target/$schedulerWebapp.war tomcat@$server:$webapp_location/ || cleanup 1
				fi

							#Copy Import-RESTful Service
							#if [ "$project" != "dictionary" ]
							#then
							#		echo "] scp $co_location/import-RESTful/target/$importRestfulWebapp.war tomcat@$server_location:$webapp_location/"
							#		scp $co_location/import-RESTful/target/$importRestfulWebapp.war tomcat@$server:$webapp_location/ || cleanup 1
							#fi

			done
		done
		##CLEAN UP AND EXIT
		cleanup 0

fi
