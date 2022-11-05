package ultrasound.devices;

public interface IMasterUltrasoundDevice extends IDevice {

	void sendBroadcast(byte command);

	void sendBroadcast(byte[] data);

	void send(byte receiverAddress, byte command);

	void send(byte receiverAddress, byte[] data);

	void setDecoderTimeout(long timeout);
	
	boolean isIdle();
}
