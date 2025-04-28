package idac.sm_rpi_based_sensors_v2.helpers;

public class CommunicationLocal extends Communication {
	
	public CommunicationLocal() {
		super();
	}
	
	@Override
	public boolean connect() {
        return true;
	}

	@Override
	public void register() {
	}

	@Override
	public void publish(String topic, String content) {
	}
	
	@Override
	public String receiveMessage(String topic, long timeout) throws InterruptedException {
		String ret = "";
		switch(topic) {
			case "nodes/parameters":
				int secondsMeasuring = 300,
					tempMeasureEvery = 5;
				ret = "{\"sensorTypeList\":[\"Environmental\",\"Accelerometer\"],"+
						"\"parameterMap\":{\"Accelerometer.SecondsMeasuring\":"+secondsMeasuring+","+
										  "\"Accelerometer.SamplingRate\":400,"+
										  "\"Accelerometer.NumberOfPeaks\":2,"+
										  "\"Accelerometer.Direction\":\"Z\","+
										  "\"Accelerometer.MeasureEveryNSeconds\":0,"+
										  "\"Accelerometer.Measure\":true,"+
										  "\"Accelerometer.Repetitions\":1,"+
										  "\"Environmental.MeasureEveryNSeconds\":"+tempMeasureEvery+","+
										  "\"Environmental.Measure\":true,"+
										  "\"Environmental.Repetitions\":"+(int)(secondsMeasuring/tempMeasureEvery+1)+"}}";
				break;
			case "nodes/start":
				ret = "1";
				break;
			default:
				break;
		}
		return ret;
	}

	@Override
	public void disconnect() {
	}
}
