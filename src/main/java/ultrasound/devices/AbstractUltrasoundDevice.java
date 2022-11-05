package ultrasound.devices;

import org.apache.commons.lang3.time.StopWatch;

import ultrasound.dataframe.CheckAddressResult;
import ultrasound.dataframe.DataFrame.DataFrameBuilder;
import ultrasound.decoder.IDecoder;
import ultrasound.encoder.IEncoder;
import ultrasound.dataframe.IDataFrame;
import ultrasound.dataframe.ParserResult;
import ultrasound.utils.log.DecoderLogger;
import ultrasound.utils.log.DeviceLogger;
import ultrasound.utils.log.EncoderLogger;
import ultrasound.utils.log.ILogger;

public abstract class AbstractUltrasoundDevice implements IDevice {

	protected static final long DEFAULT_TIMEOUT = 20000;
	protected static final long NO_TIMEOUT = 0;

	protected volatile boolean isRunning;

	protected IEncoder encoder;
	protected IDecoder decoder;
	protected Thread decoderThread;

	protected ParserResult result;
	protected CheckAddressResult checkAdrResult;
	protected IDataFrame receivedDataFrame;

	private Byte address;

	protected ILogger logger;

	protected AbstractUltrasoundDevice(byte address, IEncoder encoder, IDecoder decoder) {
		this.address = address;

		this.encoder = encoder;
		this.decoder = decoder;
		this.decoder.setDeviceAddress(address);

		logger = DeviceLogger.getInstance();
		EncoderLogger.getInstance().setLogOut(logger.getOut());
		DecoderLogger.getInstance().setLogOut(logger.getOut());

		logger.logMessage(this.toString());

	}

	public byte getAddress() {
		return address;
	}

	protected void receive(final Long timeout) {
		StopWatch watch = new StopWatch();
		watch.start();
		decoderThread = new Thread(decoder);
		decoderThread.setName("Device Decoder");
		decoderThread.start();
		do {
			pause(10, null);
			if (decoder.endOfTransmissionReceived()) {
				result = decoder.getParserResult();
				checkAdrResult = decoder.getCheckAddressParserResult();
				receivedDataFrame = decoder.getDataFrame();
				decoder.clearResult();
				stopDecoder();
				pause(1000, "Wait to perform action onTransmissionReceived");
				onTransmissionReceived();
				pause(1000, "Wait after responsing to transmission");
			}
			if (timeout > 0 && watch.getTime() >= timeout) {
				stopDecoder();
				onDecoderTimeout();
			}
		} while (decoder.isRunning());
	}
	
	public void stop() {
		isRunning = false;
		encoder.stop();
		stopDecoder();
	}

	protected void stopDecoder() {
		if (decoderThread != null && decoder.isRunning()) {
			decoder.stop();
			try {
				decoderThread.join(100);
			} catch (InterruptedException e) {
				decoderThread.interrupt();
			}
			decoderThread = null;
		} else {
			logger.logMessage("Decoder isn't running, nothing to do...");
		}

	}

	protected void send(byte receiverAddress, byte command, byte[] data) {
		IDataFrame frame = null;
		try {
			frame = new DataFrameBuilder(receiverAddress, encoder.getNoOfChannels()).command(command).data(data)
					.build();
		} catch (Exception e) {
			logger.logMessage(e.getMessage());
		}
		if (frame != null) {
			encoder.setDataFrame(frame);
			encoder.run();
		}
	}

	protected abstract void onTransmissionReceived();

	protected abstract void onDecoderTimeout();

	protected void pause(long duration, String msg) {

		try {
			if(msg != null && !msg.isEmpty()) {
				logger.logMessage("Pause " + duration + "ms: " + msg);
				logger.logMessage("Thread: " + Thread.currentThread().getName());
			}
			Thread.sleep(duration);
			if(msg != null && !msg.isEmpty()) {
				logger.logMessage("Pause ended");
			}
		} catch (InterruptedException e) {
			logger.logMessage("PAUSE BREAK: " + e.getLocalizedMessage());
			Thread.currentThread().interrupt();
		}
	}

	public IDataFrame getReceivedDataFrame() {
		return receivedDataFrame;
	}

	public ParserResult getResult() {
		return result;
	}

	public boolean isRunning() {
		return isRunning;
	}

}
