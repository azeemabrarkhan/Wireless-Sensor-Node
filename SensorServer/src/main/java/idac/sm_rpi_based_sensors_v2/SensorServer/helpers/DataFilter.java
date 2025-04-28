package idac.sm_rpi_based_sensors_v2.SensorServer.helpers;

public class DataFilter {
    double[] rawData;
	double[] filteredData;
        private IirFilterCoefficients coeffs;
	
	public DataFilter(double[] inputData, double cutoffFreq1, double cutoffFreq2, String filterType) {
		rawData = inputData;
		filteredData = applyButterworth(cutoffFreq1, cutoffFreq2, filterType);
	}
	
	public double[] getFilteredData() {
		return filteredData;
	}

	
	private double [] applyButterworth(double cutoffFreq1, double cutoffFreq2, String filterType) {
            int filterOrder = 4;
            coeffs = IirFilterDesignExstrom.design(filterType, filterOrder, cutoffFreq1, cutoffFreq2);
            double filt_dat [] = new double [rawData.length];
	    IirFilter butterworth = new IirFilter(coeffs);
            for (int i = 0; i < filt_dat.length; i++){
                filt_dat [i] = butterworth.step(rawData[i]);
            }
            return filt_dat;
	}


}
