package ultrasound.utils.log;

/**
 * This interface can be used to add external logger's output. All external
 * logger types have to implement this interface to be added as external logger
 * for {@link ILogger}
 */
public interface IExternalLogger {

	/**
	 * This method will be called by {@link ILogger} to send log message to instance
	 * of an external logger.
	 * 
	 * @param message Log message from {@link ILogger}
	 */
	void log(String message);

}
