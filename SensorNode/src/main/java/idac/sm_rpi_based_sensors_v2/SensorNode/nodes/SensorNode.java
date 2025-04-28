package idac.sm_rpi_based_sensors_v2.SensorNode.nodes;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.google.gson.Gson;

import idac.sm_rpi_based_sensors_v2.SensorNode.actuators.RGB_LED_Controller;
import idac.sm_rpi_based_sensors_v2.helpers.Communication;
import idac.sm_rpi_based_sensors_v2.helpers.CommunicationLocal;
import idac.sm_rpi_based_sensors_v2.helpers.CommunicationMQTT;
import idac.sm_rpi_based_sensors_v2.helpers.ParametersManager;
import idac.sm_rpi_based_sensors_v2.helpers.SensorAddressConfig;
import idac.sm_rpi_based_sensors_v2.pojos.Node;
import idac.sm_rpi_based_sensors_v2.pojos.Sensor;

public class SensorNode {
		
	public static void main(String args[]){
		
		String communicationType = "";
		
		if (args.length > 2)
		    System.out.println(
				"Incorrect parameters recieved. Only two parameter are permited (sensor addresses separated with a hyphen '-' and the communication mode (mqtt or local)).");
		else {			
			if (args.length >= 1)
				System.out.println("Sensor addresses recieved are: " + args[0]);
			if (args.length >= 2)
				communicationType = args[1];

			Gson gson = new Gson();
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
			LocalDateTime now;
	    		    	
	    	Communication communication;
	    	
	    	switch(communicationType){
	    		case "MQTT":
	    		case "mqtt":
	    	    	communication = new CommunicationMQTT();
	    	    	break;
    	    	default:
	    	    	communication = new CommunicationLocal();
    	    		break;
	    	} 

	    	String hostName = "Unknown";
			try //Get the computer name for reference when registering the node
			{
			    InetAddress addr = InetAddress.getLocalHost();
			    hostName = addr.getHostName();
			    addr = null;
			} catch (UnknownHostException ex)
			{
			    System.out.println("hostName can not be resolved");
			}			
			UUID uniqueId = UUID.randomUUID(); //Generates a random Universal unique identifier (UUID) to distinguish between nodes with the same name
			//hostName += "-" + uniqueId.toString(); 
			communication.setHostName(hostName);
			communication.setUniqueId(uniqueId);
	
			//Creates the node description
	    	Node node = new Node();
	    	node.setName(hostName);
	    	node.setUniqueIdentifier(uniqueId);
	    	node.setCampaignTimestamp(System.currentTimeMillis());
			if (args.length >= 1) {
				//Adds the sensors connected to the node, according to the address received in the arguments
		    	for (String address : args[0].split("-")) {
		    		byte addressByte =  SensorAddressConfig.getAddressByte(address);
		    		//If the address is valid (according to the properties file)
		    		if (SensorAddressConfig.addressSensorTypeMap.containsKey(addressByte)) {
		    			node.addSensor(addressByte, SensorAddressConfig.addressSensorTypeMap.get(addressByte));
		    		} else { //If the address is not valid, add an unidentified sensor type to the report to the server
		    			node.addSensor(addressByte, "Unidentified");
		    		}
		    	}
			} else { //If no addresses are received in the arguments, add default for testing (Accelerometer)
				//TODO: set sensor as unidentified 
    			node.addSensor(SensorAddressConfig.getAddressByte("77"), "Environmental");
			}
	
	        //client.connect();
	        now = LocalDateTime.now();
	        System.out.println(dtf.format(now) + ": Client is running");
	        
	        String message = "";
	        
	        if(communication.connect()) {
		        try { 
	        		communication.register();
	        		communication.publish("nodes/status/connected", gson.toJson(node));
		            
		            now = LocalDateTime.now();
		            System.out.println(dtf.format(now) + ": The client has published its registration");
		
		            //Receive parameters 
		        	message = communication.receiveMessage("nodes/parameters", 120); //Receive the message. It waits 2 minutes for the message to arrive or it cancels the process
		        	
		        	//This line takes the parameters received as JSON and converts it to the Parameter class
		        	ParametersManager parameters = gson.fromJson(message, ParametersManager.class);
		
		        	now = LocalDateTime.now();
		            System.out.println(String.format("%s: Parameters received are %s", dtf.format(now), gson.toJson(parameters)));
		    		
		            System.out.println(dtf.format(now) + ": The client has received all parameters. Waiting for signal to start");
		            
	        		communication.publish("nodes/status/ready", gson.toJson(node));
		
		        	message = communication.receiveMessage("nodes/start", 600); //Waits 5 minutes for signal, but could be less. It's just to be on the safe side.
		    		
		        	if(!message.equals("1")) {
		            	now = LocalDateTime.now();
		                System.out.println(String.format("%s: Process stopped. Message received = '%s'", dtf.format(now), message));
		        	}
		        	else {
		            	now = LocalDateTime.now();
		                System.out.println(String.format("%s: Start signal received. Starting data acquisition.", dtf.format(now)));
			            
		        		communication.publish("nodes/status/measuring", gson.toJson(node));
		        		
		          		new NodesDataCollection().performDataCollection(communication, parameters, node);
		        	}
		        	
		            parameters = null;
		            
		            //Continuously echo to acknowledge being connected
		            boolean running = true;
		        	while (running)
		        	{
		        		message = communication.receiveMessage("nodes/echo", 600); //Receive the message. It waits 10 minutes for the echo to arrive or it cancels the process		        		
		        		communication.publish("nodes/status/measuring", gson.toJson(node)); //Answer that measuring is under way

		        		running = false;
		        		for (Sensor sensor : node.getSensorList()){
		        			running = running || sensor.isRunning();
		        		}
		        	}
		        }
				catch (Exception ex)
				{
					RGB_LED_Controller.setLedError();
		        	now = LocalDateTime.now();
		            System.out.println(String.format("%s: Error during the process. Exiting. %s", dtf.format(now), ex.getMessage()));
				}
		    	now = LocalDateTime.now();
		        System.out.println(String.format("%s: Process finalized", dtf.format(now)));
		        communication.disconnect();
	        }
	        
	        //Release memory
	        gson = null;
	        communication = null;
	        dtf = null;
	        hostName = null;
	        now = null;
	        message = null;
	        System.gc();
		}
	}
}
