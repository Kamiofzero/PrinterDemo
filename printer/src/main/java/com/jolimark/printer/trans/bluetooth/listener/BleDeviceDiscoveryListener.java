package com.jolimark.printer.trans.bluetooth.listener;

import android.bluetooth.BluetoothDevice;

public interface BleDeviceDiscoveryListener {

    void onDeviceFound(BluetoothDevice device);
}
