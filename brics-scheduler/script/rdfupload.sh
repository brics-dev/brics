#!/bin/bash

#

# Virtuoso			Bulk Loader Script

#

# Author: Shangguan (http://www.cs.rpi.edu/~shangz, shangz@cs.rpi.edu)

# Version: 1.0

# Date: Dec 12, 2010

# Description: Bulk loader script for Virtuoso

# Usage: vload [rdf|ttl|nt|nq] [data_file] [graph_uri]



# Get input arguments

args=("$@")


input_format="ttl"
data_file_path="/opt/apache-tomcat/rdf-exports/"
export_logs_path="/opt/apache-tomcat/rdf-export-logs/"
data_file=$(ls -t /opt/apache-tomcat/rdf-exports/all* | head -1)
graph_uri="http://ninds.nih.gov:8080/allTriples.ttl"



	# Status message

	echo "Loading triples file $data_file into graph <$graph_uri>.."



	# Check if file exists

	if [ ! -e $data_file ]; then

		echo "Data file doesn't exist"

	else

		# Copy file to Virtuoso allowed directory

		virtuoso_allowed_dir="/tmp/virtuoso-tmp"

		if [ ! -d $virtuoso_allowed_dir ]; then

			mkdir $virtuoso_allowed_dir

			chmod a+w $virtuoso_allowed_dir

		fi

		cp ${data_file} ${virtuoso_allowed_dir}

		file_name_local=${data_file##*/}

		file_name_full=$virtuoso_allowed_dir/$file_name_local

		

		# Log into Virtuoso isql env

		isql_cmd="/usr/local/virtuoso-opensource/bin/isql 1111 dba"

		isql_pwd=""

		

		# Call Virtuoso loading functions for different input formats

		load_func=""

		case "$input_format" in

			"rdf")

			load_func="DB.DBA.RDF_LOAD_RDFXML_MT(file_to_string_output('$file_name_full'), '', '$graph_uri');"

			# echo $load_func

			;;

			"ttl")

			load_func="DB.DBA.TTLP_MT(file_to_string_output('$file_name_full'),'','$graph_uri', 255);"

			# echo $load_func

			;;

			"nt")

			load_func="DB.DBA.TTLP_MT(file_to_string_output('$file_name_full'),'','$graph_uri', 255);"

			# echo $load_func

			;;

			"nq")

			load_func="DB.DBA.TTLP_MT(file_to_string_output('$file_name_full'),'','$graph_uri', 512);"

			# echo $load_func

			;;

			*)

			echo "Input format unacceptable"

			echo

			echo "		Acceptable input formats"

			echo "		1) rdf -- RDF/XML"

			echo "		2) ttl -- Turtle/N3"

			echo "		3) nt -- n-triple"

			echo "		4) nq -- n-quad"

			exit 0

			;;

		esac

		
		${isql_cmd} ${isql_pwd} << EOF &> /tmp/virtuoso-tmp/vload.log
			SPARQL CLEAR GRAPH '$graph_uri';


			$load_func

			checkpoint;

			exit;

EOF

		
		# Remove temp file

		rm $file_name_full

	
		# Clear Administrative Cache

		rm clearQTcache
		wget -q --no-check-certificate --post-data="irrelevant data" https://pdbp-demo.cit.nih.gov/query/clearQTcache

		# Status message

		echo "Loading finished! Check /tmp/virtuoso-tmp/vload.log for details."	
		cp /tmp/virtuoso-tmp/vload.log $export_logs_path`basename ${data_file}`.vload.log

	fi


##Delete all rdf export files older than 2 weeks
find $data_file_path/*Triples* -daystart -mtime +14 -type f -exec rm -f '{}' \;

/home/tomcat/ibis-upload.sh
