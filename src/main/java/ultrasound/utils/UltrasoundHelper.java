package ultrasound.utils;
import java.util.Arrays;

/**
 * Helper Class for data format conversions
 * @author Maciej Sadowski
 */
public class UltrasoundHelper {

    public static String EMPTY_STRING = "";
    public static final char HIGH_VAL = '1';
    public static final char LOW_VAL = '0';

    /**
     * Converts string with binary data to array of boolean binary values
     * @param binStr String with binary values
     * @return array of boolean binary values
     */
    public static boolean[] binArrayFromBinStr(String binStr) {
        boolean[] binArray = new boolean[binStr.length()];
        for(int i=0;i<binStr.length();i++) {
            char val = binStr.toCharArray()[i];
            switch (val) {
                case HIGH_VAL:
                    binArray[i] = true;
                    break;
                case LOW_VAL:
                    binArray[i] = false;
                    break;
                default:
                    throw new IllegalArgumentException("Illegal character in input String");
            }
        }
        return binArray;
    }
    
    /**
     * Converts string with binary data to array of boolean binary values
     * @param binStr String with binary values
     * @return array of boolean binary values
     */
    public static String binStrFromBinArray(boolean[] binArray) {
        char[] binStr = new char[binArray.length];
        for(int i=0;i<binArray.length;i++) {
        	if(binArray[i]) {
        		binStr[i] = HIGH_VAL;
        	} else {
        		binStr[i] = LOW_VAL;
        	}
        }
        return String.valueOf(binStr);
    }

    /**
     * Converts an array of hex values binary coded to hexadecimal values (Length of array has to be a multiple of 4)
     * @param bin Array of binary values
     * @return String with hexadecimal data
     */
    public static String bin2hex(boolean[] bin) {
        String hex = EMPTY_STRING;
        for(int i=3;i< bin.length; i+=4) {
            String binVal = EMPTY_STRING;
            for(int j=3;j>=0;j--) {
                if(bin[i-j])
                    binVal += HIGH_VAL;
                else if(!binVal.isEmpty())
                    binVal += LOW_VAL;
            }
            if(!binVal.equals(EMPTY_STRING)) {
                hex += Integer.toHexString(Integer.parseInt(binVal,2));
            }
        }
        if(hex.equals(EMPTY_STRING) && bin.length > 0) {
            hex = "0";
        }
        return hex;
    }

    /**
     * Converts hexadecimal data in String to String with data coded binary
     * @param hex String with hexadecimal data
     * @return String with binary data padded to 4
     */
    public static String hex2bin(String hex) {
        String binStr = EMPTY_STRING;
        for (Character hexVal:hex.toCharArray()) {
            String bin = Integer.toBinaryString(Integer.parseInt(String.valueOf(hexVal),16));
            if(bin.length() != 4) {
                bin =  String.format("%1$4s", bin).replace(' ', '0');
            }
            binStr += bin;
        }
        return binStr;
    }
    
    public static boolean[] byte2bin(byte[] data) {
    	boolean[] binArray = new boolean[data.length * Byte.SIZE];
    	int pos = 0;
		for (int i = 0; i < data.length; i++) {
			for (int j = Byte.SIZE - 1; j >= 0 ; j--, pos++) {
				binArray[pos] = (data[i] & 0xFF & (1 << j)) != 0;
			}
		}
    	return binArray;
    }
    
    public static byte[] bin2byte(boolean[] data) {
    	if(data.length % 8 != 0) {
    		throw new IllegalArgumentException("Bin array length mismatch! " + binStrFromBinArray(data));
    	}
    	byte[] byteArr = new byte[data.length / Byte.SIZE];
    	int pos = 0;
    	for (int i = 0; i < byteArr.length; i++, pos += Byte.SIZE) {
    		boolean[] b = Arrays.copyOfRange(data, pos, pos + Byte.SIZE);
    		int val = Integer.parseInt(binStrFromBinArray(b),2);
    		byteArr[i] = (byte) val;
		}	
    	return byteArr;
    }
    
    public static String bytesToHex(byte[] bytes) {
    	StringBuilder result = new StringBuilder();
        for (byte i : bytes) {
            int decimal = (int)i & 0XFF;
            String hex = Integer.toHexString(decimal);
  
            if (hex.length() % 2 == 1) {
                hex = "0" + hex;
            }
  
            result.append(hex);
        }
        return result.toString();
    }
    
    public static String byteToHex(byte b) {

    	int decimal = (int)b & 0XFF;
        String hex = Integer.toHexString(decimal);

        if (hex.length() % 2 == 1) {
            hex = "0" + hex;
        }
        return hex;
    }
    
    public static byte hexToByte(String b) {

    	int i = Integer.parseInt(b, 16);
    	return (byte) i;
    }
    
    
    public static short[] sumShortArrays(short[] arrA, short[] arrB) {
    	
    	int resArrLen = arrB.length;
    	boolean aLonger = arrA.length > arrB.length;
    	
    	if(aLonger) {
    		resArrLen = arrA.length;
    	}
    	
		short[] resArr = new short[resArrLen];
		
		for (int i = 0; i < resArrLen; i++) {

			resArr[i] = (short) (arrA[i] + arrB[i]);
		}
		
		return resArr;
    	
    }

