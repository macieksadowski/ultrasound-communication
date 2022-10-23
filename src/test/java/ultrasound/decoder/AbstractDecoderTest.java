package ultrasound.decoder;

import static org.junit.Assert.assertEquals;

import java.util.stream.Stream;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ultrasound.ICoder.CoderMode;
import ultrasound.TestData;
import ultrasound.dataframe.IDataFrame;

class AbstractDecoderTest {
	
	int sampleRate = 48000;

	IDecoderSimple decoderSimple;
	IDecoder decoderDataFrame;
	TestData testData;
    Thread decoderThread;
    double audioSigMockLen = 0.0;
    
    IDataFrame receivedFrame;
    
    int nfft = 12;
    double threshold = 0.3;
    
    private void initTest(String testFileName, String data, CoderMode mode) {
    	
    	try {
			testData = new TestData(testFileName, sampleRate);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	MockDecoderBuilder builder = new MockDecoderBuilder(sampleRate, testData.getNoOfChannels(), testData.getFirstFreq(), testData.getFreqStep(),(int) Math.pow(2, nfft) , threshold);
    	builder.audioDataForMock(testData.getAudioSig());
    	
    	if(mode == CoderMode.SIMPLE) {
            decoderSimple = builder.build();
    	} else {
    		decoderDataFrame = builder.buildDataFrame();
    	}
		
		
        
        audioSigMockLen = (double) testData.getAudioSig().length / (double) sampleRate;
    	
    }
    
    @ParameterizedTest
	@MethodSource("factoryForTestRunDataFrame")
	void testRunDataFrame(String testFileName, String data) {

		initTest(testFileName, data, CoderMode.DATA_FRAME);

		decoderThread = new Thread(decoderDataFrame);
		decoderThread.start();
		do {
			pause(10);
			if (decoderDataFrame.endOfTransmissionReceived()) {
				receivedFrame = decoderDataFrame.getDataFrame();
				decoderDataFrame.stop();
			}
		} while (decoderDataFrame.isRunning());
		
		assertEquals("Data frame with message " + data + " should be decoded", data, new String(receivedFrame.getData()));
		
	}
	
	private static Stream<Arguments> factoryForTestRunDataFrame() {
		return Stream.of(
				Arguments.of("16F18000S20-Alamakota.csv", "Alamakota")
		);
	}

	@ParameterizedTest
	@MethodSource("factoryForTestRunSimple")
	void testRunDecodeSimple(String testFileName, String data) {
		
		initTest(testFileName, data, CoderMode.SIMPLE);

		decoderThread = new Thread(decoderSimple);
		decoderThread.start();
		StopWatch watch = new StopWatch();
		watch.start();
		while (true) {

			if (watch.getTime() / 1000.0 > audioSigMockLen) {
				watch.stop();
				decoderSimple.stop();

				decoderThread = null;
				break;
			}

		}

        assertEquals("Decoded hex data should be " + data, data,decoderSimple.getResHex());
		
	}
	
	private static Stream<Arguments> factoryForTestRunSimple() {
		return Stream.of(
				Arguments.of("16F20000S40-6C.csv", "6c"),
				Arguments.of("4F17000S40-6C.csv", "6c")
		);
	}
	
	protected void pause(long duration) {

		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	

}
