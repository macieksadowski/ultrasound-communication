package ultrasound;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.StopWatch;

import sw.FFT;
import ultrasound.DataFrame.DataFrameBuilder;

/**
 *
 */
public abstract class AbstractDecoder extends AbstractCoder implements Runnable {

	private boolean isRunning;

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

	protected String receivedHexMsg;
	protected ByteArrayOutputStream resByte;
	
	protected boolean[] sigBin = null;
	protected boolean[] sigBinDec = null;

	private boolean endOfTransmission = false;
	
	protected ArrayList<DataFrame> dataFrames;


	/**
	 * Constructor for a new abstract decoder object
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
		
		this.resByte = new ByteArrayOutputStream();
		
		this.dataFrames = new ArrayList<DataFrame>();

	}

	public static abstract class AbstractDecoderBuilder extends AbstractCoderBuilder {

		private final int nfft;
		private final double threshold;

		public AbstractDecoderBuilder(int sampleRate, int noOfChannels, int firstFreq, int freqStep, int nfft,
				double threshold) {
			super(sampleRate, noOfChannels, firstFreq, freqStep);
			this.nfft = nfft;
			this.threshold = threshold;
		}

		@Override
		public abstract AbstractDecoder build();

		protected void validate() {
			super.validate();

			// check if Nfft is a power of 2
			if ((nfft & nfft - 1) != 0) {
				throw new IllegalArgumentException("Nfft must be a power of 2! Decoder Stopped!");
			}
		}
	}

	/**
	*
	*/
	public void run() {

		isRunning = true;
		StopWatch watch = new StopWatch();

		logMessage("Decoder started!");
		logMessage("Sampling frequency " + sampleRate + " Hz");
		logMessage("Frame length " + N);
		logMessage("Frequency resolution " + delta_f + "Hz, DFT resolution " + nfft);
		logMessage("Bandwidth: " + freq[0][0] + "Hz - " + freq[noOfChannels - 1][1] + "Hz");

		startRecording();

		while (isRunning) {

			try {
				recordFrag = getAudioSamples();

				watch.start();
				decode();
				watch.stop();
		
				if(endOfTransmission) {
					logMessage("End of frame byte received");
					parseDataFrame();
					clearReceivedDataBuffers();
					
				}
				
				// if(this.sigBin != null)
				// logMessage("Data length: " + N + ", execution time: " + watch.getTime());
				watch.reset();
				// callback.updateFigures();

			} catch (Exception e) {
				logMessage(e.toString());
				e.printStackTrace();
				stopRecording();
				break;
			}
		}
		
		closeRecorder();
	}
	
	private void parseDataFrame() {
		byte[] resByteArr = resByte.toByteArray();
		boolean startByteFound = resByteArr[0] == ControlCodes.SOH;
		if(startByteFound) {
			byte address = resByteArr[1];
			byte command = resByteArr[2];
			
			DataFrameBuilder builder = new DataFrameBuilder(address, noOfChannels);
			builder.command(command);
			
			int pos = 3;
			ByteArrayOutputStream dataStr = new ByteArrayOutputStream();
			if(command == ControlCodes.STX) {
				
				for (; pos < resByteArr.length; pos++) {
					if(resByteArr[pos] == ControlCodes.ETX)
						break;
					dataStr.write(resByteArr[pos]);
				}
				builder.data(dataStr.toByteArray());
			}
			byte checksum = resByteArr[resByteArr.length - 2];
			
			try {
				DataFrame frame = builder.build();
				if(frame == null) {
					throw new Exception("Invalid data frame!");
				}
				
				if(frame.getChecksum() == checksum) {
					logMessage("Data frame received successfully");
					
					String receiverAddress = null;
					if (address == DataFrame.BROADCAST_ADDRESS) {
						receiverAddress = "BROADCAST";
					} else {
						receiverAddress = UltrasoundHelper.byteToHex(address);
					}
					logMessage("Receiver address: " + receiverAddress);
					
					logMessage("Command: " + ControlCodes.getCodeNameByValue(command));
					
					logMessage("Data: " +  new String(dataStr.toByteArray()));
				}
				
			} catch (Exception e) {
				logMessage(e.toString());
				e.printStackTrace();
			}
			
			
			
			
			
			
			
		}
		
		
	}
	
	private void clearReceivedDataBuffers() {
		receivedHexMsg = null;
		resByte.reset();
		
		sigBin = null;
		sigBinDec = null;
		
		endOfTransmission = false;
	}

