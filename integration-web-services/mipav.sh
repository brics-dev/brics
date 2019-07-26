#!/bin/bash

PATH_TO_MIPAV=/opt/mipav
PATH_TO_SCRIPT=/opt/apache-tomcat/brics/mipav.script

echo "Our Script: $0 $1 $2 $3" >> /tmp/tom.txt

OUTPUT_PREFIX="TRIPLANAR"

ORIGINAL_FILE=$1
ORIGINAL_FILE_PATH=`dirname $1`
ORIGINAL_FILE_NAME=`basename $1`

NEW_FILE=$2
NEW_FILE_PATH=`dirname $2`
NEW_FILE_NAME=`basename $2`

echo xvfb-run $PATH_TO_MIPAV/mipav -hide -s $PATH_TO_SCRIPT -m $NEW_FILE_PATH/$ORIGINAL_FILE_NAME -d output_prefix $OUTPUT_PREFIX -d output_dir $NEW_FILE_PATH >> /tmp/tom.txt #/$NEW_FILE_NAME
xvfb-run $PATH_TO_MIPAV/mipav -hide -s $PATH_TO_SCRIPT -m $NEW_FILE_PATH/$ORIGINAL_FILE_NAME -d output_prefix $OUTPUT_PREFIX -d output_dir $NEW_FILE_PATH #/$NEW_FILE_NAME

echo "Zip command: /usr/bin/zip  $NEW_FILE $NEW_FILE_PATH/*.jpg >> /tmp/tom/txt 2>&1 /usr/bin/zip $NEW_FILE $NEW_FILE_PATH/*.jpg" >> /tmp/tom.txt
cd $NEW_FILE_PATH
/usr/bin/zip $NEW_FILE *.jpg >> /tmp/tom.txt