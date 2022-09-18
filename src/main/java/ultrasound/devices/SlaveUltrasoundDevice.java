package ultrasound.devices;

import ultrasound.IDecoder;
import ultrasound.IEncoder;
import ultrasound.dataframe.DataFrame.ParserResultValues;
import ultrasound.dataframe.IAsciiControlCodes;
import ultrasound.dataframe.IDataFrame;

public class SlaveUltrasoundDevice extends AbstractUltrasoundDevice implements ISlaveUltrasoundDevice {

	boolean isRunning;

	public SlaveUltrasoundDevice(byte address, IEncoder encoder, IDecoder decoder) {
		super(address, encoder, decoder);
		isRunning = true;
	}

	public void run() {
		isRunning = true;

		while (isRunning) {
			receive(NO_TIMEOUT);
		}
	}

	public void stop() {
		isRunning = false;
		stopDecoder();
	}

	protected void onTransmissionReceived() {
		
		if(result.get() == ParserResultValues.PARSING_OK) {
			if(receivedDataFrame.getCommand() == IAsciiControlCodes.STX) {
				send(IDataFrame.MASTER_ADDRESS, IAsciiControlCodes.ACK, null);
				logMessage("Acknowledgment of receipt of message has been sent");
			} else {
				handleCommand(receivedDataFrame.getCommand());
			}
		} else {
			send(IDataFrame.MASTER_ADDRESS, IAsciiControlCodes.NAK, null);
			logMessage("Retry transmission was requested");
		}
		
	}
	
	protected void handleCommand(byte cmd) {
		send(IDataFrame.MASTER_ADDRESS, IAsciiControlCodes.ACK, null);
		logMessage("Acknowledgment of receipt of message has been sent");
	}

	@Override
	protected void onDecoderTimeout() {
		// TODO Auto-generated method stub
		
	}

}
