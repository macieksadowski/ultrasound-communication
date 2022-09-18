package ultrasound.devices;

public interface ISlaveUltrasoundDevice extends IDevice, Runnable {
	
	public static String IDENTIFIER = "SLV";
	
	public void run();
	
	public void stop();

}
