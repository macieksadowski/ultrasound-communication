package ultrasound.devices;

import java.io.PrintWriter;

public interface IDevice {
	
	public byte getAddress();
	
	public void connectToLogOutput(PrintWriter out);

}
