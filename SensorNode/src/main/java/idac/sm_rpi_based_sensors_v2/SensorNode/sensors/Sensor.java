package idac.sm_rpi_based_sensors_v2.SensorNode.sensors;

import com.pi4j.io.i2c.I2CDevice;

import idac.sm_rpi_based_sensors_v2.helpers.Communication;
import idac.sm_rpi_based_sensors_v2.pojos.Node;

public class Sensor implements Runnable {
	
	protected Node node;
	protected Communication communication;
	protected int repetitions;
	protected idac.sm_rpi_based_sensors_v2.pojos.Sensor sensor;

	/**
	 * I2C bus number to use to access device.
	 */
	protected final int i2cBus;

	/**
	 * Address of I2C device.
	 */
	protected final int devAddr;

	/**
	 * Abstraction of I2C device.
	 */
	protected I2CDevice device;
	
	/**
	 * Read/write buffer.
	 */
    protected byte[] BUFFER;
	
	public Sensor() {
		this.i2cBus = 0;
		this.devAddr = 0;
	}

	/**
	 * Specific address constructor.
	 * 
	 * @param address I2C address
	 */
	public Sensor(int bus, int address) {
		this.i2cBus = bus;
		this.devAddr = address;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}
	
	public idac.sm_rpi_based_sensors_v2.pojos.Sensor getSensor() {
		return sensor;
	}

	public void setSensor(idac.sm_rpi_based_sensors_v2.pojos.Sensor sensor) {
		this.sensor = sensor;
	}

	public Communication getCommunication() {
		return communication;
	}

	public void setCommunication(Communication communication) {
		this.communication = communication;
	}

	public int getRepetitions() {
		return repetitions;
	}

	public void setRepetitions(int repetitions) {
		this.repetitions = repetitions;
	}

	public void run() {
		
	}

}
