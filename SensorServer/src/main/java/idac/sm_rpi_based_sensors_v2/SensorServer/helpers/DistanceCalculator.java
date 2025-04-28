package idac.sm_rpi_based_sensors_v2.SensorServer.helpers;

public class DistanceCalculator {

    //Frequency limits for the filtering
	double cornerFreq1, cornerFreq2, cornerFreq3;
	
	int samplingRate;
	double gamma, beta;
	
	public DistanceCalculator(double cornerFreq1, double cornerFreq2, double cornerFreq3, int samplingRate, double gamma, double beta) {
		this.cornerFreq1 = cornerFreq1;
		this.cornerFreq2 = cornerFreq2;
		this.cornerFreq3 = cornerFreq3;
		this.samplingRate = samplingRate;
		this.beta = beta;
		this.gamma = gamma;		
	}
	
	public double[] calculateDistances(double[] data) {
        //Normalize the frequencies to the sampling rate. The reason for this division is that the filtering library only takes frequencies normalized according to the sampling frequency
        double[] cutoffFreqs = {
        		cornerFreq1 / samplingRate, 
        		cornerFreq2 / samplingRate, 
        		cornerFreq3 / samplingRate
        };

        //Initialize integration class with acceleration data and parameters necessary for obtaining displacements
        Integration integ = new Integration(gamma, beta, data, (double) 1 / (double) samplingRate, cutoffFreqs);

        //Perfom integrations to transform accelerations into velocities and then displacements            
        double [] displacements = integ.getNewmark()[2];
		
		return displacements;
	}
	
	public double calculateMaxDistance(double[] displacements) {

        //Calculate max displacements
        double maxDispl = 0;
        
        for (int i = 0; i < displacements.length; i++){
            if (Math.abs(maxDispl) < Math.abs(displacements[i])){
                maxDispl = displacements[i];
            }
        }
        return maxDispl;
	}
}
