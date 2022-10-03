package ultrasound.utils.log;

import ultrasound.devices.IDevice;

/**
 * Logger class for {@link IDevice}. Singleton class
 */
public final class DeviceLogger extends Logger {

	private static DeviceLogger instance;

	private DeviceLogger() {
		setTag("DEV");
	}

	public static synchronized DeviceLogger getInstance() {
		if (instance == null) {
			instance = new DeviceLogger();
		}

		return instance;
	}
}
