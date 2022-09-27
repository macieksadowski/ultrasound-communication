package ultrasound;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import sw.FFT;
import ultrasound.dataframe.DataFrame;
import ultrasound.dataframe.DataFrame.CheckAddressResult;
import ultrasound.dataframe.DataFrame.ParserResult;
import ultrasound.dataframe.DataFrame.ParserResultValues;
import ultrasound.dataframe.IAsciiControlCodes;
import ultrasound.utils.UltrasoundHelper;

/**
 *
 */
public abstract class AbstractDecoder extends AbstractCoder implements Runnable, IDecoder {

	protected int nfft;
	private final double threshold;
	protected double delta_f;
	
	private final double[] hamming;

	private final FFT fft;

	private final int lowestAnalyseFreqInd;
	private final int highestAnalyseFreqInd;
	private final int[][] freqInd;
	
	protected double[] f;
	protected double[] ampl;

	protected short[] recordFrag;
	protected double[] t;

	protected double[] vals;
	protected double[] oldVals;
	protected boolean breakInd;
	
	protected StringBuilder receivedHexMsg;
	protected ByteArrayOutputStream resByte;
	
	protected boolean[] sigBin = null;
	protected boolean[] sigBinDec = null;

	private boolean endOfTransmission = false;
	
	private ParserResult result;
	private CheckAddressResult checkAdrResult;
	
	protected ArrayList<DataFrame> dataFrames;
	
	protected Byte deviceAddress = null;


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
	protected AbstractDecoder(AbstractDecoderBuilder builder) throws Exception {

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
		
		this.receivedHexMsg = new StringBuilder();
		
		this.dataFrames = new ArrayList<DataFrame>();
		
		logMessage(this.toString());

	}

	public static abstract class AbstractDecoderBuilder extends AbstractCoderBuilder implements IDecoderBuilder {

		private final int nfft;
		private final double threshold;

		public AbstractDecoderBuilder(int sampleRate, int noOfChannels, int firstFreq, int freqStep, int nfft,
				double threshold) {
			super(sampleRate, noOfChannels, firstFreq, freqStep);
			this.nfft = nfft;
			this.threshold = threshold;
		}

		@Override
		public abstract IDecoder build();

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

		startRecording();

		while (isRunning) {

			try {
				recordFrag = getAudioSamples();

				decode();
		
			} catch (Exception e) {
				logMessage(e.toString());
				e.printStackTrace();
				stopRecording();
				break;
			}
		}
		
		stopAudioRecorder();
	}

	public void clearReceivedDataBuffers() {
		receivedHexMsg.setLength(0);
		resByte.reset();

		sigBin = null;
		sigBinDec = null;

	}

	public void stopRecording() {
		logMessage("Decoder stopped!");
		isRunning = false;
	}
	
	public void clearResult() {
		endOfTransmission = false;
		result = null;
		checkAdrResult = null;
		frame = null;
	}
	
	protected abstract void onDataFrameSuccessfullyReceived();

	protected abstract void stopAudioRecorder();

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
					receivedHexMsg.append(resHex);
					logMessage("Decoded data: " + receivedHexMsg);
					break;
				}
				case DATA_FRAME: {

					byte[] res = UltrasoundHelper.bin2byte(resBinDec);
					resByte.write(res);
					for (int i = 0; i < res.length; i++) {
						if (res[i] == IAsciiControlCodes.EOT) {
							onEOTReceived();
							clearReceivedDataBuffers();
						}
					}
					
					break;
				}
				}
				
				breakInd = false;
				
			}

		}
	}
	
	private void onEOTReceived() throws Exception {

		endOfTransmission = true;
		logMessage("End of frame byte received");
		result = new ParserResult();
		checkAdrResult = new CheckAddressResult();
		frame = DataFrame.parseDataFrame(resByte.toByteArray(), noOfChannels, result, deviceAddress, checkAdrResult);
		if (result.get() == ParserResultValues.PARSING_OK) {
			logMessage("Data frame received successfully");
			logMessage(frame.toString());
			onDataFrameSuccessfullyReceived();
		} else {
			logMessage("Data frame parsing result: " + result.get().toString());
		}
	}


	/* GETTERS AND SETTERS */

	public String getResHex() {
		return receivedHexMsg.toString();
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
	
	public boolean endOfTransmissionReceived() {
		return endOfTransmission;
	}

	public ParserResult getParserResult() {
		return result;
	}
	
	public CheckAddressResult getCheckAddressParserResult() {
		return checkAdrResult;
	}
	
	public void setDeviceAddress(Byte adr) {
		this.deviceAddress = adr;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Decoder:");
		sb.append(System.lineSeparator());
		sb.append("\tSampling frequency " + sampleRate + " Hz");
		sb.append(System.lineSeparator());
		sb.append("\tFrame length " + tOnePulse + "s");
		sb.append(System.lineSeparator());
		sb.append("\tFrequency resolution " + delta_f + "Hz, DFT resolution " + nfft);
		sb.append(System.lineSeparator());
		sb.append("\tBandwidth: " + freq[0][0] + "Hz - " + freq[noOfChannels - 1][1] + "Hz");
		sb.append(System.lineSeparator());
		return sb.toString();
	}


}
