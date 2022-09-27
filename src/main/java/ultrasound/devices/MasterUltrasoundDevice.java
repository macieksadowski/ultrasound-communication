package ultrasound.devices;

import org.apache.commons.lang3.ArrayUtils;

import ultrasound.IDecoder;
import ultrasound.IEncoder;
import ultrasound.dataframe.DataFrame.ParserResultValues;
import ultrasound.dataframe.IAsciiControlCodes;
import ultrasound.dataframe.IDataFrame;

public class MasterUltrasoundDevice extends AbstractUltrasoundDevice implements IMasterUltrasoundDevice {
	
	protected volatile boolean isRunning;
	
	private long decoderTimeout = DEFAULT_TIMEOUT;
	
	private byte lastReceiverAddress;
	private byte lastCommand;
	private byte[] lastData;
	
	private Byte receiverAddress;
	private Byte command;
	private byte[] data;
	
	private actionType actionToRun;
	private actionType runningAction;
	
	public MasterUltrasoundDevice(IEncoder encoder, IDecoder decoder) {
		super(IDataFrame.MASTER_ADDRESS, encoder, decoder);
		runningAction = actionType.none;
		actionToRun = actionType.none;
	}
	
	public void run() {
		isRunning = true;
		
		while(isRunning) {
			
			if(runningAction == actionType.none) {
				runningAction = actionToRun;
				actionToRun = actionType.none;
			}
			
			switch(runningAction) {
			case send:
				super.send(receiverAddress, command, data);		
				receive(decoderTimeout);
				break;
			case send_broadcast:
				super.send(receiverAddress, command, data);
				break;
			case none:
			default:
				
				break;
			}
			
			runningAction = actionType.none;
			
			pause(100);
		}
	}
	
	
	public void stop() {
		isRunning = false;
	}

	public void sendBroadcast(byte command) {
		sendBroadcast(command, null);
	}
	
	public void sendBroadcast(byte[] data) {
		sendBroadcast(IAsciiControlCodes.STX, data);
	}

	public void sendBroadcast(byte command, byte[] data) {
			this.receiverAddress = IDataFrame.BROADCAST_ADDRESS;
			this.command = command;
			this.data = ArrayUtils.clone(data);
			
			this.actionToRun = actionType.send_broadcast;
	}

	public void send(byte receiverAddress, byte command) {
		send(receiverAddress, command, null); 
	}
	
	public void send(byte receiverAddress, byte[] data) {
		send(receiverAddress, IAsciiControlCodes.STX, data);
	}

	public void send(byte receiverAddress, byte command, byte[] data) {
		this.receiverAddress = receiverAddress;
		this.command = command;
		this.data = ArrayUtils.clone(data);
		
		this.lastReceiverAddress = this.receiverAddress;
		this.lastCommand = this.command;
		this.lastData = this.data;
		
		this.actionToRun = actionType.send;
		
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
	
	
	public enum actionType {
		send_broadcast, send, none
	}

}
