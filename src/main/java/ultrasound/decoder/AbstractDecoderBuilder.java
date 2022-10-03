package ultrasound.decoder;

import ultrasound.AbstractCoderBuilder;

/**
 * Abstract builder class used to create new instances of
 * {@link AbstractDecoder}. It should be overridden in platform dependent
 * IDecoder implementation.
 */
public abstract class AbstractDecoderBuilder extends AbstractCoderBuilder implements IDecoderBuilder {

	protected final int nfft;
	protected final double threshold;

	/**
	 * Constructor for a new AbstractDecoder builder object. It should be overridden
	 * in platform dependent IDecoder implementation.
	 * 
	 * @param sampleRate   sample rate used by decoder
	 * @param noOfChannels number of transmission channels (has to be a power of 2)
	 * @param firstFreq    lowest frequency used by decoder (it will be a frequency
	 *                     of low signal of the first channel)
	 * @param freqStep     frequency interval between successive transmission
	 *                     channels
	 * @param nfft         size of FFT transform given as natural number n, where
	 *                     FFT Size = 2^n
	 * @param threshold    minimum amplitude of frequency to be detected as searched
	 *                     signal
	 */
	protected AbstractDecoderBuilder(int sampleRate, int noOfChannels, int firstFreq, int freqStep, int nfft,
			double threshold) {
		super(sampleRate, noOfChannels, firstFreq, freqStep);
		this.nfft = nfft;
		this.threshold = threshold;
	}

	/**
	 * This method should return a new {@link IDecoderSimple} object when all
	 * parameterswere correctly validated.
	 */
	@Override
	public abstract IDecoderSimple build();

	@Override
	protected void validate() {
		super.validate();
		// check if Nfft is a power of 2
		if ((nfft & nfft - 1) != 0) {
			throw new IllegalArgumentException("Nfft must be a power of 2! Decoder Stopped!");
		}
	}
}