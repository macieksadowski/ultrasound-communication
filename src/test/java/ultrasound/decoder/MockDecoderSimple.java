package ultrasound.decoder;

import java.util.Arrays;

class MockDecoderSimple extends AbstractDecoderSimple {

    private short[] audioData;
    private int i;

    protected MockDecoderSimple(MockDecoderBuilder builder) throws Exception {
        super(builder);
        this.audioData = builder.audioData;
        i = 0;
    }

    @Override
    public short[] getAudioSamples() {

        if (i < audioData.length) {
        	i += N;
            return Arrays.copyOfRange(this.audioData, i - N, i);
        }
        return new short[0];

    }

	@Override
	protected void stopAudioRecorder() {
		
	}

	@Override
	protected void startRecording() {
		
	}

	@Override
	public void clearResult() {
		
	}

}
