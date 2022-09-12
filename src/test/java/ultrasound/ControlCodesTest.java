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
}
