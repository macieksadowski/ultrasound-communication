package ultrasound.decoder;

public class MockDecoderBuilder extends AbstractDecoderBuilder {

    protected short[] audioData;

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
    
    public MockDecoder buildDataFrame() {
        MockDecoder decoder;
        try {
            decoder = new MockDecoder(this);
            return decoder;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}