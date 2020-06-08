#!/bin/bash

# Uncomment the line below to activate debug output in MIPAV.
#export LAX_DEBUG=true

LOG_FILE_DIR=/home/triplanar/triplanar_logs
LOG_FILE=$LOG_FILE_DIR/triplanar_log-`date +%F`.txt
PATH_TO_MIPAV=/opt/mipav
PATH_TO_SCRIPT=/opt/apache-tomcat/brics/mipav.script

# Create the log directory, if needed.
mkdir -p $LOG_FILE_DIR

echo "Running triplanar script on: `date`" >> $LOG_FILE
echo "Our Script: $0 $1 $2 $3" >> $LOG_FILE

OUTPUT_PREFIX="TRIPLANAR"

ORIGINAL_FILE=$1
ORIGINAL_FILE_PATH=`dirname $1`
ORIGINAL_FILE_NAME=`basename $1`

NEW_FILE=$2
NEW_FILE_PATH=`dirname $2`
NEW_FILE_NAME=`basename $2`

echo "mkdir -p $NEW_FILE_PATH" >> $LOG_FILE
mkdir -p $NEW_FILE_PATH >> $LOG_FILE 2>&1

echo xvfb-run -a $PATH_TO_MIPAV/mipav -hide -s $PATH_TO_SCRIPT -m /home/triplanar/$NEW_FILE_PATH/$ORIGINAL_FILE_NAME -d output_prefix $OUTPUT_PREFIX -d output_dir $NEW_FILE_PATH >> $LOG_FILE
xvfb-run -a $PATH_TO_MIPAV/mipav -hide -s $PATH_TO_SCRIPT -m /home/triplanar/$NEW_FILE_PATH/$ORIGINAL_FILE_NAME -d output_prefix $OUTPUT_PREFIX -d output_dir $NEW_FILE_PATH >> $LOG_FILE 2>&1

#echo "Zip command: /usr/bin/zip  $NEW_FILE $NEW_FILE_PATH/*.jpg >> /tmp/tom/txt 2>&1 /usr/bin/zip $NEW_FILE $NEW_FILE_PATH/*.jpg" >> /tmp/tom.txt
#cd $NEW_FILE_PATH
#/usr/bin/zip $NEW_FILE *.jpg >> /tmp/tom.txt

# Delete any old log files.
find $LOG_FILE_DIR -daystart -mtime +180 -type f -exec rm -f '{}' \;
