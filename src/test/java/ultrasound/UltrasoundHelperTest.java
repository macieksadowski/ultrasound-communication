package ultrasound;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import ultrasound.UltrasoundHelper;

@RunWith(JUnitParamsRunner.class)
public class UltrasoundHelperTest {

    @Test(expected = IllegalArgumentException.class)
    public void binArrayFromBinStr_Exception() throws IllegalArgumentException {

        UltrasoundHelper.binArrayFromBinStr("01B1");
    }

    @Test
    public void binArrayFromBinStr() {
        boolean[] fiveBool = new boolean[]{false,true,false,true};
        assertArrayEquals(fiveBool,UltrasoundHelper.binArrayFromBinStr("0101"));
    }
    
    @Test
    public void binStrFromBinArray() {
    	String fiveBoolStr = "0101";
        boolean[] fiveBool = new boolean[]{false,true,false,true};
        assertEquals(fiveBoolStr,UltrasoundHelper.binStrFromBinArray(fiveBool));
    }


    @Test
    public void hex2bin() {

        assertEquals("0000",UltrasoundHelper.hex2bin("0"));
        assertEquals("010011010100000101000011010010010100010101001011",UltrasoundHelper.hex2bin("4D414349454B"));
    }

    @Test
    public void bin2hex() {
        boolean[] fiveBinary = UltrasoundHelper.binArrayFromBinStr("0101");
        assertEquals("5",UltrasoundHelper.bin2hex(fiveBinary));

        boolean[] zeroBinary = UltrasoundHelper.binArrayFromBinStr("0000");
        assertEquals("0",UltrasoundHelper.bin2hex(zeroBinary));
    }

    @Test
    public void sumShortArrays() {
        short [] arrA = {
            0,
                    -700,
                    1000,
                    -1000,
                    2000,
                    -1000,
                    1000,
                    -700
        };
        short [] arrB = {
                0,
                700,
                -1000,
                1000,
                -2000,
                1000,
                -1000,
                700
        };
        short[] expected = {
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0
        };
        assertArrayEquals(expected,UltrasoundHelper.sumShortArrays(arrA, arrB));
    }

    @Test
    public void multiplyArrayByFactor() {
        short [] arr = {
                32767,
                -30273,
                23170,
                -12540,
                0,
                12540,
                -23170,
                30273,
                -32767,
                30273,
                -23170,
                12540
        };
        double factor = 0.5;
        short[] expected = {
                16383,
                -15136,
                11585,
                -6270,
                0,
                6270,
                -11585,
                15136,
                -16383,
                15136,
                -11585,
                6270
        };

        assertArrayEquals(expected,UltrasoundHelper.multiplyArrayByFactor(arr, factor));


    }


    @Test
    public void multiplyBooleanMatrices() {

        boolean[][] secondMatrix = {{false,true,true,false}};
        boolean[][] expected = {{true,true,false,false,true,true,false,false}};
        boolean[][] actual = UltrasoundHelper.multiplyBooleanMatrices(secondMatrix, UltrasoundHelper.G);
        assertArrayEquals(actual,expected);

    }

    @Test
    public void encHamming() {

        boolean[] msg = {false,true,true,false};
        boolean[] expected = {true,true,false,false,true,true,false,false};
        boolean[] actual = UltrasoundHelper.encHamming(msg);
        assertArrayEquals(actual,expected);

    }

    @Test
    public void secded() {

        boolean[] expected = {false,true,true,false};
        boolean[] msgEncoded = {true,true,false,false,false,true,false,false};
        boolean[] actual = new boolean[4];
        try {
            actual = UltrasoundHelper.secded(msgEncoded);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertArrayEquals(actual,expected);

    }
    
    @Test
    @Parameters(method = "parametersToTestByte2Bin")
    public void byte2bin(byte[] given, String expectedStr) {
    	boolean[] expected = UltrasoundHelper.binArrayFromBinStr(UltrasoundHelper.hex2bin(expectedStr));
    	
    	assertArrayEquals(UltrasoundHelper.byte2bin(given), expected);
    	
    }
    
    @SuppressWarnings("unused")
	private Object[] parametersToTestByte2Bin() {
		return new Object[] {
			new Object[] {new byte[] { (byte) 0xFF }, "FF"},
			new Object[] {new byte[] { (byte) 0x4D }, "4d"},
			new Object[] {new byte[] { (byte) 0x10 }, "10"},
			new Object[] {new byte[] { (byte) 0x37 }, "37"},
		};
    	
    }
    
    @Test
    @Parameters(method = "parametersToTestByte2Bin")
    public void bin2byte(byte[] expected, String given) {
    	boolean[] givenArr = UltrasoundHelper.binArrayFromBinStr(UltrasoundHelper.hex2bin(given));
    	
    	
    	assertArrayEquals(UltrasoundHelper.bin2byte(givenArr),expected);
    	
    }

}
