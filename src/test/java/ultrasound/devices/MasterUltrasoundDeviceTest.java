package ultrasound.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ultrasound.dataframe.CheckAddressResult;
import ultrasound.dataframe.DataFrame;
import ultrasound.dataframe.DataFrameHelper;
import ultrasound.dataframe.IAsciiControlCodes;
import ultrasound.dataframe.IDataFrame;
import ultrasound.dataframe.ParserResult;
import ultrasound.dataframe.ParserResult.ParserResultValues;
import ultrasound.decoder.IDecoder;
import ultrasound.encoder.IEncoder;

@ExtendWith(MockitoExtension.class)
class MasterUltrasoundDeviceTest {

	@Mock
	private IDecoder decoder;
	
	@Mock
	private IEncoder encoder;
	
	
	private MasterUltrasoundDevice master;
	private Thread masterThread;
	
	private static final int NO_OF_CHANNELS = 4;
	
	private IDataFrame ackFrame;
	
	@BeforeEach
	void init() throws Exception {
		master = new MasterUltrasoundDevice(encoder, decoder);
		masterThread = new Thread(master);
		when(encoder.getNoOfChannels()).thenReturn(NO_OF_CHANNELS);
		
		ackFrame = new DataFrame.DataFrameBuilder(NO_OF_CHANNELS).receiverAddress(IDataFrame.MASTER_ADDRESS).command(IAsciiControlCodes.ACK).build();
	}
	
	@Nested
	class sendBroadcast {
		@Test
		void testSendBroadcastCmd() throws InterruptedException {
			byte cmd = (byte) 0x01;
			
			master.sendBroadcast(cmd);
			masterThread.start();
			masterThread.join(100);
			master.stop();
			
			verify(encoder).run();
		}
		
		@Test
		void testSendBroadcastData() throws InterruptedException {
			byte[] data = {(byte) 0x01, (byte) 0xad, (byte) 0xff};
			
			master.sendBroadcast(data);
			masterThread.start();
			masterThread.join(100);
			master.stop();
			
			verify(encoder).run();
		}
	}
	
	@Nested
	class sendWithConfirmation {
		@Test
		void testSendAcknowledgeReceived() throws InterruptedException {
			

			ParserResult parserResult = new ParserResult();
			CheckAddressResult checkAddressResult = new CheckAddressResult();
			IDataFrame frame = DataFrameHelper.parseDataFrame(ackFrame.get(), NO_OF_CHANNELS, parserResult, IDataFrame.MASTER_ADDRESS, checkAddressResult);
			
			byte receiverAdr = (byte) 0x01;
			byte cmd = (byte) 0x01;
			
			when(decoder.endOfTransmissionReceived()).thenReturn(true);
			when(decoder.getParserResult()).thenReturn(parserResult);
			when(decoder.getCheckAddressParserResult()).thenReturn(checkAddressResult);
			when(decoder.getDataFrame()).thenReturn(ackFrame);
			when(decoder.isRunning()).thenReturn(master.isRunning());
			
			master.send(receiverAdr, cmd);
			masterThread.start();
			do {
				Thread.sleep(10);
			} while (master.getResult() == null);
			masterThread.join(1000);
			master.stop();
			
			verify(encoder).run();
			verify(decoder).run();
			
			assertEquals(IAsciiControlCodes.ACK, master.getReceivedDataFrame().getCommand());
			assertEquals(ParserResultValues.PARSING_OK, master.getResult().get());
			
		}
		
		@Test
		@Disabled
		void testSendAcknowledgeNotReceived() throws InterruptedException {
			

			byte receiverAdr = (byte) 0x01;
			byte cmd = (byte) 0x01;
			
			when(decoder.endOfTransmissionReceived()).thenReturn(false);
			when(decoder.isRunning()).thenReturn(master.isRunning());
			
			master.send(receiverAdr, cmd);
			masterThread.start();
			StopWatch watch = new StopWatch();
			watch.start();
			do {
				Thread.sleep(10);
			} while (watch.getTime(TimeUnit.SECONDS) < 10);
			watch.stop();
			masterThread.join(1000);
			master.stop();
			
			verify(encoder, times(2)).run();
			verify(decoder, times(2)).run();
			
			
		}
	}
	
	
	
	@AfterEach
	void tearDown() {
		masterThread.interrupt();
	}

}
