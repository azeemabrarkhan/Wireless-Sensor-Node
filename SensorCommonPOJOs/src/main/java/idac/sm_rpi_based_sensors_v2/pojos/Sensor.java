package idac.sm_rpi_based_sensors_v2.pojos;

public class Sensor {
	
	byte address;	
	String type;
	transient boolean running; //Ignore when creating JSON
	
	public Sensor() {
		type = "";
		running = false;
	}
	
	public Sensor(byte address, String type) {
		this();
		
		this.address = address;
		this.type = type;
	}

	public byte getAddress() {
		return address;
	}

	public void setAddress(byte address) {
		this.address = address;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	@Override
    /* Overriding finalize method to release memory of data structures */
    protected void finalize() throws Throwable  
    {
		type = null;
    }
}
