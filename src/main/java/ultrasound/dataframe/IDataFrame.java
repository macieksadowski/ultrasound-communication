package ultrasound.dataframe;

public interface IDataFrame {

	int MAX_MESSAGE_SIZE = Byte.MAX_VALUE;
	byte BROADCAST_ADDRESS = (byte) 0xff;
	byte MASTER_ADDRESS = (byte) 0x00;

	byte[] get();

	byte getChecksum();

	byte getCommand();

	byte[] getData();

	byte getReceiverAddress();

	String toString();

}