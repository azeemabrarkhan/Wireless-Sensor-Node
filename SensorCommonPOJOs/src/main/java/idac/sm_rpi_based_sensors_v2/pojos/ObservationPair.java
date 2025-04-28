package idac.sm_rpi_based_sensors_v2.pojos;

public class ObservationPair {
		
	long campaignTimestamp;
	double value1, value2;
	transient Sensor sensor; //Ignore when creating JSON
	transient Feature feature; //Ignore when creating JSON
	
	public ObservationPair() {		
	}
	
	public ObservationPair(Sensor sensor, Feature feature, double value1, double value2, long campaignTimestamp) {
		this.sensor = sensor;
		this.feature = feature;
		this.value1 = value1;
		this.value2 = value2;
		this.campaignTimestamp = campaignTimestamp;
	}
	
    public double getValue1() {
		return value1;
	}

	public void setValue1(double value1) {
		this.value1 = value1;
	}

	public double getValue2() {
		return value2;
	}

	public void setValue2(double value2) {
		this.value2 = value2;
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
