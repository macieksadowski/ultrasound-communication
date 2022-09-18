package ultrasound.devices;

import java.io.PrintWriter;

import org.apache.commons.lang3.time.StopWatch;

import ultrasound.IDecoder;
import ultrasound.IEncoder;
import ultrasound.dataframe.IDataFrame;
import ultrasound.dataframe.DataFrame.DataFrameBuilder;
import ultrasound.dataframe.DataFrame.ParserResult;

public abstract class AbstractUltrasoundDevice implements IDevice {
	
	protected static long DEFAULT_TIMEOUT = 20000;
	protected static long NO_TIMEOUT = 0;

	private PrintWriter out;

	protected IEncoder encoder;
	protected IDecoder decoder;
	protected Thread decoderThread;

	protected ParserResult result = null;
	protected IDataFrame receivedDataFrame = null;

	private byte address;


	public AbstractUltrasoundDevice(byte address, IEncoder encoder, IDecoder decoder) {
		this.address = address;

		this.encoder = encoder;
		this.decoder = decoder;
	}

	public byte getAddress() {
		return address;
	}

	public void connectToLogOutput(PrintWriter out) {
		this.out = out;
	}

	protected void logMessage(String s) {
		String msg = "DEV - " + s;
		if (out != null) {
			out.println(msg);
		} else {
			System.out.println(msg);
		}
	}

	protected void receive(Long timeout) {
		StopWatch watch = new StopWatch();

		decoderThread = new Thread(decoder);
		decoderThread.setName(this.getClass().getName() + " decoder thread");
		decoderThread.start();
		watch.start();
		do {
			pause(10);
			if (decoder.endOfTransmissionReceived()) {
				result = decoder.getParserResult();
				receivedDataFrame = decoder.getDataFrame();
				decoder.clearResult();
				stopDecoder();
				logMessage("PAUSE");
				pause(1000);
				onTransmissionReceived();
			} else if (timeout > 0 && watch.getTime() >= timeout) {
				stopDecoder();
				onDecoderTimeout();
			}
		} while (decoder.isRunning());
	}

	protected void stopDecoder() {
		if (decoderThread != null && decoder.isRunning()) {
			decoder.stopRecording();
			try {
				decoderThread.join(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			decoderThread = null;
		} else {
			logMessage("Decoder isn't running, nothing to do...");
		}

	}

	protected void send(byte receiverAddress, byte command, byte[] data) {

		IDataFrame frame = new DataFrameBuilder(receiverAddress, encoder.getNoOfChannels()).command(command).data(data)
				.build();

		encoder.setDataFrame(frame);
		encoder.run();

	}

	protected abstract void onTransmissionReceived();
	
	protected abstract void onDecoderTimeout();

	protected void pause(long duration) {

		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
