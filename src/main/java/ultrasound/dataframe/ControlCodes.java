package ultrasound.dataframe;

import java.lang.reflect.Field;
import java.util.HashMap;

public class ControlCodes {
	
	public static String getCodeNameByValue(byte value) {

		for (Field f : IAsciiControlCodes.class.getDeclaredFields()) {
			try {
				f.setAccessible(true);
				if (f.isAccessible()) {

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
			Field field = IAsciiControlCodes.class.getDeclaredField(name);
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
