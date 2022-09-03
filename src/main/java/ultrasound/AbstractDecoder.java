package ultrasound;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import sw.FFT;
import ultrasound.AbstractEncoder.AbstractEncoderBuilder;

/**
 *
 */
public abstract class AbstractDecoder extends AbstractCoder implements Runnable {

    protected int nfft;
    private final double threshold;
    
    protected int N;
    protected double delta_f;
	private final double[] hamming;
	
    protected double[] vals;
    protected double[] oldVals;
    protected boolean breakInd;
    
    private final FFT fft;

    private final int lowestAnalyseFreqInd;
    private final int highestAnalyseFreqInd;
    private final int[][] freqInd;  
    protected double[] f;
    
    protected double[] ampl;

    protected short[] recordFrag;
    protected double[] t;
    
    protected String resHex;
    protected boolean[] sigBin = null;
    protected boolean[] sigBinDec = null;

    
    /**
     * Constructor for a new abstract decoder object
     *
     * @param sampleRate   sample rate used by decoder
     * @param noOfChannels number of transmission channels (has to be a power of 2)
     * @param firstFreq    lowest frequency used by decoder (it will be a frequency of low signal of the first channel)
     * @param freqStep     frequency interval between successive transmission channels
     * @param nfft size of FFT transform given as natural number n, where FFT Size = 2^n
     * @param threshold minimum amplitude of frequency to be detected as searched signal
     */
    public AbstractDecoder(AbstractDecoderBuilder builder) throws Exception {
        
    	super(builder);
        this.nfft = builder.nfft;
        this.threshold = builder.threshold;
        

        this.N = (int) Math.ceil(tOnePulse * sampleRate);
        this.delta_f = sampleRate / (double) nfft;
        this.hamming = UltrasoundHelper.hamming(N);

        this.vals = new double[noOfChannels];
        this.oldVals = new double[noOfChannels];
        this.breakInd = false;
        this.fft = new FFT(nfft);
        
        double lowestAnalyseFreq = firstFreq - delta_f;
        double highestAnalyseFreq = freq[freq.length - 1][1] + delta_f;

        lowestAnalyseFreqInd = (int) Math.ceil(lowestAnalyseFreq * nfft / (double) sampleRate);
        highestAnalyseFreqInd = (int) Math.ceil(highestAnalyseFreq * nfft / (double) sampleRate);

        freqInd = new int[freq.length][2];
        for (int i = 0; i < freq.length; i++) {
            freqInd[i][0] = (int) Math.ceil(freq[i][0] * nfft / (double) sampleRate) - lowestAnalyseFreqInd;
            freqInd[i][1] = (int) Math.ceil(freq[i][1] * nfft / (double) sampleRate) - lowestAnalyseFreqInd;
        }

        f = new double[highestAnalyseFreqInd - lowestAnalyseFreqInd];
        for (int i_f = 0; i_f < f.length; i_f++) {
            f[i_f] = (double) ((lowestAnalyseFreqInd + i_f) * sampleRate) / nfft;
        }

    }
    
    public static abstract class AbstractDecoderBuilder extends AbstractCoderBuilder {
    	
    	private final int nfft;
    	private final double threshold;
    	
		public AbstractDecoderBuilder(int sampleRate, int noOfChannels, int firstFreq, int freqStep, int nfft, double threshold) {
			super(sampleRate, noOfChannels, firstFreq, freqStep);
			this.nfft = nfft;
			this.threshold = threshold;
		}
		
		@Override
		public abstract AbstractDecoder build();
		
		protected void validate() {
			super.validate();
			
	        //check if Nfft is a power of 2
	        if ((nfft & nfft - 1) != 0) {
	            throw new IllegalArgumentException("Nfft must be a power of 2! Decoder Stopped!");
	        }
		}
    }

    public int getNfft() {
        return nfft;
    }

    public double[] getAmpl() {
        return ampl;
    }

    public double[] getF() {
        return f;
    }

    public double[] getT() {
        return t;
    }
    
	public Double getTOnePulse() {
		return tOnePulse;
	}


