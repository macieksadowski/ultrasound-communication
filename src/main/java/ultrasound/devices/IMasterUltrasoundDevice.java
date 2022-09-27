package ultrasound.devices;

public interface IMasterUltrasoundDevice extends IDevice {
	
	public static String IDENTIFIER = "MST";
	
	public void sendBroadcast(byte command);
	
	public void sendBroadcast(byte[] data);
	
	public void send(byte receiverAddress, byte command);
	
	public void send(byte receiverAddress, byte[] data);
	
	public void setDecoderTimeout(long timeout);

}
