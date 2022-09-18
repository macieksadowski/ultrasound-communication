package ultrasound.devices;

import org.apache.commons.lang3.ArrayUtils;

import ultrasound.IDecoder;
import ultrasound.IEncoder;
import ultrasound.dataframe.DataFrame.DataFrameBuilder;
import ultrasound.dataframe.DataFrame.ParserResultValues;
import ultrasound.dataframe.IAsciiControlCodes;
import ultrasound.dataframe.IDataFrame;

public class MasterUltrasoundDevice extends AbstractUltrasoundDevice implements IMasterUltrasoundDevice {
	
	private long decoderTimeout = DEFAULT_TIMEOUT;
	
	private byte lastReceiverAddress;
	private byte lastCommand;
	private byte[] lastData;

	public MasterUltrasoundDevice(IEncoder encoder, IDecoder decoder) {
		super(IDataFrame.MASTER_ADDRESS, encoder, decoder);
	}

	public void sendBroadcast(byte command) {
		sendBroadcast(command, null);
	}
	
	public void sendBroadcast(byte[] data) {
		sendBroadcast(IAsciiControlCodes.STX, data);
	}

	public void sendBroadcast(byte command, byte[] data) {

		IDataFrame frame = new DataFrameBuilder(IDataFrame.BROADCAST_ADDRESS, encoder.getNoOfChannels()).command(command).data(data)
				.build();

		encoder.setDataFrame(frame);
		encoder.run();

	}

	public void send(byte receiverAddress, byte command) {
		send(receiverAddress, command, null); 
	}
	
	public void send(byte receiverAddress, byte[] data) {
		send(receiverAddress, IAsciiControlCodes.STX, data);
	}

	public void send(byte receiverAddress, byte command, byte[] data) {
		lastReceiverAddress = receiverAddress;
		lastCommand = command;
		lastData = ArrayUtils.clone(data);
		super.send(receiverAddress, command, data);		
		receive(decoderTimeout);
	}


	public void setDecoderTimeout(long timeout) {
		this.decoderTimeout = timeout;
	}

	@Override
	protected void onTransmissionReceived() {
		
		if (receivedDataFrame != null) {
			if (result.get() == ParserResultValues.PARSING_OK && receivedDataFrame.getCommand() == IAsciiControlCodes.ACK) {
				logMessage("Transmission receipt acknowledged");
			} else {
				logMessage("Retry transmission was requested");
				send(lastReceiverAddress, lastCommand, lastData);
			}
		}
		
	}

	@Override
	protected void onDecoderTimeout() {
		logMessage("Transmission confirmation not received");
		send(lastReceiverAddress, lastCommand, lastData);
	}

}
