package ultrasound.dataframe;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ControlCodes {

	private ControlCodes() {

	}

	@SuppressWarnings("deprecation")
	public static String getCodeNameByValue(byte value) {

		for (Field f : IControlCodes.class.getDeclaredFields()) {
			try {
				f.setAccessible(true);
				if (f.isAccessible()) {

					if (value == f.getByte(null)) {
						return f.getName();
					}

				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public static Byte getCodeByName(String name) {
		try {
			Field field = IControlCodes.class.getDeclaredField(name);
			field.setAccessible(true);
			return field.getByte(null);
		} catch (NoSuchFieldException e) {
			System.err.println("Code not found!");
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	public static Map<String, Byte> getAsMap() {
		HashMap<String, Byte> map = new HashMap<>();
		for (Field f : ControlCodes.class.getDeclaredFields()) {
			try {
				f.setAccessible(true);
				if (f.isAccessible()) {

					map.put(f.getName(), f.getByte(null));

				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		return map;
	}

}
