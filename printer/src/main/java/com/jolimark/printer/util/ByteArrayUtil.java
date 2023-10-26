package com.jolimark.printer.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class ByteArrayUtil {
    private static final String TAG = "ByteArrayUtils";

    public static byte[] mergeArrays(byte[] array1, int off1, int len1, byte[] array2, int off2, int len2) {
        byte[] resultArray = null;
        if (array1 == null && array2 != null) {
            resultArray = subArray(array2, off2, len2);
        } else if (array2 == null && array1 != null) {
            resultArray = subArray(array1, off1, len1);
        } else if (array1 != null && array2 != null) {
            resultArray = new byte[len1 + len2];
            int index = 0;
            System.arraycopy(array1, off1, resultArray, index, len1);
            index += len1;
            System.arraycopy(array2, off2, resultArray, index, len2);
        }
        return resultArray;
    }

    public static byte[] mergeArrays(byte[] array1, byte[] array2) {
        byte[] resultArray = null;
        if (array1 != null && array2 == null) {
            resultArray = array1;
        } else if (array1 == null && array2 != null) {
            resultArray = array2;
        } else if (array1 != null && array2 != null) {
            int len1 = array1.length;
            int len2 = array2.length;
            int off1 = 0;
            int off2 = 0;
            resultArray = new byte[len1 + len2];
            int index = 0;
            System.arraycopy(array1, off1, resultArray, index, len1);
            index += len1;
            System.arraycopy(array2, off2, resultArray, index, len2);
        }
        return resultArray;
    }


    public static byte[] subArray(byte[] array, int off, int len) {
        byte[] resultArray = new byte[len];
        System.arraycopy(array, off, resultArray, 0, len);
        return resultArray;
    }


    public static String toArrayString(byte[] byteList, int length) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < length; i++) {
            byte b = byteList[i];
            int integer = b & 0xff;
            sb.append(Integer.toHexString(integer));
            if (i + 1 < length) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }


    public static ArrayList<byte[]> splitArray(byte[] source, int splitSize) {
        if (source == null)
            return null;
        LogUtil.i(TAG, "data length " + source.length + ".");
        ArrayList<byte[]> arrays = new ArrayList<>();
        int restCount = source.length;
        while (restCount >= splitSize) {
            byte[] buf = new byte[splitSize];
            System.arraycopy(source, source.length - restCount, buf, 0, splitSize);
            restCount -= splitSize;
            arrays.add(buf);
        }
        if (restCount > 0) {
            byte[] buf = new byte[restCount];
            System.arraycopy(source, source.length - restCount, buf, 0, restCount);
            arrays.add(buf);
        }
        LogUtil.i(TAG, "split to " + arrays.size() + " packages.");
        return arrays;
    }

    /**
     * 字符串转换成GB18030编码的byte数组
     *
     * @param str
     * @return
     */
    public static byte[] stringToByte(String str) {
        byte[] s2byte = null;
        try {
            s2byte = str.getBytes("GB18030");
        } catch (UnsupportedEncodingException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        return s2byte;
    }


    public static String toHex(byte[] bytes, int length) {
        if (bytes == null)
            return "";

        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        for (int i = 0; i < length; i++) {
            sb.append(Integer.toHexString(bytes[i] & 0xff));
            sb.append(" ");
        }
        sb.append("]");
        return sb.toString();
    }
}
