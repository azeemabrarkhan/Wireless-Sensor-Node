package idac.sm_rpi_based_sensors_v2.pojos;

public class Feature {

	String description;
	
	public Feature() {
		description = "";
	}
	
	public Feature(String description) {
		this();
		
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

//	public void addObservation(Observation data) {
//		if (observationList == null)
//			observationList = new ArrayList<Observation>();
//		observationList.add(data);
//	}
//
//	public void addObservation(long timeStamp, double data) {
//		if (observationList == null)
//			observationList = new ArrayList<Observation>();
//		observationList.add(new Observation(timeStamp, data));
//	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (obj.getClass() == this.getClass()) {
			Feature reading = (Feature) obj;
			return description == reading.getDescription();
		}
		else
			return false;
	}

	@Override
	/* Overriding finalize method to release memory of data structures */
	protected void finalize() throws Throwable {
//		observationList.clear();
//		observationList = null;
		description = null;
	}

}
