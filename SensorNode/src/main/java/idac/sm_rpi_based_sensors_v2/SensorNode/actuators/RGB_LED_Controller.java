package idac.sm_rpi_based_sensors_v2.SensorNode.actuators;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

//https://pi4j.com/1.2/example/control.html
public class RGB_LED_Controller {

	/**
	 * Number of the red pin of the LED
	 */
	public static final Pin PIN_RED  = RaspiPin.GPIO_16;
	/**
	 * Number of the blue pin of the LED
	 */
	public static final Pin PIN_BLUE  = RaspiPin.GPIO_20;
	/**
	 * Number of the green pin of the LED
	 */
	public static final Pin PIN_GREEN  = RaspiPin.GPIO_26;
	
	static GpioController gpio;
	static GpioPinDigitalOutput pinRed, pinBlue, pinGreen;
	
	static {
		try {
			// create gpio controller
	        gpio = GpioFactory.getInstance();

	        // provision gpio pin #01 as an output pin and turn on
	        pinRed = gpio.provisionDigitalOutputPin(PIN_RED, "RedLED", PinState.LOW);
	        pinBlue = gpio.provisionDigitalOutputPin(PIN_BLUE, "GreenLED", PinState.HIGH);
	        pinGreen = gpio.provisionDigitalOutputPin(PIN_GREEN, "BlueLED", PinState.LOW);

	        pinRed.setShutdownOptions(true, PinState.LOW);
	        pinBlue.setShutdownOptions(true, PinState.LOW);
	        pinGreen.setShutdownOptions(true, PinState.LOW);	        
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void setLedRunning() {
		pinRed.low();
		pinGreen.low();
		pinBlue.high();
	}
	
	public static void setLedWaiting() {
		pinRed.low();
		pinGreen.high();
		pinBlue.high();
	}
	
	public static void setLedError() {
		pinRed.high();
		pinGreen.low();
		pinBlue.low();
	}
}
