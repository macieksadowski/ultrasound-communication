package ultrasound.dataframe;

import java.lang.reflect.Field;
import java.util.HashMap;

public class ControlCodes {
	
	/**
	 * Null
	 */
	public static final byte NUL = 0x00;

	/**
	 * Start of Header
	 */
	public static final byte SOH = 0x01;

	/**
	 * Start of Text
	 */
	public static final byte STX = 0x02;

	/**
	 * End of Text
	 */
	public static final byte ETX = 0x03;

	/**
	 * End of Transmission
	 */
	public static final byte EOT = 0x04;

	/**
	 * Enquiry
	 */
	public static final byte ENQ = 0x05;

	/**
	 * Acknowledge
	 */
	public static final byte ACK = 0x06;

	/**
	 * Bell
	 */
	public static final byte BEL = 0x07;

	/**
	 * Backspace
	 */
	public static final byte BS = 0x08;

	/**
	 * Horizontal Tab
	 */
	public static final byte HT = 0x09;

	/**
	 * Line Feed
	 */
	public static final byte LF = 0x0A;

	/**
	 * Vertical Tab
	 */
	public static final byte VT = 0x0B;

	/**
	 * Form Feed
	 */
	public static final byte FF = 0x0C;

	/**
	 * Carriage Return
	 */
	public static final byte CR = 0x0D;

	/**
	 * Shift Out
	 */
	public static final byte SO = 0x0E;

	/**
	 * Shift In
	 */
	public static final byte SI = 0x0F;

	/**
	 * Data Link Escape
	 */
	public static final byte DLE = 0x10;

	/**
	 * Device Control 1
	 */
	public static final byte DC1 = 0x11;

	/**
	 * Device Control 2
	 */
	public static final byte DC2 = 0x12;

	/**
	 * Device Control 3
	 */
	public static final byte DC3 = 0x13;

	/**
	 * Device Control 4
	 */
	public static final byte DC4 = 0x14;

	/**
	 * Negative Acknowledge
	 */
	public static final byte NAK = 0x15;

	/**
	 * Synchronize
	 */
	public static final byte SYN = 0x16;

	/**
	 * End of Transmission Block
	 */
	public static final byte ETB = 0x17;

	/**
	 * Cancel
	 */
	public static final byte CAN = 0x18;

	/**
	 * End of Medium
	 */
	public static final byte EM = 0x19;

	/**
	 * Substitute
	 */
	public static final byte SUB = 0x1A;

	/**
	 * Escape
	 */
	public static final byte ESC = 0x1B;

	/**
	 * File Separator
	 */
	public static final byte FS = 0x1C;

	/**
	 * Group Separator
	 */
	public static final byte GS = 0x1D;

	/**
	 * Record Separator
	 */
	public static final byte RS = 0x1E;

	/**
	 * Unit Separator
	 */
	public static final byte US = 0x1F;

	public static String getCodeNameByValue(byte value) {

		for (Field f : ControlCodes.class.getDeclaredFields()) {
			try {
				f.setAccessible(true);
				if (f.canAccess(null)) {

					if(value == f.getByte(null)) {
						return f.getName();
					}

				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		return null;
	}
	
	public static Byte getCodeByName(String name) {
		try {
			Field field = ControlCodes.class.getDeclaredField(name);
			field.setAccessible(true);
			return field.getByte(null);
		} catch (NoSuchFieldException e) {
			System.err.println("Code not found!");
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static HashMap<String, Byte> getAsMap() {
		HashMap<String, Byte> map = new HashMap<String, Byte>();
		for (Field f : ControlCodes.class.getDeclaredFields()) {
			try {
				f.setAccessible(true);
				if (f.isAccessible()) {

					map.put(f.getName(), f.getByte(null));

				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		return map;
	}

}
