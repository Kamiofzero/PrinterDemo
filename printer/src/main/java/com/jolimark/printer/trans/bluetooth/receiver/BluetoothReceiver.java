package com.jolimark.printer.trans.bluetooth.receiver;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jolimark.printer.trans.bluetooth.listener.BTDeviceAclListener;
import com.jolimark.printer.trans.bluetooth.listener.BTDeviceBondListener;
import com.jolimark.printer.trans.bluetooth.listener.BTDeviceDiscoveryListener;
import com.jolimark.printer.trans.bluetooth.listener.BTDevicePairListener;
import com.jolimark.printer.trans.bluetooth.listener.BluetoothStateListener;
import com.jolimark.printer.util.LogUtil;

@SuppressLint("MissingPermission")

public class BluetoothReceiver extends BroadcastReceiver {

    private final String TAG = "BluetoothReceiver";


    public BluetoothStateListener bluetoothStateListener;
    public BTDeviceBondListener btDeviceBondListener;
    public BTDeviceDiscoveryListener btDeviceDiscoveryListener;
    public BTDevicePairListener btDevicePairListener;
    public BTDeviceAclListener bTDeviceAclListener;

    private boolean flag_bt_state;
    private boolean flag_bt_device_bond;
    private boolean flag_bt_device_discovery;
    private boolean flag_bt_device_pair_request;
    private boolean flag_bt_device_acl;


    public void setBluetoothStateListener(BluetoothStateListener bluetoothStateListener) {
        this.bluetoothStateListener = bluetoothStateListener;
        flag_bt_state = bluetoothStateListener == null ? false : true;

    }

    public void setBtDeviceBondListener(BTDeviceBondListener btDeviceBondListener) {
        this.btDeviceBondListener = btDeviceBondListener;
        flag_bt_device_bond = btDeviceBondListener == null ? false : true;
    }

    public void setBtDeviceDiscoveryListener(BTDeviceDiscoveryListener btDeviceDiscoveryListener) {
        this.btDeviceDiscoveryListener = btDeviceDiscoveryListener;
        flag_bt_device_discovery = btDeviceDiscoveryListener == null ? false : true;
    }

    public void setBtDevicePairListener(BTDevicePairListener btDevicePairListener) {
        this.btDevicePairListener = btDevicePairListener;
        flag_bt_device_pair_request = btDevicePairListener == null ? false : true;

    }

    public void setBTDeviceAclListener(BTDeviceAclListener bTDeviceAclListener) {
        this.bTDeviceAclListener = bTDeviceAclListener;
        flag_bt_device_acl = bTDeviceAclListener == null ? false : true;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
//        LogUtil.i(TAG, "onReceive: " + action);
        switch (action) {

            //蓝牙开关状态
            case BluetoothAdapter.ACTION_STATE_CHANGED: {
                if (!flag_bt_state)
                    return;

                int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                if (blueState == BluetoothAdapter.STATE_ON) {
                    LogUtil.i(TAG, "bluetooth on");
                    if (bluetoothStateListener != null)
                        bluetoothStateListener.onBluetoothEnabled();
                } else if (blueState == BluetoothAdapter.STATE_TURNING_ON) {
                    LogUtil.i(TAG, "bluetooth turning on");
                } else if (blueState == BluetoothAdapter.STATE_OFF) {
                    LogUtil.i(TAG, "bluetooth off");
                    if (bluetoothStateListener != null)
                        bluetoothStateListener.onBluetoothDisabled();
                } else if (blueState == BluetoothAdapter.STATE_TURNING_OFF) {
                    LogUtil.i(TAG, "bluetooth turning off");
                }
                break;
            }
            //蓝牙设备搜索开始
            case BluetoothAdapter.ACTION_DISCOVERY_STARTED: {
                if (!flag_bt_device_discovery)
                    return;
                LogUtil.i(TAG, "discovery bluetooth devices start.");
                if (btDeviceDiscoveryListener != null)
                    btDeviceDiscoveryListener.onDeviceStart();
                break;
            }
            //蓝牙设备搜索结束
            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED: {
                if (!flag_bt_device_discovery)
                    return;
                LogUtil.i(TAG, "discovery bluetooth devices finish.");
                if (btDeviceDiscoveryListener != null)
                    btDeviceDiscoveryListener.onDeviceFinish();
                break;
            }

            //蓝牙设备搜索找到设备
            case BluetoothDevice.ACTION_FOUND: {
                if (!flag_bt_device_discovery)
                    return;
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
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
                    LogUtil.i(TAG, "bluetooth device found " + "[" + device.getName() + "," + device.getAddress() + "," + type + "]");
                }
                if (btDeviceDiscoveryListener != null)
                    btDeviceDiscoveryListener.onDeviceFound(device);
                break;
            }
            //蓝牙设备配对状态
            case BluetoothDevice.ACTION_BOND_STATE_CHANGED: {
                if (!flag_bt_device_bond)
                    return;
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                if (state == BluetoothDevice.BOND_BONDED) {
                    if (device != null)
                        LogUtil.i(TAG, " bluetooth device [" + device.getName() + " , " + device.getAddress() + "] bonded");
                    if (btDeviceBondListener != null)
                        btDeviceBondListener.onBTDeviceBonded(device);

                } else if (state == BluetoothDevice.BOND_BONDING) {
                    LogUtil.i(TAG, " bluetooth device [" + device.getName() + " , " + device.getAddress() + "] bonding");
                    if (btDeviceBondListener != null)
                        btDeviceBondListener.onBTDeviceBonding(device);

                } else if (state == BluetoothDevice.BOND_NONE) {
                    LogUtil.i(TAG, " bluetooth device [" + device.getName() + " , " + device.getAddress() + "] unbond");
                    if (btDeviceBondListener != null)
                        btDeviceBondListener.onBTDeviceBondNone(device);
                }
                break;
            }
            //蓝牙设备通讯连接
            case BluetoothDevice.ACTION_ACL_CONNECTED: {
                if (!flag_bt_device_acl)
                    return;

                if (bTDeviceAclListener != null) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device != null)
                        LogUtil.i(TAG, " bluetooth device [" + device.getName() + " , " + device.getAddress() + "] acl connected");
                    bTDeviceAclListener.onAclConnected(device);
                }


                break;
            }
            //蓝牙设备通讯连接断开
            case BluetoothDevice.ACTION_ACL_DISCONNECTED: {
                if (!flag_bt_device_acl)
                    return;

                if (bTDeviceAclListener != null) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device != null)
                        LogUtil.i(TAG, " bluetooth device [" + device.getName() + " , " + device.getAddress() + "] acl disconnected");
                    bTDeviceAclListener.onAclDisConnected(device);
                }
                break;
            }
            //蓝牙设备通讯连接请求
            case BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED: {
                if (!flag_bt_device_acl)
                    return;

                if (bTDeviceAclListener != null) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    bTDeviceAclListener.onAclConnectRequest(device);
                }
                break;
            }

            //蓝牙设备配对许可询问（用于自动配对时取消弹窗）
            case BluetoothDevice.ACTION_PAIRING_REQUEST: {
                if (!flag_bt_device_pair_request)
                    return;

                if (btDevicePairListener != null)
                    btDevicePairListener.onDevicePair();
                break;
            }
        }
    }
}
