package ultrasound.decoder;

import java.util.Arrays;

class MockDecoderSimple extends AbstractDecoderSimple {

    private short[] audioData;
    private int i;

    protected MockDecoderSimple(MockDecoderBuilder builder) throws Exception {
        super(builder);
        this.audioData = builder.audioData;
        i = N;
        this.recordFrag = new short[N];
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
		
	}

}
