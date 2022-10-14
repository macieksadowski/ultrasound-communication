package ultrasound.encoder;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public final class MockEncoder extends AbstractEncoder {

	private List<Short> buffer;

	private MockEncoder(MockEncoderBuilder builder) {

		super(builder);

	}

	public static class MockEncoderBuilder extends AbstractEncoderBuilder {

		public MockEncoderBuilder(int sampleRate, int noOfChannels, int firstFreq, int freqStep) {
			super(sampleRate, noOfChannels, firstFreq, freqStep);

		}

		@Override
		public MockEncoder build() {
			validate();
			return new MockEncoder(this);
		}

	}

	@Override
	protected void playSound(short[] soundData) {

		int byteLength = soundData.length * 2;
		ByteBuffer bb = ByteBuffer.allocate(byteLength);
		bb.asShortBuffer().put(soundData);

		for (short s : soundData) {
			buffer.add(s);
		}
	}

	@Override
	protected void constructAudioStream() {
		buffer = new ArrayList<Short>();
	}

	@Override
	protected void closeAudioStream() {

	}

	public short[] getOutputBuffer() {
		short[] retArr = new short[buffer.size()];
		for (int i = 0; i < retArr.length; i++) {
			retArr[i] = buffer.get(i);
		}
		return retArr;
	}
}