#!/bin/bash

now=$(date +'%Y.%m.%d.%H:%M:%S')
exec 2> /home/pi/Desktop/sm_rpi_sensors/log/RPi_run_$now.log  # send stderr to RPi.log
exec 1>&2			  # send stdout to RPi.log

if [ "$1" != "" ]; then
    echo "Parameter 1 equals $1"
fi

if [ "$2" != "" ]; then
    echo "Parameter 1 equals $2"
fi

cd /home/pi/Desktop/working_directory #/SensorNode
sudo java -jar SensorNode-0.0.1-SNAPSHOT.jar $1 $2 #target/SensorNode-0.0.1-SNAPSHOT.jar

# TODO:
#1. check if the folder working directory exists
#2. check if the .java file / files exist (nodes & SensorNodes)
#3. once these 3 are done ... only then run the code
#4. indication of the the failed state at any point
