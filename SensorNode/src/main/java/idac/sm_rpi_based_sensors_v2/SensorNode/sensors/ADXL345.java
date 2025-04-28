package idac.sm_rpi_based_sensors_v2.SensorNode.sensors;

import com.google.gson.Gson;
import com.pi4j.io.i2c.I2CFactory;

//import idac.sm_rpi_based_sensors_v2.SensorNode.actuators.RGB_LED_Controller;
import idac.sm_rpi_based_sensors_v2.SensorNode.fourier.FrequencySpectrum;
import idac.sm_rpi_based_sensors_v2.SensorNode.fourier.PeakPicking;
import idac.sm_rpi_based_sensors_v2.pojos.Feature;
import idac.sm_rpi_based_sensors_v2.pojos.Observation;
import idac.sm_rpi_based_sensors_v2.pojos.ObservationPair;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ADXL345 extends Sensor {

	private int secondsMeasuring, samplingRate, numberOfPeaks, lengthOfDataset, measureEveryNSeconds;

	/**
	 * Address of the ADXL345 with the ALT pin tied to GND (low).
	 */
	public static final byte ADXL345_ADDRESS_ALT_LOW  = 0x53;

	/**
	 * Address of the ADXL345 with the ALT pin tied to VCC (high).
	 */
	public static final byte ADXL345_ADDRESS_ALT_HIGH = 0x1D;

	public static final byte ADXL345_DEFAULT_ADDRESS  = ADXL345_ADDRESS_ALT_LOW;

	public static final byte ADXL345_REGISTER_DEVID          = 0x00;
	public static final byte ADXL345_REGISTER_RESERVED1      = 0x01;
	public static final byte ADXL345_REGISTER_THRESH_TAP     = 0x1D;
	public static final byte ADXL345_REGISTER_OFSX           = 0x1E;
	public static final byte ADXL345_REGISTER_OFSY           = 0x1F;
	public static final byte ADXL345_REGISTER_OFSZ           = 0x20;
	public static final byte ADXL345_REGISTER_DUR            = 0x21;
	public static final byte ADXL345_REGISTER_LATENT         = 0x22;
	public static final byte ADXL345_REGISTER_WINDOW         = 0x23;
	public static final byte ADXL345_REGISTER_THRESH_ACT     = 0x24;
	public static final byte ADXL345_REGISTER_THRESH_INACT   = 0x25;
	public static final byte ADXL345_REGISTER_TIME_INACT     = 0x26;
	public static final byte ADXL345_REGISTER_ACT_INACT_CTL  = 0x27;
	public static final byte ADXL345_REGISTER_THRESH_FF      = 0x28;
	public static final byte ADXL345_REGISTER_TIME_FF        = 0x29;
	public static final byte ADXL345_REGISTER_TAP_AXES       = 0x2A;
	public static final byte ADXL345_REGISTER_ACT_TAP_STATUS = 0x2B;
	public static final byte ADXL345_REGISTER_BW_RATE        = 0x2C;
	public static final byte ADXL345_REGISTER_POWER_CTL      = 0x2D;
	public static final byte ADXL345_REGISTER_INT_ENABLE     = 0x2E;
	public static final byte ADXL345_REGISTER_INT_MAP        = 0x2F;
	public static final byte ADXL345_REGISTER_INT_SOURCE     = 0x30;
	public static final byte ADXL345_REGISTER_DATA_FORMAT    = 0x31;
	public static final byte ADXL345_REGISTER_DATAX0         = 0x32;
	public static final byte ADXL345_REGISTER_DATAX1         = 0x33;
	public static final byte ADXL345_REGISTER_DATAY0         = 0x34;
	public static final byte ADXL345_REGISTER_DATAY1         = 0x35;
	public static final byte ADXL345_REGISTER_DATAZ0         = 0x36;
	public static final byte ADXL345_REGISTER_DATAZ1         = 0x37;
	public static final byte ADXL345_REGISTER_FIFO_CTL       = 0x38;
	public static final byte ADXL345_REGISTER_FIFO_STATUS    = 0x39;

	public static final byte ADXL345_AIC_ACT_AC_BIT          = 7;
	public static final byte ADXL345_AIC_ACT_X_BIT           = 6;
	public static final byte ADXL345_AIC_ACT_Y_BIT           = 5;
	public static final byte ADXL345_AIC_ACT_Z_BIT           = 4;
	public static final byte ADXL345_AIC_INACT_AC_BIT        = 3;
	public static final byte ADXL345_AIC_INACT_X_BIT         = 2;
	public static final byte ADXL345_AIC_INACT_Y_BIT         = 1;
	public static final byte ADXL345_AIC_INACT_Z_BIT         = 0;

	public static final byte ADXL345_TAPAXIS_SUP_BIT         = 3;
	public static final byte ADXL345_TAPAXIS_X_BIT           = 2;
	public static final byte ADXL345_TAPAXIS_Y_BIT           = 1;
	public static final byte ADXL345_TAPAXIS_Z_BIT           = 0;

	public static final byte ADXL345_TAPSTAT_ACTX_BIT        = 6;
	public static final byte ADXL345_TAPSTAT_ACTY_BIT        = 5;
	public static final byte ADXL345_TAPSTAT_ACTZ_BIT        = 4;
	public static final byte ADXL345_TAPSTAT_ASLEEP_BIT      = 3;
	public static final byte ADXL345_TAPSTAT_TAPX_BIT        = 2;
	public static final byte ADXL345_TAPSTAT_TAPY_BIT        = 1;
	public static final byte ADXL345_TAPSTAT_TAPZ_BIT        = 0;

	public static final byte ADXL345_BW_LOWPOWER_BIT         = 4;
	public static final byte ADXL345_BW_RATE_BIT             = 3;
	public static final byte ADXL345_BW_RATE_LENGTH          = 4;

	public static final byte ADXL345_RATE_3200               = 0b1111;
	public static final byte ADXL345_RATE_1600               = 0b1110;
	public static final byte ADXL345_RATE_800                = 0b1101;
	public static final byte ADXL345_RATE_400                = 0b1100;
	public static final byte ADXL345_RATE_200                = 0b1011;
	public static final byte ADXL345_RATE_100                = 0b1010;
	public static final byte ADXL345_RATE_50                 = 0b1001;
	public static final byte ADXL345_RATE_25                 = 0b1000;
	public static final byte ADXL345_RATE_12P5               = 0b0111;
	public static final byte ADXL345_RATE_6P25               = 0b0110;
	public static final byte ADXL345_RATE_3P13               = 0b0101;
	public static final byte ADXL345_RATE_1P56               = 0b0100;
	public static final byte ADXL345_RATE_0P78               = 0b0011;
	public static final byte ADXL345_RATE_0P39               = 0b0010;
	public static final byte ADXL345_RATE_0P20               = 0b0001;
	public static final byte ADXL345_RATE_0P10               = 0b0000;

	public static final byte ADXL345_PCTL_LINK_BIT           = 5;
	public static final byte ADXL345_PCTL_AUTOSLEEP_BIT      = 4;
	public static final byte ADXL345_PCTL_MEASURE_BIT        = 3;
	public static final byte ADXL345_PCTL_SLEEP_BIT          = 2;
	public static final byte ADXL345_PCTL_WAKEUP_BIT         = 1;
	public static final byte ADXL345_PCTL_WAKEUP_LENGTH      = 2;

	public static final byte ADXL345_WAKEUP_8HZ              = 0b00;
	public static final byte ADXL345_WAKEUP_4HZ              = 0b01;
	public static final byte ADXL345_WAKEUP_2HZ              = 0b10;
	public static final byte ADXL345_WAKEUP_1HZ              = 0b11;

	public static final byte ADXL345_INT_DATA_READY_BIT      = 7;
	public static final byte ADXL345_INT_SINGLE_TAP_BIT      = 6;
	public static final byte ADXL345_INT_DOUBLE_TAP_BIT      = 5;
	public static final byte ADXL345_INT_ACTIVITY_BIT        = 4;
	public static final byte ADXL345_INT_INACTIVITY_BIT      = 3;
	public static final byte ADXL345_INT_FREE_FALL_BIT       = 2;
	public static final byte ADXL345_INT_WATERMARK_BIT       = 1;
	public static final byte ADXL345_INT_OVERRUN_BIT         = 0;

	public static final byte ADXL345_FORMAT_SELFTEST_BIT     = 7;
	public static final byte ADXL345_FORMAT_SPIMODE_BIT      = 6;
	public static final byte ADXL345_FORMAT_INTMODE_BIT      = 5;
	public static final byte ADXL345_FORMAT_FULL_RES_BIT     = 3;
	public static final byte ADXL345_FORMAT_JUSTIFY_BIT      = 2;
	public static final byte ADXL345_FORMAT_RANGE_BIT        = 1;
	public static final byte ADXL345_FORMAT_RANGE_LENGTH     = 2;

	public static final byte ADXL345_RANGE_2G                = 0b00;
	public static final byte ADXL345_RANGE_4G                = 0b01;
	public static final byte ADXL345_RANGE_8G                = 0b10;
	public static final byte ADXL345_RANGE_16G               = 0b11;

	public static final byte ADXL345_FIFO_MODE_BIT           = 7;
	public static final byte ADXL345_FIFO_MODE_LENGTH        = 2;
	public static final byte ADXL345_FIFO_TRIGGER_BIT        = 5;
	public static final byte ADXL345_FIFO_SAMPLES_BIT        = 4;
	public static final byte ADXL345_FIFO_SAMPLES_LENGTH     = 5;

	public static final byte ADXL345_FIFO_MODE_BYPASS        = 0b00;
	public static final byte ADXL345_FIFO_MODE_FIFO          = 0b01;
	public static final byte ADXL345_FIFO_MODE_STREAM        = 0b10;
	public static final byte ADXL345_FIFO_MODE_TRIGGER       = 0b11;

	public static final byte ADXL345_FIFOSTAT_TRIGGER_BIT    = 7;
	public static final byte ADXL345_FIFOSTAT_LENGTH_BIT     = 5;
	public static final byte ADXL345_FIFOSTAT_LENGTH_LENGTH  = 6;

	public enum AXIS {
		X(0),
		Y(1),
		Z(2);

		private final int value;

		AXIS(final int newValue) {
			value = newValue;
		}

		public int getValue() { return value; }
	}

	/**
	 * The G range that the device is operating in.
	 * 
	 * 0x0 = +/- 2G
	 * 0x1 = +/- 4G
	 * 0x2 = +/- 6G
	 * 0x3 = +/- 8G
	 * 
	 * This value will be out of sync with the device until a read or write of
	 * the range occurs.
	 */
	private int range;

	/**
	 * Full resolution mode toggle.
	 * 
	 * This value will be out of sync with the device until a read or write of
	 * the full resolution bit occurs.
	 */
	private boolean resolution;

	public ADXL345(int bus) {
		this(bus, ADXL345_DEFAULT_ADDRESS);
	}

	/**
	 * Specific address constructor.
	 * 
	 * @param address I2C address
	 */
	public ADXL345(int bus, int address) {
		super(bus, address);
		BUFFER = new byte[6];
	}

	public int getSecondsMeasuring() {
		return secondsMeasuring;
	}

	public void setSecondsMeasuring(int secondsMeasuring) {
		this.secondsMeasuring = secondsMeasuring;
	}

	public int getSamplingRate() {
		return samplingRate;
	}

	public void setSamplingRate(int samplingRate) {
		this.samplingRate = samplingRate;
	}

	public int getNumberOfPeaks() {
		return numberOfPeaks;
	}

	public void setNumberOfPeaks(int numberOfPeaks) {
		this.numberOfPeaks = numberOfPeaks;
	}

	public int getMeasureEveryNSeconds() {
		return measureEveryNSeconds;
	}

	public void setMeasureEveryNSeconds(int measureEveryNSeconds) {
		this.measureEveryNSeconds = measureEveryNSeconds;
	}

	/**
	 * Setup the sensor for general usage.
	 * 
	 * @throws IOException 
	 */
	public void setup() throws IOException, I2CFactory.UnsupportedBusNumberException {
		this.lengthOfDataset = samplingRate * secondsMeasuring;
		// http://pi4j.com/example/control.html
		device = I2CFactory.getInstance(i2cBus).getDevice(devAddr);
		System.out.println(device.getAddress());

		device.write(ADXL345_REGISTER_POWER_CTL, (byte) 0); // reset power settings
		/*i2c.write(ADXL345_REGISTER_FIFO_CTL, (byte) 0b10001111); //FIFO Mode to STREAM
		i2c.write(ADXL345_REGISTER_ACT_INACT_CTL, (byte) 0); //
		i2c.write(ADXL345_REGISTER_INT_ENABLE, (byte) 0); //disable interrupts
		i2c.write(ADXL345_REGISTER_TAP_AXES, (byte) 0); //FIFO Mode to STREAM*/
		readFullResolution();
		readRange();
		writeMeasureEnabled(true);
	}

	/**
	 * Verify the device ID.
	 * 
	 * @return True if device ID is valid, false otherwise
	 * @throws IOException 
	 */
	public boolean verifyDeviceID() throws IOException {
		return readDeviceID() == 0xE5;
	}

	/**
	 * Reads the device ID.
	 * 
	 * Register 0x00 -- DEVID (Read Only)
	 * 
	 * The DEVID register holds a fixed device ID code of 0xE5 (345 octal).
	 * 
	 * @return Device ID
	 * @throws IOException 
	 */
	public int readDeviceID() throws IOException {
		return device.read(ADXL345_REGISTER_DEVID);
	}

	/**
	 * Read axis offsets.
	 * 
	 * Register 0x1E, 0x1F, 0x20 -- OFSX, OFSY, OFSZ (Read/Write)
	 * 
	 * The OFSX, OFSY, and OFSZ registers are each eight bits and offer user-set
	 * offset adjustments in twos complement format with a scale factor of 15.6
	 * mg/LSB (that is, 0x7F = 2 g). The value stored in the offset registers is
	 * automatically added to the acceleration data, and the resulting value is
	 * stored in the output data registers. For additional information regarding
	 * offset calibration and the use of the offset registers, refer to the
	 * Offset Calibration section.
	 * 
	 * @param offsets Array of axis offsets whereas index 0 = x, 1 = y, 2 = z
	 * @throws IOException
	 */
	public void readOffsets(short[] offsets) throws IOException {
		if (offsets == null)
			throw new NullPointerException("Offsets array cannot be null");
		if (offsets.length != 3)
			throw new IllegalArgumentException("Offset array must have a length of 3");

		int offset = 0;
		int size = 3;
		device.read(ADXL345_REGISTER_OFSX, BUFFER, offset, size);
		offsets[0] = (short) (BUFFER[0] & 0xFF);
		offsets[1] = (short) (BUFFER[1] & 0xFF);
		offsets[2] = (short) (BUFFER[2] & 0xFF);
	}

	/**
	 * Write axis offsets.
	 * 
	 * Register 0x1E, 0x1F, 0x20 -- OFSX, OFSY, OFSZ (Read/Write)
	 * 
	 * The OFSX, OFSY, and OFSZ registers are each eight bits and offer user-set
	 * offset adjustments in twos complement format with a scale factor of 15.6
	 * mg/LSB (that is, 0x7F = 2 g). The value stored in the offset registers is
	 * automatically added to the acceleration data, and the resulting value is
	 * stored in the output data registers. For additional information regarding
	 * offset calibration and the use of the offset registers, refer to the
	 * Offset Calibration section.
	 * 
	 * @param offsets Array of axis offsets whereas index 0 = x, 1 = y, 2 = z
	 * @throws IOException
	 */
	public void writeOffsets(short[] offsets) throws IOException {
		if (offsets == null)
			throw new NullPointerException("Offsets array cannot be null");
		if (offsets.length != 3)
			throw new IllegalArgumentException("Offset array must have a length of 3");

		device.write(ADXL345_REGISTER_OFSX, (byte) (offsets[0] & 0xFF));
		device.write(ADXL345_REGISTER_OFSY, (byte) (offsets[1] & 0xFF));
		device.write(ADXL345_REGISTER_OFSZ, (byte) (offsets[2] & 0xFF));
	}

	/**
	 * Read the device bandwidth rate.
	 * 
	 * Register 0x2C -- BW_RATE (Read/Write)
	 * 
	 * These bits select the device bandwidth and output data rate (see Table 7
	 * and Table 8 for details). The default value is 0x0A, which translates to
	 * a 100 Hz output data rate. An output data rate should be selected that is
	 * appropriate for the communication protocol and frequency selected.
	 * Selecting too high of an output data rate with a low communication speed
	 * results in samples being discarded.
	 * 
	 * <pre>
	 * Table 7. Typical Current Consumption vs. Data Rate
	 * Output Data Rate (Hz) | Bandwidth (Hz) | Rate Code |   Value   | Idd (uA)
	 *          3200         |      1600      |    1111   | 0xF or 15 |   140
	 *          1600         |       800      |    1110   | 0xE or 14 |    90
	 *           800         |       400      |    1101   | 0xD or 13 |   140
	 *           400         |       200      |    1100   | 0xC or 12 |   140
	 *           200         |       100      |    1011   | 0xB or 11 |   140
	 *           100         |        50      |    1010   | 0xA or 10 |   140
	 *            50         |        25      |    1001   | 0x9 or 9  |    90
	 *            25         |      12.5      |    1000   | 0x8 or 8  |    60
	 *          12.5         |      6.25      |    0111   | 0x7 or 7  |    50
	 *          6.25         |      3.13      |    0110   | 0x6 or 6  |    45
	 *          3.13         |      1.56      |    0101   | 0x5 or 5  |    40
	 *          1.56         |      0.78      |    0100   | 0x4 or 4  |    34
	 *          0.78         |      0.39      |    0011   | 0x3 or 3  |    23
	 *          0.39         |      0.20      |    0010   | 0x2 or 2  |    23
	 *          0.20         |      0.10      |    0001   | 0x1 or 1  |    23
	 *          0.10         |      0.05      |    0000   | 0x0 or 0  |    23
	 * </pre>
	 * 
	 * @return Data rate (0 to 15)
	 * @throws IOException
	 */
	public int readRate() throws IOException {
		return I2Cdev.readBits(device, ADXL345_REGISTER_BW_RATE, ADXL345_BW_RATE_BIT, ADXL345_BW_RATE_LENGTH);
	}

	/**
	 * Write the device bandwidth rate.
	 * 
	 * Register 0x2C -- BW_RATE (Read/Write)
	 * 
	 * These bits select the device bandwidth and output data rate (see Table 7
	 * and Table 8 for details). The default value is 0x0A, which translates to
	 * a 100 Hz output data rate. An output data rate should be selected that is
	 * appropriate for the communication protocol and frequency selected.
	 * Selecting too high of an output data rate with a low communication speed
	 * results in samples being discarded.
	 * 
	 * <pre>
	 * Table 7. Typical Current Consumption vs. Data Rate
	 * Output Data Rate (Hz) | Bandwidth (Hz) | Rate Code |   Value   | Idd (uA)
	 *          3200         |      1600      |    1111   | 0xF or 15 |   140
	 *          1600         |       800      |    1110   | 0xE or 14 |    90
	 *           800         |       400      |    1101   | 0xD or 13 |   140
	 *           400         |       200      |    1100   | 0xC or 12 |   140
	 *           200         |       100      |    1011   | 0xB or 11 |   140
	 *           100         |        50      |    1010   | 0xA or 10 |   140
	 *            50         |        25      |    1001   | 0x9 or 9  |    90
	 *            25         |      12.5      |    1000   | 0x8 or 8  |    60
	 *          12.5         |      6.25      |    0111   | 0x7 or 7  |    50
	 *          6.25         |      3.13      |    0110   | 0x6 or 6  |    45
	 *          3.13         |      1.56      |    0101   | 0x5 or 5  |    40
	 *          1.56         |      0.78      |    0100   | 0x4 or 4  |    34
	 *          0.78         |      0.39      |    0011   | 0x3 or 3  |    23
	 *          0.39         |      0.20      |    0010   | 0x2 or 2  |    23
	 *          0.20         |      0.10      |    0001   | 0x1 or 1  |    23
	 *          0.10         |      0.05      |    0000   | 0x0 or 0  |    23
	 * </pre>
	 * 
	 * @param rate Data rate (0 to 15)
	 * @throws IOException
	 */
	public void writeRate(int rate) throws IOException {
		I2Cdev.writeBits(device, ADXL345_REGISTER_BW_RATE, ADXL345_BW_RATE_BIT, ADXL345_BW_RATE_LENGTH, rate);
	}

	/**
	 * Read measurement enabled status.
	 * 
	 * Register 0x2D -- POWER_CTL (Read/Write)
	 * 
	 * A setting of 0 in the measure bit places the part into standby mode, and
	 * a setting of 1 places the part into measurement mode. The ADXL345 powers
	 * up in standby mode with minimum power consumption.
	 * 
	 * @return Measurement enabled status
	 * @throws IOException
	 */
	public boolean readMeasureEnabled() throws IOException {
		return I2Cdev.readBit(device, ADXL345_REGISTER_POWER_CTL, ADXL345_PCTL_MEASURE_BIT);
	}

	/**
	 * Write measurement enabled status.
	 * 
	 * Register 0x2D -- POWER_CTL (Read/Write)
	 * 
	 * A setting of 0 in the measure bit places the part into standby mode, and
	 * a setting of 1 places the part into measurement mode. The ADXL345 powers
	 * up in standby mode with minimum power consumption.
	 * 
	 * @param enabled Measurement enabled status
	 * @throws IOException
	 */
	public void writeMeasureEnabled(boolean enabled) throws IOException {
		I2Cdev.writeBit(device, ADXL345_REGISTER_POWER_CTL, ADXL345_PCTL_MEASURE_BIT, enabled);
	}

	/**
	 * Read full resolution mode setting.
	 * 
	 * Register 0x31 -- DATA_FORMAT (Read/Write)
	 * 
	 * When this bit is set to a value of 1, the device is in full resolution
	 * mode, where the output resolution increases with the g range set by the
	 * range bits to maintain a 4 mg/LSB scale factor. When the FULL_RES bit is
	 * set to 0, the device is in 10-bit mode, and the range bits determine the
	 * maximum g range and scale factor.
	 * 
	 * @return Full resolution enabled setting
	 * @throws IOException
	 */
	public boolean readFullResolution() throws IOException {
		resolution = I2Cdev.readBit(device, ADXL345_REGISTER_DATA_FORMAT, ADXL345_FORMAT_FULL_RES_BIT);
		return resolution;
	}

	/**
	 * Write full resolution mode setting.
	 * 
	 * Register 0x31 -- DATA_FORMAT (Read/Write)
	 * 
	 * When this bit is set to a value of 1, the device is in full resolution
	 * mode, where the output resolution increases with the g range set by the
	 * range bits to maintain a 4 mg/LSB scale factor. When the FULL_RES bit is
	 * set to 0, the device is in 10-bit mode, and the range bits determine the
	 * maximum g range and scale factor.
	 * 
	 * @param resolution Full resolution enabled setting
	 * @throws IOException
	 */
	public void writeFullResolution(boolean resolution) throws IOException {
		this.resolution = resolution;
		I2Cdev.writeBit(device, ADXL345_REGISTER_DATA_FORMAT, ADXL345_FORMAT_FULL_RES_BIT, resolution);
	}

	/**
	 * Read data range setting.
	 * 
	 * Register 0x31 -- DATA_FORMAT (Read/Write)
	 * 
	 * These bits set the g range as described in Table 21.
	 * 
	 * <pre>
	 * Table 21. g Range Setting
	 * | Setting |  g Range | Value |
	 * |    00   | +/-  2 g |   0   |
	 * |    01   | +/-  4 g |   1   |
	 * |    10   | +/-  8 g |   2   |
	 * |    11   | +/- 16 g |   3   |
	 * </pre>
	 * 
	 * @return Range value (0 to 3)
	 * @throws IOException 
	 */
	public int readRange() throws IOException {
		range = I2Cdev.readBits(device, ADXL345_REGISTER_DATA_FORMAT, ADXL345_FORMAT_RANGE_BIT, ADXL345_FORMAT_RANGE_LENGTH);
		return range;
	}

	/**
	 * Write data range setting.
	 * 
	 * Register 0x31 -- DATA_FORMAT (Read/Write)
	 * 
	 * These bits set the g range as described in Table 21.
	 * 
	 * <pre>
	 * Table 21. g Range Setting
	 * | Setting |  g Range | Value |
	 * |    00   | +/-  2 g |   0   |
	 * |    01   | +/-  4 g |   1   |
	 * |    10   | +/-  8 g |   2   |
	 * |    11   | +/- 16 g |   3   |
	 * </pre>
	 * 
	 * @param range Range value (0 to 3)
	 * @throws IOException 
	 */
	public void writeRange(int range) throws IOException {
		this.range = range;
		I2Cdev.writeBits(device, ADXL345_REGISTER_DATA_FORMAT, ADXL345_FORMAT_RANGE_BIT, ADXL345_FORMAT_RANGE_LENGTH, range);
	}

	/**
	 * Read raw acceleration values.
	 * 
	 * Register 0x32 to Register 0x37 -- DATAX0, DATAX1, DATAY0, DATAY1, DATAZ0,
	 * DATAZ1 (Read Only)
	 * 
	 * These six bytes (Register 0x32 to Register 0x37) are eight bits each and
	 * hold the output data for each axis. Register 0x32 and Register 0x33 hold
	 * the output data for the x-axis, Register 0x34 and Register 0x35 hold the
	 * output data for the y-axis, and Register 0x36 and Register 0x37 hold the
	 * output data for the z-axis. The output data is twos complement, with
	 * DATAx0 as the least significant byte and DATAx1 as the most significant
	 * byte, where x represent X, Y, or Z. The DATA_FORMAT register (Address
	 * 0x31) controls the format of the data. It is recommended that a
	 * multiple-byte read of all registers be performed to prevent a change in
	 * data between reads of sequential registers.
	 * 
	 * @param raw Array of raw values whereas index 0 = x, 1 = y, 2 = z
	 * @throws IOException
	 */
	public void readRawAcceleration(short[] raw) throws IOException {
		if (raw.length != 3)
			throw new IllegalArgumentException("Acceleration array must have a length of 3");

		int offset = 0;
		int size = 6;
		device.read(ADXL345_REGISTER_DATAX0, BUFFER, offset, size);

		raw[0] = (short) ((BUFFER[1] << 8) | (BUFFER[0] & 0xFF)); // x
		raw[1] = (short) ((BUFFER[3] << 8) | (BUFFER[2] & 0xFF)); // y
		raw[2] = (short) ((BUFFER[5] << 8) | (BUFFER[4] & 0xFF)); // z
	}

	/**
	 * Determines the scaling factor of raw values to obtain Gs.
	 * 
	 * The scale factor changes dependent on other device settings, so be sure
	 * to get the scaling factor after writing desired settings.
	 * 
	 * https://www.sparkfun.com/tutorials/240
	 * 
	 * @return Raw value scaling factor
	 */
	public float getScalingFactor() {
		int bits = resolution ? 10 + range : 10;
		float gRange = 4f * (float) Math.pow(2, range);
		float bitRange = (float) Math.pow(2, bits);
		return gRange / bitRange;
	}

	/**
	 * This method re-samples data based on a given sampling frequency
	 * Along the vector of time, the time that correspond to the given sampling is searched,
	 * then, if the time is not exactly the one needed, interpolation is performed, for finding 
	 * the value a the specific needed point
	 * 
	 * Created by Stalin Ibanez 
	 * last version: 2019-07-10
	 */
	public double[][] getSamplingData(double dataAccRaw [][], long dataTime [], int samplingFrequency, int points, int lengthOfDataset, long sampledTime []){
		double mstime;
		double[] m1 = new double[3];
		double[] acc1 = new double[3];
		double[] timemili= new double[points];	

		double timestep = (1000/(double)samplingFrequency);
		//int totalpoints = (int)(accraw[vectorlength]/timestep);

		double[][] accsampled = new double[3][lengthOfDataset];

		for(int j=0;j<points;j++){
			timemili[j]=(dataTime[j]-dataTime[0]);
		}

		accsampled[0][0]=dataAccRaw[0][0];
		accsampled[1][0]=dataAccRaw[1][0];
		accsampled[2][0]=dataAccRaw[2][0];
		sampledTime[0] = dataTime[0];
		for(int i=1; i < lengthOfDataset ; i++){
			mstime=timestep*(double)i;
			for(int j=1;j<points;j++){
				if (mstime <= timemili[j] ) {
					m1[0] = (dataAccRaw[0][j]-dataAccRaw[0][j-1])/(timemili[j]-timemili[j-1]);
					acc1[0] = dataAccRaw[0][j-1] + m1[0]*(mstime-timemili[j-1]);
					m1[1] = (dataAccRaw[1][j]-dataAccRaw[1][j-1])/(timemili[j]-timemili[j-1]);
					acc1[1] = dataAccRaw[1][j-1] + m1[1]*(mstime-timemili[j-1]);
					m1[2] = (dataAccRaw[2][j]-dataAccRaw[2][j-1])/(timemili[j]-timemili[j-1]);
					acc1[2] = dataAccRaw[2][j-1] + m1[2]*(mstime-timemili[j-1]);
					break;
				}
			}
			accsampled[0][i]=acc1[0];
			accsampled[1][i]=acc1[1];
			accsampled[2][i]=acc1[2];	
			sampledTime[i] = (long)mstime + sampledTime[0];
		}	

		return accsampled;
	}

	public double[][] getBaseLineCorrection(double dataAcc [][]){         
		double[][] acc1 = dataAcc;
		double[] sum = new double[3];
		sum[0] = sum[1] = sum[2] = 0;

		for(int i=0;i<acc1[0].length;i++){
			sum[0]+=acc1[0][i]; 
			sum[1]+=acc1[1][i];
			sum[2]+=acc1[2][i];
		}	
		sum[0]=sum[0]/acc1[0].length;
		sum[1]=sum[1]/acc1[0].length;
		sum[2]=sum[2]/acc1[0].length;		

		for(int i=0;i<acc1[0].length;i++){
			acc1[0][i]=acc1[0][i]-sum[0];
			acc1[1][i]=acc1[1][i]-sum[1];
			acc1[2][i]=acc1[2][i]-sum[2]; 
		}	

		return acc1;
	}	

	public double[][] nextPow2vector(double dataVector [][]){         
		double[][] vector = dataVector;
		int value = vector[0].length;
		int highestOneBit = Integer.highestOneBit(value);

		if (value == highestOneBit) {
			return vector;
		}

		value = highestOneBit << 1;
		double[][] newVector = new double[3][value];

		for (int i = 0; i < vector[0].length; i++){
			newVector[0][i] = vector[0][i];
			newVector[1][i] = vector[1][i];
			newVector[2][i] = vector[2][i];
		}

		return newVector;
	}

	public void run() {
		sensor.setRunning(true);
		try
		{	
			//RGB_LED_Controller.setLedRunning();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH:mm:ss"),
				 	 csvF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			long date=System.currentTimeMillis();
			Date resultdate = new Date(date);
			
			short[] offsets = {0, 0, 9};

			// Step 2: Defining the maximum amplitude of the acceleration sensor 1: 2G, 4G, 8G
			this.writeRange(ADXL345.ADXL345_RANGE_4G);
			this.writeFullResolution(true);    
			this.writeOffsets(offsets);       	

			// step 3: Define here the maximum sampling rate possible by the sensors
			this.writeRate(ADXL345.ADXL345_RATE_1600);

			System.out.println(sdf.format(new Date(System.currentTimeMillis())) + ": ADXL345 sensor configured");

			Gson gson = new Gson();
			Feature featureAccX = new Feature("Acceleration X");
			Feature featureAccY = new Feature("Acceleration Y");
			Feature featureAccZ = new Feature("Acceleration Z");
			Feature featureCorrectedAccX = new Feature("Corrected acceleration X");
			Feature featureCorrectedAccY = new Feature("Corrected acceleration Y");
			Feature featureCorrectedAccZ = new Feature("Corrected acceleration Z");
			Feature featureFrequencyX = new Feature("Frequency X");
			Feature featureFrequencyY = new Feature("Frequency Y");
			Feature featureFrequencyZ = new Feature("Frequency Z");
			Feature featurePeakX = new Feature("Peak X");
			Feature featurePeakY = new Feature("Peak Y");
			Feature featurePeakZ = new Feature("Peak Z");

			int repetitionCount = 0;
	        while (repetitionCount < getRepetitions() || getRepetitions() == 0) {	        	
	        	Date loopDate = new Date(System.currentTimeMillis());
	        	
				//Get the scaling factor of the first sensor
				float scalingFactor = this.getScalingFactor();
				int factorMeasuringpoints = 800; //variable related with the size of the vector when sampling as fast as possible (empirical)
				int lengthOfDatasetRaw	= factorMeasuringpoints * secondsMeasuring;

				short[] raw = new short[3]; //Each value of the vector represent one direction of the acceleration		
				long[] timeinter = new long[lengthOfDatasetRaw];        
				double[][] x_accelerations_raw = new double[3][lengthOfDatasetRaw]; 		
				double[][] x_accelerations = new double[3][lengthOfDataset],
						x_accelerations_bl = new double[3][lengthOfDataset];		

				System.out.println(sdf.format(new Date(System.currentTimeMillis())) + ": ADXL345 sensor raw sampling starting");
				// sampling as fast as possible
				int aux = 0;        
				long t0 = System.currentTimeMillis();				
				while ( ((System.currentTimeMillis()-t0)) < secondsMeasuring*1000 && aux < lengthOfDatasetRaw) {
					this.readRawAcceleration(raw);	
					timeinter[aux] = System.currentTimeMillis();
					x_accelerations_raw[0][aux] = (double) raw[0]*scalingFactor;
					x_accelerations_raw[1][aux] = (double) raw[1]*scalingFactor;
					x_accelerations_raw[2][aux] = (double) raw[2]*scalingFactor;
					aux = aux+1;
				}
				System.out.println(sdf.format(new Date(System.currentTimeMillis())) + ": ADXL345 sensor raw sampling finished");

				ArrayList<Observation> rawObservationListX = new ArrayList<Observation>();
				ArrayList<Observation> rawObservationListY = new ArrayList<Observation>();
				ArrayList<Observation> rawObservationListZ = new ArrayList<Observation>();

				long[] timeSampled = new long[lengthOfDataset];

				System.out.println(sdf.format(new Date(System.currentTimeMillis())) + ": ADXL345 sensor resampling starting");
				//Resample data to requested sampling rate 
				x_accelerations = getSamplingData(x_accelerations_raw, timeinter, samplingRate, aux, lengthOfDataset, timeSampled);
				System.out.println(sdf.format(new Date(System.currentTimeMillis())) + ": ADXL345 sensor resampling finished");

				// Storing Data in RPI - not necessary in real implementations
				FileWriter writer = new FileWriter("/home/pi/Desktop/Data/"+"RawAcc_" + sdf.format(loopDate) + ".csv");
				writer.write("sensor_node_ID, campaignTimestamp, timestamp, X, Y, Z\n");
				for(int k = 0; k < x_accelerations[0].length; k++){
					writer.write(node.getName()+","+csvF.format(node.getCampaignTimestamp())+","+csvF.format(timeSampled[k])+ "," + 
							 	 x_accelerations[0][k]+ "," + x_accelerations[1][k]+ "," + x_accelerations[2][k] + "\n");

					rawObservationListX.add(new Observation(sensor, featureAccX, timeSampled[k], x_accelerations[0][k], t0));
					rawObservationListY.add(new Observation(sensor, featureAccY, timeSampled[k], x_accelerations[1][k], t0));
					rawObservationListZ.add(new Observation(sensor, featureAccZ, timeSampled[k], x_accelerations[2][k], t0));
				}
				writer.flush();
				writer.close();
				writer = null;

				System.out.println(sdf.format(new Date(System.currentTimeMillis())) + ": ADXL345 sensor baseline correction starting");
				// Removing offset by subtracting the mean value, i.e. baseline correction
				x_accelerations_bl = getBaseLineCorrection(x_accelerations);

				// Extending length of vector to a power of 2 (due to FFT)
				double[][] x_accelerations_bl_ext = nextPow2vector(x_accelerations_bl);		    	
				System.out.println(sdf.format(new Date(System.currentTimeMillis())) + ": ADXL345 sensor baseline correction finished");

				ArrayList<Observation> correctedObservationListX = new ArrayList<Observation>();
				ArrayList<Observation> correctedObservationListY = new ArrayList<Observation>();
				ArrayList<Observation> correctedObservationListZ = new ArrayList<Observation>();

				// Saving corrected data with filtering and sampling
				writer = new FileWriter("/home/pi/Desktop/Data/"+"CorrectedAcc_" + sdf.format(loopDate) + ".csv");
				writer.write("sensor_node_ID, campaignTimestamp, timestamp, X, Y, Z\n");
				for(int k = 0; k < timeSampled.length; k++){
					writer.write(node.getName()+","+csvF.format(node.getCampaignTimestamp())+","+csvF.format(timeSampled[k])+ "," + 
								 x_accelerations_bl_ext[0][k]+ "," + x_accelerations_bl_ext[1][k]+ "," + x_accelerations_bl_ext[2][k] + "\n");

					correctedObservationListX.add(new Observation(sensor, featureCorrectedAccX, timeSampled[k], x_accelerations_bl_ext[0][k], t0));
					correctedObservationListY.add(new Observation(sensor, featureCorrectedAccY, timeSampled[k], x_accelerations_bl_ext[1][k], t0));
					correctedObservationListZ.add(new Observation(sensor, featureCorrectedAccZ, timeSampled[k], x_accelerations_bl_ext[2][k], t0));
				}
				writer.flush();
				writer.close();
				writer = null;

				ArrayList<ObservationPair> frequencyListX = new ArrayList<ObservationPair>();
				ArrayList<ObservationPair> frequencyListY = new ArrayList<ObservationPair>();
				ArrayList<ObservationPair> frequencyListZ = new ArrayList<ObservationPair>();
				ArrayList<Observation> peakListX = new ArrayList<Observation>();
				ArrayList<Observation> peakListY = new ArrayList<Observation>();
				ArrayList<Observation> peakListZ = new ArrayList<Observation>();

				System.out.println(sdf.format(new Date(System.currentTimeMillis())) + ": ADXL345 sensor frequencies calculations starting");
				double deltaT = 1/(double)samplingRate;
				String axis = ""; 
				for(int i = 0; i < 3; ++i) {		    		
					switch(i) {
					case 0:
						axis = "X";
						break;
					case 1:
						axis = "Y";
						break;
					case 2:
						axis = "Z";
						break;
					}

					// Calculating the frequency spectrum of the stored data
					FrequencySpectrum fSpec = new FrequencySpectrum(x_accelerations_bl_ext[i], deltaT);
					// Performing the Peak picking of the frequency spectrum
					PeakPicking pp = new PeakPicking(numberOfPeaks, fSpec);
					int[] detectedPeaks = pp.getPeaks();

					// Extracting and saving frequencies and amplitudes (comparison reasons)
					double [] freqs = fSpec.getFrequencies();
					double [] amplitudes = fSpec.getAmplitudeSpectrum();

					writer = new FileWriter("/home/pi/Desktop/Data/"+"Frequencies" + axis + "_" + sdf.format(loopDate) + ".csv");
					writer.write("frequency,amplitude\n");
					for(int k = 0; k < amplitudes.length; k++){
						writer.write(freqs[k] + "\t" + amplitudes[k] + "\n");
						switch(i) {
						case 0:
							frequencyListX.add(new ObservationPair(sensor, featureFrequencyX, freqs[k], amplitudes[k], t0));
							break;
						case 1:
							frequencyListY.add(new ObservationPair(sensor, featureFrequencyY, freqs[k], amplitudes[k], t0));
							break;
						case 2:
							frequencyListZ.add(new ObservationPair(sensor, featureFrequencyZ, freqs[k], amplitudes[k], t0));
							break;
						}
					}
					writer.flush();
					writer.close();
					writer = null;

					writer = new FileWriter("/home/pi/Desktop/Data/"+"Peaks" + axis + "_" + sdf.format(loopDate) + ".csv");
					writer.write("peak\n");
					for(int k = 0; k < detectedPeaks.length; k++){
						writer.write(freqs[detectedPeaks[k]] + "\n");
						switch(i) {
						case 0:
							peakListX.add(new Observation(sensor, featurePeakX, date, freqs[detectedPeaks[k]], t0));
							break;
						case 1:
							peakListY.add(new Observation(sensor, featurePeakY, date, freqs[detectedPeaks[k]], t0));
							break;
						case 2:
							peakListZ.add(new Observation(sensor, featurePeakZ, date, freqs[detectedPeaks[k]], t0));
							break;
						}
					}
					writer.flush();
					writer.close();
					writer = null;

					fSpec = null;
					pp = null;
					detectedPeaks = null;
					freqs = null;
					amplitudes = null;
				}
				System.out.println(sdf.format(new Date(System.currentTimeMillis())) + ": ADXL345 sensor frequencies calculations finished");
				axis = null;

				HashMap<String, ArrayList<Observation>> rawAccelerationsMap = new HashMap<String, ArrayList<Observation>>();
				rawAccelerationsMap.put("X", rawObservationListX);
				rawAccelerationsMap.put("Y", rawObservationListY);
				rawAccelerationsMap.put("Z", rawObservationListZ);
				HashMap<String, ArrayList<Observation>> correctedAccelerationsMap = new HashMap<String, ArrayList<Observation>>();
				correctedAccelerationsMap.put("X", correctedObservationListX);
				correctedAccelerationsMap.put("Y", correctedObservationListY);
				correctedAccelerationsMap.put("Z", correctedObservationListZ);
				HashMap<String, ArrayList<ObservationPair>> frequencyMap = new HashMap<String, ArrayList<ObservationPair>>();
				frequencyMap.put("X", frequencyListX);
				frequencyMap.put("Y", frequencyListY);
				frequencyMap.put("Z", frequencyListZ);
				HashMap<String, ArrayList<Observation>> peaksMap = new HashMap<String, ArrayList<Observation>>();
				peaksMap.put("X", peakListX);
				peaksMap.put("Y", peakListY);
				peaksMap.put("Z", peakListZ);

				if(secondsMeasuring <= 120) {
					System.out.println(sdf.format(new Date(System.currentTimeMillis())) + ": ADXL345 publishing raw data");
					communication.publish("nodes/data/"+node.getName()+"/"+sensor.getType()+"/Accelerations", gson.toJson(rawAccelerationsMap));
					Thread.sleep(100);
					System.out.println(sdf.format(new Date(System.currentTimeMillis())) + ": ADXL345 publishing corrected data");
					communication.publish("nodes/data/"+node.getName()+"/"+sensor.getType()+"/Corrected accelerations", gson.toJson(correctedAccelerationsMap));
					Thread.sleep(100);
				}
				System.out.println(sdf.format(new Date(System.currentTimeMillis())) + ": ADXL345 publishing frequencies data");
				communication.publish("nodes/data/"+node.getName()+"/"+sensor.getType()+"/Frequencies", gson.toJson(frequencyMap));
				Thread.sleep(100);
				System.out.println(sdf.format(new Date(System.currentTimeMillis())) + ": ADXL345 publishing peaks data");
				communication.publish("nodes/data/"+node.getName()+"/"+sensor.getType()+"/Peaks", gson.toJson(peaksMap));

				//Release memory
				raw = null;		
				timeinter = null;        
				x_accelerations_raw = null; 		
				x_accelerations = null;
				x_accelerations_bl = null;	
				x_accelerations_bl_ext = null;

				rawAccelerationsMap.clear();
				correctedAccelerationsMap.clear();
				frequencyMap.clear();
				peaksMap.clear();
				rawAccelerationsMap = null;
				correctedAccelerationsMap = null;
				frequencyMap = null;
				peaksMap = null;

				rawObservationListX = null;
				rawObservationListY = null;
				rawObservationListZ = null;
				correctedObservationListX = null;
				correctedObservationListY = null;
				correctedObservationListZ = null;
				frequencyListX = null;
				frequencyListY = null;
				frequencyListZ = null;
				peakListX = null;
				peakListY = null;
				peakListZ = null;
              	
              	++repetitionCount;

				//RGB_LED_Controller.setLedWaiting();
              	if(measureEveryNSeconds > 0)
              		Thread.sleep(measureEveryNSeconds * 1000 - 300);
			}
			System.out.println(sdf.format(new Date(System.currentTimeMillis())) + ": ADXL345 measuring finished");
			sensor.setRunning(false);
	        
			//Not necessary to dispose this code as code is unreachable at the moment
			gson = null;
			featureAccX = null;
			featureAccY = null;
			featureAccZ = null;
			featureFrequencyX = null;
			featureFrequencyY = null;
			featureFrequencyZ = null;
			featurePeakX = null;
			featurePeakY = null;
			featurePeakZ = null;
			featureCorrectedAccX = null;
			featureCorrectedAccY = null;
			featureCorrectedAccZ = null;
	        sdf = null;
			resultdate = null;
			offsets = null;
		}
		catch (IOException ex) {
			//RGB_LED_Controller.setLedError();
			System.out.println(String.format("IO error during the process of ADXL345. Exiting. %s", ex.getMessage()));
		} catch (InterruptedException ex) {
			//RGB_LED_Controller.setLedError();
			System.out.println(String.format("Interruption error during the process of ADXL345. Exiting. %s", ex.getMessage()));
		}
	}

	public void runCalibration() {
		sensor.setRunning(true);
		try
		{	
			//RGB_LED_Controller.setLedRunning();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH:mm:ss");
			
			short[] offsets = {0, 0, 9};

			// Step 2: Defining the maximum amplitude of the acceleration sensor 1: 2G, 4G, 8G
			this.writeRange(ADXL345.ADXL345_RANGE_4G);
			this.writeFullResolution(true);    
			this.writeOffsets(offsets);

			// step 3: Define here the maximum sampling rate possible by the sensors
			this.writeRate(ADXL345.ADXL345_RATE_1600);

			System.out.println(sdf.format(new Date(System.currentTimeMillis())) + ": ADXL345 sensor configured");

			int repetitionCount = 0;
			short[] raw;
	        while (repetitionCount < getRepetitions() || getRepetitions() == 0) {		
				//Get the scaling factor of the first sensor
				float scalingFactor = this.getScalingFactor();

				raw = new short[3]; //Each value of the vector represent one direction of the acceleration		

				this.readRawAcceleration(raw);
				
				System.out.printf(sdf.format(new Date(System.currentTimeMillis())) + " - Acceleration X: %d - Y: %d - Z: %d %n", raw[0], raw[1], raw[2]);
				System.out.printf(sdf.format(new Date(System.currentTimeMillis())) + " - Acceleration scaled X: %.2f - Y: %.2f - Z: %.2f %n", (double) raw[0]*scalingFactor, (double) raw[1]*scalingFactor, (double) raw[2]*scalingFactor);

				//Release memory
				raw = null;		
              	
              	++repetitionCount;

				//RGB_LED_Controller.setLedWaiting();
              	if(measureEveryNSeconds > 0)
              		Thread.sleep(measureEveryNSeconds * 1000);
			}
	        sdf = null;
		}
		catch (IOException ex) {
			//RGB_LED_Controller.setLedError();
			System.out.println(String.format("IO error during the process of ADXL345. Exiting. %s", ex.getMessage()));
		} catch (InterruptedException ex) {
			//RGB_LED_Controller.setLedError();
			System.out.println(String.format("Interruption error during the process of ADXL345. Exiting. %s", ex.getMessage()));
		}
		sensor.setRunning(false);
	}

}