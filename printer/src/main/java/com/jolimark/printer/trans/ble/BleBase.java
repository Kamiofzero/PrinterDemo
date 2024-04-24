package com.jolimark.printer.trans.ble;

/**
 * 蓝牙传输方式
 */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;


import com.jolimark.printer.trans.ble.util.ByteArrayUtils;
import com.jolimark.printer.util.LogUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

class BleBase {
    private static final String TAG = "BluetoothBase";

    private BluetoothDevice btDev;
    private BluetoothGatt bluetoothGatt;

    private Context context;
    private int connectTryCount;

    HashMap<String, Callback> callbackHashMap = new HashMap<>();
    Callback connectCallback;

//    private boolean isConnected;


    final int STATE_DISCONNECTED = 0;
    final int STATE_CONNECTING = 1;
    final int STATE_CONNECTED = 2;

    int state = STATE_DISCONNECTED;

    ConnectionListener connectionListener;
    Timer connectTimer = new Timer();
    TimerTask connectTask;
    //    Object lock = new Object();
//    boolean connectTimeout;

    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    public boolean openTrans(Context context, String btDevAddress, Callback callback) {
        if (btDevAddress == null) {
            LogUtil.i(TAG, "bt address null");
            return false;
        }
        this.context = context;
        this.connectCallback = callback;
        btDev = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(btDevAddress);
        connectTryCount = 3;

        state = STATE_CONNECTING;
//        connectTimeout = false;
        connectTask = new ConnectTimerTask();
        connectTimer.schedule(connectTask, 10000);

        connect();
        LogUtil.i(TAG, "connectGatt");

//        curTimeTask = new TimerTask() {
//            @Override
//            public void run() {
////                connectLoop = false;
//                synchronized (lock) {
//                    LogUtil.i(TAG, "connect time out.");
//                    if (connectCallback != null) {
//                        connectCallback.onFail("connect time out");
//                        connectCallback = null;
//                    }
//                }
//            }
//        };
//        timer.schedule(curTimeTask, 10000);
        return true;

    }


    private void connect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bluetoothGatt = btDev.connectGatt(context, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE);
        } else {
            bluetoothGatt = btDev.connectGatt(context, false, bluetoothGattCallback);
        }
    }


    public boolean isConnected() {
        return state == STATE_CONNECTED;
    }

    public int getState() {
        return state;
    }

    class ConnectTimerTask extends TimerTask {
        @Override
        public void run() {
            LogUtil.i(TAG, "10 second connect timeout");
//                connectTimeout = true;
            if (bluetoothGatt != null) {
                bluetoothGatt.disconnect();
                bluetoothGatt.close();
                if (connectTryCount > 0) {
                    connectTryCount--;
                    connect();
                } else {
                    LogUtil.i(TAG, "connect fail");
                    if (connectCallback != null) {
                        connectCallback.onFail("connect fail");
                        connectCallback = null;
                    }
                }
            }
        }
    }


    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            LogUtil.i(TAG, "onConnectionStateChange -> status: " + status + " , newState: " + newState);

            if (newState == BluetoothGatt.STATE_CONNECTED) {
                LogUtil.i(TAG, "connected , to discover services");
                connectTask.cancel();
                gatt.discoverServices();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                gatt.disconnect();
                gatt.close();
                //如果是连接中，则表示连接失败
                if (state == STATE_CONNECTING) {
                    connectTask.cancel();
                    connectTryCount--;
                    if (connectTryCount > 0) {
                        connect();
                    } else {
                        LogUtil.i(TAG, "connect fail");
                        state = STATE_DISCONNECTED;
                        if (connectCallback != null) {
                            connectCallback.onFail("connect fail");
                            connectCallback = null;
                        }
                    }
                }
                //如果是已连接，则表示连接断开
                else {
                    LogUtil.i(TAG, "disconnected");
                    state = STATE_DISCONNECTED;
                    if (connectionListener != null) {
                        connectionListener.onDeviceDisconnected();
                    }
                }

            }

