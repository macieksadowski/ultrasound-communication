package ultrasound.utils.log;

import ultrasound.decoder.IDecoder;

/**
 * Logger class for {@link IDecoder}. Singleton class
 */
public final class DecoderLogger extends Logger {

	private static DecoderLogger instance;

	private DecoderLogger() {
		setTag("DEC");
	}

	public static synchronized DecoderLogger getInstance() {
		if (instance == null) {
			instance = new DecoderLogger();
		}

		return instance;
	}
}
