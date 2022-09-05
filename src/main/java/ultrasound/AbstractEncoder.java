package ultrasound;

import java.security.InvalidAlgorithmParameterException;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.ml.neuralnet.twod.util.HitHistogram;

public abstract class AbstractEncoder extends AbstractCoder implements Runnable {

	protected double tBreak;
	protected String hexData = "";
	
	protected int N;
	protected final short[][][] sines;
	private boolean isTransmitting = false;
	protected boolean[] signalBinEncoded;

	/**
	 * Abstract class of an encoder, which converts hex data and transmit it as sound transmission.
	 * All abstract methods should be implemented platform-specific.
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
		if (builder.tBreak != 0) {
			this.tBreak = builder.tBreak;
		}

		N = (int) ((tOnePulse + tBreak) * sampleRate);
		sines = new short[noOfChannels][2][N];

		for (int i = 0; i < noOfChannels; i++) {

			sines[i][0] = genTone(freq[i][0], tOnePulse, tBreak / 2.0, 0.05);
			sines[i][1] = genTone(freq[i][1], tOnePulse, tBreak / 2.0, 0.05);
		}
	}

	/**
	 * Builder for new instances {@link AbstractEncoder}
	 */
	public static abstract class AbstractEncoderBuilder extends AbstractCoderBuilder {

		private double tBreak;

		/**
		 * 
		 * @param sampleRate   sample rate used by encoder
		 * @param noOfChannels number of transmission channels (has to be a power of 2)
		 * @param firstFreq    lowest frequency used by encoder (it will be a frequency
		 *                     of low signal of the first channel)
		 * @param freqStep     frequency interval between successive transmission
		 *                     channels
		 */
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
	 * Method used to start sound transmission
	 */
	public void run() {

		try {
			validateHexData();

			logMessage("Transmitting message...");

			constructAudioStream();

			transmit();

			closeAudioStream();

			logMessage("Transmission ended.");
			logMessage("Message: " + hexData);
			logMessage("Bin message: " + UltrasoundHelper.hex2bin(hexData));
			logMessage("Bin message encoded: " + getBinaryMessageString());
			logMessage("Bandwidth: " + freq[0][0] + "Hz - " + freq[noOfChannels - 1][1] + "Hz");
			logMessage("Speed rate: " + Math.floor((double) noOfChannels / (tOnePulse + tBreak)) + "b/s");

		} catch (Exception e) {
			e.printStackTrace();
			logMessage(e.toString());
		}
	}
	
	protected abstract void closeAudioStream();

	protected abstract void playSound(short[] soundData);

	protected abstract void constructAudioStream() throws Exception;

	/**
	 *
	 * @param hexData
	 */
	private void transmit() {

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
		if (isSecdedEnabled()) {
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
	 * Helper's method used to generate tone data of given length with silence
	 * before and after signal
	 * 
	 * @param freq        tone frequency
	 * @param toneLength  length of signal to be generated
	 * @param breakLength length of silence before and after signal
	 * @return array of values for ToneGenerator
	 */
	private short[] genTone(double freq, double toneLength, double breakLength, double fadeLength) {

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

	/**
	 * Validates field {@link AbstractEncoder#hexData} which stores hexadecimal
	 * message to transmit as {@code String} The message can not be empty and should
	 * not contain white characters. Only digits and letters from 'a' to 'f' are
	 * allowed. Letters' case is unimportant.
	 * 
	 * @throws InvalidAlgorithmParameterException - if
	 *                                            {@link AbstractEncoder#hexData} is
	 *                                            empty or contains invalid
	 *                                            characters
	 */
	private void validateHexData() throws InvalidAlgorithmParameterException {
		if (hexData != null && !hexData.isBlank()) {
			if(!hexData.matches("-?[0-9a-fA-F]+")) {
				throw new InvalidAlgorithmParameterException("Hex data contains invalid characters!");
			}
		} else {
			throw new InvalidAlgorithmParameterException("Hex data can not be empty!");
		}

	}


	/** Getters and setters */
	
	/**
	 * Sets hexadecimal data to transmit. This String will be validated before transmission using method {@link AbstractEncoder#validateHexData()}
	 * 
	 * @param hexData hexadecimal data as String
	 */
	public void setHexData(String hexData) {
		this.hexData = hexData;
	}

	/**
	 * This method is used to check state of the encoder.
	 * @return {@code boolean} true if encoder is currently transmitting signal 
	 */
	public boolean isTransmitting() {
		return isTransmitting;
	}

	/**
	 * Getter method for field {@link AbstractEncoder#tBreak}
	 * @return tBreak in seconds
	 */
	public Double getTBreak() {
		return tBreak;
	}


	/**
	 * Returns message to transmit as binary data.
	 * If encoder uses SECDED encoding this message will be encoded with SECDED.
	 * @return {@code boolean[] } array contains binary message
	 */
	public boolean[] getBinaryMessage() {
		return signalBinEncoded;
	}

	/**
	 * Returns message to transmit as binary data converted to {@code String}
	 * If encoder uses SECDED encoding this message will be encoded with SECDED.
	 * @return {@code String } contains binary message
	 */
	public String getBinaryMessageString() {
		return UltrasoundHelper.binStrFromBinArray(signalBinEncoded);
	}

}
