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


    public BluetoothPrinter(String mac) {
        super();
        transtype = TransType.BLUETOOTH;
        bluetoothBase.setMac(mac);
        setName("bluetooth/" + mac);
    }

    @Override
    protected int initPackageSize() {
        return 1024;
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

}
