package idac.sm_rpi_based_sensors_v2.pojos;

import java.util.List;

public class Data {
	
	String nodeName;
	List<Observation> rawData;
	List<Observation> processedData;
	
	public Data() {
		nodeName = "";
	}
	
	public Data(String nodeName) {
		this.nodeName = nodeName;
	}
	
	public String getNodeName() {
		return nodeName;
	}
	
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	
	public List<Observation> getRawData() {
		return rawData;
	}
	
	public void setRawData(List<Observation> rawData) {
		this.rawData = rawData;
	}
	
	public List<Observation> getProcessedData() {
		return processedData;
	}
	
	public void setProcessedData(List<Observation> processedData) {
		this.processedData = processedData;
	}
	
    @Override
    /* Overriding finalize method to release memory of data structures */
    protected void finalize() throws Throwable  
    {
    	rawData.clear();
        rawData = null; 
        processedData.clear();
        processedData = null;
        nodeName = null;
    }
}