//
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                LogUtil.i(TAG, "onConnectionStateChange -> success");
//
//                if (newState == BluetoothGatt.STATE_CONNECTED) {
//                    LogUtil.i(TAG, "connected");
//                    connectTask.cancel();
//                    gatt.discoverServices();
//                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
//                    gatt.disconnect();
//                    gatt.close();
//                    //蓝牙关闭导致连接失败，回调此处
//                    if (state == STATE_CONNECTING) {
//                        connectTask.cancel();
//                        LogUtil.i(TAG, "connect fail");
//                        if (connectCallback != null) {
//                            connectCallback.onFail("connect fail");
//                            connectCallback = null;
//                        }
//                    }
//                    //蓝牙关闭导致断开连接，回调此处
//                    else {
//                        LogUtil.i(TAG, "disconnected");
//                        state = STATE_DISCONNECTED;
//                        if (connectionListener != null) {
//                            connectionListener.onDeviceDisconnected();
//                        }
//                    }
//
//                }
//            } else {
//                LogUtil.i(TAG, "onConnectionStateChange -> fail");
//                gatt.disconnect();
//                gatt.close();
//
//                //已连接时，因设备自身断开连接，回调此处
//                if (state == STATE_CONNECTED) {
//                    state = STATE_DISCONNECTED;
//                    if (connectionListener != null) {
//                        connectionListener.onDeviceDisconnected();
//                    }
//                }
//                //在连接中，连接失败时，回调此处
//                else if (state == STATE_CONNECTING) {
//                    //超30秒连接失败则不再尝试剩余连接次数
//                    if (connectTimeout) {
//                        connectTimeout = false;
//                        if (connectCallback != null) {
//                            connectCallback.onFail("connect fail");
//                            connectCallback = null;
//                        }
//                        return;
//                    }
//
//                    //不超时情况下，防止第一次连接失败，进行多次连接
//                    if (connectTryCount > 0) {
//                        connectTryCount--;
//                        connect();
//                    } else {
//                        connectTask.cancel();
//                        LogUtil.i(TAG, "connect fail");
//                        if (connectCallback != null) {
//                            connectCallback.onFail("connect fail");
//                            connectCallback = null;
//                        }
//
//                    }
//                }
//            }

        }


        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> serviceList = gatt.getServices();
            StringBuilder sb = new StringBuilder();
            for (BluetoothGattService service : serviceList) {
                sb.append("Service :\r\n");
                sb.append("uuid:" + service.getUuid().toString() + "\r\n");
                List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();
                sb.append("    Characteristics:\r\n");
                for (BluetoothGattCharacteristic characteristic : characteristicList) {
                    sb.append("    uuid:" + characteristic.getUuid().toString() + "\r\n");
                    int charaProp = characteristic.getProperties();
                    sb.append("    prop: [");
                    if (((charaProp >> 1) & 0x01) == 1) {
                        sb.append("read");
                        sb.append(" ");
                    }
                    if (((charaProp >> 2) & 0x01) == 1) {
                        sb.append("write_no_response");
                        sb.append(" ");
                    }
                    if (((charaProp >> 3) & 0x01) == 1) {
                        sb.append("write");
                        sb.append(" ");
                    }
                    if (((charaProp >> 4) & 0x01) == 1) {
                        sb.append("notify");
                        sb.append(" ");
                    }
                    if (((charaProp >> 5) & 0x01) == 1) {
                        sb.append("indicate");
                        sb.append(" ");
                    }
                    if (((charaProp >> 6) & 0x01) == 1) {
                        sb.append("signed_write");
                    }
                    sb.append("]\r\n");
                }
            }
            LogUtil.i(TAG, "find services.");