	public void stopRecording() {
		logMessage("Decoder stopped!");
		isRunning = false;
	}

	protected abstract void closeRecorder();

	protected abstract void startRecording();

	/**
	 * This method which should be overridden in decoder implementations. It should
	 * returns raw audio data samples, which contain signal to decode.
	 *
	 * @return short[] audio data samples
	 */
	protected abstract short[] getAudioSamples() throws Exception;

	/**
	 * @throws Exception 
	*
	*/
	private void decode() throws Exception {

		double[] frag = new double[N];

		// Convert from short to double
		for (int i = 0; i < N; i++) {
			frag[i] = (double) recordFrag[i] / Short.MAX_VALUE;
		}

		// Hamming window
		for (int i = 0; i < N; i++) {
			frag[i] = frag[i] * hamming[i];
		}

		// Execute fft on selected samples
		ampl = new double[f.length];

		// Zero padding
		int pad = nfft - N;
		double[] x = ArrayUtils.clone(frag);
		if (pad > 0) {
			double[] zeros = new double[pad];
			Arrays.fill(zeros, 0.0);
			x = ArrayUtils.addAll(frag, zeros);
		}

		// FFT Calculation
		double[] y = new double[this.nfft];
		Arrays.fill(y, 0.0);
		this.fft.fft(x, y);

		// show usable FFT output

		for (int ii = 0; ii < ampl.length; ii++) {
			int ind = lowestAnalyseFreqInd + ii;
			ampl[ii] = x[ind] * x[ind] + y[ind] * y[ind];
		}

		// Iterate for every transmission's channel
		boolean valFound = true;
		boolean valChanged = false;

		for (int j = 0; j < noOfChannels; j++) {

			// Analyse only in range of frequencies used by current channel
			int fMaxInd = UltrasoundHelper.findMaxValueIndex(ampl, freqInd[j][0], freqInd[j][1] + 1);
			double Amax = ampl[fMaxInd];

			if (Amax > threshold) {
				vals[j] = f[fMaxInd];
			} else {
				vals[j] = 0;
				valFound = false;
				continue;
			}

			// If value of frequency is different from its value in previous step
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

		// If found searched frequencies on all channels
		if (valFound && valChanged && breakInd) {

			// saveToFile("frag",frag );
			boolean[] resBin = null;

			for (int j = 0; j < noOfChannels; j++) {

				// If in range of low state freq
				if (vals[j] <= freq[j][0] + delta_f && vals[j] >= freq[j][0] - delta_f) {
					resBin = ArrayUtils.add(resBin, false);
				}
				// If in range of high state freq
				else if (vals[j] <= freq[j][1] + delta_f && vals[j] >= freq[j][1] - delta_f) {
					resBin = ArrayUtils.add(resBin, true);
				} else {
					break;
				}
			}

			if (resBin != null && resBin.length % noOfChannels == 0 && resBin.length % 16 == 0) {
				
				boolean[] resBinDec = null;
				sigBin = ArrayUtils.addAll(sigBin, resBin);

				if (isSecdedEnabled()) {
					try {
						for (int ii = 8; ii <= resBin.length; ii += 8) {
							boolean[] encoded4th = Arrays.copyOfRange(resBin, ii - 8, ii);
							resBinDec = ArrayUtils.addAll(resBinDec, UltrasoundHelper.secded(encoded4th));
						}
					} catch (Exception e) {
						logMessage(e.getMessage());
						clearReceivedDataBuffers();
						return;
					}
					
				} else {
					resBinDec = ArrayUtils.clone(resBin);
				}
				
				sigBinDec = ArrayUtils.addAll(sigBinDec, resBinDec);
				
				logMessage("Decoded data binary: " + UltrasoundHelper.binStrFromBinArray(sigBinDec));

				char[] resHex = UltrasoundHelper.bin2hex(resBinDec).toCharArray();
				
				switch (mode) {
				case SIMPLE: {
					receivedHexMsg += resHex;
					logMessage("Decoded data: " + receivedHexMsg);
					break;
				}
				case DATA_FRAME: {
					
					byte[] res = UltrasoundHelper.bin2byte(resBinDec);
					for(int i=0;i<res.length;i++) {
						if(res[i] == ControlCodes.EOT) {
							endOfTransmission = true;
						}
					}
					resByte.write(res);
					

				}
				}
				
				breakInd = false;
				
			}

		}
	}

	/* GETTERS AND SETTERS */

	public String getResHex() {
		return receivedHexMsg;
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

	public boolean isRunning() {
		return isRunning;
	}

}
