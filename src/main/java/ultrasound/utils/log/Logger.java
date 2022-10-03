package ultrasound.utils.log;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Abstract class implementing all methods for a logger object. To create a new
 * logger create a new singleton class which extends this abstract class.
 * 
 * @author M. Sadowski
 *
 */
public abstract class Logger implements ILogger {

	/**
	 * The logger object tag. Should be overridden in each type of logger. This tag
	 * will be added to the beginning of each log message to distinguish messages
	 * from different sources when they have been printed in the same stream.
	 */
	protected String tag = "LOG";

	private PrintStream out;
	private IExternalLogger extLogger;

	protected Logger() {
		this.out = System.out;
	}

	@Override
	public void logMessage(String msg) {
		logMessage(tag, msg);
	}

	@Override
	public void logMessage(String tag, String msg) {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

		String message = now.format(format) + ": " + tag + " - " + msg;
		out.println(message);
		if (extLogger != null) {
			extLogger.log(message);
		}
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getTag() {
		return tag;
	}

	public void setLogOut(PrintStream out) {
		this.out = out;
	}

	public PrintStream getOut() {
		return out;
	}

	public void connectExternalLogger(IExternalLogger extLog) {
		this.extLogger = extLog;
	}

}
