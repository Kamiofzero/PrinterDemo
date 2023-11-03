package com.jolimark.printer.printer;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JmPrinter {

    public static HashMap<String, BasePrinter> printerHashMap = new HashMap<>();


//    public static BasePrinter createPrinter(TransType type, String name) {
//        if (name == null || name.isEmpty()) return null;
//        if (type == null) return null;
//        BasePrinter basePrinter = printerHashMap.get(name);
//        if (basePrinter == null) {
//            if (type == TransType.WIFI) basePrinter = new WifiPrinter();
//            else if (type == TransType.BLUETOOTH) basePrinter = new BluetoothPrinter();
//            else if (type == TransType.USB) basePrinter = new UsbPrinter();
//            if (basePrinter != null) {
//                basePrinter.setName(name);
//                printerHashMap.put(name, basePrinter);
//            }
//        }
//        return basePrinter;
//    }


    public static WifiPrinter getWifiPrinter(String ip, int port) {
        if (ip == null || ip.isEmpty() || port < 0) {
            return null;
        }
        String key = "wifi/" + ip + "/" + port;
        BasePrinter basePrinter = printerHashMap.get(key);
        if (basePrinter == null) {
            basePrinter = new WifiPrinter(ip, port);
            printerHashMap.put(key, basePrinter);
        }
        return (WifiPrinter) basePrinter;
    }

    public static BluetoothPrinter getBluetoothPrinter(String mac) {
        if (mac == null || mac.isEmpty()) {
            return null;
        }
        String key = "bluetooth/" + mac;
        BasePrinter basePrinter = printerHashMap.get(key);
        if (basePrinter == null) {
            basePrinter = new BluetoothPrinter(mac);
            printerHashMap.put(key, basePrinter);
        }
        return (BluetoothPrinter) basePrinter;
    }

    public static UsbPrinter getUsbPrinter(Context context, int vid, int pid) {
        if (vid < 0 || pid < 0) {
            return null;
        }
        String key = "usb/" + vid + "/" + pid;
        BasePrinter basePrinter = printerHashMap.get(key);
        if (basePrinter == null) {
            basePrinter = new UsbPrinter(context, vid, pid);
            printerHashMap.put(key, basePrinter);
        }
        return (UsbPrinter) basePrinter;
    }


//    public static <T> T getPrinter(String name) {
//        if (name == null || name.isEmpty()) return null;
//        BasePrinter basePrinter = printerHashMap.get(name);
//        if (basePrinter != null) return (T) basePrinter;
//        return null;
//    }

    public static boolean isDevicesEmpty() {
        return printerHashMap.size() == 0;
    }


    public static List<BasePrinter> getPrinters() {
        List printerList = new ArrayList();
        for (Map.Entry<String, BasePrinter> entry : printerHashMap.entrySet()) {
            printerList.add(entry.getValue());
        }
        return printerList;
    }

    public static void removePrinter(BasePrinter printer) {
        printerHashMap.remove(printer.getName());
    }

    public static void release() {
        printerHashMap.clear();
    }

    public static void addPrinter(BasePrinter printer) {
        printerHashMap.put(printer.getName(), printer);
    }
}
