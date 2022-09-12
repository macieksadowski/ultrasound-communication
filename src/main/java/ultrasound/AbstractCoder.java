package ultrasound;

import ultrasound.AbstractEncoder.AbstractEncoderBuilder;

/**
 * Abstract class contains common fields used both in decoder and encoder
 */
public abstract class AbstractCoder {

    protected int sampleRate;
    protected int noOfChannels;
    protected int firstFreq;
    protected int freqStep;
    protected int[][] freq;
    protected boolean secdedEnabled = true;
	protected double tOnePulse;
	
	protected CoderMode mode;
	protected DataFrame frame;

    /**
     * Constructor for a new abstract encoder/decoder object
     * @param sampleRate sample rate used by decoder
     * @param noOfChannels number of transmission channels (has to be a power of 2)
     * @param firstFreq lowest frequency used by decoder (it will be a frequency of low signal of the first channel)
     * @param freqStep frequency interval between successive transmission channels
     */   
    protected AbstractCoder(AbstractCoderBuilder builder) {
        this.sampleRate = builder.sampleRate;
        this.noOfChannels = builder.noOfChannels;
        this.firstFreq = builder.firstFreq;
        this.freqStep = builder.freqStep;
        this.mode = builder.mode;
        
        if(builder.secdedEnabled != null) {
        	this.secdedEnabled = builder.secdedEnabled;
        }
        
        this.tOnePulse = 2.0 / (double) freqStep;
        if(builder.tOnePulse != 0) {
        	this.tOnePulse = builder.tOnePulse;
        }
        
        this.freq = new int[noOfChannels][2];

        for (int i = 0; i < noOfChannels; i++) {
            freq[i][0] = firstFreq + i * 2 * freqStep;
            freq[i][1] = firstFreq + freqStep + i * 2 * freqStep;
        }
    }
    
    public enum CoderMode {
    	SIMPLE,
    	DATA_FRAME
    }
    
    public abstract static class AbstractCoderBuilder {
    	
    	private final int sampleRate;
    	private final int noOfChannels;
    	private final int firstFreq;
    	private final int freqStep;
    	private Boolean secdedEnabled;
		private double tOnePulse;
		private CoderMode mode;
		
    	public AbstractCoderBuilder(int sampleRate, int noOfChannels, int firstFreq, int freqStep) {
    		this.sampleRate = sampleRate;
    		 this.noOfChannels = noOfChannels;
    	     this.firstFreq = firstFreq;
    	     this.freqStep = freqStep;
    	     this.mode = AbstractCoder.CoderMode.SIMPLE;
    	}
    	
		public AbstractCoderBuilder tOnePulse(double tOnePulse) {
			this.tOnePulse = tOnePulse;
			return this;
		}
		
		public AbstractCoderBuilder mode(CoderMode mode) {
			this.mode = mode;
			return this;
		}
    	
    	public AbstractCoderBuilder secdedEnabled(boolean secdedEnabled) {
    		this.secdedEnabled = secdedEnabled;
    		return this;
    	}
    	
    	public abstract AbstractCoder build();
    	
    	protected void validate() {

		}
    }
    
    protected abstract void logMessage(String message);

	/**
	 * Get the sample rate used in encoder/decoder
	 * @return Integer sample rate of encoder/decoder
	 */
	public Integer getSampleRate() {
        return sampleRate;
    }

	
	public boolean isSecdedEnabled() {
		return secdedEnabled;
	}
	
	public CoderMode getMode() {
		return mode;
	}

}