//            LogUtil.i(TAG, sb.toString());
            LogUtil.i(TAG, "connect ready.");

            state = STATE_CONNECTED;
            if (connectCallback != null) {
                connectCallback.onSuccess(null);
                connectCallback = null;
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            LogUtil.i(TAG, "onCharacteristicWrite, status: " + status);
            Callback callback = callbackHashMap.get(characteristic.getUuid().toString());

            if (status == BluetoothGatt.GATT_SUCCESS) {
//                writeState = 1;
                callback.onSuccess(null);
                LogUtil.i(TAG, "[" + characteristic.getUuid() + "]: write success.");

            } else {
//                writeState = -1;
                callback.onFail("Write characteristic fail");
                LogUtil.i(TAG, "[" + characteristic.getUuid() + "]: write fail.");
            }
            callbackHashMap.remove(characteristic.getUuid().toString());
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            LogUtil.i(TAG, "onCharacteristicRead, status: " + status);

            Callback callback = callbackHashMap.get(characteristic.getUuid().toString());

            if (status == BluetoothGatt.GATT_SUCCESS) {

                byte[] buff = characteristic.getValue();
                if (buff != null) {
                    LogUtil.i(TAG, "[" + characteristic.getUuid() + "]: read -> " + ByteArrayUtils.toArrayString(buff, buff.length));
                    callback.onSuccess(buff);
                } else {
                    LogUtil.i(TAG, "[" + characteristic.getUuid() + "]: read none.");
                    callback.onFail("read characteristic none");
                }

            } else {
                LogUtil.i(TAG, "[" + characteristic.getUuid() + "]: read fail.");
                callback.onFail("read characteristic fail");
            }
            callbackHashMap.remove(characteristic.getUuid().toString());

        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            Callback callback = callbackHashMap.get(characteristic.getUuid().toString());
            byte[] buff = characteristic.getValue();
            if (buff != null) {
                callback.onSuccess(buff);
                LogUtil.i(TAG, "[" + characteristic.getUuid() + "]: notify -> " + ByteArrayUtils.toArrayString(buff, buff.length));
            } else {
                callback.onFail("notify characteristic null");
                LogUtil.i(TAG, "[" + characteristic.getUuid() + "]: notify null.");
            }

        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            LogUtil.i(TAG, "onDescriptorWrite");

        }


        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }
    };


    public void closeTrans() {
        if (state == STATE_CONNECTED || state == STATE_CONNECTING) {
            LogUtil.i(TAG, "bluetoothGatt disconnect");
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            callbackHashMap.clear();
            state = STATE_DISCONNECTED;
        } else {
            LogUtil.i(TAG, "bluetoothGatt not connect");
        }
    }


    public boolean sendData(byte[] data, String serviceUUid, String writeCharacteristicUUid, Callback callback) {
        if (state != STATE_CONNECTED) {
            LogUtil.i(TAG, "bluetoothGatt not connect.");
            return false;
        }
        LogUtil.i(TAG, "sendData -> serviceUUid:[" + serviceUUid + "] , writeCharacteristicUUid:[" + writeCharacteristicUUid + "] ");
        BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(serviceUUid));
        if (service == null) {
            LogUtil.i(TAG, "can not find service.");
            return false;
        }
        LogUtil.i(TAG, "service found.");

        BluetoothGattCharacteristic writeCharacteristic = service.getCharacteristic(UUID.fromString(writeCharacteristicUUid));
        if (writeCharacteristic == null) {
            LogUtil.i(TAG, "can not find writeCharacteristic.");
            return false;
        }
        LogUtil.i(TAG, "writeCharacteristic found.");
        LogUtil.i(TAG, "send " + data.length + " bytes, " + ByteArrayUtils.toArrayString(data, data.length));
        callbackHashMap.put(writeCharacteristicUUid, callback);

        writeCharacteristic.setValue(data);
        writeCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        bluetoothGatt.writeCharacteristic(writeCharacteristic);


        return true;
    }


    public boolean receiveData(String serviceUUid, String readCharacteristicUUid, Callback callback) {
        if (state != STATE_CONNECTED) {
            LogUtil.i(TAG, "bluetoothGatt not connect.");
            return false;
        }
        LogUtil.i(TAG, "receiveData -> serviceUUid:[" + serviceUUid + "] , readCharacteristicUUid:[" + readCharacteristicUUid + "]");
        BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(serviceUUid));
        if (service == null) {
            LogUtil.i(TAG, "can not find service.");
            return false;
        }
        LogUtil.i(TAG, "service found.");

        BluetoothGattCharacteristic readCharacteristic = service.getCharacteristic(UUID.fromString(readCharacteristicUUid));
        if (readCharacteristic == null) {
            LogUtil.i(TAG, "can not find characteristic.");
            return false;
        }
        LogUtil.i(TAG, "characteristic found.");

        callbackHashMap.put(readCharacteristicUUid, callback);

        bluetoothGatt.readCharacteristic(readCharacteristic);
        LogUtil.i(TAG, "read characteristic.");
        return true;
    }


    public boolean notifyData(String serviceUUid, String notifyCharacteristicUUid, Callback callback) {
        if (state != STATE_CONNECTED) {
            LogUtil.i(TAG, "bluetoothGatt not connect.");
            return false;
        }
        LogUtil.i(TAG, "notifyData -> serviceUUid:[" + serviceUUid + "] , notifyCharacteristicUUid:[" + notifyCharacteristicUUid + "]");
        BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(serviceUUid));
        if (service == null) {
            LogUtil.i(TAG, "can not find service.");
            return false;
        }
        LogUtil.i(TAG, "service found.");

        BluetoothGattCharacteristic notifyCharacteristic = service.getCharacteristic(UUID.fromString(notifyCharacteristicUUid));
        if (notifyCharacteristic == null) {
            LogUtil.i(TAG, "can not find characteristic.");
            return false;
        }
        LogUtil.i(TAG, "characteristic found.");

        callbackHashMap.put(notifyCharacteristicUUid, callback);


        BluetoothGattDescriptor descriptor = notifyCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
        if (descriptor == null) {
            LogUtil.i(TAG, "can not find descriptor.");
            return false;
        }
        LogUtil.i(TAG, "descriptor found.");
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);

