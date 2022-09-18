package ultrasound;

import java.security.InvalidAlgorithmParameterException;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import ultrasound.dataframe.IDataFrame;
import ultrasound.utils.UltrasoundHelper;

public abstract class AbstractEncoder extends AbstractCoder implements Runnable, IEncoder {

	protected double tBreak;
	private double fadeLength;
	protected final short[][][] sines;
	
	protected String hexData = "";
	
	protected boolean[] signalBinEncoded;
	boolean[] signalBin;

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

		this.fadeLength = 0.05;
		if (builder.fadeLength != 0) {
			this.fadeLength = builder.fadeLength;
		}

		N = (int) Math.ceil((tOnePulse + tBreak) * sampleRate);
		sines = new short[noOfChannels][2][N];

		for (int i = 0; i < noOfChannels; i++) {
			sines[i][0] = genTone(freq[i][0]);
			sines[i][1] = genTone(freq[i][1]);
		}
	}

	public static abstract class AbstractEncoderBuilder extends AbstractCoderBuilder implements IEncoderBuilder {

		private double tBreak;
		private double fadeLength;

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

		public IEncoderBuilder tBreak(double tBreak) {
			this.tBreak = tBreak;
			return this;
		}
		
		public IEncoderBuilder fadeLength(double fadeLength) {
			this.fadeLength = fadeLength;
			return this;
		}

		@Override
		public abstract IEncoder build();

		protected void validate() {
			super.validate();
		}

	}

	public void run() {

		try {
			
			if(this.mode.equals(ICoder.CoderMode.SIMPLE)) {
				validateHexData();
			}
			
			logMessage("Transmitting message...");

			constructAudioStream();

			transmit();

			closeAudioStream();

			logMessage("Transmission ended.");
			if(mode == CoderMode.SIMPLE) {
				logMessage("Message: " + hexData);
			} else {
				logMessage(frame.toString());
			}
			
			logMessage("Bin message: " + getBinaryMessageString());
			logMessage("Hex message: " + getHexMessageString());
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

		isRunning = true;
		
		switch(this.mode) {
		case SIMPLE: {
			// Signal conversion form hex to binary
			signalBin = UltrasoundHelper.binArrayFromBinStr(UltrasoundHelper.hex2bin(hexData));
			
			int pad = (2 * signalBin.length) % noOfChannels;
			if (pad != 0) {
				boolean[] zeros = new boolean[noOfChannels - pad / 2];
				Arrays.fill(zeros, false);
				signalBin = ArrayUtils.addAll(signalBin, zeros);
			}
			break;
		}
		case DATA_FRAME: {
			signalBin = UltrasoundHelper.byte2bin(frame.get());
			break;
		}
			
		default:
			return;
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

		playSound(genTone(40));
		
		for (int i = 0; i < (int) signalBinEncoded.length / noOfChannels; i++) {

			short[] curTactSig = new short[N];
			for (int j = 0; j < noOfChannels; j++) {

				int freqInd = signalBinEncoded[bytePos] ? 1 : 0;
				curTactSig = UltrasoundHelper.sumShortArrays(curTactSig,
						UltrasoundHelper.multiplyArrayByFactor(sines[j][freqInd], 1 / (double) noOfChannels));
				bytePos++;
			}

			playSound(curTactSig);

		}
		playSound(genTone(40));
		isRunning = false;
	}

	/**
	 * Helper's method used to generate tone data of given frequency with silence
	 * before and after signal
	 * 
	 * @param freq frequency of signal to be generated [Hz]
	 * @return {@code short[]} array with samples of generated signal 
	 */
	private short[] genTone(double freq) {

		int Nbreak = (int) Math.ceil(tBreak / 2.0 * sampleRate);
		int Nsig = N - 2 * Nbreak;

		short[] sample = new short[N];
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
	
	public void setHexData(String hexData) {
		this.hexData = hexData;
	}
	
	public void setDataFrame(IDataFrame frame) {
		this.frame = frame;
	}

	public Double getTBreak() {
		return tBreak;
	}

	public boolean[] getBinaryMessage() {
		return signalBinEncoded;
	}

	public String getBinaryMessageString() {
		return UltrasoundHelper.binStrFromBinArray(signalBin);
	}

	public String getHexMessageString() {
		return UltrasoundHelper.bin2hex(signalBin);
	}

}
