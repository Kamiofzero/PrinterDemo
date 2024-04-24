package com.jolimark.printer.trans.ble.receiver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jolimark.ble.listener.BTDeviceBondListener;
import com.jolimark.ble.util.LogUtil;


public class BTDeviceBondReceiver extends BroadcastReceiver {

    private final String TAG = "BTDeviceBondReceiver";


    public void setBtDeviceBondListener(BTDeviceBondListener btDeviceBondListener) {
        this.btDeviceBondListener = btDeviceBondListener;
    }

    public BTDeviceBondListener btDeviceBondListener;


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                if (state == BluetoothDevice.BOND_BONDED) {
                     LogUtil.i(TAG, " bt device bonded");
                    if (btDeviceBondListener != null)
                        btDeviceBondListener.onBTDeviceBonded();

                } else if (state == BluetoothDevice.BOND_BONDING) {
                     LogUtil.i(TAG, " bluetooth device bonding");
                    if (btDeviceBondListener != null)
                        btDeviceBondListener.onBTDeviceBonding();

                } else if (state == BluetoothDevice.BOND_NONE) {
                     LogUtil.i(TAG, " bluetooth device none bond");
                    if (btDeviceBondListener != null)
                        btDeviceBondListener.onBTDeviceBondNone();
                }
                break;
            default:
                break;
        }
    }
}
