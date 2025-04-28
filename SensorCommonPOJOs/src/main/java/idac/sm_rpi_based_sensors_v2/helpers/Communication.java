package idac.sm_rpi_based_sensors_v2.helpers;

import java.util.UUID;

public abstract class Communication {

	protected String hostName;
	UUID uniqueId;
	
	public Communication() {
		hostName = "Unknown";
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public UUID getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(UUID uniqueId) {
		this.uniqueId = uniqueId;
	}
	
	public abstract boolean connect();
	
	public abstract void register();
	
	public abstract void publish(String topic, String content);
	
	public abstract String receiveMessage(String topic, long timeout) throws InterruptedException;
	
	public abstract void disconnect();

}
