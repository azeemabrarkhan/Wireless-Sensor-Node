package idac.sm_rpi_based_sensors_v2.pojos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Node {
	
	long campaignTimestamp;
	String name;	
	UUID uniqueIdentifier;
	List<Sensor> sensorList;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UUID getUniqueIdentifier() {
		return uniqueIdentifier;
	}

	public void setUniqueIdentifier(UUID uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}

	public List<Sensor> getSensorList() {
		return sensorList;
	}

	public void setSensorList(List<Sensor> sensorList) {
		this.sensorList = sensorList;
	}

	public void addSensor(Sensor sensor) {
		if(sensorList == null)
			sensorList = new ArrayList<Sensor>();
		sensorList.add(sensor);
	}

	public void addSensor(byte address, String type) {
		if(sensorList == null)
			sensorList = new ArrayList<Sensor>();
		sensorList.add(new Sensor(address, type));
	}
	
    public long getCampaignTimestamp() {
		return campaignTimestamp;
	}

	public void setCampaignTimestamp(long campaignTimestamp) {
		this.campaignTimestamp = campaignTimestamp;
	}

	@Override
    /* Overriding finalize method to release memory of data structures */
    protected void finalize() throws Throwable  
    {
		sensorList.clear();
		sensorList = null;
		uniqueIdentifier = null;
    }
	
}
