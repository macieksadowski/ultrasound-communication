package ultrasound;

import ultrasound.ICoder.CoderMode;

/**
 * Abstract builder class used to create new instances of {@link AbstractCoder}.
 * It should be overridden in platform dependent Device implementation.
 */
public abstract class AbstractCoderBuilder implements ICoderBuilder {

	final int sampleRate;
	final int noOfChannels;
	final int firstFreq;
	final int freqStep;
	Boolean secdedEnabled;
	double tOnePulse;
	CoderMode mode;

	/**
	 * Constructor for a new AbstractCoder builder object. It should be overridden
	 * in platform dependent IDevice implementation.
	 * 
	 * @param sampleRate   sample rate used by device
	 * @param noOfChannels number of transmission channels (has to be a power of 2)
	 * @param firstFreq    lowest frequency used by device (it will be a frequency
	 *                     of low signal of the first channel)
	 * @param freqStep     frequency interval between successive transmission
	 *                     channels
	 */
	protected AbstractCoderBuilder(int sampleRate, int noOfChannels, int firstFreq, int freqStep) {
		this.sampleRate = sampleRate;
		this.noOfChannels = noOfChannels;
		this.firstFreq = firstFreq;
		this.freqStep = freqStep;
		this.mode = ICoder.CoderMode.SIMPLE;
	}

	public ICoderBuilder tOnePulse(double tOnePulse) {
		this.tOnePulse = tOnePulse;
		return this;
	}

	public ICoderBuilder mode(CoderMode mode) {
		this.mode = mode;
		return this;
	}

	public ICoderBuilder secdedEnabled(boolean secdedEnabled) {
		this.secdedEnabled = secdedEnabled;
		return this;
	}

	/**
	 * Any parameters' validation should be performed in this method. It should be
	 * then call in {@link AbstractCoderBuilder#build()} before instantiating new
	 * object.
	 */
	protected void validate() {

	}
}