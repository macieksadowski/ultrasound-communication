package ultrasound;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ControlCodesTest {

	
    @Test
    public void getCodeNameByValue() {
        byte controlCode = ControlCodes.ACK;
        String controlCodeName = "ACK";
        assertEquals(controlCodeName, ControlCodes.getCodeNameByValue(controlCode));
    }
    
    @Test
    public void getCodeByName() {
        byte controlCode = ControlCodes.ACK;
        String controlCodeName = "ACK";
        assertEquals(controlCode, ControlCodes.getCodeByName(controlCodeName).byteValue());
    }
}
