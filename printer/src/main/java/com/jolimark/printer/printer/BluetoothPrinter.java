package com.jolimark.printer.printer;

import com.jolimark.printer.trans.TransBase;
import com.jolimark.printer.trans.TransType;
import com.jolimark.printer.trans.bluetooth.BluetoothBase;

public class BluetoothPrinter extends BasePrinter {
    private BluetoothBase bluetoothBase = new BluetoothBase();

    @Override
    public String getDeviceInfo() {
        return "[mac:" + bluetoothBase.getMac() + "]";
    }

    @Override
    protected TransBase getTransBase() {
        return bluetoothBase;
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

    @Override
    protected int initSendDelay() {
        return 50;
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
