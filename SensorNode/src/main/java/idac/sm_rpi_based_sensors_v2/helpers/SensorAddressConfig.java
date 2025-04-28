package idac.sm_rpi_based_sensors_v2.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/*
 * This class loads the sensor addresses into memory
 */
public class SensorAddressConfig {
	
	/**
	 * Map where the I2C addresses and the sensors associated are stored. This map is loaded
	 * based on the properties file.
	 * 
	 * A Map is <Key, Value> structure which maps a key to a value in the form of
	 * pairs. It's like a dictionary where the key is unique and has
	 * assigned a value.
	 */
	public static Map<Byte, String> addressSensorMap = null;
	
	/**
	 * hMap where the I2C addresses and the sensor types associated are stored. This map is loaded
	 * based on the properties file.
	 */
	public static Map<Byte, String> addressSensorTypeMap = null;

	/**
	 * The Properties instance where the properties file is loaded into memory.
	 */
	public static Properties prop;

	static {
		try {
			InputStream is = null;

			// First try to load a custom properties file. This file is generated when a
			// property is modified. Also, the user can save the file manually in the same
			// folder as the JAR and add the desired values.
			String propertiesPath = "SensorAddress.properties";
			
			File f = new File(propertiesPath);
			if (f.exists() && !f.isDirectory()) {
				System.out.println("Attempting to find the parameters definition 'properties' file in " + propertiesPath);
				is = new FileInputStream(f);
			} else { // If there is no modified properties file, load the default properties file.
				is = ParameterConfig.class.getResourceAsStream("/SensorAddress.properties");
			}

			if (is == null) {
				System.out.println("ERROR: Could not find the parameters definition 'properties' file.");
			} else {
				prop = new Properties();
				prop.load(is); // Load properties file into memory
				
				// Close the input stream and release memory
				is.close();
				is = null;

				/*
				 * The HashMap is a map where the values are ordered according to their hash
				 * value representation. The ordering is arbitrary. For more on this, please
				 * read:
				 * https://www.geeksforgeeks.org/differences-treemap-hashmap-linkedhashmap-java/
				 */
				addressSensorMap = new HashMap<Byte, String>();				
				addressSensorTypeMap = new HashMap<Byte, String>();
				
			    for (String key : prop.stringPropertyNames()) {
			    	byte address = getAddressByte(key);
			    	addressSensorMap.put(address, prop.getProperty(key).split("#")[0]);
			    	addressSensorTypeMap.put(address, prop.getProperty(key).split("#")[1]);
			    }
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static byte getAddressByte(String address) {
		Byte ret = null;
		ret = (byte) ((Character.digit(address.charAt(0), 16) << 4) + Character.digit(address.charAt(1), 16));
		return ret;
	}
}
