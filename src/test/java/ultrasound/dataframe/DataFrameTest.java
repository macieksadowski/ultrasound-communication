package ultrasound.dataframe;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import ultrasound.dataframe.DataFrame.CheckAddressResult;
import ultrasound.dataframe.DataFrame.CheckAddressResultValues;
import ultrasound.dataframe.DataFrame.ParserResult;
import ultrasound.dataframe.DataFrame.ParserResultValues;
import ultrasound.utils.UltrasoundHelper;

@RunWith(JUnitParamsRunner.class)
public class DataFrameTest {

	
	@Test
    @Parameters(method = "parametersToTestParseDataFrame")
    public void parseDataFrameTest(byte[] given, int noOfChannels, ParserResultValues expected) {
	
		CheckAddressResult checkAdrRes = new CheckAddressResult();
		ParserResult parserResult = new ParserResult();	
		try {
			DataFrame.parseDataFrame(given, noOfChannels, parserResult, null, checkAdrRes);
		} catch (Exception e) {
			e.printStackTrace();
		}		
		assertEquals(expected, parserResult.get());
    	
    }
	
    
    @SuppressWarnings("unused")
	private Object[] parametersToTestParseDataFrame() {
		return new Object[] {
			new Object[] {str2byte("0000000111111111000001110000001100000100"), 16, ParserResultValues.PARSING_OK},
			new Object[] {str2byte("000000010000000100000110000000110000001100000100"), 16, ParserResultValues.INCORRECT_FRAME_LENGTH},
			new Object[] {str2byte("000000011111111111111111000001110000001100000100"), 16, ParserResultValues.INCORRECT_FRAME_LENGTH},
		};
    	
    }
    
	@Test
    @Parameters(method = "parametersToParseDataFrameCheckAddress")
    public void parseDataFrameCheckAddressTest(byte[] given, int noOfChannels, byte address, CheckAddressResultValues expected) {
	
		CheckAddressResult checkAdrRes = new CheckAddressResult();
		ParserResult parserResult = new ParserResult();	
		try {
			DataFrame.parseDataFrame(given, noOfChannels, parserResult, address, checkAdrRes);
		} catch (Exception e) {
			e.printStackTrace();
		}		
		assertEquals(expected, checkAdrRes.get());
    	
    }
	
    
    @SuppressWarnings("unused")
	private Object[] parametersToParseDataFrameCheckAddress() {
		return new Object[] {
			new Object[] {str2byte("0000000111111111000001110000001100000100"), 16, IDataFrame.BROADCAST_ADDRESS, CheckAddressResultValues.BROADCAST},
		};
    	
    }
    
    private static byte[] str2byte(String message) {
    	return UltrasoundHelper.bin2byte(UltrasoundHelper.binArrayFromBinStr(message));
    }
    
}
