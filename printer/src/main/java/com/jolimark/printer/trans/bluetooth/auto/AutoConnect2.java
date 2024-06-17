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
import com.jolimark.printer.util.CycleThread;
import com.jolimark.printer.util.LogUtil;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

public class AutoConnect2 {
    private final String TAG = "AutoConnect2";

    private Context context;

    private BluetoothBase base;

    private ExecutorService service;
    private Callback2 callback;

    private BluetoothUtil util;

    private int connectState = -1;

    private Timer timer = new Timer();
    private TimerTask task;

    private Handler handler;

    public AutoConnect2(Context context, BluetoothBase base, ExecutorService service, Handler handler, Callback2 callback) {
        this.context = context;
        this.base = base;
        this.service = service;
        this.handler = handler;
        this.callback = callback;
        util = new BluetoothUtil();
    }

    public void destroy() {
        util.unregisterBluetoothReceiver(context);
        if (task != null) task.cancel();
        timer.cancel();
        base.disconnect();
    }

    private BluetoothStateListener bluetoothStateListener = new BluetoothStateListener() {
        @Override
        public void onBluetoothEnabled() {
            if (connectState == -1) {
                util.startDiscoveryBTDevice();
            }
        }

        @Override
        public void onBluetoothDisabled() {
            if (task != null) task.cancel();
            util.stopDiscoveryBTDevice();
        }
    };

    private BTDeviceDiscoveryListener btDeviceDiscoveryListener = new BTDeviceDiscoveryListener() {
        @Override
        public void onDeviceStart() {

        }

        @Override
        public void onDeviceFound(BluetoothDevice device) {
            if (base.getMac().equals(device.getAddress())) {
                connectState = 0;
                util.stopDiscoveryBTDevice();
                util.setBTDeviceDiscoveryListener(null);
                handler.post(() -> callback.onConnecting(device.getAddress()));
                LogUtil.i(TAG, "device [" + base.getMac() + "] found");
                connect();
            }
        }

        @Override
        public void onDeviceFinish() {
            if (connectState == -1 && util.isBluetoothEnabled()) {
                scheduleDiscovery();
            }
        }
    };


    public void autoConnect() {
        util.registerBluetoothReceiver(context);
        util.setBluetoothStateListener(bluetoothStateListener);
        util.setBTDeviceDiscoveryListener(btDeviceDiscoveryListener);
        util.startDiscoveryBTDevice();
    }

    private void connect() {
        service.execute(() -> {
            if (!base.connect()) {
                connectState = -1;
                scheduleDiscovery();
                handler.post(() -> callback.onConnectFail(base.getMac()));
            } else {
                connectState = 1;
                handler.post(() -> callback.onConnected(base.getMac()));
                checkTask = new CheckTask();
                checkTask.start();
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

    private CheckTask checkTask;

    class CheckTask extends CycleThread {

        @Override
        public void doBeforeCycle() {
            setWaitMilliSeconds(1000);
        }

        @Override
        public void doInCycle() {
            service.execute(() -> {
                int ret = base.receiveData(new byte[1], 100);
                if (ret == -1) {
                    connectState = -1;
                    handler.post(() -> callback.onDisconnect(base.getMac()));
                    stopCycle();
                    scheduleDiscovery();
                }
            });
        }

        @Override
        public void doAfterCycle() {

        }
    }

}
