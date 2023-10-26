package com.jolimark.printer.trans.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.jolimark.printer.trans.bluetooth.listener.BTDeviceAclListener;
import com.jolimark.printer.trans.bluetooth.listener.BTDeviceBondListener;
import com.jolimark.printer.trans.bluetooth.listener.BTDeviceDiscoveryListener;
import com.jolimark.printer.trans.bluetooth.listener.BluetoothStateListener;
import com.jolimark.printer.trans.bluetooth.receiver.BluetoothReceiver;
import com.jolimark.printer.util.LogUtil;

import java.lang.reflect.Method;
import java.util.Set;

@SuppressLint("MissingPermission")
public class BluetoothUtil {
    private final String TAG = "BluetoothUtil";

    private BluetoothAdapter bluetoothAdapter;

    private BluetoothReceiver bluetoothReceiver;

    public BluetoothUtil() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * 注册蓝牙相关广播
     */
    public void registerBluetoothReceiver(Context context) {
        synchronized (BluetoothUtil.class) {
            if (bluetoothReceiver != null) {
                LogUtil.i(TAG, "BluetoothReceiver is already registered, should unregister it first before register again.");
                return;
            }
        }
        LogUtil.i(TAG,"registerBluetoothReceiver");
        bluetoothReceiver = new BluetoothReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        context.registerReceiver(bluetoothReceiver, filter);
    }

    /**
     * 注销蓝牙广播
     */
    public void unregisterBluetoothReceiver(Context context) {
        context.unregisterReceiver(bluetoothReceiver);
        synchronized (BluetoothUtil.class) {
            bluetoothReceiver = null;
        }
    }


    public void setBluetoothStateListener(BluetoothStateListener bluetoothStateListener) {
        if (bluetoothReceiver!= null)
            bluetoothReceiver.setBluetoothStateListener(bluetoothStateListener);
    }

    public void setBTDeviceBondListener(BTDeviceBondListener btDeviceBondListener) {
        if (bluetoothReceiver != null)
            bluetoothReceiver.setBtDeviceBondListener(btDeviceBondListener);
    }


    public void setBTDeviceDiscoveryListener(BTDeviceDiscoveryListener btDeviceDiscoveryListener) {
        if (bluetoothReceiver != null)
            bluetoothReceiver.setBtDeviceDiscoveryListener(btDeviceDiscoveryListener);
    }

    public void setBTDeviceAclListener(BTDeviceAclListener btDeviceAclListener) {
        if (bluetoothReceiver != null)
            bluetoothReceiver.setBTDeviceAclListener(btDeviceAclListener);
    }


    /**
     * 搜索蓝牙设备
     */
    public void startDiscoveryBTDevice() {
        if (bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
        bluetoothAdapter.startDiscovery();
    }

    /**
     * 停止搜索蓝牙设备
     */
    public void stopDiscoveryBTDevice() {
        bluetoothAdapter.cancelDiscovery();
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

    /**
     * 获取已配对的蓝牙设备列表
     *
     * @return
     */
    public Set<BluetoothDevice> getBondDevices() {
        return bluetoothAdapter.getBondedDevices();
    }


    public BluetoothDevice getDevice(String btdAddress) {
        return this.bluetoothAdapter.getRemoteDevice(btdAddress);
    }


    /**
     * 配对蓝牙设备
     *
     * @return
     */
    public boolean bondDevice(String address) {
        if (TextUtils.isEmpty(address))
            return false;
        BluetoothBase bluetoothBase = new BluetoothBase();
        bluetoothBase.setBtDevAddress(address);
        boolean ret = bluetoothBase.connect();
        if (!ret) {
            BluetoothDevice device = getDevice(address);
            if (device != null && device.getBondState() == BluetoothDevice.BOND_BONDED) {
                ret = true;
            }
        }
        bluetoothBase.disconnect();
        return ret;
    }

    public boolean unBondDevice(String address) {

        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
        boolean ret = false;
        try {
            Method removeBondMethod = device.getClass().getDeclaredMethod("removeBond");
            ret = (Boolean) removeBondMethod.invoke(device);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }


}
