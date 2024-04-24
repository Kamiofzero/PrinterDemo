package com.jolimark.printer.trans.ble.util;

import java.util.ArrayList;

public class ByteArrayUtils {
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
            sb.append(Integer.toHexString(b));
            if (i + 1 < length) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }


    public static ArrayList<byte[]> splitArray(byte[] source) {
        if (source == null)
            return null;
        ArrayList<byte[]> arrays = new ArrayList<>();
        int splitSize = 1024 * 10;
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


    public static byte[] copyArray(byte[] source, int pos, int length) {
        if (source == null) {
            LogUtil.i(TAG, "source array is null.");
            return null;
        }
        if (pos > source.length - 1 || pos < 0) {
            LogUtil.i(TAG, "pos is out of array bound.");
            return null;
        }

        if (pos + length > source.length) {
            LogUtil.i(TAG, "copy length is out of array bound.");
            return null;
        }

        byte[] bytes = new byte[length];
        System.arraycopy(source, pos, bytes, 0, length);
        return bytes;
    }
}
