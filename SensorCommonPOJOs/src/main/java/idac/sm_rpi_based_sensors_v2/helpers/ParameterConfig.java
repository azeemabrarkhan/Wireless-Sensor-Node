package idac.sm_rpi_based_sensors_v2.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

/*
 * This class loads the parameters into memory
 */
public class ParameterConfig {

	/**
	 * ArrayList where the sensor types that are defined are stored. This list
	 * contains all sensors that the nodes are capable of being used according to
	 * the properties file.
	 */
	public static List<String> sensorTypeList = null;

	/**
	 * HashMap where the parameter request questions are stored. This map is loaded
	 * based on the properties file.
	 * 
	 * A Map is <Key, Value> structure which maps a key to a value in the form of
	 * pairs. It's basically like a dictionary where the key is unique and has
	 * assigned a value.
	 */
	public static Map<String, String> sensorParametersRequestMap = null;

	/**
	 * HashMap where the parameter default values are stored. This map is loaded
	 * based on the properties file.
	 * 
	 * The default value is determined to be an abstract Object as it can be a
	 * string, an integer or a real number. The exact type for each parameter is
	 * defined based on the properties file and has to be treated carefully.
	 */
	public static Map<String, Object> sensorParametersDefaultMap = null;

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
			String propertiesPath = "ParametersDefinitionCustom.properties";
			
			File f = new File(propertiesPath);
			if (f.exists() && !f.isDirectory()) {
				System.out.println("Attempting to find the parameters definition 'properties' file in " + propertiesPath);
				is = new FileInputStream(f);
			} else { // If there is no modified properties file, load the default properties file.
				is = ParameterConfig.class.getResourceAsStream("/ParametersDefinition.properties");
			}

			if (is == null) {
				System.out.println("ERROR: Could not find the parameters definition 'properties' file.");
			} else {
				prop = new Properties();
				prop.load(is); // Load properties file into memory
				
				// Close the input stream and release memory
				is.close();
				is = null;

				// Get all accepted sensor types as a list. The list in the properties file
				// cannot contain spaces between values and each element has to be separated by
				// a comma.
				sensorTypeList = Arrays.asList(prop.getProperty("SensorType").split(","));
				if (sensorTypeList.size() > 0) {

					/*
					 * The HashMap is a map where the values are ordered according to their hash
					 * value representation. The ordering is arbitrary. For more on this, please
					 * read:
					 * https://www.geeksforgeeks.org/differences-treemap-hashmap-linkedhashmap-java/
					 */
					sensorParametersRequestMap = new HashMap<String, String>();
					sensorParametersDefaultMap = new HashMap<String, Object>();

					// For each sensor type
					for (String type : sensorTypeList) {
						// Get all the parameters of the sensor type as a list. The list in the
						// properties file cannot contain spaces between values and each element has to
						// be separated by a comma.
						List<String> parameterList = Arrays.asList(prop.getProperty(type + ".Parameters").split(","));
						
						// For each parameter of the sensor type
						for (String parameter : parameterList) {
							// Add the request question to the request map.
							sensorParametersRequestMap.put(type + "." + parameter,
									prop.getProperty(type + "." + parameter + ".Request"));

							// Switch between all possible types that the parameter can implement
							switch (prop.getProperty(type + "." + parameter + ".Type")) {
								case "Integer": // If it is an Integer, add to the default map parsing the value as an
												// Integer.
									sensorParametersDefaultMap.put(type + "." + parameter,
											Integer.parseInt(prop.getProperty(type + "." + parameter + ".Default")));
									break;
								case "Double": // If it is a Double, add to the default map parsing the value as a Double.
									sensorParametersDefaultMap.put(type + "." + parameter,
											Double.parseDouble(prop.getProperty(type + "." + parameter + ".Default")));
									break;
								case "String": // If it is a String, add to the default map without parsing, as getProperty method returns String.
									sensorParametersDefaultMap.put(type + "." + parameter,
											prop.getProperty(type + "." + parameter + ".Default"));
									break;
								case "Boolean": // If it is a String, add to the default map parsing the value as a String.
									sensorParametersDefaultMap.put(type + "." + parameter,
											prop.getProperty(type + "." + parameter + ".Default")=="1");
									break;
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
