package ultrasound;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

public abstract class AbstractEncoder extends AbstractCoder {

	protected double tBreak;
	protected int N;
	protected final short[][][] sines;
	private boolean isTransmitting = false;
	protected boolean[] signalBinEncoded;
	
	/**
	 * Implementation of an encoder, which converts hex data to sound transmission
	 * 
	 * @param sampleRate   sample rate used by decoder
	 * @param noOfChannels number of transmission channels (has to be a power of 2)
	 * @param firstFreq    lowest frequency used by decoder (it will be a frequency
	 *                     of low signal of the first channel)
	 * @param freqStep     frequency interval between successive transmission
	 *                     channels
	 */
	protected AbstractEncoder(AbstractEncoderBuilder builder) {

		super(builder);

		this.tBreak = 2.0 * this.tOnePulse;
		if(builder.tBreak != 0) {
			this.tBreak = builder.tBreak;
		}

		N = (int) ((tOnePulse + tBreak) * sampleRate);
		sines = new short[noOfChannels][2][N];

		for (int i = 0; i < noOfChannels; i++) {

			sines[i][0] = genTone(freq[i][0], tOnePulse, tBreak / 2.0, 0.05);
			sines[i][1] = genTone(freq[i][1], tOnePulse, tBreak / 2.0, 0.05);
		}
	}
	
	public static abstract class AbstractEncoderBuilder extends AbstractCoderBuilder {
		
		@SuppressWarnings("unused")
		private double tBreak;
		
		public AbstractEncoderBuilder(int sampleRate, int noOfChannels, int firstFreq, int freqStep) {
			super(sampleRate, noOfChannels, firstFreq, freqStep);
		}
		
		public AbstractEncoderBuilder tBreak(double tBreak) {
			this.tBreak = tBreak;
			return this;
		}
		
		@Override
		public abstract AbstractEncoder build();
		
		protected void validate() {
			super.validate();
		}
		
	}
	
	

	/**
	 *
	 * @param hexData
	 */
	protected void transmit(String hexData) {

		isTransmitting = true;

		// Signal conversion form hex to freq
		final String binMsg = UltrasoundHelper.hex2bin(hexData);
		boolean[] signalBin = UltrasoundHelper.binArrayFromBinStr(binMsg);
		int pad = (2 * signalBin.length) % noOfChannels;
		if (pad != 0) {
			boolean[] zeros = new boolean[noOfChannels - pad / 2];
			Arrays.fill(zeros, false);
			signalBin = ArrayUtils.addAll(signalBin, zeros);
		}

		
		// Hamming code
		if(isSecdedEnabled()) {
			signalBinEncoded = new boolean[signalBin.length * 2];
			int pos = 0;
			for (int i = 3; i < signalBin.length; i += 4) {
				boolean[] oneByte = Arrays.copyOfRange(signalBin, i - 3, i + 1);
				boolean[] oneByteEncoded = UltrasoundHelper.encHamming(oneByte);
				for (int j = 0; j < 8; j++) {
					signalBinEncoded[pos] = oneByteEncoded[j];
					pos++;
				}
			}
		} else {
			signalBinEncoded = signalBin;
		}
		

		// Soundfile generation
		int bytePos = 0;

		for (int i = 0; i < (int) signalBinEncoded.length / noOfChannels; i++) {

			short[] curTactSig = new short[N + 1];
			for (int j = 0; j < noOfChannels; j++) {

				int freqInd = signalBinEncoded[bytePos] ? 1 : 0;
				curTactSig = UltrasoundHelper.sumShortArrays(curTactSig,
						UltrasoundHelper.multiplyArrayByFactor(sines[j][freqInd], 1 / (double) noOfChannels));
				bytePos++;
			}

			playSound(curTactSig);

		}
		isTransmitting = false;
	}

	/**
	 *
	 * @param soundData
	 */
	protected abstract void playSound(short[] soundData);

	/**
	 * Helper's method used to generate tone data of given length with silence
	 * before and after signal
	 * 
	 * @param freq        tone frequency
	 * @param toneLength  length of signal to be generated
	 * @param breakLength length of silence before and after signal
	 * @return array of values for ToneGenerator
	 */
	protected short[] genTone(double freq, double toneLength, double breakLength, double fadeLength) {

		int N = (int) Math.ceil((toneLength + 2 * breakLength) * sampleRate);
		int Nbreak = (int) (breakLength * sampleRate);
		int Nsig = N - 2 * Nbreak;
		short[] sample = new short[N];
		double[] sampleD = new double[N];
		double filterStep = 1.0 / (fadeLength * Nsig);
		double angle = 0;
		double increment = 2 * Math.PI * freq / sampleRate;

		int iSig = 0;

		for (int i = 0; i < N; ++i) {
			double filterVal = 1.0;

			if (i > Nbreak && i < N - Nbreak) {

				if (i < Nbreak + fadeLength * Nsig)
					filterVal = filterStep * iSig;
				else if (i > Nbreak + (1.0 - fadeLength) * Nsig)
					filterVal = -1.0 * filterStep * (iSig - Nsig);
				sample[i] = (short) (filterVal * Math.sin(angle) * Short.MAX_VALUE);
				sampleD[i] = filterVal * Math.sin(angle);

				angle += increment;
				iSig++;
			}

		}
		return sample;
	}

	public boolean isTransmitting() {
		return isTransmitting;
	}

	/** Getters and setters */

	public Double getTOnePulse() {
		return tOnePulse;
	}

	public void settOnePulse(Double tOnePulse) {
		this.tOnePulse = tOnePulse;
	}

	public Double getTBreak() {
		return tBreak;
	}

	public void settBreak(Double tBreak) {
		this.tBreak = tBreak;
	}
	
	public boolean[] getBinaryMessage() {
		return signalBinEncoded;
	}
	
	public String getBinaryMessageString() {
		return UltrasoundHelper.binStrFromBinArray(signalBinEncoded);
	}

}
