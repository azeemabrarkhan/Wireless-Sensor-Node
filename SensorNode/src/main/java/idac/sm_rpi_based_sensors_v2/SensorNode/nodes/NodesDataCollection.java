package idac.sm_rpi_based_sensors_v2.SensorNode.nodes;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import idac.sm_rpi_based_sensors_v2.SensorNode.sensors.ADXL345;
import idac.sm_rpi_based_sensors_v2.SensorNode.sensors.BME280;
import idac.sm_rpi_based_sensors_v2.SensorNode.sensors.HCSR04;
import idac.sm_rpi_based_sensors_v2.SensorNode.sensors.Sensor;
import idac.sm_rpi_based_sensors_v2.helpers.Communication;
import idac.sm_rpi_based_sensors_v2.helpers.ParametersManager;
import idac.sm_rpi_based_sensors_v2.pojos.Node;

/* Class defined for sampling data from the accelerometers as fast as possible and then  
 * re-sampling the data according to a given sampling frequency
 * 
 * Remark: Here, the maximum amplitude of the accelerations should be defined and the maximum sampling from the nodes
 * see lines: 51 and 55!
 * 
 * Created by Stalin Ibanez 
 * Modified by Joaquin Peralta
 * last version: 2021-05-06
 */
public class NodesDataCollection {

	void performDataCollection(Communication communication, ParametersManager parameters, Node node) throws IOException, UnsupportedBusNumberException, Exception {         

		if (node == null || node.getSensorList().size() == 0) {
			throw new Exception("The node is invalid or has no sensors connected");
		} else {	        
	        // Configuration of sensors
	        // Load them based on the address Set received as parameter  
	        Map<idac.sm_rpi_based_sensors_v2.pojos.Sensor, Sensor> sensorMap = new HashMap<idac.sm_rpi_based_sensors_v2.pojos.Sensor, Sensor>();
	        for(idac.sm_rpi_based_sensors_v2.pojos.Sensor sensor : node.getSensorList()) {
	        	switch(sensor.getType()) {
	        		case "Accelerometer":
	        			if((boolean)parameters.getParameterMap().get("Accelerometer.Measure")) {
		        			System.out.println("ADXL345 sensor setup start.");
				        	ADXL345 adxl345 = new ADXL345(I2CBus.BUS_1, sensor.getAddress());
				        	adxl345.setNode(node);
				        	adxl345.setSamplingRate((int)(double)parameters.getParameterMap().get("Accelerometer.SamplingRate"));
				        	adxl345.setSecondsMeasuring((int)(double)parameters.getParameterMap().get("Accelerometer.SecondsMeasuring"));
				        	adxl345.setNumberOfPeaks((int)(double)parameters.getParameterMap().get("Accelerometer.NumberOfPeaks"));
				        	adxl345.setMeasureEveryNSeconds((int)(double)parameters.getParameterMap().get("Accelerometer.MeasureEveryNSeconds"));
				        	adxl345.setRepetitions((int)(double)parameters.getParameterMap().get("Accelerometer.Repetitions"));
				        	adxl345.setSensor(sensor);
				        	adxl345.setCommunication(communication);
				        	adxl345.setup();
				        	sensor.setRunning(true);
		        			System.out.println("ADXL345 sensor setup done.");
				        	sensorMap.put(sensor, adxl345);
	        			}
	        			else {
		        			System.out.println("ADXL345 sensor omitted.");
	        			}
			        	break;
	        		case "Environmental":
	        			if((boolean)parameters.getParameterMap().get("Environmental.Measure")) {
		        			System.out.println("BME280 sensor setup start.");
		        			BME280 bme280 = new BME280(I2CBus.BUS_1, sensor.getAddress());
		        			bme280.setNode(node);
		        			bme280.setMeasureEveryNSeconds((int)(double)parameters.getParameterMap().get("Environmental.MeasureEveryNSeconds"));
		        			bme280.setRepetitions((int)(double)parameters.getParameterMap().get("Environmental.Repetitions"));
		        			bme280.setSensor(sensor);
		        			bme280.setCommunication(communication);
		        			bme280.setup();	        
				        	sensor.setRunning(true);			
		        			System.out.println("BME280 sensor setup done.");	        			
		        			sensorMap.put(sensor, bme280);
	        			}
	        			else {
		        			System.out.println("BME280 sensor omitted.");
				        	sensor.setRunning(false);
	        			}
			        	break;
					case "Ultrasonic":
					if((boolean)parameters.getParameterMap().get("Ultrasonic.Measure")) {
						System.out.println("HCSR04 sensor setup start.");
						HCSR04 hcsr04 = new HCSR04(0, 1, 1000, 23529411);
						hcsr04.setNode(node);
						hcsr04.setMeasureEveryNSeconds((int)(double)parameters.getParameterMap().get("Ultrasonic.MeasureEveryNSeconds"));
						hcsr04.setRepetitions((int)(double)parameters.getParameterMap().get("Ultrasonic.Repetitions"));
						hcsr04.setSensor(sensor);
						hcsr04.setCommunication(communication);
						hcsr04.setup();
						sensor.setRunning(true);
						System.out.println("HCSR04 sensor setup done.");
						sensorMap.put(sensor, hcsr04);
					}
					else {
						System.out.println("HCSR04 sensor omitted.");
					}
					break;
	        	}
	        }

	        for(Sensor sensor : sensorMap.values()) {	
	        	Thread t = new Thread(sensor); 
	        	t.start(); 
	        }	        
		}
	}	
}
