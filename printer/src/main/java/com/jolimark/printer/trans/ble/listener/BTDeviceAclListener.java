package com.jolimark.printer.trans.ble.listener;

import android.bluetooth.BluetoothDevice;

public interface BTDeviceAclListener {

    void onAclConnected(BluetoothDevice device);

    void onAclConnectRequest(BluetoothDevice device);

    void onAclDisConnected(BluetoothDevice device);

}
