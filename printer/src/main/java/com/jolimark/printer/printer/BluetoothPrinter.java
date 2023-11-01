package com.jolimark.printer.printer;

import com.jolimark.printer.direction.Comm;
import com.jolimark.printer.trans.TransType;
import com.jolimark.printer.trans.bluetooth.BluetoothBase;

public class BluetoothPrinter extends BasePrinter {
    private BluetoothBase bluetoothBase;

    @Override
    protected Comm getComm() {
        bluetoothBase = new BluetoothBase();
        return new Comm(bluetoothBase);
    }

    @Override
    public String getDeviceInfo() {
        return "[mac:" + bluetoothBase.getMac() + "]";
    }

    public BluetoothPrinter() {
        super();
        transtype = TransType.BLUETOOTH;
    }

    @Override
    protected int initPackageSize() {
        return 1024;
    }


    public void setMac(String address) {
        bluetoothBase.setBtDevAddress(address);
    }

    public String getMac() {
        return bluetoothBase.getMac();
    }

}
