package ultrasound.decoder;

import ultrasound.ICoder;

public interface IDecoderSimple extends ICoder, Runnable {

	/**
	*
	*/
	void run();

	void clearReceivedDataBuffers();

	void clearResult();

	void stopDecoder();

	String getResHex();

	int getNfft();

	double[] getF();

}