package com.jolimark.printer.trans.ble.receiver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jolimark.ble.listener.BTDeviceDiscoveryListener;
import com.jolimark.ble.util.LogUtil;


public class BTDeviceDiscoveryReceiver extends BroadcastReceiver {

    private final String TAG = "BluetoothStateReceiver";


    public void setBtDeviceDiscoveryListener(BTDeviceDiscoveryListener btDeviceDiscoveryListener) {
        this.btDeviceDiscoveryListener = btDeviceDiscoveryListener;
    }

    public BTDeviceDiscoveryListener btDeviceDiscoveryListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case BluetoothDevice.ACTION_FOUND:
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String type;
                switch (device.getType()) {
                    case BluetoothDevice.DEVICE_TYPE_CLASSIC: {
                        type = "classic";
                        break;
                    }
                    case BluetoothDevice.DEVICE_TYPE_LE: {
                        type = "LE";
                        break;
                    }
                    case BluetoothDevice.DEVICE_TYPE_DUAL: {
                        type = "Dual";
                        break;
                    }
                    default: {
                        type = "unknown";
                        break;
                    }
                }
                LogUtil.i(TAG, "bt device found " + "[" + device.getName() + "," + device.getAddress() + "," + type + "]");
                if (btDeviceDiscoveryListener != null)
                    btDeviceDiscoveryListener.onDeviceFound(device);
                break;
        }
    }
}
