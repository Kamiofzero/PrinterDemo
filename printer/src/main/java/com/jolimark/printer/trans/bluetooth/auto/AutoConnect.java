package com.jolimark.printer.trans.bluetooth.auto;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;

import com.jolimark.printer.callback.Callback2;
import com.jolimark.printer.trans.bluetooth.BluetoothBase;
import com.jolimark.printer.trans.bluetooth.BluetoothUtil;
import com.jolimark.printer.trans.bluetooth.listener.BTDeviceAclListener;
import com.jolimark.printer.trans.bluetooth.listener.BTDeviceDiscoveryListener;
import com.jolimark.printer.trans.bluetooth.listener.BluetoothStateListener;
import com.jolimark.printer.util.LogUtil;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

public class AutoConnect {
    private final String TAG = "AutoConnect";

    private Context context;

    private BluetoothBase base;

    private ExecutorService service;
    private Callback2 callback;

    private BluetoothUtil util;

    private int connectState = -1;

    private Timer timer = new Timer();
    private TimerTask task;

    private Handler handler;

    private boolean isDestroy;

    public AutoConnect(Context context, BluetoothBase base, ExecutorService service, Handler handler, Callback2 callback) {
        this.context = context;
        this.base = base;
        this.service = service;
        this.handler = handler;
        this.callback = callback;
        util = new BluetoothUtil();
    }

    public void destroy() {
        base.disconnect();
        synchronized (AutoConnect.this) {
            isDestroy = true;
            util.unregisterBluetoothReceiver(context);
            if (task != null) task.cancel();
            timer.cancel();
            base = null;
            service = null;
            handler = null;
            callback = null;
        }
    }

    private BluetoothStateListener bluetoothStateListener = new BluetoothStateListener() {
        @Override
        public void onBluetoothEnabled() {
            if (!isDestroy) {
                if (connectState == -1) {
                    util.startDiscoveryBTDevice();
                }
            }
        }

        @Override
        public void onBluetoothDisabled() {
            if (!isDestroy) {
                if (task != null) task.cancel();
                util.stopDiscoveryBTDevice();
            }
        }
    };

    private BTDeviceDiscoveryListener btDeviceDiscoveryListener = new BTDeviceDiscoveryListener() {
        @Override
        public void onDeviceStart() {

        }

        @Override
        public void onDeviceFound(BluetoothDevice device) {
            if (!isDestroy) {
                if (base.getMac().equals(device.getAddress())) {
                    connectState = 0;
                    util.stopDiscoveryBTDevice();
                    util.setBTDeviceDiscoveryListener(null);
                    callback.onConnecting(device.getAddress());
                    LogUtil.i(TAG, "device [" + base.getMac() + "] found");
                    connect();
                }
            }
        }

        @Override
        public void onDeviceFinish() {
            if (!isDestroy) {
                if (connectState == -1 && util.isBluetoothEnabled()) {
                    scheduleDiscovery();
                }
            }
        }
    };

    private BTDeviceAclListener btDeviceAclListener = new BTDeviceAclListener() {
        @Override
        public void onAclConnected(BluetoothDevice device) {

        }

        @Override
        public void onAclConnectRequest(BluetoothDevice device) {

        }

        @Override
        public void onAclDisConnected(BluetoothDevice device) {
            if (!isDestroy) {
                connectState = -1;
                scheduleDiscovery();
                callback.onDisconnect(device.getAddress());
            }
        }
    };

    public void autoConnect() {
        util.registerBluetoothReceiver(context);
        util.setBluetoothStateListener(bluetoothStateListener);
        util.setBTDeviceDiscoveryListener(btDeviceDiscoveryListener);
        util.setBTDeviceAclListener(btDeviceAclListener);
//        util.startDiscoveryBTDevice();
        synchronized (AutoConnect.this) {
            connectState = 0;
            callback.onConnecting(base.getMac());
            LogUtil.i(TAG, "auto connect device [" + base.getMac() + "]");
            connect();
        }
    }

    private void connect() {
        service.execute(() -> {
            synchronized (AutoConnect.this) {
                if (!isDestroy) {
                    if (!base.connect()) {
                        connectState = -1;
                        scheduleDiscovery();
                        handler.post(() -> {
                            if (!isDestroy)
                                callback.onConnectFail(base.getMac());
                        });
                    } else {
                        connectState = 1;
                        handler.post(() -> {
                            if (!isDestroy)
                                callback.onConnected(base.getMac());
                        });
                    }
                }
            }
        });
    }

    public void scheduleDiscovery() {
        task = new DiscoveryTask();
        timer.schedule(task, 1000);
    }


    class DiscoveryTask extends TimerTask {

        @Override
        public void run() {
            util.setBTDeviceDiscoveryListener(btDeviceDiscoveryListener);
            util.startDiscoveryBTDevice();
        }
    }
}
