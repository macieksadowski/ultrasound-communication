package ultrasound.encoder;

import ultrasound.AbstractCoderBuilder;

public abstract class AbstractEncoderBuilder extends AbstractCoderBuilder implements IEncoderBuilder {

	protected double tBreak;
	protected double fadeLength;

	/**
	 * 
	 * @param sampleRate   sample rate used by encoder
	 * @param noOfChannels number of transmission channels (has to be a power of 2)
	 * @param firstFreq    lowest frequency used by encoder (it will be a frequency
	 *                     of low signal of the first channel)
	 * @param freqStep     frequency interval between successive transmission
	 *                     channels
	 */
	protected AbstractEncoderBuilder(int sampleRate, int noOfChannels, int firstFreq, int freqStep) {
		super(sampleRate, noOfChannels, firstFreq, freqStep);
	}

	public IEncoderBuilder tBreak(double tBreak) {
		this.tBreak = tBreak;
		return this;
	}

	public IEncoderBuilder fadeLength(double fadeLength) {
		this.fadeLength = fadeLength;
		return this;
	}

	/**
	 * This method should return a new {@link IEncoder} object when all parameters
	 * were correctly validated.
	 */
	@Override
	public abstract IEncoder build();

}