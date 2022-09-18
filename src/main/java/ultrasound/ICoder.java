package ultrasound;

import ultrasound.dataframe.IDataFrame;

public interface ICoder {
	
    public enum CoderMode {
    	SIMPLE,
    	DATA_FRAME
    }
    
    public interface ICoderBuilder {
    	
		public ICoderBuilder tOnePulse(double tOnePulse);
		
		public ICoderBuilder mode(CoderMode mode);
    	
    	public ICoderBuilder secdedEnabled(boolean secdedEnabled);
    	
    	public abstract ICoder build();
    	
    }

	/**
	 * Get the sample rate used in encoder/decoder
	 * @return Integer sample rate of encoder/decoder
	 */
    public Integer getSampleRate();
	
	public int getNoOfChannels();

	public int getFirstFreq();

	public int getFreqStep();

	public double gettOnePulse();
	
	public boolean isSecdedEnabled();
	
	public CoderMode getMode();
	
	public boolean isRunning();
	
	public IDataFrame getDataFrame();

}