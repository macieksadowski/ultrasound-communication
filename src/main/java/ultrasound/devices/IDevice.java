package ultrasound.devices;

import java.io.PrintWriter;

public interface IDevice extends Runnable {
	
	public byte getAddress();
	
	public void connectToLogOutput(PrintWriter out);
	
	public void stop();

}
