package ultrasound.devices;

import ultrasound.dataframe.CheckAddressResult.CheckAddressResultValues;
import ultrasound.decoder.IDecoder;
import ultrasound.encoder.IEncoder;
import ultrasound.dataframe.IControlCodes;
import ultrasound.dataframe.IDataFrame;

public class SlaveUltrasoundDevice extends AbstractUltrasoundDevice {

	public SlaveUltrasoundDevice(byte address, IEncoder encoder, IDecoder decoder) {
		super(address, encoder, decoder);
		logger.setTag("SLV");
	}

	public void run() {
		isRunning = true;

		while (isRunning) {
			receive(NO_TIMEOUT);
		}
	}

	protected void onTransmissionReceived() {

		switch (result.get()) {
			case PARSING_OK:
				onParsingOk();
				break;
			case OTHER_RECIPIENT:
				logger.logMessage("Message for another recipient");
				break;
			default:
				if (checkAdrResult.get() == CheckAddressResultValues.OK) {
					send(IDataFrame.MASTER_ADDRESS, IControlCodes.NAK, null);
					logger.logMessage("Retry transmission was requested");
				}
				break;
			}
	}

	private void onParsingOk() {
		if (receivedDataFrame.getCommand() == IControlCodes.STX) {
			handleData(receivedDataFrame.getData());
		} else {
			handleCommand(receivedDataFrame.getCommand());
		}
		if (checkAdrResult.get() == CheckAddressResultValues.OK) {
			send(IDataFrame.MASTER_ADDRESS, IControlCodes.ACK, null);
			logger.logMessage("Acknowledgment of receipt of message has been sent");
		}
	}

	protected void handleData(byte[] data) {
		if (checkAdrResult.get() == CheckAddressResultValues.BROADCAST) {
			logger.logMessage("Broadcast data from master received");
		} else {
			logger.logMessage("Data from master received");

		}
	}

	protected void handleCommand(byte cmd) {
		if (checkAdrResult.get() == CheckAddressResultValues.BROADCAST) {
			logger.logMessage("Broadcast command from master received");
		} else {
			logger.logMessage("Command from master received");
		}
	}

	@Override
	protected void onDecoderTimeout() {
		// TODO document why this method is empty
	}

	@Override
	public String toString() {
		return "Slave device ADR: 0x" + getAddress();
	}

}
