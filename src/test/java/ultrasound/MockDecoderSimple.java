package ultrasound;

import java.util.Arrays;

import ultrasound.decoder.AbstractDecoderBuilder;
import ultrasound.decoder.AbstractDecoderSimple;

class MockDecoderSimple extends AbstractDecoderSimple {

    private short[] audioData;
    private int i;

    private MockDecoderSimple(MockDecoderBuilder builder) throws Exception {
        super(builder);
        this.audioData = builder.audioData;
        i = N;
        this.recordFrag = new short[N];
    }

    public static class MockDecoderBuilder extends AbstractDecoderBuilder {

        private short[] audioData;

        public MockDecoderBuilder(int sampleRate, int noOfChannels, int firstFreq, int freqStep, int nfft,
                                  double threshold) {
            super(sampleRate, noOfChannels, firstFreq, freqStep, nfft, threshold);

        }

        public MockDecoderBuilder audioDataForMock(short[] audioData) {
            this.audioData = audioData;
            return this;
        }

        @Override
        public MockDecoderSimple build() {
            MockDecoderSimple decoder;
            try {
                decoder = new MockDecoderSimple(this);
                return decoder;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }


    }

    @Override
    public short[] getAudioSamples() {

        short[] frag = new short[N];
        if (i < audioData.length)
            frag = Arrays.copyOfRange(this.audioData, i - N, i);
        i += N;
        return frag;

    }

	@Override
	protected void stopAudioRecorder() {
		
	}

	@Override
	protected void startRecording() {
		
	}

	@Override
	public void clearResult() {
		// TODO Auto-generated method stub
		
	}

}
