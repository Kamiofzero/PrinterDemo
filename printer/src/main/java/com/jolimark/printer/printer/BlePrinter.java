package com.jolimark.printer.printer;

import android.content.Context;

import com.jolimark.printer.trans.TransBase;
import com.jolimark.printer.trans.TransType;
import com.jolimark.printer.trans.ble.BleBase;

public class BlePrinter extends BasePrinter {

    BleBase bleBase;

    public BlePrinter(Context context, String mac) {
        super();
        transtype = TransType.BLE;
        bleBase.setContext(context);
        bleBase.setBtAddress(mac);
        setName("ble/" + mac);
    }

    @Override
    public String getDeviceInfo() {
        return "[mac:" + bleBase.getBtAddress() + "]";
    }

    public String getMac() {
        return bleBase.getBtAddress();
    }

    @Override
    protected TransBase getTransBase() {
        bleBase = new BleBase();
        return bleBase;
    }

    @Override
    protected int initPackageSize() {
        return 20;
    }

    @Override
    protected int initSendDelay() {
        return 0;
    }
}
