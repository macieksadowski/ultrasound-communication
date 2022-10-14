package ultrasound.decoder;

import java.util.Arrays;

import ultrasound.dataframe.CheckAddressResult;
import ultrasound.dataframe.ParserResult;

public class MockDecoder extends AbstractDecoder implements IDecoder {
	
    private short[] audioData;
    private int i;

	
	protected MockDecoder(MockDecoderBuilder builder) throws Exception {
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
	public void setDeviceAddress(Byte adr) {
		
	}

	@Override
	public ParserResult getParserResult() {
		return null;
	}

	@Override
	public CheckAddressResult getCheckAddressParserResult() {
		return null;
	}

	@Override
	protected void onDataFrameSuccessfullyReceived() {

	}

	@Override
	protected void stopAudioRecorder() {

	}

	@Override
	protected void startRecording() {

	}
}
