package com.jolimark.printer.trans.bluetooth.listener;


import android.bluetooth.BluetoothDevice;

public interface BTDeviceBondListener {

    void onBTDeviceBonding(BluetoothDevice bluetoothDevice);

    void onBTDeviceBonded(BluetoothDevice bluetoothDevice);

    void onBTDeviceBondNone(BluetoothDevice bluetoothDevice);
}
