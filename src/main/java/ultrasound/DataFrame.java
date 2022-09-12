package ultrasound;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

public class DataFrame {

	public static final int MAX_MESSAGE_SIZE = Byte.MAX_VALUE;

	public static final byte BROADCAST_ADDRESS = (byte) 0xff;

	private byte receiverAddress;
	private Byte command;
	private byte checksum;
	private byte[] data;

	private ByteArrayOutputStream outputStream;

	private DataFrame(DataFrameBuilder builder) throws IOException {

		this.outputStream = new ByteArrayOutputStream();

		outputStream.write(ControlCodes.SOH);

		this.receiverAddress = builder.receiverAddress;
		outputStream.write(receiverAddress);

		this.command = builder.command;
		if (command == null) {
			command = ControlCodes.STX;
		}
		outputStream.write(command);

		this.data = builder.data;
		if (data != null) {
			this.data = padData(builder.noOfTransmissionChannels);
			outputStream.write(data);
			outputStream.write(ControlCodes.ETX);
		}

		this.checksum = calculateChecksum();
		outputStream.write(checksum);

		outputStream.write(ControlCodes.EOT);

		/*
		 * 6B = 48b = min. size
		 * 
		 * 
		 */
	}

	public static class DataFrameBuilder {
		private byte[] data;
		private byte receiverAddress;
		private Byte command;
		private int noOfTransmissionChannels;

		public DataFrameBuilder(byte receiverAddress, int noOfTransmissionChannels) {
			this.receiverAddress = receiverAddress;
			this.noOfTransmissionChannels = noOfTransmissionChannels;
		}

		public DataFrameBuilder data(byte[] data) {
			this.data = data;
			return this;
		}

		public DataFrameBuilder command(byte command) {
			this.command = command;
			return this;
		}

		public DataFrame build() {
			validate();
			try {
				return new DataFrame(this);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		private void validate() {
			if (command == null) {
				throw new NullPointerException("Data frame must contain a command!");
			} else if (command == ControlCodes.STX) {
				if (data == null) {
					throw new IllegalArgumentException("Start of text command given but no text!");
				}
			}
			if (data != null && data.length > MAX_MESSAGE_SIZE) {
				throw new IllegalArgumentException("Message size is greater than maximal allowed!");
			}
		}

	}

	public byte[] get() {
		return outputStream.toByteArray();
	}

	private byte calculateChecksum() {
		return (byte) this.get().length;

	}

	public byte getChecksum() {
		return this.checksum;
	}

	private byte[] padData(int noOfTransmissionChannels) {

		// Length of message in bits (plus 2 because of checksum and stop byte)
		int binMsgSize = (outputStream.size() + 2) * 8;

		int pad = (2 * binMsgSize) % noOfTransmissionChannels;
		if (pad != 0) {

			byte[] padArray = new byte[(noOfTransmissionChannels - pad / 2) / 8];
			Arrays.fill(padArray, (byte) 0x00);
			ArrayUtils.addAll(data, padArray);

		}

		return data;
	}

}
