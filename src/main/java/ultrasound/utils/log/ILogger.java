package ultrasound.utils.log;

import java.io.PrintStream;

/**
 * This interface provides methods for handling app's logs. Default logger
 * output will by default System's logger output (System.out) Each logger type
 * should be a separate singleton class
 * 
 * @author M. Sadowski
 *
 */
public interface ILogger {

	/**
	 * This method is used to print log message on output of logger's instance
	 * 
	 * @param msg Log message to print on log output
	 */
	void logMessage(String msg);

	/**
	 * This method is used to print log message on output of logger's instance.
	 * 
	 * @param tag When tag given default logger's tag will be overridden
	 * @param msg Log message to print on log output
	 */
	void logMessage(String tag, String msg);

	/**
	 * This method changes logger's output.
	 * 
	 * @param out
	 */
	void setLogOut(PrintStream out);

	/**
	 * Method to overwrite logger's default tag.
	 * Tag should be overridden in each type of logger. This tag
	 * will be added to the beginning of each log message to distinguish messages
	 * from different sources when they have been printed in the same stream.
	 * 
	 * @param tag New logger's tag. It should be 3 capital letters but it's not mandatory.
	 */
	void setTag(String tag);

	/**
	 * Getter for logger's tag parameter
	 * 
	 * @return String tag
	 */
	String getTag();

	/**
	 * Getter for logger's output stream
	 * 
	 * @return PrintStream logger's output stream
	 */
	PrintStream getOut();

	/**
	 * This method can be used to add external logger's output. All log messages
	 * will be send to that external logger using method
	 * {@link IExternalLogger#log(String)} The external logger has to implement
	 * interface {@link IExternalLogger}
	 * 
	 * @param extLog external logger to connect
	 */
	void connectExternalLogger(IExternalLogger extLog);
}
