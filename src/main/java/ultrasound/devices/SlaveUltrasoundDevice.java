package ultrasound.devices;

import ultrasound.IDecoder;
import ultrasound.IEncoder;
import ultrasound.dataframe.DataFrame.CheckAddressResultValues;
import ultrasound.dataframe.IAsciiControlCodes;
import ultrasound.dataframe.IDataFrame;

public class SlaveUltrasoundDevice extends AbstractUltrasoundDevice implements ISlaveUltrasoundDevice {

	protected volatile boolean isRunning;
	
	public SlaveUltrasoundDevice(byte address, IEncoder encoder, IDecoder decoder) {
		super(address, encoder, decoder);
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

		switch(result.get()) {
		case PARSING_OK:
			if (receivedDataFrame.getCommand() == IAsciiControlCodes.STX) {
				handleData(receivedDataFrame.getData());
			} else {
				handleCommand(receivedDataFrame.getCommand());
			}
			if(checkAdrResult.get() == CheckAddressResultValues.OK) {
				send(IDataFrame.MASTER_ADDRESS, IAsciiControlCodes.ACK, null);
				logMessage("Acknowledgment of receipt of message has been sent");
			}
			break;
		case OTHER_RECIPIENT:
			logMessage("Message for another recipient");
			break;
		default:
			if (checkAdrResult.get() == CheckAddressResultValues.OK) {
				send(IDataFrame.MASTER_ADDRESS, IAsciiControlCodes.NAK, null);
				logMessage("Retry transmission was requested");
			}
			break;
		}

	}
	
	protected void handleData(byte[] data) {
		if(checkAdrResult.get() == CheckAddressResultValues.BROADCAST) {
			logMessage("Broadcast data from master received");
		} else {
			logMessage("Data from master received");
			
		}
	}

	protected void handleCommand(byte cmd) {
		if(checkAdrResult.get() == CheckAddressResultValues.BROADCAST) {
			logMessage("Broadcast command from master received");
		} else {
			logMessage("Command from master received");
		}
	}

	@Override
	protected void onDecoderTimeout() {

	}
	
	@Override
	public String toString() {
		return "Slave device ADR: 0x" + getAddress();
	}

}
