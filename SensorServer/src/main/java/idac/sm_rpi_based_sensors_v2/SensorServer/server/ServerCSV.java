package idac.sm_rpi_based_sensors_v2.SensorServer.server;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import idac.sm_rpi_based_sensors_v2.SensorServer.helpers.DistanceCalculator;
import idac.sm_rpi_based_sensors_v2.SensorServer.helpers.FrequencySpectrum;
import idac.sm_rpi_based_sensors_v2.SensorServer.helpers.PeakPicking;

public class ServerCSV {

	public static void main(String args[]){

        //*********START PARAMETER DEFINITION*********     
		
		//Change here the file name to the file you want to load. 
		//The file should be in the resource folder of the project, i.e.
		//SensorServer\src\main\resources
		String file = "M4_downsampled_50khz.csv";
		
		//Change here to the sampling rate used during measurement
		//It will probably by 50Hz
		int samplingRate = 50;
		
		//Change here for the number of peaks you want to detect, larger than 1
		int numberOfPeaks = 5;

        //*********END PARAMETER DEFINITION*********
		
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

        ArrayList<Double> observationX = new ArrayList<Double>();
        ArrayList<Double> observationY = new ArrayList<Double>();
        ArrayList<Double> observationZ = new ArrayList<Double>();
        ArrayList<Date> timestamp = new ArrayList<Date>();
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss"),
						 timestampF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"),
					 	 csvF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		timestampF.setTimeZone(TimeZone.getTimeZone("GMT"));
		long date=System.currentTimeMillis();
		Date resultdate = new Date(date);
        //FileWriter writer;

        System.out.println("\n" + dtf.format(LocalDateTime.now()) + ": Opening file");
        
        try { 
        	BufferedReader br = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(file), "UTF-8"));
        	String[] lineValues = null;
        	FileWriter writer;

        	//Read first line and check if it is a CSV file. THe first line should be headers, so it should be ommited when extracting the data
        	String line = br.readLine();
        	if(line.length() == 0 || !line.contains(","))
        	{
                br.close();
                br = null;
        		throw new Exception("The file has an incorrect format");
        	}
        	//Iterate every line and put the X, Y and Z values in the corresponding map ArrayList
            while ((line = br.readLine()) != null) {
            	lineValues = line.split(",");
            	observationX.add(Double.parseDouble(lineValues[3])*9.80665);//Column 4 should contain the X value. We use index 3 because it is 0-based.
            	observationY.add(Double.parseDouble(lineValues[4])*9.80665);//Column 5 should contain the Y value. We use index 4 because it is 0-based.
            	observationZ.add(Double.parseDouble(lineValues[5])*9.80665);//Column 6 should contain the Z value. We use index 5 because it is 0-based.
            	timestamp.add(timestampF.parse(lineValues[2].replace("\"", "")));
            }
            br.close();
            br = null;
            System.out.println("\n" + dtf.format(LocalDateTime.now()) + ": File read");
            
            //*********START DISTANCE CALCULATION*********            
            //Frequency limits for the filtering
            double cornerFreq1 = 24;
            double cornerFreq2 = 0.4;
            double cornerFreq3 = samplingRate / 2 - 0.01; //This corner frequency is used according to the sampling rate. It must be slightly less than the Nyquist frequency.
            
            double gamma = 0.5;
            double beta = 0.25;

            System.out.println("\n" + dtf.format(LocalDateTime.now()) + ": Calculating distances");
            DistanceCalculator calculator = new DistanceCalculator(cornerFreq1, cornerFreq2, cornerFreq3, samplingRate, gamma, beta);
            double[] distancesX = calculator.calculateDistances(observationX.stream().mapToDouble(d -> d).toArray());
            double[] distancesY = calculator.calculateDistances(observationY.stream().mapToDouble(d -> d).toArray());
            double[] distancesZ = calculator.calculateDistances(observationZ.stream().mapToDouble(d -> d).toArray());
            double maxDistanceX = calculator.calculateMaxDistance(distancesX);
            double maxDistanceY = calculator.calculateMaxDistance(distancesY);
            double maxDistanceZ = calculator.calculateMaxDistance(distancesZ);

            System.out.println(String.format("Max distance for X axis is: %f", maxDistanceX));
            System.out.println(String.format("Max distance for Y axis is: %f", maxDistanceY));
            System.out.println(String.format("Max distance for Z axis is: %f", maxDistanceZ));
            
