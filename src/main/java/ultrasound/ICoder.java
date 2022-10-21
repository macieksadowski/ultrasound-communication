package ultrasound;

import ultrasound.dataframe.IDataFrame;

/**
 * Interface providing common methods for encoder and decoder devices
 *
 */
public interface ICoder {

	/**
	 * Enum used to define mode in which device should be launched.
	 * <ul>
	 * <li>{@code CoderMode#SIMPLE} - raw hex data will be send and receive</li>
	 * <li>{@code CoderMode#DATA_FRAME} - data will be transmitted encapsulated in
	 * frames</li>
	 * </ul>
	 */
	public enum CoderMode {
		SIMPLE, DATA_FRAME
	}
	
	/**
	 * Method used to start device
	 */
	void run();
	
	/**
	 * Method used to immediately stop device.
	 */
	void stop();

	/**
	 * Get the sample rate used in encoder/decoder
	 * 
	 * @return Integer sample rate of encoder/decoder
	 */
	Integer getSampleRate();

	/**
	 * Get number of transmission channels
	 * 
	 * @return <code>int</code> number of transmission channels
	 */
	int getNoOfChannels();

	/**
	 * Get the lowest transmission frequency (Frequency of the 1st channel)
	 * 
	 * @return <code>int</code> frequency in [Hz]
	 */
	int getFirstFreq();

	/**
	 * Get frequency interval between successive transmission channels
	 * 
	 * @return <code>int</code> frequency in [Hz]
	 */
	int getFreqStep();

	/**
	 * Get time length of one transmission pulse
	 * 
	 * @return <code>double</code> time length of one transmission pulse in seconds
	 *         [s]
	 */
	double gettOnePulse();

	/**
	 * Returns <code>true</code> when SECDED Encoding is enabled
	 * 
	 * @return <code>boolean</code> flag for SECDED Encoding
	 */
	boolean isSecdedEnabled();

	/**
	 * Get device mode (Simple or Data frame) {@link CoderMode}
	 * 
	 * @return {@link CoderMode} of the device
	 */
	CoderMode getMode();

	/**
	 * Returns {@code true} if the device is currently running
	 * 
	 * @return <code>boolean</code> flag of device state
	 */
	boolean isRunning();

	/**
	 * Returns current {@link IDataFrame} stored in the device
	 * 
	 * @return {@link IDataFrame} object
	 */
	IDataFrame getDataFrame();

}
