package com.jolimark.printer.trans.ble.listener;

import android.bluetooth.BluetoothDevice;

public interface BleDeviceDiscoveryListener {

    void onDeviceFound(BluetoothDevice device);

    void onSearchEnd();
}
