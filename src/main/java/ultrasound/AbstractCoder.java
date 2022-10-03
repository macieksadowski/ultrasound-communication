package ultrasound;

import ultrasound.dataframe.IDataFrame;
import ultrasound.utils.log.ILogger;

/**
 * Abstract class contains common fields used both in decoder and encoder
 */
public abstract class AbstractCoder implements ICoder {

	protected int sampleRate;
	protected int noOfChannels;
	protected int firstFreq;
	protected int freqStep;
	protected int[][] freq;
	protected boolean secdedEnabled = true;
	protected double tOnePulse;

	protected boolean isRunning;
	protected int N;

	protected CoderMode mode;
	protected IDataFrame frame;

	protected ILogger logger;

	/**
	 * Internal constructor for a new abstract encoder/decoder object. To
	 * instantiate new object of this type use {@link AbstractCoderBuilder}
	 * 
	 * @param builder {@link AbstractCoderBuilder}
	 */
	protected AbstractCoder(AbstractCoderBuilder builder) {
		this.sampleRate = builder.sampleRate;
		this.noOfChannels = builder.noOfChannels;
		this.firstFreq = builder.firstFreq;
		this.freqStep = builder.freqStep;
		this.mode = builder.mode;

		if (builder.secdedEnabled != null) {
			this.secdedEnabled = builder.secdedEnabled;
		}

		this.tOnePulse = 2.0 / freqStep;
		if (builder.tOnePulse != 0) {
			this.tOnePulse = builder.tOnePulse;
		}

		this.freq = new int[noOfChannels][2];

		for (int i = 0; i < noOfChannels; i++) {
			freq[i][0] = firstFreq + i * 2 * freqStep;
			freq[i][1] = firstFreq + freqStep + i * 2 * freqStep;
		}
	}

	public Integer getSampleRate() {
		return sampleRate;
	}

	public int getNoOfChannels() {
		return noOfChannels;
	}

	public int getFirstFreq() {
		return firstFreq;
	}

	public int getFreqStep() {
		return freqStep;
	}

	public double gettOnePulse() {
		return tOnePulse;
	}

	public boolean isSecdedEnabled() {
		return secdedEnabled;
	}

	public CoderMode getMode() {
		return mode;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public IDataFrame getDataFrame() {
		return frame;
	}

}
