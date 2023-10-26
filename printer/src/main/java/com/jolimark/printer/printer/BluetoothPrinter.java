package com.jolimark.printer.printer;

import com.jolimark.printer.direction.Comm;
import com.jolimark.printer.trans.bluetooth.BluetoothBase;

public class BluetoothPrinter extends BasePrinter {
    private BluetoothBase bluetoothBase;

    @Override
    protected Comm getComm() {
        bluetoothBase = new BluetoothBase();
        return new Comm(bluetoothBase);
    }

    @Override
    protected int initPackageSize() {
        return 1024;
    }


    public void setDeviceAddress(String address) {
        bluetoothBase.setBtDevAddress(address);
    }

    public String getDeviceAddress(){
        return bluetoothBase.getBtDevAddress();
    }

}
