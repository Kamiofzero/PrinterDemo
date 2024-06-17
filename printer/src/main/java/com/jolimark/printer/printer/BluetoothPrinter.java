package com.jolimark.printer.printer;

import android.content.Context;

import com.jolimark.printer.callback.Callback2;
import com.jolimark.printer.trans.TransBase;
import com.jolimark.printer.trans.TransType;
import com.jolimark.printer.trans.bluetooth.BluetoothBase;
import com.jolimark.printer.trans.bluetooth.auto.AutoConnect;
import com.jolimark.printer.trans.bluetooth.auto.AutoConnect2;

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

    private AutoConnect autoConnect;

    public void connectLong(Context context, Callback2<String> callback) {
        if (autoConnect == null) {
            autoConnect = new AutoConnect(context, bluetoothBase, executorService, mainHandler, callback);
            autoConnect.autoConnect();
        }
    }
    public void disconnectLong() {
        if (autoConnect != null) {
            autoConnect.destroy();
            autoConnect = null;
        }
        bluetoothBase.disconnect();
    }

    private AutoConnect2 autoConnect2;
    public void connectLong2(Context context, Callback2<String> callback) {
        if (autoConnect2 == null) {
            autoConnect2 = new AutoConnect2(context, bluetoothBase, executorService, mainHandler, callback);
            autoConnect2.autoConnect();
        }
    }
    public void disconnectLong2() {
        if (autoConnect2 != null) {
            autoConnect2.destroy();
            autoConnect2 = null;
        }
    }


}
