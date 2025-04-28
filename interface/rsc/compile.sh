#!/bin/bash

now=$(date +'%Y.%m.%d.%H:%M:%S')
exec 2> /home/pi/Desktop/sm_rpi_sensors/log/RPi_compile_$now.log  # send stderr to RPi.log
exec 1>&2			  # send stdout to RPi.log

sudo chmod -R 777 /home/pi/Desktop/working_directory
cd /home/pi/Desktop/working_directory
bash buildclient.sh

# TODO:
#1. check if the folder working directory exists
#2. check if the .java file / files exist (nodes & SensorNodes)
