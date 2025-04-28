# io
# -*- coding: utf-8 -*-

""" The io module is run during the RPi boot sequence.
    (using /etc/rc.local) - the 'nohup' and '&' allow
    the io module to be run independently and
    unattended to in the background.
"""

import os
import time
import shutil
import logging
import subprocess
import requests
from threading import Thread
from time import sleep
from datetime import datetime

import RPi.GPIO as GPIO

src_dir = os.path.dirname(os.path.abspath("__file__"))
working_dir = os.path.abspath(os.path.join(src_dir, os.pardir))

#A resistor of 100 Ohm is necessary for this button, to prevent damaging the RPi in case of incorrect usage, and connect it to the 3.3V out of the RPi
buttonPin = 21

###
#RGB LEDs should be common cathode
#Use the following link to know which resistors to use: https://ledcalculator.net/#p=3.3&v=2&c=16&n=3&o=w
#The voltage drop is defined according to the specifications of the LED, which can be found at the manufacturers website
#Each LED should consumo at most 16mA
#With our corrent LEDs, the resistores required are:
#Red LED: 82 Ohm (Forward current 2V)
#Green LED: 1 Ohm (Forward current 3.3V)
#Blue LED: 1 Ohm (Forward current 3.3V)
###
ledPin = [16, 20, 26] #Red, blue, green

running_java = False

GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
GPIO.setup(ledPin, GPIO.OUT, initial=GPIO.LOW)
GPIO.setup(buttonPin, GPIO.IN, pull_up_down=GPIO.PUD_DOWN)

current_time = datetime.now().strftime("%d.%m.%Y %H:%M:%S")
logging.basicConfig(
    filename='/home/pi/Desktop/sm_rpi_sensors/log/RPi_py '+current_time+'.log',
    level=logging.DEBUG,
    format='[%(levelname)-8s] (%(asctime)s): %(message)s',
    datefmt='%d.%m.%Y %H.%M.%S')

#Uncomment this for testing in console
# console = logging.StreamHandler()
# console.setLevel(logging.INFO)
# logging.getLogger('').addHandler(console)

# copy USB contents to working directory
target_folder = "/home/pi/Desktop"
excluded = []
file_name = ""

def led_ready(): #green
    GPIO.output(ledPin[2], GPIO.HIGH)  # green
    GPIO.output(ledPin[0], GPIO.LOW)   # red
    GPIO.output(ledPin[1], GPIO.LOW)   # blue


def led_busy(): #yellow
    GPIO.output(ledPin[2], GPIO.HIGH)   # green
    GPIO.output(ledPin[0], GPIO.HIGH)  # red
    GPIO.output(ledPin[1], GPIO.LOW)   # blue


def led_running(): #blue
    GPIO.output(ledPin[2], GPIO.LOW)   # green
    GPIO.output(ledPin[0], GPIO.LOW)   # red
    GPIO.output(ledPin[1], GPIO.HIGH)  # blue


def led_error(): #red
    GPIO.output(ledPin[2], GPIO.LOW)   # green
    GPIO.output(ledPin[0], GPIO.HIGH)  # red
    GPIO.output(ledPin[1], GPIO.LOW)   # blue


def led_waiting(): #cyan
    GPIO.output(ledPin[2], GPIO.HIGH)   # green
    GPIO.output(ledPin[0], GPIO.LOW)  # red
    GPIO.output(ledPin[1], GPIO.HIGH)   # blue


def led_off(): #purple
    GPIO.output(ledPin[2], GPIO.LOW)   # green
    GPIO.output(ledPin[0], GPIO.HIGH)  # red
    GPIO.output(ledPin[1], GPIO.HIGH)   # blue


def get_mounted_drives():
    try:
        logging.info("Getting mounted drives")
        output = subprocess.check_output(["/bin/bash", "-c", "lsblk"]).decode("utf-8").replace("├─", "").replace("└─", "").split("\n")
        logging.info(output)
        return [(item.split()[0],
                 item[item.find("/"):]) for item in output if "/" in item]
    except Exception as e: 
        logging.error(e)
        return []


def get_connected_i2c():
    try:
        logging.info("Getting connected sensors addresses through I2C interface")
        output = subprocess.check_output(["/bin/bash", "-c", "i2cdetect -y 1"]).decode("utf-8") #Get connected I2C devices.
        logging.info(output)
        output = [line[4:].replace('--','') for line in output.split("\n")[1:]] #Removes column headers and row address ID
        item = []
        for line in output:
            item = item + [port for port in line.split(' ') if port != '']
        logging.info("Sensors addresses are: " + str(item))
        return item
    except Exception as e: 
        logging.error(e)
        return []


def identify(disk):
    try:
        command = "find /dev/disk -ls | grep /" + disk
        output = subprocess.check_output(["/bin/bash", "-c", command]).decode("utf-8")
        if "usb" in output:
            return True
        else:
            return False
    except Exception as e: 
        logging.error(e)
        return False


