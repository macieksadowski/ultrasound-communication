package ultrasound.devices;

import org.apache.commons.lang3.ArrayUtils;

import ultrasound.dataframe.IControlCodes;
import ultrasound.dataframe.IDataFrame;
import ultrasound.dataframe.ParserResult.ParserResultValues;
import ultrasound.decoder.IDecoder;
import ultrasound.encoder.IEncoder;

public class MasterUltrasoundDevice extends AbstractUltrasoundDevice implements IMasterUltrasoundDevice {

	private long decoderTimeout = DEFAULT_TIMEOUT;

	private byte lastReceiverAddress;
	private byte lastCommand;
	private byte[] lastData;

	private Byte receiverAddress;
	private Byte command;
	private byte[] data;

	private actionType actionToRun;
	private actionType runningAction;
	
	private boolean idle;

	public MasterUltrasoundDevice(IEncoder encoder, IDecoder decoder) {
		super(IDataFrame.MASTER_ADDRESS, encoder, decoder);
		logger.setTag("MST");
		runningAction = actionType.NONE;
		actionToRun = actionType.NONE;
	}

	public void run() {
		isRunning = true;
		idle = true;

		while (isRunning) {

			if (runningAction == actionType.NONE) {
				runningAction = actionToRun;
				actionToRun = actionType.NONE;
			}

			switch (runningAction) {
				case SEND:
					idle = false;
					super.send(receiverAddress, command, data);
					receive(decoderTimeout);
					break;
				case SEND_BROADCAST:
					idle = false;
					super.send(receiverAddress, command, data);
					break;
				case NONE:
					idle = true;
					break;
				default:
					break;
			}
			runningAction = actionType.NONE;
			pause(50, null);
		}
	}

	public void sendBroadcast(byte command) {
		sendBroadcast(command, null);
	}

	public void sendBroadcast(byte[] data) {
		sendBroadcast(IControlCodes.STX, data);
	}

	public void sendBroadcast(byte command, byte[] data) {
		this.receiverAddress = IDataFrame.BROADCAST_ADDRESS;
		this.command = command;
		this.data = ArrayUtils.clone(data);

		this.actionToRun = actionType.SEND_BROADCAST;
	}

	public void send(byte receiverAddress, byte command) {
		send(receiverAddress, command, null);
	}

	public void send(byte receiverAddress, byte[] data) {
		send(receiverAddress, IControlCodes.STX, data);
	}

	@Override
	public void send(byte receiverAddress, byte command, byte[] data) {
		this.receiverAddress = receiverAddress;
		this.command = command;
		this.data = ArrayUtils.clone(data);

		this.lastReceiverAddress = this.receiverAddress;
		this.lastCommand = this.command;
		this.lastData = this.data;

		this.actionToRun = actionType.SEND;

	}

	public void setDecoderTimeout(long timeout) {
		this.decoderTimeout = timeout;
	}
	
	public boolean isIdle() {
		return idle;
	}

	@Override
	protected void onTransmissionReceived() {

		if (receivedDataFrame != null) {
			if (result.get() == ParserResultValues.PARSING_OK
					&& receivedDataFrame.getCommand() == IControlCodes.ACK) {
				logger.logMessage("Transmission receipt acknowledged");
			} else {
				logger.logMessage("Retry transmission was requested");
				send(lastReceiverAddress, lastCommand, lastData);
			}
		}

	}

	@Override
	protected void onDecoderTimeout() {
		logger.logMessage("Transmission confirmation not received");
		send(lastReceiverAddress, lastCommand, lastData);
	}

	public enum actionType {
		SEND_BROADCAST, SEND, NONE
	}

}
