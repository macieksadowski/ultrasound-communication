package ultrasound.dataframe;

public class CheckAddressResult {
	
	public enum CheckAddressResultValues {
		OK, BROADCAST, OTHER_RECIPIENT
	}

	private CheckAddressResultValues value;
	
	void set(CheckAddressResultValues value) {
		this.value = value;
	}
	
	public CheckAddressResultValues get() {
		return value;
	}
	
	@Override
	public String toString() {
		if(value == null) {
			return "empty";
		}
		return this.value.toString();
	}
	
}

