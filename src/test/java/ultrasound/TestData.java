package ultrasound;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ultrasound.utils.FileUtil;

public class TestData {
	
	private int sampleRate;
	private int noOfChannels;
	private int firstFreq;
	private int freqStep;
	private short[] audioSig;

	public TestData(String name, int sampleRate) throws Exception {
		
		
		Pattern pattern = Pattern.compile("([0-9]{1,2})F([0-9]*?)S([0-9]*)");
		Matcher matcher = pattern.matcher(name);
		if(matcher.find()) {
			this.sampleRate = sampleRate;
			this.noOfChannels = Integer.parseInt(matcher.group(1));
			this.firstFreq = Integer.parseInt(matcher.group(2));
			this.freqStep = Integer.parseInt(matcher.group(3));

		}
		
		File file = new File(this.getClass().getClassLoader().getResource(name).toURI());
    	List<String> tmpArr = FileUtil.readDataFromCsvFile(file);
    	List<Short> audioSigList = new ArrayList<Short>();
    	for (String string : tmpArr) {
			audioSigList.add((short) Integer.parseInt(string));
		}
        this.audioSig = new short[audioSigList.size()];
        for (int i = 0; i < this.audioSig.length; i++) {
        	this.audioSig[i] = audioSigList.get(i);
        }
	}

	public int getSampleRate() {
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

	public short[] getAudioSig() {
		return audioSig;
	}
	
	
	
}
