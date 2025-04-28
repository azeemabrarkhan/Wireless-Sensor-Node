package idac.sm_rpi_based_sensors_v2.SensorServer.server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gson.Gson;

import idac.sm_rpi_based_sensors_v2.helpers.Communication;
import idac.sm_rpi_based_sensors_v2.helpers.ParametersManager;
import idac.sm_rpi_based_sensors_v2.pojos.Data;
import idac.sm_rpi_based_sensors_v2.pojos.Node;
import idac.sm_rpi_based_sensors_v2.pojos.Sensor;

public class ServerMQTT {

	public static void main(String args[]){
		
		Communication communication = new Communication();
		
		String rawDataPath = "./Results/";
		Path dirPathObj = Paths.get(rawDataPath);
        if(Files.notExists(dirPathObj)) {
            try {
                // Creating The New Directory Structure
                Files.createDirectories(dirPathObj);
            } catch (IOException ioExceptionObj) {
                System.out.println("Problem Occured While Creating The Directory Structure= " + ioExceptionObj.getMessage());
            }
        }
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
		LocalDateTime now = LocalDateTime.now();  
		Gson gson = new Gson();
		List<Data> nodeData = new ArrayList<Data>();
		BufferedReader reader;
        String message = "";
        //FileWriter writer;

		// ===== Number of nodes ==========//
		int numberOfNodes = 3; // sensor nodes to be used		
        System.out.println(String.format("Enter the number of nodes to be used.\n(Default=%d)",numberOfNodes));    
        reader = new BufferedReader(new InputStreamReader(System.in));
        String input;
		try {
			input = reader.readLine();
	        if(input != null && !input.equals(""))
	        	numberOfNodes = Integer.parseInt(input);
		} catch (IOException e1) {
	        System.out.println(String.format("Error receiving number of nodes, %d will be used.", numberOfNodes));
		}     
		// ===== Number of nodes ==========//

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
		hostName += "-" + uniqueId.toString(); 
		communication.setHostName(hostName);
		communication.setUniqueId(uniqueId);
		
        if(communication.connect()) {
	    	now = LocalDateTime.now();
	        System.out.println("\n" + dtf.format(now) + ": Server is running");
	        
	        try { 
        		communication.register();
        		
	            communication.subscribe("nodes/status/connected");
	            
	        	now = LocalDateTime.now();
	            System.out.println(String.format("%s: Server is subscribed and waiting for %d nodes", dtf.format(now), numberOfNodes));
	
	            ArrayList<String> sensorTypeList = new ArrayList<String>();             
	            for(int i = 0; i < numberOfNodes; ++i) {
	            	message = communication.receiveMessage(120); //Receive the message. It waits 2 minutes for the message to arrive or it cancels the process
	            	Node node = gson.fromJson(message, Node.class);
	            	
	            	for(Sensor sensor : node.getSensorList()) {
	            		if (!sensorTypeList.contains(sensor.getType()))
	            			sensorTypeList.add(sensor.getType());
	            	}
	            	
		        	now = LocalDateTime.now();
	                System.out.println(String.format("%s: Node %s connected", dtf.format(now), node.getName()));
	            }
	            
	        	now = LocalDateTime.now();
	            System.out.println(dtf.format(now) + ": All nodes have connected\n--------------------");            
	
	    		// Step 1: define input parameters
	    		// ===== Input Parameters ==========//
	    		ParametersManager parameters = new ParametersManager(sensorTypeList);
	    		parameters.requestParameters(reader);
	    		// ===== Input Parameters ==========//
	            
	            // Sending input parameters to the sensor nodes
	            System.out.println("Press enter to send parameters to nodes");
	            reader.readLine();
	
	            communication.publish("nodes/parameters", gson.toJson(parameters));
	
	        	now = LocalDateTime.now();
	            System.out.println("\n" + dtf.format(now) + ": Input parameters transmitted\n--------------------");
	            
	            // Sending input parameters to the sensor nodes
	            System.out.println("Press 1 to send start signal to nodes or any other thing to exit.\n(Default=1)");
	            input = reader.readLine();
	            if(input == null || input.equals(""))
	            	input = "1";      
	            
	            communication.publish("nodes/start", input);
	
	        	now = LocalDateTime.now();
	            if (input != "1") {
		            System.out.println("\n" + dtf.format(now) + ": Proccess stopped. The input was '" + input +"'\n--------------------");
	            } else {
		            System.out.println("\n" + dtf.format(now) + ": Start signal sent '" + input +"'\n--------------------");

		            communication.subscribe("nodes/data/#");
		        	now = LocalDateTime.now();
		            System.out.println("\n" + dtf.format(now) + ": Waiting data from nodes");
		            
//		            for(int i = 0; i < numberOfNodes; ++i) {
//		            	//Waits to receive the data from a node. It waits for a time window of the seconds measuring times two (this is for giving a cushion in case there is a delay in the message reception).
//		            	receivedMessage = publishes.receive((Integer)parameters.getParameterMap().get("Accelerometer.SecondsMeasuring") * 2, TimeUnit.SECONDS).get();
//		            	message = new String(receivedMessage.getPayloadAsBytes());
//		            	
//		            	Data data = gson.fromJson(message, Data.class);
//		            	nodeData.add(data);
//		            	
//			        	now = LocalDateTime.now();
//		                System.out.println(String.format("%s: Node %s connected", dtf.format(now), message));
//		
//		                receivedMessage = null; //Sets null to avoid memory leaks
//		            }
		            
		            // Storing data into a file in ./Results/
		            //long date=System.currentTimeMillis();
		          	//writer = new FileWriter(rawDataPath + Long.toString(date) + "_Acc.txt");
		
	//	      		int lengthOfDataSet = (Integer)parameters.getParameterMap().get("Accelerometer.SecondsMeasuring") * (Integer)parameters.getParameterMap().get("Accelerometer.SamplingRate");
	//	          	for(int node = 0; node < numberOfNodes; node++){
	//	    	      	for(int k = 0; k < lengthOfDataSet; k++){
	//	    	      		writer.write(nodeData.get(node).getRawData().get(0)[k]  + "	");
	//	    	      		writer.write(nodeData.get(node).getRawData().get(1)[k]  + "\n");      		
	//	    	      	    }
	//	          	writer.write("\n \n \n");
	//	          	}
	//	          	
	//	          	for(int node = 0; node < numberOfNodes; node++){
	//	    	      	for(int k = 0; k < (Integer)parameters.getParameterMap().get("Accelerometer.NumberOfPeaks"); k++){
	//	    	      		writer.write(nodeData.get(node).getProcessedData().get(0)[k]  + "	");
	//	    	      		writer.write(nodeData.get(node).getProcessedData().get(1)[k]  + "\n");      		
	//	    	      	    }
	//	          	writer.write("\n \n \n");
	//	          	}
	//	          	
	//	          	writer.flush();
	//	          	writer.close();
	//	            
	//	          	// Final output of the console
	//	            System.out.println("\nAcceleration-data written into " + rawDataPath);
	//	            System.out.print("\nReceived Frequencies: \n" );
	//	
	//	          	for(int node = 0; node < numberOfNodes; node++){
	//	          		System.out.println("\nSensor Node " + (node + 1) + " - " + nodeData.get(node).getNodeName());
	//	          		System.out.println("\tSensor 1\tSensor 2");
	//	    	        for (int i = 0; i < (Integer)parameters.getParameterMap().get("Accelerometer.NumberOfPeaks"); i++) {
	//	    	        	System.out.print("Peak " + (i+1) + "\t" + nodeData.get(node).getProcessedData().get(0)[i] + "\t\t");
	//	    				System.out.println(nodeData.get(node).getProcessedData().get(1)[i]);
	//	    			}
	//	          	}
	            }
	          	
	        	now = LocalDateTime.now();
	            System.out.println(String.format("%s: Process finalized", dtf.format(now)));
	            communication.disconnect();
	
	            if (parameters.isParametersChanged()) {
		            System.out.println("Do you want to save parameter modification? (Y=yes / N=No).\n(Default=N)");
		            input = reader.readLine();
		            if (input == null || input.equals(""))
		            	input = "N";
	
		        	now = LocalDateTime.now();
		            if (input.toUpperCase().startsWith("Y") && parameters.saveParameters())
		                System.out.println(String.format("%s: Parameters changed successfully.", dtf.format(now)));
		            else
		                System.out.println(String.format("%s: Parameters were not changed.", dtf.format(now)));
	            }

	          	parameters = null;
	
	        } catch (Exception e) {
				e.printStackTrace();
			}
        }
        
        //Release memory
        communication = null;
        gson = null;
        rawDataPath =null;
        dtf = null;
        nodeData.clear();
        nodeData = null;
        reader = null;
        message = null;
        //writer = null;
        System.gc();
	}
}
