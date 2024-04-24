package com.jolimark.printer.trans.ble.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jolimark.ble.listener.BTDevicePairListener;
import com.jolimark.ble.util.LogUtil;


public class BTDevicePairReceiver extends BroadcastReceiver {
    private final String TAG = "BTDevicePairReceiver";


    public void setBtDevicePairListener(BTDevicePairListener btDevicePairListener) {
        this.btDevicePairListener = btDevicePairListener;
    }

    public BTDevicePairListener btDevicePairListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.bluetooth.device.action.PAIRING_REQUEST")) {
            LogUtil.i(TAG,"BTDevicePairReceiver -> bt device pair request");
            if (btDevicePairListener != null)
                btDevicePairListener.onDevicePair();
        }
    }

}
