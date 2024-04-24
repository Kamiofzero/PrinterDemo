package com.jolimark.printer.trans.ble.listener;


public interface BTDeviceBondListener {

    void onBTDeviceBonding();

    void onBTDeviceBonded();

    void onBTDeviceBondNone();
}
