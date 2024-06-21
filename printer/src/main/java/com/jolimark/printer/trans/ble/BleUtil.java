package com.jolimark.printer.trans.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.IntentFilter;

import com.jolimark.printer.trans.ble.listener.BleDeviceDiscoveryListener;
import com.jolimark.printer.trans.ble.listener.BluetoothStateListener;
import com.jolimark.printer.trans.ble.receiver.BluetoothReceiver;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class BleUtil {
    private static final String TAG = "BleUtil";


    private BluetoothAdapter bluetoothAdapter;
    private BluetoothReceiver bluetoothReceiver;
    private BleDeviceDiscoveryListener BLEDeviceDiscoveryListener;

    private Timer timer;
    private TimerTask curTimeTask;


    public BleUtil() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothReceiver = new BluetoothReceiver();
        timer = new Timer();
    }


    /**
     * 注册蓝牙相关广播
     */
    public void registerBluetoothReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(bluetoothReceiver, filter);
    }

    /**
     * 注销蓝牙广播
     */
    public void unregisterBluetoothReceiver(Context context) {
        context.unregisterReceiver(bluetoothReceiver);
        bluetoothReceiver = null;
    }


    /**
     * 开启蓝牙
     */
    public void enableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
    }

    /**
     * 判断蓝牙是否开启
     *
     * @return
     */
    public boolean isBluetoothEnabled() {
        return bluetoothAdapter.isEnabled();
    }


    public void setBluetoothStateListener(BluetoothStateListener bluetoothStateListener) {
        bluetoothReceiver.setBluetoothStateListener(bluetoothStateListener);
    }

    public void setBLEDeviceDiscoveryListener(BleDeviceDiscoveryListener BLEDeviceDiscoveryListener) {
        this.BLEDeviceDiscoveryListener = BLEDeviceDiscoveryListener;
    }


    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
//            LogUtil.i(TAG, "bleDeviceDiscoveryListener -> ble device found " + "[" + device.getName() + "," + device.getAddress() + "]");
            if (BLEDeviceDiscoveryListener != null) {
                BLEDeviceDiscoveryListener.onDeviceFound(device);
            }
        }
    };


    public void scanBle() {
        bluetoothAdapter.startLeScan(leScanCallback);
        curTimeTask = new TimerTask() {
            @Override
            public void run() {
                bluetoothAdapter.stopLeScan(leScanCallback);
                if (BLEDeviceDiscoveryListener != null) {
                    BLEDeviceDiscoveryListener.onSearchEnd();
                }

            }
        };
        timer.schedule(curTimeTask, 10000);
    }


    public void stopScanBle() {
        if (curTimeTask != null)
            curTimeTask.cancel();
        bluetoothAdapter.stopLeScan(leScanCallback);
    }


    public Set<BluetoothDevice> getBondDevices() {
        return bluetoothAdapter.getBondedDevices();
    }
}
