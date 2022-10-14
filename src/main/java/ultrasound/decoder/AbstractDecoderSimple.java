package ultrasound.decoder;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import sw.FFT;
import ultrasound.AbstractCoder;
import ultrasound.utils.UltrasoundHelper;
import ultrasound.utils.log.DecoderLogger;

/**
 *
 */
public abstract class AbstractDecoderSimple extends AbstractCoder implements IDecoderSimple {

	protected int nfft;
	private final double threshold;
	protected double deltaF;
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

	protected boolean[] sigBin;
	protected boolean[] sigBinDec;

	/**
	 * Internal constructor for a new abstract encoder/decoder object. To
	 * instantiate new object of this type use {@link AbstractDecoderBuilder}
	 * 
	 * @param builder {@link AbstractDecoderBuilder}
	 * @throws Exception on {@link FFT} initialization error
	 */
	protected AbstractDecoderSimple(AbstractDecoderBuilder builder) throws Exception {

		super(builder);

		logger = DecoderLogger.getInstance();

		this.nfft = builder.nfft;
		this.threshold = builder.threshold;

		this.N = (int) Math.ceil(tOnePulse * sampleRate);
		this.deltaF = sampleRate / (double) nfft;
		this.hamming = UltrasoundHelper.hamming(N);

		this.vals = new double[noOfChannels];
		this.oldVals = new double[noOfChannels];
		this.breakInd = false;
		this.fft = new FFT(nfft);

		double lowestAnalyseFreq = firstFreq - deltaF;
		double highestAnalyseFreq = freq[freq.length - 1][1] + deltaF;

		lowestAnalyseFreqInd = freqToFreqIndex(lowestAnalyseFreq);
		highestAnalyseFreqInd = freqToFreqIndex(highestAnalyseFreq);

		freqInd = new int[freq.length][2];
		for (int i = 0; i < freq.length; i++) {
			freqInd[i][0] = freqToFreqIndex(freq[i][0]) - lowestAnalyseFreqInd;
			freqInd[i][1] = freqToFreqIndex(freq[i][1]) - lowestAnalyseFreqInd;
		}

		f = new double[highestAnalyseFreqInd - lowestAnalyseFreqInd];
		for (int i_f = 0; i_f < f.length; i_f++) {
			f[i_f] = freqIndexToFrequency(lowestAnalyseFreqInd + i_f);
		}

		this.receivedHexMsg = new StringBuilder();

		logger.logMessage(this.toString());

	}

	/**
	*
	*/
	public void run() {

		logger.logMessage("Decoder started!");

		isRunning = true;

		startRecording();

		while (isRunning) {

			try {
				recordFrag = getAudioSamples();
				decode();

			} catch (Exception e) {
				logger.logMessage(e.toString());
				e.printStackTrace();
				stopDecoder();
				break;
			}
		}

		stopAudioRecorder();
	}

	public void clearReceivedDataBuffers() {
		receivedHexMsg.setLength(0);

		sigBin = null;
		sigBinDec = null;

	}

	public void stopDecoder() {
		logger.logMessage("Decoder stopped!");
		isRunning = false;
	}

	protected abstract void stopAudioRecorder();

	protected abstract void startRecording();

	/**
	 * This method which should be overridden in decoder implementations. It should
	 * returns raw audio data samples, which contain signal to decode.
	 *
	 * @return short[] audio data samples
	 * @throws IllegalStateException when Audio Recorder was not initialized
	 */
	protected abstract short[] getAudioSamples() throws IllegalStateException;

	private void decode() {

		double[] frag = UltrasoundHelper.shortArrayToDoubleArray(recordFrag);

		// Hamming window
		for (int i = 0; i < N; i++) {
			frag[i] = frag[i] * hamming[i];
		}

		double[] x = zeroPadding(frag);

		calculateFFT(x);

		// Iterate for every transmission's channel
		boolean valFound = true;
		boolean valChanged = false;

		for (int j = 0; j < noOfChannels; j++) {

			int foundVal = analyseChannelForSignalPresence(j);
			if (foundVal != -1) {
				vals[j] = f[foundVal];
			} else {
				vals[j] = 0;
				valFound = false;
				continue;
			}
			valChanged = checkIfFreqValuesChanged(j);
			if (valChanged) {
				oldVals = ArrayUtils.clone(vals);
			}
		}
		for (double val : vals) {
			if (val == 0.0) {
				breakInd = true;
				oldVals = ArrayUtils.clone(vals);
				break;
			}
		}
		if (valFound && valChanged && breakInd) {
			onValuesFoundOnAllChannels();
		}
	}