//            if (enabled == true) {
//        mBluetoothGatt.writeDescriptor(descriptor);
//            }

//            else {
//                BluetoothGattDescriptor descriptor = characteristic
//                        .getDescriptor(UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
//                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
//                mBluetoothGatt.writeDescriptor(descriptor);
//            }
//    }

//        bluetoothGatt.requestMtu(128);
        bluetoothGatt.setCharacteristicNotification(notifyCharacteristic, true);
        LogUtil.i(TAG, "notify characteristic.");

        return true;
    }

    public boolean cancelNotifyData(String serviceUUid, String notifyCharacteristicUUid) {
        if (state != STATE_CONNECTED) {
            LogUtil.i(TAG, "bluetoothGatt not connect.");
            return false;
        }
        LogUtil.i(TAG, "cancelNotifyData -> serviceUUid:[" + serviceUUid + "] , notifyCharacteristicUUid:[" + notifyCharacteristicUUid + "]");
        BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(serviceUUid));
        if (service == null) {
            LogUtil.i(TAG, "can not find service.");
            return false;
        }
        LogUtil.i(TAG, "service found.");

        BluetoothGattCharacteristic notifyCharacteristic = service.getCharacteristic(UUID.fromString(notifyCharacteristicUUid));
        if (notifyCharacteristic == null) {
            LogUtil.i(TAG, "can not find characteristic.");
            return false;
        }
        LogUtil.i(TAG, "characteristic found.");

        BluetoothGattDescriptor descriptor = notifyCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
        if (descriptor == null) {
            LogUtil.i(TAG, "can not find descriptor.");
            return false;
        }
        LogUtil.i(TAG, "cancel notify characteristic.");

        callbackHashMap.remove(notifyCharacteristicUUid);


        LogUtil.i(TAG, "descriptor found.");
        descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);

        bluetoothGatt.setCharacteristicNotification(notifyCharacteristic, false);
        LogUtil.i(TAG, "cancel notify characteristic.");

        return true;
    }


    public void setListener(ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }


    public void release() {
        connectionListener = null;
        context = null;
    }
}
