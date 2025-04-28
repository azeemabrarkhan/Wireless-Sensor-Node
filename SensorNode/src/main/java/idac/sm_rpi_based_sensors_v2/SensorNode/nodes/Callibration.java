package idac.sm_rpi_based_sensors_v2.SensorNode.nodes;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import idac.sm_rpi_based_sensors_v2.SensorNode.sensors.ADXL345;
import idac.sm_rpi_based_sensors_v2.pojos.Node;
import idac.sm_rpi_based_sensors_v2.pojos.Sensor;

public class Callibration {

	public static void main(String[] args) {
    	try {
    		ADXL345 adxl345 = new ADXL345(I2CBus.BUS_1, 0x53);
        	adxl345.setNode(new Node());
        	adxl345.setSamplingRate(0);
        	adxl345.setSecondsMeasuring(0);
        	adxl345.setNumberOfPeaks(0);
        	adxl345.setMeasureEveryNSeconds(1);
        	adxl345.setRepetitions(0);
        	adxl345.setSensor(new Sensor());
			adxl345.setup();
			
			adxl345.runCalibration();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedBusNumberException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
