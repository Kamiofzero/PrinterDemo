package com.jolimark.printer.trans.ble.listener;

import android.bluetooth.BluetoothDevice;

public interface BTDeviceDiscoveryListener {

    void onDeviceStart();

    void onDeviceFound(BluetoothDevice device);

    void onDeviceFinish();
}
