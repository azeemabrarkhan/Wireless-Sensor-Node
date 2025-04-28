package idac.sm_rpi_based_sensors_v2.SensorNode.sensors;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

//import idac.sm_rpi_based_sensors_v2.SensorNode.actuators.RGB_LED_Controller;
import idac.sm_rpi_based_sensors_v2.pojos.Feature;

import com.google.gson.Gson;
import com.pi4j.io.i2c.I2CFactory;

import idac.sm_rpi_based_sensors_v2.pojos.Node;
import idac.sm_rpi_based_sensors_v2.pojos.Observation;

public class BME280 extends Sensor {
	
	private int measureEveryNSeconds;
	
	/**
	 * Address of the BME280 with the ALT pin tied to GND (low).
	 */
	public static final byte BME280_ADDRESS_ALT_LOW  	= 0x76;

	/**
	 * Address of the BME280 with the ALT pin tied to VCC (high).
	 */
	public static final byte BME280_ADDRESS_ALT_HIGH 	= 0x77;

	public static final byte BME280_DEFAULT_ADDRESS  	= BME280_ADDRESS_ALT_HIGH;
	
	public static final byte BME280_REGISTER_POWER_CTL = 0x2D;
	public static final byte BME280_REGISTER_DEVID     = 0x00;

	public BME280(int bus) {
		this(bus, BME280_DEFAULT_ADDRESS);
	}

	/**
	 * Specific address constructor.
	 * 
	 * @param address I2C address
	 */
	public BME280(int bus, int address) {
		super(bus, address);
		BUFFER = new byte[24];
	}

	public int getMeasureEveryNSeconds() {
		return measureEveryNSeconds;
	}

