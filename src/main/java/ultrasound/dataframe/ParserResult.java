package ultrasound.dataframe;

public class ParserResult {
	
	public enum ParserResultValues {
		PARSING_OK, INCORRECT_FRAME_LENGTH, START_BYTE_NOT_FOUND, FRAME_BUILD_ERROR, CHECKSUM_INCORRECT, OTHER_RECIPIENT
	}
	
	
	private ParserResultValues value;
	
	void set(ParserResultValues value) {
		this.value = value;
	}
	
	public ParserResultValues get() {
		return value;
	}
	
	@Override
	public String toString() {
		if(value != null) {
			return this.value.toString();
		} else return "empty";
	}
	
}