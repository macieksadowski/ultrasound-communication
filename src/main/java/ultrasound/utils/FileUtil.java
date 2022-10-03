package ultrasound.utils;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public final class FileUtil {

	private FileUtil() {

	}

	public static boolean saveToAudioFile(String name, AudioFileFormat.Type fileType,
			AudioInputStream audioInputStream) {
		System.out.println("Saving...");
		if (null == name || null == fileType || audioInputStream == null) {
			return false;
		}
		try {
			audioInputStream.reset();
		} catch (Exception e) {
			return false;
		}
		int i = 0;
		File myFile = new File(name + "." + fileType.getExtension());
		while (myFile.exists()) {
			String temp = "" + i + myFile.getName();
			myFile = new File(temp);
		}
		try {
			AudioSystem.write(audioInputStream, fileType, myFile);
		} catch (Exception ex) {
			return false;
		}
		System.out.println("Saved " + myFile.getAbsolutePath());
		return true;
	}

	public static boolean saveToFile(String name, short[] data) {

		if (null == name || null == data) {
			return false;
		}
		File myFile = new File(name + ".csv");
		int i = 0;
		while (myFile.exists()) {
			String temp = "" + i + myFile.getName();
			myFile = new File(temp);
			i++;
		}
		try (FileWriter writer = new FileWriter(myFile,StandardCharsets.UTF_8)) {

			int len = data.length;
			for (int j = 0; j < len; j++) {
				writer.write(data[j] + "," + "");
			}
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	public static short[] byteToShort(byte[] input) {
		int shortIndex;
		int byteIndex;
		int iterations = input.length / 2;

		short[] buffer = new short[input.length / 2];

		shortIndex = byteIndex = 0;

		while (shortIndex != iterations) {
			buffer[shortIndex] = (short) ((input[byteIndex] & 0x00FF) | (input[byteIndex + 1] << 8));

			++shortIndex;
			byteIndex += 2;
		}

		return buffer;
	}

	public static boolean saveToFile(String name, double[] data) {

		if (null == name || null == data) {
			return false;
		}
		File myFile = new File(name + ".csv");
		int i = 0;
		while (myFile.exists()) {
			String temp = "" + i + myFile.getName();
			myFile = new File(temp);
			i++;
		}
		try (FileWriter writer = new FileWriter(myFile,StandardCharsets.UTF_8)) {

			int len = data.length;
			for (int j = 0; j < len; j++) {
				writer.write(data[j] + "," + "");
			}

		} catch (Exception ex) {
			return false;
		}

		return true;
	}
}
