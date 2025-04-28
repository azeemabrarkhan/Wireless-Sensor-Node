package idac.sm_rpi_based_sensors_v2.pojos;

public class Observation {
	
	long timestamp, campaignTimestamp;	
	double result;
	transient Sensor sensor; //Ignore when creating JSON
	transient Feature feature; //Ignore when creating JSON
	
	public Observation() {		
	}
	
	public Observation(Sensor sensor, Feature feature, long timestamp, double result, long campaignTimestamp) {
		this.sensor = sensor;
		this.feature = feature;
		this.timestamp = timestamp;
		this.result = result;
		this.campaignTimestamp = campaignTimestamp;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public double getResult() {
		return result;
	}

	public void setResult(double result) {
		this.result = result;
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
    	sensor = null;
    	feature = null;
    }
}
