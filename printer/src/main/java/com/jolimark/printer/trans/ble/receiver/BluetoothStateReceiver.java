package com.jolimark.printer.trans.ble.receiver;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jolimark.ble.listener.BluetoothStateListener;
import com.jolimark.ble.util.LogUtil;


public class BluetoothStateReceiver extends BroadcastReceiver {

    private final String TAG = "BluetoothStateReceiver";


    public void setBluetoothStateListener(BluetoothStateListener bluetoothStateListener) {
        this.bluetoothStateListener = bluetoothStateListener;
    }

    public BluetoothStateListener bluetoothStateListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                if (blueState == BluetoothAdapter.STATE_ON) {
                    LogUtil.i(TAG, " bluetooth on");
                    if (bluetoothStateListener != null)
                        bluetoothStateListener.onBluetoothEnabled();
                } else if (blueState == BluetoothAdapter.STATE_TURNING_ON) {
                    LogUtil.i(TAG, " bluetooth turning on");
                } else if (blueState == BluetoothAdapter.STATE_OFF) {
                    LogUtil.i(TAG, " bluetooth off");
                    if (bluetoothStateListener != null)
                        bluetoothStateListener.onBluetoothDisabled();
                } else if (blueState == BluetoothAdapter.STATE_TURNING_OFF) {
                    LogUtil.i(TAG, " bluetooth turning off");
                }
                break;
        }
    }
}
