package util;

import java.util.Random;

/**
 * Created by YotWei on 2017/12/5.
 * to process data
 */
public class DataUtil {

    public static int byteArray2Int(byte[] arr) {
        return (arr[0] & 0xff) << 24 |
                (arr[1] & 0xff) << 16 |
                (arr[2] & 0xff) << 8 |
                (arr[3] & 0xff);
    }

    public static byte[] int2ByteArray(int n) {
        return new byte[]{
                (byte) ((n & 0xff000000) >>> 24),
                (byte) ((n & 0xff0000) >>> 16),
                (byte) ((n & 0xff00) >>> 8),
                (byte) n
        };
    }

    public static byte[] mergeByteArrays(byte[]... arrays) {
        int totalLen = 0;
        for (byte[] array : arrays) {
            totalLen += array.length;
        }
        byte[] dstArr = new byte[totalLen];
        int progress = 0;
        for (byte[] arr : arrays) {
            System.arraycopy(arr, 0, dstArr, progress, arr.length);
            progress += arr.length;
        }
        return dstArr;
    }

    public static String generateRandomString() {
        Random random = new Random();
        return String.format("%08x%016x", random.nextInt(), System.currentTimeMillis());
    }

    public static String randomNickname() {
        Random r = new Random();
        int i = r.nextInt();
        return String.format("%08x", i);
    }
}
