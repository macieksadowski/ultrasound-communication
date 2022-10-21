package ultrasound.encoder;

import static org.junit.Assert.assertArrayEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ultrasound.ICoder.CoderMode;
import ultrasound.TestData;
import ultrasound.dataframe.DataFrame.DataFrameBuilder;
import ultrasound.dataframe.IAsciiControlCodes;
import ultrasound.dataframe.IDataFrame;
import ultrasound.encoder.MockEncoder.MockEncoderBuilder;

class AbstractEncoderTest {
	
	int sampleRate = 48000;

	MockEncoder encoder;
	TestData testData;
	
	private void initTest(String testFileName, String data, CoderMode mode) {
		
		try {
			testData = new TestData(testFileName, sampleRate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		MockEncoderBuilder builder = new MockEncoderBuilder(sampleRate, testData.getNoOfChannels(), testData.getFirstFreq(), testData.getFreqStep());
		builder.mode(mode);
		builder.fadeLength(0.05);
		encoder = builder.build();
	}


	@ParameterizedTest
	@MethodSource("factoryForTestRunDataFrame")
	void testRunDataFrame(String testFileName, String data) {
		
		initTest(testFileName, data, CoderMode.DATA_FRAME);
		
		DataFrameBuilder frameBuilder = new DataFrameBuilder(IDataFrame.BROADCAST_ADDRESS, testData.getNoOfChannels());
		frameBuilder.command(IAsciiControlCodes.STX);
		frameBuilder.data(data.getBytes());

		try {
			IDataFrame frame = frameBuilder.build();
			encoder.setDataFrame(frame);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		encoder.run();
		assertArrayEquals("Data frame with message " + data + " should be generated",testData.getAudioSig(), encoder.getOutputBuffer());
		
	}
	
	private static Stream<Arguments> factoryForTestRunDataFrame() {
		return Stream.of(
				Arguments.of("16F18000S20-Alamakota.csv", "Alamakota")
		);
	}
	
	@Disabled
	@ParameterizedTest
	@MethodSource("factoryForTestRunSimple")
	void testRunSimple(String testFileName, String data) {
		
		initTest(testFileName, data, CoderMode.SIMPLE);
		
		encoder.setHexData(data);
		
		encoder.run();
		assertArrayEquals("Signal with message " + data + " should be generated",testData.getAudioSig(), encoder.getOutputBuffer());
		
	}
	
	private static Stream<Arguments> factoryForTestRunSimple() {
		return Stream.of(
				Arguments.of("16F20000S40-6C.csv", "6C")
		);
	}

}
