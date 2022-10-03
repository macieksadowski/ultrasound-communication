package ultrasound.dataframe;

import java.io.ByteArrayOutputStream;

import ultrasound.dataframe.CheckAddressResult.CheckAddressResultValues;
import ultrasound.dataframe.ParserResult.ParserResultValues;

public class DataFrameHelper {

	private DataFrameHelper() {

	}

	public static IDataFrame parseDataFrame(byte[] byteArr, int noOfChannels, ParserResult result, Byte deviceAddress,
			CheckAddressResult checkAddressResult) {

		IDataFrame frame = null;

		result.set(ParserResultValues.INCORRECT_FRAME_LENGTH);

		if (byteArr.length >= DataFrame.MIN_DATA_FRAME_LENGTH_IN_BYTES) {

			result.set(ParserResultValues.START_BYTE_NOT_FOUND);
			boolean startByteFound = byteArr[0] == IAsciiControlCodes.SOH;
			if (startByteFound) {
				byte address = byteArr[1];
				checkAddressResult.set(checkAddress(byteArr, deviceAddress));
				if (checkAddressResult.get() == CheckAddressResultValues.OK
						|| checkAddressResult.get() == CheckAddressResultValues.BROADCAST) {

					DataFrame.DataFrameBuilder builder = new DataFrame.DataFrameBuilder(address, noOfChannels);

					byte command = byteArr[2];
					builder.command(command);

					// Frame with command should always have minimum length!
					if (command != IAsciiControlCodes.STX
							&& byteArr.length != DataFrame.MIN_DATA_FRAME_LENGTH_IN_BYTES) {
						result.set(ParserResultValues.INCORRECT_FRAME_LENGTH);
						return frame;
					}

					int pos = 3;
					ByteArrayOutputStream dataStr = new ByteArrayOutputStream();
					if (command == IAsciiControlCodes.STX) {

						for (; pos < byteArr.length; pos++) {
							if (byteArr[pos] == IAsciiControlCodes.ETX)
								break;
							dataStr.write(byteArr[pos]);
						}
						builder.data(dataStr.toByteArray());
					}
					byte checksum = byteArr[byteArr.length - 2];

					try {
						frame = builder.build();
					} catch (Exception e) {
						result.set(ParserResultValues.FRAME_BUILD_ERROR);
					}
					if (frame == null) {
						result.set(ParserResultValues.FRAME_BUILD_ERROR);
					} else {
						if (frame.getChecksum() == checksum) {
							result.set(ParserResultValues.PARSING_OK);
						} else {
							result.set(ParserResultValues.CHECKSUM_INCORRECT);
						}
					}
				} else {
					result.set(ParserResultValues.OTHER_RECIPIENT);
				}
			}
		}

		return frame;
	}

	private static CheckAddressResultValues checkAddress(byte[] byteArr, Byte deviceAddress) {

		if (deviceAddress == null) {
			return CheckAddressResultValues.OK;
		}

		CheckAddressResultValues result = null;

		byte address = byteArr[1];
		if (address == IDataFrame.BROADCAST_ADDRESS) {
			result = CheckAddressResultValues.BROADCAST;
		} else if (address == deviceAddress) {
			result = CheckAddressResultValues.OK;
		} else {
			result = CheckAddressResultValues.OTHER_RECIPIENT;
		}

		return result;
	}

}
