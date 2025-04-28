package idac.sm_rpi_based_sensors_v2.SensorNode.sensors;

public class PowerOf2Extension {

	int nextPow2Value(int value){
		int highestOneBit = Integer.highestOneBit(value);
		if (value == highestOneBit) {
			return value;
		}		
		value = highestOneBit << 1;
		return value;
	}

}
