package ultrasound.decoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import ultrasound.dataframe.CheckAddressResult;
import ultrasound.dataframe.DataFrame;
import ultrasound.dataframe.DataFrameHelper;
import ultrasound.dataframe.IAsciiControlCodes;
import ultrasound.dataframe.ParserResult;
import ultrasound.dataframe.ParserResult.ParserResultValues;
import ultrasound.utils.UltrasoundHelper;

/**
 *
 */
public abstract class AbstractDecoder extends AbstractDecoderSimple implements IDecoder  {

	protected ByteArrayOutputStream resByte;
	
	private ParserResult result;
	private CheckAddressResult checkAdrResult;

	protected ArrayList<DataFrame> dataFrames;
	protected Byte deviceAddress;
	
	private boolean endOfTransmission;
	
	protected AbstractDecoder(AbstractDecoderBuilder builder) throws Exception {
		super(builder);
		
		this.resByte = new ByteArrayOutputStream();
		
		this.dataFrames = new ArrayList<>();
	}
	
	protected abstract void onDataFrameSuccessfullyReceived();
	
	
	@Override
	public void clearReceivedDataBuffers() {
		super.clearReceivedDataBuffers();
		resByte.reset();
	}
	
	@Override
	public void clearResult() {
		endOfTransmission = false;
		result = null;
		checkAdrResult = null;
		frame = null;
	}

	@Override
	protected void onNewBinaryDataDecoded(boolean[] resBinDec) {

		byte[] res = UltrasoundHelper.bin2byte(resBinDec);
		try {
			resByte.write(res);
		} catch (IOException e) {
			logger.logMessage(e.getMessage());
		}
		for (int i = 0; i < res.length; i++) {
			if (res[i] == IAsciiControlCodes.EOT) {
				onEOTReceived();
				clearReceivedDataBuffers();
			}
		}
	}
	
	private void onEOTReceived() {
		endOfTransmission = true;
		logger.logMessage("End of frame byte received");

		result = new ParserResult();
		checkAdrResult = new CheckAddressResult();

		frame = DataFrameHelper.parseDataFrame(resByte.toByteArray(), noOfChannels, result, deviceAddress,
				checkAdrResult);
		if (result.get() == ParserResultValues.PARSING_OK) {
			logger.logMessage("Data frame received successfully");
			logger.logMessage(frame.toString());
			onDataFrameSuccessfullyReceived();
		} else {
			logger.logMessage("Data frame parsing result: " + result.get().toString());
		}
	}
	
	public ParserResult getParserResult() {
		return result;
	}

	public CheckAddressResult getCheckAddressParserResult() {
		return checkAdrResult;
	}

	public void setDeviceAddress(Byte adr) {
		this.deviceAddress = adr;
	}
	
	public boolean endOfTransmissionReceived() {
		return endOfTransmission;
	}

	
}