	public void setMeasureEveryNSeconds(int measureEveryNSeconds) {
		this.measureEveryNSeconds = measureEveryNSeconds;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	/**
	 * Setup the sensor for general usage.
	 * 
	 * @throws IOException 
	 */
	public void setup() throws IOException, I2CFactory.UnsupportedBusNumberException {
		// http://pi4j.com/example/control.html
		device = I2CFactory.getInstance(i2cBus).getDevice(devAddr);
		device.write(BME280_REGISTER_POWER_CTL, (byte) 0); // reset power settings
	}

	public void readData(double[] raw) throws IOException{
		device.read(0x88, BUFFER, 0, 24);

		// Convert the data
		// temp coefficients
		int dig_T1 = (BUFFER[0] & 0xFF) + ((BUFFER[1] & 0xFF) * 256);
		int dig_T2 = (BUFFER[2] & 0xFF) + ((BUFFER[3] & 0xFF) * 256);
		if(dig_T2 > 32767)
		{
			dig_T2 -= 65536;
		}
		int dig_T3 = (BUFFER[4] & 0xFF) + ((BUFFER[5] & 0xFF) * 256);
		if(dig_T3 > 32767)
		{
			dig_T3 -= 65536;
		}

		// pressure coefficients
		int dig_P1 = (BUFFER[6] & 0xFF) + ((BUFFER[7] & 0xFF) * 256);
		int dig_P2 = (BUFFER[8] & 0xFF) + ((BUFFER[9] & 0xFF) * 256);
		if(dig_P2 > 32767)
		{
			dig_P2 -= 65536;
		}
		int dig_P3 = (BUFFER[10] & 0xFF) + ((BUFFER[11] & 0xFF) * 256);
		if(dig_P3 > 32767)
		{
			dig_P3 -= 65536;
		}
		int dig_P4 = (BUFFER[12] & 0xFF) + ((BUFFER[13] & 0xFF) * 256);
		if(dig_P4 > 32767)
		{
			dig_P4 -= 65536;
		}
		int dig_P5 = (BUFFER[14] & 0xFF) + ((BUFFER[15] & 0xFF) * 256);
		if(dig_P5 > 32767)
		{
			dig_P5 -= 65536;
		}
		int dig_P6 = (BUFFER[16] & 0xFF) + ((BUFFER[17] & 0xFF) * 256);
		if(dig_P6 > 32767)
		{
			dig_P6 -= 65536;
		}
		int dig_P7 = (BUFFER[18] & 0xFF) + ((BUFFER[19] & 0xFF) * 256);
		if(dig_P7 > 32767)
		{
			dig_P7 -= 65536;
		}
		int dig_P8 = (BUFFER[20] & 0xFF) + ((BUFFER[21] & 0xFF) * 256);
		if(dig_P8 > 32767)
		{
			dig_P8 -= 65536;
		}
		int dig_P9 = (BUFFER[22] & 0xFF) + ((BUFFER[23] & 0xFF) * 256);
		if(dig_P9 > 32767)
		{
			dig_P9 -= 65536;
		}

		// Read 1 byte of data from address 0xA1(161)
		int dig_H1 = ((byte)device.read(0xA1) & 0xFF);

		// Read 7 bytes of data from address 0xE1(225)
		device.read(0xE1, BUFFER, 0, 7);

		// Convert the data
		// humidity coefficients
		int dig_H2 = (BUFFER[0] & 0xFF) + (BUFFER[1] * 256);
		if(dig_H2 > 32767)
		{
			dig_H2 -= 65536;
		}
		int dig_H3 = BUFFER[2] & 0xFF ;
		int dig_H4 = ((BUFFER[3] & 0xFF) * 16) + (BUFFER[4] & 0xF);
		if(dig_H4 > 32767)
		{
			dig_H4 -= 65536;
		}
		int dig_H5 = ((BUFFER[4] & 0xFF) / 16) + ((BUFFER[5] & 0xFF) * 16);
		if(dig_H5 > 32767)
		{
			dig_H5 -= 65536;
		}
		int dig_H6 = BUFFER[6] & 0xFF;
		if(dig_H6 > 127)
		{
			dig_H6 -= 256;
		}

		// Select control humidity register
		// Humidity over sampling rate = 1
		device.write(0xF2 , (byte)0x01);
		// Select control measurement register
		// Normal mode, temp and pressure over sampling rate = 1
		device.write(0xF4 , (byte)0x27);
		// Select config register
		// Stand_by time = 1000 ms
		device.write(0xF5 , (byte)0xA0);

		// Read 8 bytes of data from address 0xF7(247)
		// pressure msBUFFER, pressure msb, pressure lsb, temp msb1, temp msb, temp lsb, humidity lsb, humidity msb
		byte[] data = new byte[8];
		device.read(0xF7, data, 0, 8);

		// Convert pressure and temperature data to 19-bits
		long adc_p = (((long)(data[0] & 0xFF) * 65536) + ((long)(data[1] & 0xFF) * 256) + (long)(data[2] & 0xF0)) / 16;
		long adc_t = (((long)(data[3] & 0xFF) * 65536) + ((long)(data[4] & 0xFF) * 256) + (long)(data[5] & 0xF0)) / 16;
		// Convert the humidity data
		long adc_h = ((long)(data[6] & 0xFF) * 256 + (long)(data[7] & 0xFF));

		// Temperature offset calculations
		double var1 = (((double)adc_t) / 16384.0 - ((double)dig_T1) / 1024.0) * ((double)dig_T2);
		double var2 = ((((double)adc_t) / 131072.0 - ((double)dig_T1) / 8192.0) *
				(((double)adc_t)/131072.0 - ((double)dig_T1)/8192.0)) * ((double)dig_T3);
		double t_fine = (long)(var1 + var2);
		double cTemp = (var1 + var2) / 5120.0;
		double fTemp = cTemp * 1.8 + 32;

		// Pressure offset calculations
		var1 = ((double)t_fine / 2.0) - 64000.0;
		var2 = var1 * var1 * ((double)dig_P6) / 32768.0;
		var2 = var2 + var1 * ((double)dig_P5) * 2.0;
		var2 = (var2 / 4.0) + (((double)dig_P4) * 65536.0);
		var1 = (((double) dig_P3) * var1 * var1 / 524288.0 + ((double) dig_P2) * var1) / 524288.0;
		var1 = (1.0 + var1 / 32768.0) * ((double)dig_P1);
		double p = 1048576.0 - (double)adc_p;
		p = (p - (var2 / 4096.0)) * 6250.0 / var1;
		var1 = ((double) dig_P9) * p * p / 2147483648.0;
		var2 = p * ((double) dig_P8) / 32768.0;
		double pressure = (p + (var1 + var2 + ((double)dig_P7)) / 16.0) / 100;

		// Humidity offset calculations
		double var_H = (((double)t_fine) - 76800.0);
		var_H = (adc_h - (dig_H4 * 64.0 + dig_H5 / 16384.0 * var_H)) * (dig_H2 / 65536.0 * (1.0 + dig_H6 / 67108864.0 * var_H * (1.0 + dig_H3 / 67108864.0 * var_H)));
		double humidity = var_H * (1.0 -  dig_H1 * var_H / 524288.0);
		if(humidity > 100.0) {
			humidity = 100.0;
		}else if(humidity < 0.0) {
			humidity = 0.0;
		}

		raw[0] = cTemp;
		raw[1] = fTemp;
		raw[2] = pressure;
		raw[3] = humidity;    
	}
	
	public void run() {
		sensor.setRunning(true);
		try
		{	
			Gson gson = new Gson();
			Feature featureTempC = new Feature("Temperature in Celsius");
			Feature featurePressure = new Feature("Pressure in hPa");
			Feature featureHumidity = new Feature("Humidity in % RH");

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH:mm:ss");
			
			double[] raw = new double[4];
			
			//Read first time to stabilize sensor
        	readData(raw);	
        	Thread.sleep(1000);
			
			int repetitionCount = 0;
	        while (repetitionCount < getRepetitions() || getRepetitions() == 0) {
	        	//RGB_LED_Controller.setLedRunning();
				
	        	readData(raw);			      	
		      	// Output data to screen
				System.out.println(sdf.format(new Date(System.currentTimeMillis())) + ": Environmental data measured");
				System.out.printf("Temperature in Celsius : %.2f C %n", raw[0]);
				//System.out.printf("Temperature in Fahrenheit : %.2f F %n", raw[1]);
				System.out.printf("Pressure : %.2f hPa %n", raw[2]);
				System.out.printf("Relative Humidity : %.2f %% RH %n", raw[3]);
				
				///HERE

            	// Saving raw data without filtering and sampling (for comparison reasons)
            	long timestamp=System.currentTimeMillis();
				
				Observation observationTempC = new Observation(sensor, featureTempC, timestamp, raw[0], node.getCampaignTimestamp());
				Observation observationPressure = new Observation(sensor, featurePressure, timestamp, raw[2], node.getCampaignTimestamp());
				Observation observationHumidity = new Observation(sensor, featureHumidity, timestamp, raw[3], node.getCampaignTimestamp());
				
				communication.publish("nodes/data/"+node.getName()+"/"+sensor.getType()+"/"+featureTempC.getDescription(), gson.toJson(observationTempC));
				Thread.sleep(100);
				communication.publish("nodes/data/"+node.getName()+"/"+sensor.getType()+"/"+featurePressure.getDescription(), gson.toJson(observationPressure));
				Thread.sleep(100);
				communication.publish("nodes/data/"+node.getName()+"/"+sensor.getType()+"/"+featureHumidity.getDescription(), gson.toJson(observationHumidity));
				
				FileWriter writer = new FileWriter("/home/pi/Desktop/Data/EnvironmentData.csv", true);
    	    	writer.write(timestamp + "," + raw[0] + "," + raw[2] + "," + raw[3] + "\n");
        	    writer.flush();
              	writer.close();
              	writer = null;
              	
              	++repetitionCount;

              	//RGB_LED_Controller.setLedWaiting();
              	if(measureEveryNSeconds > 0)
              		Thread.sleep(measureEveryNSeconds * 1000 - 300);
	        }
	        
	        sdf = null;
	        featureTempC = null;
	        featurePressure = null;
	        featureHumidity = null;
	        gson = null;
		}
		catch (Exception ex)
		{
			//RGB_LED_Controller.setLedError();
            System.out.println(String.format("Error during the process of BME280. Exiting. %s", ex.getMessage()));
		}
		sensor.setRunning(false);
	}
}