    /**
     *
     */
    protected void decode() {

        double[] frag = new double[N];

        //Convert from short to double
        for (int i = 0; i < N; i++) {
            frag[i] = (double) recordFrag[i] / Short.MAX_VALUE;
        }

        //Hamming window
        for (int i = 0; i < N; i++) {
            frag[i] = frag[i] * hamming[i];
        }

        // Execute fft on selected samples
        ampl = new double[f.length];

        //Zero padding
        int pad = nfft - N;
        double[] x = ArrayUtils.clone(frag);
        if (pad > 0) {
            double[] zeros = new double[pad];
            Arrays.fill(zeros, 0.0);
            x = ArrayUtils.addAll(frag, zeros);
        }

        //FFT Calculation
        double[] y = new double[this.nfft];
        Arrays.fill(y, 0.0);
        this.fft.fft(x, y);

        //show usable FFT output

        for (int ii = 0; ii < ampl.length; ii++) {
            int ind = lowestAnalyseFreqInd + ii;
            ampl[ii] = x[ind] * x[ind] + y[ind] * y[ind];
        }

        //Iterate for every transmission's channel
        boolean valFound = true;
        boolean valChanged = false;

        for (int j = 0; j < noOfChannels; j++) {

            //Analyse only in range of frequencies used by current channel
            int fMaxInd = UltrasoundHelper.findMaxValueIndex(ampl, freqInd[j][0], freqInd[j][1] + 1);
            double Amax = ampl[fMaxInd];

            if (Amax > threshold) {
                vals[j] = f[fMaxInd];
            } else {
                vals[j] = 0;
                valFound = false;
                continue;
            }

            //If value of frequency is different from its value in previous step
            if (vals[j] < oldVals[j] - delta_f || vals[j] > oldVals[j] + delta_f) {
                valChanged = true;
                oldVals = ArrayUtils.clone(vals);
            }

        }

        for (double val : vals) {
            if (val == 0) {
                breakInd = true;
                oldVals = ArrayUtils.clone(vals);
                break;
            }
        }

        //If found searched frequencies on all channels
        if (valFound && valChanged && breakInd) {

        	//saveToFile("frag",frag );
            boolean[] resBin = null;

            for (int j = 0; j < noOfChannels; j++) {

                //If in range of low state freq
                if (vals[j] <= freq[j][0] + delta_f && vals[j] >= freq[j][0] - delta_f) {
                    resBin = ArrayUtils.add(resBin, false);
                }
                //If in range of high state freq
                else if (vals[j] <= freq[j][1] + delta_f && vals[j] >= freq[j][1] - delta_f) {
                    resBin = ArrayUtils.add(resBin, true);
                } else {
                    break;
                }
            }

			if (resBin != null && resBin.length == noOfChannels) {
				sigBin = ArrayUtils.addAll(sigBin, resBin);

				if (isSecdedEnabled()) {
					if (sigBin.length % 8 == 0) {
						try {
							for (int ii = 8; ii <= sigBin.length; ii += 8) {
								boolean[] encoded4th = Arrays.copyOfRange(sigBin, ii - 8, ii);
								sigBinDec = ArrayUtils.addAll(sigBinDec, UltrasoundHelper.secded(encoded4th));
							}

						} catch (Exception e) {

						}
					}
				} else {
					sigBinDec = ArrayUtils.clone(sigBin);
				}

				breakInd = false;
				if (sigBinDec != null && sigBinDec.length % 4 == 0) {

					resHex = UltrasoundHelper.bin2hex(sigBinDec);
					logMessage("Decoded data binary: " + UltrasoundHelper.binStrFromBinArray(sigBinDec));
					sigBinDec = null;

					logMessage("Decoded data: " + resHex);

				}
			}

        }
    }

    /**
     * This method which should be overridden in decoder implementations. It should returns raw audio data samples, which contain signal to decode.
     *
     * @return short[] audio data samples
     */
    protected abstract short[] getAudioSamples() throws Exception;

    /* GETTERS AND SETTERS */

    public String getResHex() {
        return resHex;
    }
    
    protected abstract void logMessage(String message);
    
    public static boolean saveToFile(String name, double[] data) {
    	//System.out.println("Saving...");
        if (null == name || null == data) {
            return false;
        }
        File myFile = new File(name + ".csv");
        int i = 0;
        while (myFile.exists()) {
            String temp = "" + i + myFile.getName();
            myFile = new File(temp);
            i++;
        }
        try {
        	FileWriter writer = new FileWriter(myFile);
        	int len = data.length;
        	for (int j = 0; j < len; j++) {
        	   writer.write(data[j] + "," + "");
        	}
        	writer.close();
            
        } catch (Exception ex) {
            return false;
        } 
        //System.out.println("Saved " + myFile.getAbsolutePath());
        return true;
    }

}
