package ultrasound.utils;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class UltrasoundHelperTest {

    @Test
    void testBbinArrayFromBinStrException() throws IllegalArgumentException {

        assertThrows(IllegalArgumentException.class, () -> UltrasoundHelper.binArrayFromBinStr("01B1"));
    }

    @Test
    void testBinArrayFromBinStr() {
        boolean[] fiveBool = new boolean[]{false,true,false,true};
        assertArrayEquals(fiveBool,UltrasoundHelper.binArrayFromBinStr("0101"));
    }
    
    @Test
    void testBinStrFromBinArray() {
    	String fiveBoolStr = "0101";
        boolean[] fiveBool = new boolean[]{false,true,false,true};
        assertEquals(fiveBoolStr,UltrasoundHelper.binStrFromBinArray(fiveBool));
    }


    @Test
    void testHex2bin() {
    	assertAll(
    		() -> assertEquals("0000",UltrasoundHelper.hex2bin("0")),
        	() -> assertEquals("010011010100000101000011010010010100010101001011",UltrasoundHelper.hex2bin("4D414349454B"))
        );
    }
    
    @Test
    void testHexToByte() {

    	assertEquals((byte) 0xff, UltrasoundHelper.hexToByte("ff"));
    }

    @Test
    void testBin2hex() {
        boolean[] fiveBinary = UltrasoundHelper.binArrayFromBinStr("0101");
        boolean[] zeroBinary = UltrasoundHelper.binArrayFromBinStr("0000");
        assertAll(
        	() -> assertEquals("5",UltrasoundHelper.bin2hex(fiveBinary)),
        	() -> assertEquals("0",UltrasoundHelper.bin2hex(zeroBinary))
        );
    }

    @Test
    void testSumShortArrays() {
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
    void testMultiplyArrayByFactor() {
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
    void testMultiplyBooleanMatrices() {

        boolean[][] secondMatrix = {{false,true,true,false}};
        boolean[][] expected = {{true,true,false,false,true,true,false,false}};
        boolean[][] actual = UltrasoundHelper.multiplyBooleanMatrices(secondMatrix, UltrasoundHelper.matrixG);
        assertArrayEquals(actual,expected);

    }

    @Test
    void testEncHamming() {

        boolean[] msg = {false,true,true,false};
        boolean[] expected = {true,true,false,false,true,true,false,false};
        boolean[] actual = UltrasoundHelper.encHamming(msg);
        assertArrayEquals(actual,expected);

    }

    @Test
    void testSecded() {

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
}
