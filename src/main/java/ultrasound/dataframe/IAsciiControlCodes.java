package ultrasound.dataframe;

public interface IAsciiControlCodes {

	/**
	 * Null
	 */
	byte NUL = 0x00;
	/**
	 * Start of Header
	 */
	byte SOH = 0x01;
	/**
	 * Start of Text
	 */
	byte STX = 0x02;
	/**
	 * End of Text
	 */
	byte ETX = 0x03;
	/**
	 * End of Transmission
	 */
	byte EOT = 0x04;
	/**
	 * Enquiry
	 */
	byte ENQ = 0x05;
	/**
	 * Acknowledge
	 */
	byte ACK = 0x06;
	/**
	 * Bell
	 */
	byte BEL = 0x07;
	/**
	 * Backspace
	 */
	byte BS = 0x08;
	/**
	 * Horizontal Tab
	 */
	byte HT = 0x09;
	/**
	 * Line Feed
	 */
	byte LF = 0x0A;
	/**
	 * Vertical Tab
	 */
	byte VT = 0x0B;
	/**
	 * Form Feed
	 */
	byte FF = 0x0C;
	/**
	 * Carriage Return
	 */
	byte CR = 0x0D;
	/**
	 * Shift Out
	 */
	byte SO = 0x0E;
	/**
	 * Shift In
	 */
	byte SI = 0x0F;
	/**
	 * Data Link Escape
	 */
	byte DLE = 0x10;
	/**
	 * Device Control 1
	 */
	byte DC1 = 0x11;
	/**
	 * Device Control 2
	 */
	byte DC2 = 0x12;
	/**
	 * Device Control 3
	 */
	byte DC3 = 0x13;
	/**
	 * Device Control 4
	 */
	byte DC4 = 0x14;
	/**
	 * Negative Acknowledge
	 */
	byte NAK = 0x15;
	/**
	 * Synchronize
	 */
	byte SYN = 0x16;
	/**
	 * End of Transmission Block
	 */
	byte ETB = 0x17;
	/**
	 * Cancel
	 */
	byte CAN = 0x18;
	/**
	 * End of Medium
	 */
	byte EM = 0x19;
	/**
	 * Substitute
	 */
	byte SUB = 0x1A;
	/**
	 * Escape
	 */
	byte ESC = 0x1B;
	/**
	 * File Separator
	 */
	byte FS = 0x1C;
	/**
	 * Group Separator
	 */
	byte GS = 0x1D;
	/**
	 * Record Separator
	 */
	byte RS = 0x1E;
	/**
	 * Unit Separator
	 */
	byte US = 0x1F;

}