package idac.sm_rpi_based_sensors_v2.SensorServer.helpers;

public class Integration {
     private final double gamma;
     private final double beta;
     private final double timeStep;
     private final double [] cutoffFreqs;
     private final double [] accelerations;
    
    public Integration(double g, double b, double [] inData, double dt, double [] f_cut){
        gamma = g;
        beta = b;
        accelerations = inData;
        cutoffFreqs = f_cut;
        timeStep = dt;
    }

    
    public double [][] getNewmark(){
        
        return applyNewmarkIntegration();
        
    }

    private double [][] applyNewmarkIntegration(){
        
        double Integrated_Data[][] = new double[3][accelerations.length];
        Integrated_Data[1][0] = 0;
        Integrated_Data[2][0] = 0;
        
        //Before integrating the accelerations, run the accelerations though a low pass filter (we are using the 25Hz limit here...)
        DataFilter filter1 = new DataFilter(accelerations, cutoffFreqs[0], 0, "lowpass");
        double [] accs_filtered = filter1.getFilteredData();
        Integrated_Data[0] = accs_filtered;

        //Integrate accelerations to obtain velocities
        for (int j = 1; j < Integrated_Data[1].length; j++) {

            Integrated_Data[1][j] = Integrated_Data[1][j - 1] + (1 - gamma) * timeStep * accs_filtered[j - 1] + gamma * timeStep * accs_filtered[j];

        }
        
        //After obtaining the velocities and before integrating them to obtain the displacements, run the velocities through a high pass filter (we are using the 0.4Hz and the near Nyquist frequency limits here...)
        //We are using a band pass filter instead of a high pass filter. The reason for that is that the high pass filter doesn't work as expected, so, instead, 
        //we use a "customized" band pass filter to behave like a high pass filter.
        DataFilter filter2 = new DataFilter(Integrated_Data[0], cutoffFreqs[1], cutoffFreqs[2], "bandpass");
        Integrated_Data [1] = filter2.getFilteredData();
        
        //Integrate velocities to obtain displacements
        for (int j = 1; j < Integrated_Data[2].length; j++) {

            Integrated_Data[2][j] = Integrated_Data[2][j - 1] + Integrated_Data[1][j - 1] * timeStep + ((1 - 2 * beta) / 2) * Math.pow(timeStep, 2) * accs_filtered[j - 1]
                    + beta * Math.pow(timeStep, 2) * accs_filtered[j];
        }
        
        return Integrated_Data; 
    }
}
