package ultrasound;

import ultrasound.dataframe.IDataFrame;

public interface IEncoder extends ICoder {
	
	/**
	 * Builder for new instances {@link IEncoder}
	 */
	public interface IEncoderBuilder extends ICoderBuilder {

		public IEncoderBuilder tBreak(double tBreak);
		
		public IEncoderBuilder fadeLength(double fadeLength);

	}

	/**
	 * Method used to start sound transmission
	 */
	void run();

	/**
	 * Sets hexadecimal data to transmit. This String will be validated before transmission using method {@link AbstractEncoder#validateHexData()}
	 * 
	 * @param hexData hexadecimal data as String
	 */
	void setHexData(String hexData);

	void setDataFrame(IDataFrame frame);

	/**
	 * This method is used to check state of the encoder.
	 * @return {@code boolean} true if encoder is currently transmitting signal 
	 */
	boolean isRunning();

	/**
	 * Getter method for field {@link AbstractEncoder#tBreak}
	 * @return tBreak in seconds
	 */
	Double getTBreak();

	/**
	 * Returns message to transmit as binary data.
	 * If encoder uses SECDED encoding this message will be encoded with SECDED.
	 * @return {@code boolean[] } array contains binary message
	 */
	boolean[] getBinaryMessage();

	/**
	 * Returns message to transmit as binary data converted to {@code String}
	 * @return {@code String } contains binary message
	 */
	String getBinaryMessageString();

	/**
	 * Returns message to transmit as hex data converted to {@code String}
	 * @return {@code String } contains hex message
	 */
	String getHexMessageString();

}