def copy_usb():
    done = []
    new_paths = [dev for dev in get_mounted_drives() if not dev in done and not dev[1] == "/"]
    valid = [dev for dev in new_paths if (identify(dev[0]), dev[1].split("/")[-1] in excluded) == (True, False)]

    #Validate that there is at least one USB memory connected
    if len(valid) > 0:
        try:
            shutil.rmtree('/home/pi/Desktop/working_directory',  ignore_errors=True)
        except Exception as e:
            logging.error(e)
            pass

        for item in valid:
            folder = item[1] + "/working_directory"

            #If the working_directory exists in the USB, copy, if not ommit
            if(os.path.isdir(folder)):
                target = target_folder + "/" + folder.split("/")[-1]

                try:
                    shutil.copytree(folder, target)
                except Exception as e:
                    logging.error(e)
                time.sleep(2)


def run_java_files(communication):
    logging.error("compiling *.java files ...")      
    running_java = True
    try:
        #std_out = subprocess.check_output(['bash', '/home/pi/Desktop/shm_rpi_v2/interface/rsc/compile.sh'])
        #logging.info("*.java files compiled successfully !!")
        ports = get_connected_i2c()
        led_running()
        logging.info("Java code running... check server to specify accelerometer data acquisition parameters")
        if len(ports) > 0:
            std_out = subprocess.check_output(['bash', '/home/pi/Desktop/sm_rpi_sensors/interface/rsc/run.sh', '-'.join(ports), communication])
        else:
            std_out = subprocess.check_output(['bash', '/home/pi/Desktop/sm_rpi_sensors/interface/rsc/run.sh'])
        #print(" ... check server to specify accelerometer data acquisition parameters ")  # todo: echo this message
    except Exception as e:
        logging.error(e)
    finally:
        led_ready()
    running_java = False


def handle(pin):
    global start
    global end
    elapsed = 0
    logging.info("Handle called")
    #print ("Test line: here I am after defining elapsed 0")

    if GPIO.input(buttonPin) == 1:
        start = time.time()
        logging.info ("Test line: inside GPIO == 1 - Button pressed")
        led_waiting()
        while  GPIO.input(buttonPin) == 1:
            sleep(0.1)
            #print ("Test line: inside while loop")
        
        if GPIO.input(buttonPin) == 0:
            led_ready()
            end = time.time()
            elapsed = end - start
            logging.info ("Test line: inside GPIO == 0 - Button released - {} elapsed".format(elapsed))
            #print(elapsed)        

            run_mode = ""

            if elapsed <= 1:
                logging.info ("Test line: Minus than 1 - Do nothing ")
            elif elapsed <= 5:
                logging.info ("Test line: Minus than 5 - Local communication")
                sleep(1)
                if running_java:
                    logging.info ("Java code is already running.")
                else:
                    led_running()
                    time.sleep(0.2)
                    led_off()
                    time.sleep(0.2)
                    led_running()
                    time.sleep(0.2)
                    led_off()
                    time.sleep(0.2)
                    led_busy()
                    run_mode = "local"
                    copy_usb()
                    logging.info ("Test line: Copy USB")
                    time.sleep(5)  # make sure files finish copying     
                    thread = Thread(target = run_java_files, args = (run_mode,))
                    thread.start()
            elif elapsed <= 10:
                logging.info ("Test line: Minus than 10 - MQTT communication")
                sleep(1)
                if running_java:
                    logging.info ("Java code is already running.")
                else:
                    led_running()
                    time.sleep(0.2)
                    led_off()
                    time.sleep(0.2)
                    led_running()
                    time.sleep(0.2)
                    led_off()
                    time.sleep(0.2)
                    led_running()
                    time.sleep(0.2)
                    led_off()
                    time.sleep(0.2)
                    led_busy()

                    copy_usb()
                    logging.info ("Test line: Copy USB")
                    time.sleep(5)  # make sure files finish copying   

                    if check_internet():
                        logging.info('Internet OK: Starting process')
                        run_mode = "mqtt"   
                        thread = Thread(target = run_java_files, args = (run_mode,))
                        thread.start()
                    else:
                        logging.info('Internet connection lost')
            elif (elapsed > 10 and elapsed <= 20):
                logging.info ("Test line: More than 10 seconds and less than 15 ")
                led_off()
                time.sleep(2)
                os.system("sudo shutdown -h now")
                
            else:
                logging.info ("More than 15 secs")
                #This is an error state, since the button remains pushed
                #for some unkown reason
                led_error()

# def check_internet():
#     url = "www.google.com"
#     timeout = 5
#     try:
#         request = requests.get(url, timeout=timeout)
#         ret = True
#     except (requests.ConnectionError, requests.Timeout) as exception:
#         ret = False
#     return ret

def check_internet():
    #url = "192.168.9.84"
    f = open('/home/pi/Desktop/working_directory/host.config', "r")
    url = f.readline()

    response = os.system("ping -c 1 " + url)
    timeout = 5
    try:
        if response == 0:
          ret = True
        else:
          ret = False
    except (requests.ConnectionError, requests.Timeout) as exception:
        ret = False
    return ret
 
def main():
    logging.info('Process started')
    
    try:
        led_ready()
        GPIO.add_event_detect(buttonPin, GPIO.RISING, callback=handle, bouncetime = 5000)  # once input is detected, use interrupt to run handle

        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        logging.info("Process interrupted")
    except Exception as e:
        logging.error(e)
    finally:
        GPIO.cleanup()
        logging.info("Process ended and cleaned")

if __name__ == "__main__":
    main()
