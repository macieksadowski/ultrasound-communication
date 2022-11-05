package ultrasound.dataframe;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import ultrasound.utils.UltrasoundHelper;

/**
 * Data frame model for Ultrasound Transmission
 * 
 * There are two basic types of data frame:
 * <ol>
 * <li>Command frame - for sending a control command to a device</li>
 * <li>Data frame - for sending data to a device</li>
 * </ol>
 * 
 * <pre>
 * 1) Command frame
 *  -------------------------------------------------------------------------------------------------------
 * |         SOH        |  Receiver address  |       Command      |      Checksum      |         EOT        | 
 *  -------------------------------------------------------------------------------------------------------
 *  1                    2                    3                    4                    5
 *  
 * 2) Data frame
 *  --------------------------------------------------------------------------------------------------------------------------------------------------
 * |         SOH        |  Receiver address  |         STX        |         Data        -      Padding      |     Checksum       |         EOT        | 
 *  --------------------------------------------------------------------------------------------------------------------------------------------------
 *  1                    2                    3                    4-n                                       n-1                  n
 * </pre>
 * <ul>
 * <li>Receiver address: Address of device which should receive transmission -
 * values from 0 to 126. Address 127 is reserved for broadcast
 * transmission.</li>
 * <li>Command: One byte ASCII command. For the list of commands see
 * {@link ControlCodes}</li>
 * <li>Data: data to transmit maximum length is defined by constant
 * {@link DataFrame#MAX_MESSAGE_SIZE} (in diagram as 'n'). Data will be
 * automatically padded with nulls to get correct frame length.</li>
 * </ul>
 * 
 * @author M.Sadowski
 *
 */
public final class DataFrame implements IDataFrame {

	/**
	 * Length of command frame in bytes (the shortest valid data frame)
	 */
	public static final int MIN_DATA_FRAME_LENGTH_IN_BYTES = 5;
	
	private byte receiverAddress;
	private Byte command;
	private byte checksum;
	private byte[] data;

	private ByteArrayOutputStream outputStream;

	/**
	 * Private constructor for DataFrame. To create new instance of DataFrame use
	 * {@link DataFrameBuilder}
	 * 
	 * @param builder
	 * @throws IOException on OutputStream error
	 */
	private DataFrame(DataFrameBuilder builder) throws IOException {

		this.outputStream = new ByteArrayOutputStream();

		outputStream.write(IControlCodes.SOH);

		this.receiverAddress = builder.receiverAddress;
		outputStream.write(receiverAddress);

		this.command = builder.command;
		if (command == null) {
			command = IControlCodes.STX;
		}
		outputStream.write(command);

		this.data = builder.data;
		if (data != null) {
			this.data = padData(builder.noOfTransmissionChannels);
			outputStream.write(data);
			outputStream.write(IControlCodes.ETX);
		}

		this.checksum = calculateChecksum();
		outputStream.write(checksum);

		outputStream.write(IControlCodes.EOT);

	}

	/**
	 * Builder for new instances of {@link DataFrame}
	 *
	 */
	public static class DataFrameBuilder {
		private byte[] data;
		private byte receiverAddress;
		private Byte command;
		private int noOfTransmissionChannels;

		public DataFrameBuilder(int noOfTransmissionChannels) {
			this.receiverAddress = BROADCAST_ADDRESS;
			this.noOfTransmissionChannels = noOfTransmissionChannels;
		}

		public DataFrameBuilder(byte receiverAddress, int noOfTransmissionChannels) {
			this.receiverAddress = receiverAddress;
			this.noOfTransmissionChannels = noOfTransmissionChannels;
		}

		public DataFrameBuilder receiverAddress(byte receiverAddress) {
			this.receiverAddress = receiverAddress;
			return this;
		}

		public DataFrameBuilder data(byte[] data) {
			this.data = data;
			return this;
		}

		public DataFrameBuilder command(byte command) {
			this.command = command;
			return this;
		}

		public IDataFrame build() throws Exception {
			validate();
			return new DataFrame(this);
		}

		private void validate() {
			if (command == null) {
				throw new NullPointerException("Data frame must contain a command!");
			} 
			if (command == IControlCodes.STX && (data == null || data.length == 0)) {
				throw new IllegalArgumentException("Start of text command given but no text!");
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

	public byte getCommand() {
		return this.command;
	}

	public byte[] getData() {
		return this.data;
	}

	public byte getReceiverAddress() {
		return this.receiverAddress;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Data frame:");
		sb.append(System.lineSeparator());

		String recAdr = null;
		if (receiverAddress == IDataFrame.BROADCAST_ADDRESS) {
			recAdr = "BROADCAST";
		} else {
			recAdr = UltrasoundHelper.byteToHex(receiverAddress);
		}
		sb.append("\tReceiver address: " + recAdr);
		sb.append(System.lineSeparator());
		sb.append("\tCommand: " + ControlCodes.getCodeNameByValue(command));
		if (data != null) {
			sb.append(System.lineSeparator());
			sb.append("\tData: " + new String(data, StandardCharsets.US_ASCII));
		}
		return sb.toString();
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
