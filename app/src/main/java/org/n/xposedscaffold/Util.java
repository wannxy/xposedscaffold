package org.n.xposedscaffold;

import java.util.Arrays;

import de.robv.android.xposed.XposedBridge;

public class Util {

    public final static boolean isDebug = false;

    public static String OBJECT = "OBJECT";
    public static String METHOD = "METHOD";
    public static String FIELD = "FIELD";
    public static String SET_FIELD = "SET_FIELD";
    public static String GET_FIELD = "GET_FIELD";
    public static String FIND = "FIND";
    public static String DBUEG = "DEBUG";
    public static String RETURN = "RETURN";
    public static String ARG = "ARG";

    public static String bytesToHex3(byte[] bytes) {
        StringBuilder buf = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) { // 使用String的format方法进行转换
            //buf.append(String.format("%02x", Integer.valueOf(b & 0xff)));
            buf.append(String.format("%02x", b));
        }
        return buf.toString();
    }

    public static void print(String parName, Object parValue, String providerName, int index) {
        print(Util.ARG, parName, parValue, providerName, index);
    }

    public static void print(String TAG, String parName, Object parValue, String providerName, int index) {
        if (parValue instanceof byte[]) {
            log(TAG, providerName + "[ " + index + " ]: " + parName + " -> " + Arrays.toString((byte[]) parValue));
        } else if (parValue instanceof long[]) {
            log(TAG, providerName + "[ " + index + " ]: " + parName + " -> " + Arrays.toString((long[]) parValue));
        } else if (parValue instanceof String[]) {
            log(TAG, providerName + "[ " + index + " ]: " + parName + " -> " + Arrays.toString((String[]) parValue));
        } else {
            log(TAG, providerName + "[ " + index + " ]: " + parName + " -> " + parValue);
        }
    }

    public static void log(String TAG, String msg) {
        if (!isDebug) {
            if (Util.DBUEG.equals(TAG)) {
                return;
            }
        }
        XposedBridge.log("wzyd_hook: " + TAG + "->" + msg);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String byteArray2String(byte[] array, int radix) {
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
