package ultrasound.dataframe;

/**
 * Interface for Data frame model {@link DataFrame}
 * @author M. Sadowski
 *
 */
public interface IDataFrame {

	/**
	 * Defines maximum length of Data Frame in bytes
	 */
	int MAX_MESSAGE_SIZE = Byte.MAX_VALUE;
	
	/**
	 * Defines address for broadcast transmission
	 */
	byte BROADCAST_ADDRESS = (byte) 0xff;
	
	/**
	 * Defines address of master device
	 */
	byte MASTER_ADDRESS = (byte) 0x00;

	/**
	 * Returns Data Frame as <code>byte</code> array
	 * @return <code>byte</code> array containing Data Frame
	 */
	byte[] get();

	/**
	 * Returns checksum byte of the data frame
	 * @return <code>byte</code> checksum
	 */
	byte getChecksum();

	/**
	 * Returns command byte of the data frame
	 * @return <code>byte</code> command
	 */
	byte getCommand();

	/**
	 * Returns a message from the data frame
	 * @return <code>byte</code> array containing byte message
	 */
	byte[] getData();

	/**
	 * Returns the receiver address byte from the data frame
	 * @return <code>byte</code> receiver address
	 */
	byte getReceiverAddress();

}