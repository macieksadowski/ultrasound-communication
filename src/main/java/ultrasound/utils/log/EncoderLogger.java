package ultrasound.utils.log;

import ultrasound.encoder.IEncoder;

/**
 * Logger class for {@link IEncoder}. Singleton class
 */
public final class EncoderLogger extends Logger {

	private static EncoderLogger instance;

	private EncoderLogger() {
		setTag("ENC");
	}

	public static synchronized EncoderLogger getInstance() {
		if (instance == null) {
			instance = new EncoderLogger();
		}

		return instance;
	}
}
