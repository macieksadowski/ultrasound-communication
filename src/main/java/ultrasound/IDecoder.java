package ultrasound;

import ultrasound.dataframe.DataFrame.CheckAddressResult;
import ultrasound.dataframe.DataFrame.ParserResult;

public interface IDecoder extends ICoder, Runnable {
	
	/**
	 * Builder for new instances {@link IDecoder}
	 */
	public interface IDecoderBuilder extends ICoderBuilder {

	}
	
	/**
	*
	*/
	void run();

	void clearReceivedDataBuffers();
	
	void clearResult();

	void stopRecording();

	String getResHex();

	int getNfft();

	double[] getAmpl();

	double[] getF();

	double[] getT();
	
	boolean endOfTransmissionReceived();
	
	void setDeviceAddress(Byte adr);

	ParserResult getParserResult();
	
	CheckAddressResult getCheckAddressParserResult();

}