package com.jolimark.printer.printer;

import android.content.Context;
import android.graphics.Bitmap;

import com.jolimark.printer.callback.Callback;
import com.jolimark.printer.callback.Callback2;
import com.jolimark.printer.trans.TransBase;
import com.jolimark.printer.trans.TransType;
import com.jolimark.printer.trans.bluetooth.BluetoothBase;
import com.jolimark.printer.trans.bluetooth.auto.AutoConnect3;

public class BluetoothPrinter extends BasePrinter {
    private BluetoothBase bluetoothBase;

    @Override
    public String getDeviceInfo() {
        return "[mac:" + bluetoothBase.getMac() + "]";
    }

    @Override
    protected TransBase getTransBase() {
        bluetoothBase = new BluetoothBase();
        return bluetoothBase;
    }

    /**
     * 一个printer对应一个地址，不提供切换地址的方法
     *
     * @param mac
     */
    public BluetoothPrinter(String mac) {
        super();
        transtype = TransType.BLUETOOTH;
        bluetoothBase.setMac(mac);
        setName("bluetooth/" + mac);
    }

    @Override
    protected int initPackageSize() {
//        return 20;
        return 1024;
    }

    @Override
    protected int initSendDelay() {
        return 50;
//        return 0;
    }


//    public void setMac(String address) {
//        bluetoothBase.setMac(address);
//        if (getName() == null || getName().isEmpty()) {
//            setName("bluetooth[" + address + "]");
//        }
//    }

    public String getMac() {
        return bluetoothBase.getMac();
    }

    private AutoConnect3 autoConnect;


    public void connectL(Context context, Callback2<String> callback) {
        if (autoConnect == null) {
            autoConnect = new AutoConnect3(context, bluetoothBase.getMac(), commBase, executorService, mainHandler, callback);
            autoConnect.autoConnect();
        }
    }

    public void disconnectL() {
        if (autoConnect != null) {
            autoConnect.destroy();
            autoConnect = null;
        }
    }


    public void printTextL(String text, Callback callback) {
        if (autoConnect != null) autoConnect.printText(text, callback);
//        printText(text, callback);
    }

    public void printImgL(Bitmap bitmap, Callback callback) {
        if (autoConnect != null) autoConnect.printImg(bitmap, callback);
//        printImg(bitmap,callback);
    }

    public void printL(byte[] bytes, Callback callback) {
        if (autoConnect != null) autoConnect.print(bytes, callback);
//        printL(bytes,callback);
    }


}
