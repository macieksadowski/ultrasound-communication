package ultrasound.dataframe;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ultrasound.dataframe.CheckAddressResult.CheckAddressResultValues;
import ultrasound.dataframe.ParserResult.ParserResultValues;
import ultrasound.utils.UltrasoundHelper;

public class DataFrameTest {
	
	private int noOfChannels;
	private CheckAddressResult checkAdrRes;
	private ParserResult parserResult;
	
	@BeforeEach
	void init() {
		noOfChannels = 16;
		checkAdrRes = new CheckAddressResult();
		parserResult = new ParserResult();
	}

	
	
	 //CHECKSUM_INCORRECT, OTHER_RECIPIENT
	@Nested
	class parserTest {
		
		@Test 
		void testReturnParsingOk() {
			byte[] given = str2byte("0000000111111111000001110000001100000100");
			ParserResultValues expectedParserResult = ParserResultValues.PARSING_OK;
			DataFrameHelper.parseDataFrame(given, noOfChannels, parserResult, (byte) 0x01, checkAdrRes);
			assertEquals(expectedParserResult, parserResult.get());
		}
		
		@Test
		void testReturnIncorrectFrameLengthTooShort() {
			byte[] given = str2byte("00000001111111110000001100000100");
			ParserResultValues expected = ParserResultValues.INCORRECT_FRAME_LENGTH;
			DataFrameHelper.parseDataFrame(given, noOfChannels, parserResult, null, checkAdrRes);
			assertEquals(expected, parserResult.get());
		}
		
		@Test
		void testReturnIncorrectFrameLengthTooLongCmdFrame() {
			byte[] given = str2byte("000000010000000100000110000000110000001100000100");
			ParserResultValues expected = ParserResultValues.INCORRECT_FRAME_LENGTH;
			DataFrameHelper.parseDataFrame(given, noOfChannels, parserResult, null, checkAdrRes);
			assertEquals(expected, parserResult.get());
		}
		
		@Test
		void testReturnStartByteNotFound() {
			byte[] given = str2byte("0000011100000111000000110000010000000100");
			ParserResultValues expected = ParserResultValues.START_BYTE_NOT_FOUND;
			DataFrameHelper.parseDataFrame(given, noOfChannels, parserResult, null, checkAdrRes);
			assertEquals(expected, parserResult.get());
		}
		
		@Test 
		void testReturnFrameBuildError() {
			byte[] given = str2byte("0000000111111111000000100000001100000100");
			ParserResultValues expectedParserResult = ParserResultValues.FRAME_BUILD_ERROR;
			DataFrameHelper.parseDataFrame(given, noOfChannels, parserResult, (byte) 0x01, checkAdrRes);
			assertEquals(expectedParserResult, parserResult.get());
		}
		
		@Test 
		void testReturnChecksumIncorrect() {
			byte[] given = str2byte("000000011111111100000010010000110000001100000100");
			ParserResultValues expectedParserResult = ParserResultValues.CHECKSUM_INCORRECT;
			DataFrameHelper.parseDataFrame(given, noOfChannels, parserResult, (byte) 0x01, checkAdrRes);
			assertEquals(expectedParserResult, parserResult.get());
		}
	}
	
	
	
	
	@Nested
	class parseAddressTest {
		
		@Test 
		void testReturnCheckAddressBroadcast() {
			byte[] given = str2byte("0000000111111111000001110000001100000100");
			CheckAddressResultValues expectedCheckAddressResult = CheckAddressResultValues.BROADCAST;
			DataFrameHelper.parseDataFrame(given, noOfChannels, parserResult, (byte) 0x01, checkAdrRes);
			assertEquals(expectedCheckAddressResult, checkAdrRes.get());
			
		}
		
		@Test 
		void testReturnCheckAddressOkOnNoDeviceAddress() {
			byte[] given = str2byte("0000000111111111000001110000001100000100");
			CheckAddressResultValues expectedCheckAddressResult = CheckAddressResultValues.OK;
			DataFrameHelper.parseDataFrame(given, noOfChannels, parserResult, null, checkAdrRes);
			assertEquals(expectedCheckAddressResult, checkAdrRes.get());
			
		}
		
		@Test 
		void testReturnCheckAddressOk() {
			byte[] given = str2byte("0000000100000100000001110000001100000100");
			CheckAddressResultValues expectedCheckAddressResult = CheckAddressResultValues.OK;
			DataFrameHelper.parseDataFrame(given, noOfChannels, parserResult, (byte) 0x04, checkAdrRes);
			assertEquals(expectedCheckAddressResult, checkAdrRes.get());
		}
		
		
		
		@Test 
		void testReturnCheckAddressOtherRecipient() {
			byte[] given = str2byte("0000000100000010000001110000001100000100");
			CheckAddressResultValues expectedCheckAddressResult = CheckAddressResultValues.OTHER_RECIPIENT;
			DataFrameHelper.parseDataFrame(given, noOfChannels, parserResult, (byte) 0x01, checkAdrRes);
			assertEquals(expectedCheckAddressResult, checkAdrRes.get());
			
		}
		
		
	}

	private static byte[] str2byte(String message) {
		return UltrasoundHelper.bin2byte(UltrasoundHelper.binArrayFromBinStr(message));
	}

}
