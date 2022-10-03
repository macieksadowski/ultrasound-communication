package ultrasound.devices;

import ultrasound.dataframe.IDataFrame;
import ultrasound.dataframe.ParserResult;

/**
 * Interface for all types of devices in ultrasound communication system. 
 * Every device should be run in a new thread.
 * @author M. Sadowski
 *
 */
public interface IDevice extends Runnable {
	
	/**
	 * Getter method for device address
	 * @return byte device address
	 */
	byte getAddress();
	
	/**
	 * Method used to stop the operation of the device
	 */
	void stop();
	
	/**
	 * Returns data frame stored in device's memory
	 * @return data frame object
	 */
	IDataFrame getReceivedDataFrame();
	
	/**
	 * Returns result of parsing latest sent/received data frame
	 * @return parser's result
	 */
	ParserResult getResult();
	
	/**
	 * Returns state of the device 
	 * @return true when the device is currently in operation
	 */
	boolean isRunning();

}