            //Storing distances into a file in ./Results/
            writer = new FileWriter(rawDataPath +sdf.format(resultdate)+ "_Distances.csv");
      		writer.write("timestamp,distanceX,distanceY,distanceZ\n");
          	for(int i = 0; i < distancesX.length; i++){
          		writer.write(csvF.format(timestamp.get(i)) +","+distancesX[i]+","+distancesY[i]+","+distancesZ[i]+"\n");
          	}
			writer.flush();
			writer.close();
			writer = null;
			
            System.out.println("\nDistances file saved to '"+rawDataPath +sdf.format(resultdate)+ "_Distances.csv"+"'");
            
            //Release memory
          	distancesX = null;
          	distancesY = null;
          	distancesZ = null;
            //*********END DISTANCE CALCULATION*********

            //*********START PEAK PIKING CALCULATION*********
          	double deltaT = 1/(double)samplingRate;

            System.out.println("\n" + dtf.format(LocalDateTime.now()) + ": Calculating frequencies");
			// Calculating the frequency spectrum of the X-axis data
			FrequencySpectrum fSpec = new FrequencySpectrum(observationX.stream().mapToDouble(d -> d).toArray(), deltaT);
			// Performing the Peak picking of the frequency spectrum
			PeakPicking pp = new PeakPicking(numberOfPeaks, fSpec);
			// Extracting and saving frequencies, amplitudes and peaks
			double [] freqsX = fSpec.getFrequencies();
			double [] amplitudesX = fSpec.getAmplitudeSpectrum();
			int[] detectedPeaksX = pp.getPeaks();
			
			// Calculating the frequency spectrum of the Y-axis data
			fSpec = new FrequencySpectrum(observationY.stream().mapToDouble(d -> d).toArray(), deltaT);
			// Performing the Peak picking of the frequency spectrum
			pp = new PeakPicking(numberOfPeaks, fSpec);
			// Extracting and saving frequencies, amplitudes and peaks
			double [] amplitudesY = fSpec.getAmplitudeSpectrum();
			int[] detectedPeaksY = pp.getPeaks();
			
			// Calculating the frequency spectrum of the Z-axis data
			fSpec = new FrequencySpectrum(observationZ.stream().mapToDouble(d -> d).toArray(), deltaT);
			// Performing the Peak picking of the frequency spectrum
			pp = new PeakPicking(numberOfPeaks, fSpec);
			// Extracting and saving frequencies, amplitudes and peaks
			double [] amplitudesZ = fSpec.getAmplitudeSpectrum();
			int[] detectedPeaksZ = pp.getPeaks();

            System.out.println(String.format("Peaks detected for X axis are in the indexes: %s", Arrays.toString(detectedPeaksX)));
            System.out.println(String.format("Peaks detected for Y axis are in the indexes: %s", Arrays.toString(detectedPeaksY)));
            System.out.println(String.format("Peaks detected for Z axis are in the indexes: %s", Arrays.toString(detectedPeaksZ)));

            //Storing frequencies and amplitudes into a file in ./Results/
            writer = new FileWriter(rawDataPath +sdf.format(resultdate)+ "_Frequencies.csv");
            writer.write("frequency,amplitudeX,amplitudeY,amplitudeZ\n");
          	for(int i = 0; i < amplitudesX.length; i++){
          		writer.write(freqsX[i]+","+amplitudesX[i]+","+amplitudesY[i]+","+amplitudesZ[i]+"\n");
          	}
			writer.flush();
			writer.close();
			writer = null;
			
            System.out.println("\nFrequencies file saved to '"+rawDataPath +sdf.format(resultdate)+ "_Frequencies.csv"+"'");
            
            //Release memory
            fSpec = null;
            pp = null;
            freqsX = null;
            amplitudesX = null;
            detectedPeaksX = null;
            amplitudesY = null;
            detectedPeaksY = null;
            amplitudesZ = null;
            detectedPeaksZ = null;
			
            //*********END PEAK PIKING CALCULATION*********                  	
       	
          	br = null;
          	lineValues = null;
          	writer = null;

        } catch (Exception e) {
			e.printStackTrace();
		}
        
        //Release memory
      	observationX = null;
      	observationY = null;
      	observationZ = null;
        rawDataPath =null;
        dtf = null;
        sdf = null;
        timestampF = null;
        resultdate = null;
        System.gc();
	}
}
