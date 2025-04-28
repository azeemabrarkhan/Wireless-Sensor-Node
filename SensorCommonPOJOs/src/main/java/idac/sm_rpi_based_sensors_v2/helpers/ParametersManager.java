package idac.sm_rpi_based_sensors_v2.helpers;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParametersManager {

	/**
	 * List where the sensor types that are being used are stored. This list
	 * contains all sensors that the nodes are using and should only have types that
	 * exist in the properties file.
	 */
	List<String> sensorTypeList;

	/**
	 * TreeMap where the parameters that are needed are stored. This map is loaded
	 * based on the sensorTypeList variable.
	 * 
	 * A Map is <Key, Value> structure which maps a key to a value in the form of
	 * pairs. It's like a dictionary where the key is unique and has
	 * assigned a value.
	 * 
	 * The TreeMap is an map where the values are ordered as they are inserted,
	 * based on the key.
	 */
	TreeMap<String, Object> parameterMap;

	/**
	 * Indicates whether the parameters are modified by the user.
	 */
	boolean parametersChanged;

	/**
	 * Initialize a ParametersManager object
	 */
	public ParametersManager() {
		parametersChanged = false;
		parameterMap = new TreeMap<String, Object>();
	}

	/**
	 * Initialize a ParametersManager object based on a list of sensor types to be
	 * used
	 * 
	 * @param sensorTypeList The list of sensors to be used
	 */
	
	public ParametersManager(ArrayList<String> sensorTypeList) {
		this(); // Calls the constructor without parameters for initial attributes
				// initialization

		this.sensorTypeList = new ArrayList<>(sensorTypeList); // Creates a copy of the list to avoid possible
																// unintended modifications

		for (String type : sensorTypeList) { // For each sensor type in the list
			if (ParameterConfig.sensorTypeList.contains(type)) { // If the sensor type exists in the properties file
				// Iterate over all the properties of the sensor type and add them to the
				// parameterMap with its default value
				for (Map.Entry<String, Object> entry : ParameterConfig.sensorParametersDefaultMap.entrySet()) {
					parameterMap.put(entry.getKey(), entry.getValue());
				}
			}
		}
	}

	public TreeMap<String, Object> getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(TreeMap<String, Object> parameterMap) {
		this.parameterMap = parameterMap;
	}

	public List<String> getSensorTypeList() {
		return sensorTypeList;
	}

	public void setSensorTypeList(List<String> sensorTypeList) {
		this.sensorTypeList = sensorTypeList;
	}

	public boolean isParametersChanged() {
		return parametersChanged;
	}

	public void setParametersChanged(boolean parametersChanged) {
		this.parametersChanged = parametersChanged;
	}

	/**
	 * Requests the parameters to user in the console.
	 * 
	 * @param reader A BufferedReader instance which will receive the input of the
	 *               user. It is received as parameter to avoid multiple instances
	 *               of the same object, throughout the code.
	 */
	public void requestParameters(BufferedReader reader) {
		for (String sensorType : sensorTypeList) { // For each sensor type in the list, request the parameters
			System.out.println("--------------------\n" + sensorType + " parameters\n--------------------");

			/*
			 * This for iterates over all the pairs of parameters keys and default values.
			 * 
			 * The Map.Entry is a pair of Key-Value from the parameterMap.
			 *  
			 * The parameterMap.subMap() method filters the parameterMap based on a range of
			 * values. In this case, we filter all parameters that start with the sensor
			 * type name. Then we call the entrySet() method that generates a set of all entries in the map.
			 */
			for (Map.Entry<String, Object> entry : parameterMap.subMap(sensorType, sensorType + Character.MAX_VALUE)
					.entrySet()) {
				
				//Get the type of the parameter and assign the format character needed for the request
				String typeOfParam = "s";
				if (entry.getValue() instanceof Integer)
					typeOfParam = "d";
				else if (entry.getValue() instanceof Double)
					typeOfParam = "f";
				else if (entry.getValue() instanceof Boolean)
					typeOfParam = "b";

				//Request the parameter. If no value is entered, the default value will be used.
				System.out.println(String.format(ParameterConfig.sensorParametersRequestMap.get(entry.getKey())
						+ "\n(Default=%" + typeOfParam + ")", entry.getValue()));
				reader = new BufferedReader(new InputStreamReader(System.in));
				String input;
				try {
					input = reader.readLine();
					
					//If an input is received, save it in the map parsing according to the input type.
					if (input != null && !input.equals("")) {
						Object parsedValue = null;
						switch (typeOfParam) {
							case "s":
								parsedValue = new String(input);
								break;
							case "d":
								parsedValue = Integer.parseInt(input);
								break;
							case "f":
								parsedValue = Double.parseDouble(input);
								break;
							case "b":
								parsedValue = input == "1";
								break;
						}
						parameterMap.put(entry.getKey(), parsedValue);
						
						//Indicate the parameters were changed
						parametersChanged = true;
						
						//Save the change to the properties loaded into memory
						ParameterConfig.prop.setProperty(entry.getKey() + ".Default", input);
					}
				} catch (IOException e1) {
					System.out.println("Error receiving parameter '" + entry.getKey() + "', default will be used.");
				}
			}
		}
	}

	/**
	 * Initialize a ParametersManager object based on a list of sensor types to be
	 * used
	 * 
	 * @return If the saving process executed correctly.
	 */
	public boolean saveParameters() {
		boolean ret = true;
		try {
			FileOutputStream out = new FileOutputStream("ParametersDefinitionCustom.properties");
			ParameterConfig.prop.store(out, null);
			out.close();
		} catch (IOException e) {
			System.out.println("Error saving new parameters");
			e.printStackTrace();
			ret = false;
		}
		return ret;
	}

	@Override
	/* Overriding finalize method to release memory of data structures */
	protected void finalize() throws Throwable {
		parameterMap.clear();
		parameterMap = null;
		sensorTypeList.clear();
		sensorTypeList = null;
	}
}
