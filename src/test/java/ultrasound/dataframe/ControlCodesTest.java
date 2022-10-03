package ultrasound.dataframe;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

class ControlCodesTest {

	
    @Test
    void testGetCodeNameByValue() {
        byte controlCode = IAsciiControlCodes.ACK;
        String controlCodeName = "ACK";
        assertEquals(controlCodeName, ControlCodes.getCodeNameByValue(controlCode));
    }
    
    @Test
    void testGetCodeByName() {
        byte controlCode = IAsciiControlCodes.ACK;
        String controlCodeName = "ACK";
        assertEquals(controlCode, ControlCodes.getCodeByName(controlCodeName).byteValue());
    }
}
