package ultrasound;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;


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