	private void onValuesFoundOnAllChannels() {
		boolean[] resBin = convertFreqValsToBinary();

		if (resBin != null && resBin.length % noOfChannels == 0 && resBin.length % 16 == 0) {
			sigBin = ArrayUtils.addAll(sigBin, resBin);
			boolean[] resBinDec = decodeSecdedEncodedBinaryData(resBin);
			sigBinDec = ArrayUtils.addAll(sigBinDec, resBinDec);
			if (sigBinDec != null) {
				logger.logMessage("Decoded data binary: " + UltrasoundHelper.binStrFromBinArray(sigBinDec));
				onNewBinaryDataDecoded(resBinDec);
			}
			breakInd = false;
		}
		
	}

	private int analyseChannelForSignalPresence(int j) {
		// Analyze only in range of frequencies used by current channel
		int fMaxInd = UltrasoundHelper.findMaxValueIndex(ampl, freqInd[j][0], freqInd[j][1] + 1);
		if (ampl[fMaxInd] > threshold) {
			return fMaxInd;
		}
		return -1;
	}

	/**
	 * Returns true if value of frequency is different from its value in previous
	 * step
	 * 
	 * @param channelNo Number of channel to analyze
	 * @return true if value changed on given channel
	 */
	private boolean checkIfFreqValuesChanged(int channelNo) {
		return (vals[channelNo] < oldVals[channelNo] - deltaF || vals[channelNo] > oldVals[channelNo] + deltaF);
	}

	/**
	 * @param resBinDec
	 */
	protected void onNewBinaryDataDecoded(boolean[] resBinDec) {
		char[] resHex = UltrasoundHelper.bin2hex(resBinDec).toCharArray();
		receivedHexMsg.append(resHex);
		logger.logMessage("Decoded data: " + receivedHexMsg);
	}

	/**
	 * @param resBin
	 * @return
	 */
	private boolean[] decodeSecdedEncodedBinaryData(boolean[] resBin) {
		boolean[] resBinDec = null;

		if (isSecdedEnabled()) {
			try {
				for (int ii = 8; ii <= resBin.length; ii += 8) {
					boolean[] encoded4th = Arrays.copyOfRange(resBin, ii - 8, ii);
					resBinDec = ArrayUtils.addAll(resBinDec, UltrasoundHelper.secded(encoded4th));
				}
			} catch (Exception e) {
				logger.logMessage(e.getMessage());
				clearReceivedDataBuffers();
				return new boolean[0];
			}

		} else {
			resBinDec = ArrayUtils.clone(resBin);
		}
		return resBinDec;
	}

	/**
	 * @return
	 */
	private boolean[] convertFreqValsToBinary() {
		boolean[] resBin = null;

		for (int j = 0; j < noOfChannels; j++) {
			if (vals[j] <= freq[j][0] + deltaF && vals[j] >= freq[j][0] - deltaF) {
				resBin = ArrayUtils.add(resBin, false);
			} else if (vals[j] <= freq[j][1] + deltaF && vals[j] >= freq[j][1] - deltaF) {
				resBin = ArrayUtils.add(resBin, true);
			} else {
				break;
			}
		}
		return resBin;
	}

	/**
	 * @param x
	 */
	private void calculateFFT(double[] x) {

		ampl = new double[f.length];
		// FFT Calculation
		double[] y = new double[this.nfft];
		this.fft.fft(x, y);

		// show usable FFT output

		for (int ii = 0; ii < ampl.length; ii++) {
			int ind = lowestAnalyseFreqInd + ii;
			ampl[ii] = x[ind] * x[ind] + y[ind] * y[ind];
		}
	}

	/**
	 * @param frag
	 * @return
	 */
	private double[] zeroPadding(double[] frag) {
		// Zero padding
		int pad = nfft - N;
		double[] x = ArrayUtils.clone(frag);
		if (pad > 0) {
			x = ArrayUtils.addAll(frag, new double[pad]);
		}
		return x;
	}

	private int freqToFreqIndex(double frequency) {
		return (int) Math.ceil(frequency * nfft / sampleRate);
	}

	private double freqIndexToFrequency(int freqInd) {
		return (double) (freqInd * sampleRate) / nfft;
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Decoder:");
		sb.append(System.lineSeparator());
		sb.append("\tSampling frequency " + sampleRate + " Hz");
		sb.append(System.lineSeparator());
		sb.append("\tFrame length " + tOnePulse + "s");
		sb.append(System.lineSeparator());
		sb.append("\tFrequency resolution " + deltaF + "Hz, DFT resolution " + nfft);
		sb.append(System.lineSeparator());
		sb.append("\tBandwidth: " + freq[0][0] + "Hz - " + freq[noOfChannels - 1][1] + "Hz");
		sb.append(System.lineSeparator());
		return sb.toString();
	}

}