    public static short [] multiplyArrayByFactor(short[] arr, double factor) {

        short[] resArr = new short[arr.length];
        for(int i = 0 ; i< arr.length;i++) {
            resArr[i] = (short)(arr[i] * factor);
        }
        return resArr;
    }

    public static int findMaxValueIndex(double[] array) {
        return findMaxValueIndex(array,0,array.length);
    }

    public static int findMaxValueIndex(double[] array,int startInd,int endInd) {
        int maxValInd = startInd;
        if(endInd < 0 || endInd >= array.length)
            endInd = array.length - 1;
        if(startInd < 0)
            startInd = 0;
        if(startInd > endInd)
            startInd = endInd -1;
        for (int i = startInd; i < endInd; i++) {
            if (array[i] > array[maxValInd])
                maxValInd = i;
        }
        return maxValInd;
    }

    static boolean[][] G = new boolean[][]{
            {true, true, true, false, false, false, false, true},
            {true, false, false, true, true, false, false, true},
            {false, true, false, true, false, true, false, true},
            {true, true, false, true, false, false, true, false}
    };

    static boolean[][] Htransp = new boolean[][]{
            {true, false, false, true,},
            {false, true, false, true,},
            {true, true, false, true,},
            {false, false, true, true,},
            {true, false, true, true,},
            {false, true, true, true,},
            {true, true, true, true,},
            {false, false, false, true,}
    };

    static boolean[][] R = new boolean[][]{
            {false, false, true, false, false, false, false, false,},
            {false, false, false, false, true, false, false, false,},
            {false, false, false, false, false, true, false, false,},
            {false, false, false, false, false, false, true, false,}
    };

    static boolean[][] Rtransp = new boolean[][]{
            {false, false, false, false,},
            {false, false, false, false,},
            {true, false, false, false,},
            {false, false, false, false,},
            {false, true, false, false,},
            {false, false, true, false,},
            {false, false, false, true,},
            {false, false, false, false,}
    };

    public static boolean[] encHamming(boolean[] msg) throws IllegalArgumentException {

        int k = 4;
        if(msg.length != k) {
            throw new IllegalArgumentException("Message length incorrect! Is " + String.valueOf(msg.length) + " should be " + String.valueOf(k));
        }

        boolean[][] msgWrapped = { msg };
        return multiplyBooleanMatrices(msgWrapped,G)[0];

    }

    public static boolean[] secded(boolean[] msgEncoded) throws IllegalArgumentException, Exception {

        int n = 8;// of codeword bits per block

        if(msgEncoded.length != n) {
            throw new IllegalArgumentException("Message length incorrect! Is ("+String.valueOf(msgEncoded.length)+"), should be ("+String.valueOf(n)+")");
        }

        boolean[][] msgEncodedWrapped = new boolean[][] {msgEncoded};
        boolean[] syndrome = multiplyBooleanMatrices(msgEncodedWrapped, Htransp)[0];



        int syndromeSum = 0;
        for(int i=0;i<syndrome.length;i++) {
            syndromeSum += syndrome[i] ? 1 : 0;
        }
        if(syndromeSum != 0) {
            //Check parity bit
            int sum = 0;
            for(int i=0;i<n;i++) {
                sum += msgEncoded[i] ? 1 : 0;
            }
            boolean parity = sum % 2 == 0 ? false : true;

            if(parity != msgEncoded[n-1]) {
                //Find position of the error in codeword (index)
                boolean found = false;
                int index = -1;
                for(int i = 0; i < n;i++) {
                    if(!found) {
                        boolean[][] errvect = new boolean[1][n];
                        for(int j=0;j<errvect.length;j++) {
                            errvect[0][j] = false;
                        }
                        errvect[0][i] = true;

                        boolean[] search = multiplyBooleanMatrices(errvect, Htransp)[0];
                        if(Arrays.equals(search, syndrome)) {
                            found = true;
                            index = i;
                        }
                    }
                }
                if(index != -1) {
                    //Log.i("SECDED","Position of error in codeword="+ index);
                    msgEncoded[index] =  !msgEncoded[index]; //Corrected codeword;
                }
            } else {
                throw new Exception("Double error detected!");
            }
        }
        msgEncodedWrapped = new boolean[][] {msgEncoded};
        return multiplyBooleanMatrices(msgEncodedWrapped,Rtransp)[0];

    }


    public static boolean[][] multiplyBooleanMatrices(boolean[][] A,boolean[][] B) {
        boolean[][] resultMatrix = new boolean[A.length][B[0].length];

        for (int row = 0; row < resultMatrix.length; row++) {
            for (int col = 0; col < resultMatrix[row].length; col++) {

                int cell = 0;
                for (int i = 0; i < B.length; i++) {
                    int aVal = A[row][i] ? 1 : 0;
                    int bVal = B[i][col] ? 1 : 0;
                    cell += aVal * bVal;
                }
                cell %= 2;
                resultMatrix[row][col] = cell == 0 ? false : true;
            }
        }
        return resultMatrix;
    }

    public static double[] hamming(int N) {
        double[] hammingWindow = new double[N];
        for(int i = 0; i < N; i++) {
            hammingWindow[i] = 0.54 - 0.46 * Math.cos((2*i*Math.PI)/(double)(N -1));
        }
        return hammingWindow;
    }
}
