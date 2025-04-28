#! bin/bash

# config automates updating /etc/rc.local respectively for each RPi


echo "updating /etc/rc.local ..."

if [ -f /etc/rc.local ]; 
    then
	sudo rm -rf /etc/rc.local
fi
sudo cp /home/pi/Desktop/sm_rpi_sensors/interface/rsc/rc.local /etc/
sudo chmod 766 /etc/rc.local
echo "/etc/rc.local updated"
sudo reboot now

