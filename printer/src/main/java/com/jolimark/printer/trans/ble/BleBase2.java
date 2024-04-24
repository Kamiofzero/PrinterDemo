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

import com.jolimark.printer.trans.TransBase;
import com.jolimark.printer.trans.ble.util.ByteArrayUtils;
import com.jolimark.printer.util.LogUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

class BleBase2 extends TransBase {
    private static final String TAG = "BluetoothBase";

    private BluetoothDevice btDev;
    private BluetoothGatt bluetoothGatt;

    private Context context;
    private int connectTryCount;

    HashMap<String, Callback> notificationMap = new HashMap<>();
    HashMap<String, ReturnData> writeReadMap = new HashMap<>();
    HashMap<String, Callback> writeReadCallbackMap = new HashMap<>();
    Callback connectCallback;

    final int STATE_DISCONNECTED = 0;
    final int STATE_CONNECTING = 1;
    final int STATE_CONNECTED = 2;

    int state = STATE_DISCONNECTED;
    boolean f_connect;

    ConnectionListener connectionListener;
    Timer connectTimer = new Timer();
    ConnectTimerTask connectTask;

    BleCallback bleCallback;

    public void setContext(Context context) {
        this.context = context;
    }


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
        connect();
        return true;
    }


    private void connect_r() {
        synchronized (BleBase2.this) {
            if (state == STATE_DISCONNECTED) {
                return;
            }
            LogUtil.i(TAG, "connect try count : " + connectTryCount);
            if (connectTryCount > 0) {
                connectTryCount--;
            } else {
                LogUtil.i(TAG, "connect fail");
                state = STATE_DISCONNECTED;
                if (connectCallback != null) {
                    connectCallback.onFail("connect fail");
                    connectCallback = null;
                }
                return;
            }
            ConnectLock connectLock = new ConnectLock();
            bleCallback = new BleCallback();
            bleCallback.setConnectLock(connectLock);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                bluetoothGatt = btDev.connectGatt(context, false, bleCallback, BluetoothDevice.TRANSPORT_LE);
            } else {
                bluetoothGatt = btDev.connectGatt(context, false, bleCallback);
            }
            connectTask = new ConnectTimerTask();
            connectTask.setBluetoothGatt(bluetoothGatt);
            connectTask.setConnectLock(connectLock);
            connectTimer.schedule(connectTask, 10000);
            LogUtil.i(TAG, "connectGatt");
        }
    }

    @Override
    public boolean connect() {

        return false;
    }

    @Override
    public boolean sendData(byte[] bytes) {
        return false;
    }

    @Override
    public int receiveData(byte[] buffer, int timeout) {
        return 0;
    }

    @Override
    public void disconnect() {

    }


    public boolean isConnected() {
        return state == STATE_CONNECTED;
    }

    public int getState() {
        return state;
    }


    private class ConnectTimerTask extends TimerTask {

        public ConnectLock connectLock;
        BluetoothGatt bluetoothGatt;

        public void setConnectLock(ConnectLock connectLock) {
            this.connectLock = connectLock;
        }

        public void setBluetoothGatt(BluetoothGatt bluetoothGatt) {
            this.bluetoothGatt = bluetoothGatt;
        }

        @Override
        public void run() {
            if (connectLock.lock()) {
                LogUtil.i(TAG, "10 second connect timeout");
                if (bluetoothGatt != null) {
                    LogUtil.i(TAG, "disconnect gatt");
                    bluetoothGatt.disconnect();
                    bluetoothGatt.close();
                    connect();
                }
            }
        }
    }


    class ConnectLock {
        boolean isUsed = false;

        public synchronized boolean lock() {
            if (!isUsed) {
                isUsed = true;
                return true;
            }
            return false;
        }
    }

    private class BleCallback extends BluetoothGattCallback {

        ConnectLock connectLock;

        public void setConnectLock(ConnectLock connectLock) {
            this.connectLock = connectLock;
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            LogUtil.i(TAG, "onConnectionStateChange -> status: " + status + " , newState: " + newState);

            if (newState == BluetoothGatt.STATE_CONNECTED) {
                LogUtil.i(TAG, "connected , to discover services");
                if (connectTask != null) connectTask.cancel();
                if (connectLock.lock()) gatt.discoverServices();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                gatt.disconnect();
                gatt.close();
                //如果是连接中，则表示连接失败
                if (state == STATE_CONNECTING) {
                    LogUtil.i(TAG, "connect fail this run");
                    if (connectTask != null) connectTask.cancel();
                    if (connectLock.lock()) connect();
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
            LogUtil.i(TAG, "onCharacteristicWrite uuid: " + characteristic.getUuid().toString() + " status: " + status);
            Callback callback = writeReadCallbackMap.get(characteristic.getUuid().toString());
            LogUtil.i(TAG, "characteristic uuid " + characteristic.getUuid().toString());
            ReturnData returnData = writeReadMap.get(characteristic.getUuid().toString());

            LogUtil.i(TAG, "callback:" + callback + ", returnData: " + returnData);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (callback != null) callback.onSuccess(null);
                if (returnData != null) returnData.valueEnable = 1;
                LogUtil.i(TAG, "[" + characteristic.getUuid() + "]: write success.");

            } else {
                if (callback != null) callback.onFail("Write characteristic fail");
                if (returnData != null) returnData.valueEnable = -1;
                LogUtil.i(TAG, "[" + characteristic.getUuid() + "]: write fail.");
            }
            if (callback != null) writeReadCallbackMap.remove(characteristic.getUuid().toString());
            if (returnData != null) writeReadMap.remove(characteristic.getUuid().toString());


        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            LogUtil.i(TAG, "onCharacteristicRead uuid: " + characteristic.getUuid().toString() + " status: " + status);


            Callback callback = writeReadCallbackMap.get(characteristic.getUuid().toString());
            ReturnData returnData = writeReadMap.get(characteristic.getUuid().toString());
            LogUtil.i(TAG, "returnData: " + returnData);

            if (status == BluetoothGatt.GATT_SUCCESS) {

                byte[] buff = characteristic.getValue();
                if (buff != null) {
                    LogUtil.i(TAG, "[" + characteristic.getUuid() + "]: read -> " + ByteArrayUtils.toArrayString(buff, buff.length));
                    if (callback != null) callback.onSuccess(buff);
                    if (returnData != null) {
                        returnData.valueEnable = 1;
                        returnData.data = buff;
                    }
                } else {
                    LogUtil.i(TAG, "[" + characteristic.getUuid() + "]: read none.");
                    if (callback != null) callback.onFail("read characteristic none");
                    if (returnData != null) returnData.valueEnable = -1;
                }

            } else {
                LogUtil.i(TAG, "[" + characteristic.getUuid() + "]: read fail.");
                if (callback != null) callback.onFail("read characteristic fail");
                if (returnData != null) returnData.valueEnable = -1;
            }
            if (callback != null) writeReadCallbackMap.remove(characteristic.getUuid().toString());
            if (returnData != null) writeReadMap.remove(characteristic.getUuid().toString());

        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//            LogUtil.i(TAG, "onCharacteristicChanged");
            Callback callback = notificationMap.get(characteristic.getUuid().toString());
            byte[] buff = characteristic.getValue();
            if (buff != null) {
                callback.onSuccess(buff);
//                LogUtil.i(TAG, "[" + characteristic.getUuid() + "]: notify -> " + ByteArrayUtils.toArrayString(buff, buff.length));
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
    }

    ;

    /**
     * 在连接中调用时，如果没调用connect方法，则取消connectTask及把bluetoothGatt断连即可
     * 如果已经在调用connect，则通过同步卡住，该方法执行在前，则state设为STATE_DISCONNECTED后，connect方法不会继续
     * 该方法执行在后，则也是取消connectTask及断连BluetoothGatt
     */
    public void closeTrans() {
        synchronized (BleBase2.this) {
            if (state >= STATE_CONNECTING) {
                LogUtil.i(TAG, "bluetoothGatt disconnect");
                bluetoothGatt.disconnect();
                bluetoothGatt.close();
                notificationMap.clear();
                writeReadMap.clear();
                int tempState = state;
                state = STATE_DISCONNECTED;
                if (tempState == STATE_CONNECTING) {
                    if (connectTask != null) connectTask.cancel();
                    if (connectCallback != null) {
                        connectCallback.onFail("connect fail");
                        connectCallback = null;
                    }
                }
            } else {
                LogUtil.i(TAG, "bluetoothGatt not connect");
            }
        }
    }


    private boolean checkConnected() {
        if (state != STATE_CONNECTED) {
            LogUtil.i(TAG, "bluetoothGatt not connect.");
            return false;
        }
        return true;
    }


    private BluetoothGattCharacteristic findCharacter(String serviceUUid, String characteristicUUid) {
        BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(serviceUUid));
        if (service == null) {
            LogUtil.i(TAG, "can not find service.");
            return null;
        }
        LogUtil.i(TAG, "service found.");

        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characteristicUUid));
        if (characteristic == null) {
            LogUtil.i(TAG, "can not find characteristic.");
            return null;
        }
        LogUtil.i(TAG, "characteristic found.");
        return characteristic;
    }


    private boolean writeCharacter(byte[] data, String serviceUUid, String writeCharacteristicUUid) {
        BluetoothGattCharacteristic writeCharacteristic = findCharacter(serviceUUid, writeCharacteristicUUid);
        if (writeCharacteristic == null) {
            return false;
        }
        LogUtil.i(TAG, "data: " + data.length + " bytes, " + ByteArrayUtils.toArrayString(data, data.length));
        writeCharacteristic.setValue(data);
//        writeCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        bluetoothGatt.writeCharacteristic(writeCharacteristic);
        LogUtil.i(TAG, "write characteristic.");

        return true;
    }

    private boolean readCharacter(String serviceUUid, String writeCharacteristicUUid) {
        BluetoothGattCharacteristic readCharacteristic = findCharacter(serviceUUid, writeCharacteristicUUid);
        if (readCharacteristic == null) {
            return false;
        }
        bluetoothGatt.readCharacteristic(readCharacteristic);
        LogUtil.i(TAG, "read characteristic.");

        return true;
    }


    public boolean sendData(byte[] data, String serviceUUid, String writeCharacteristicUUid, Callback callback) {
        LogUtil.i(TAG, "sendData -> serviceUUid:[" + serviceUUid + "] , writeCharacteristicUUid:[" + writeCharacteristicUUid + "] ");

        if (!checkConnected()) return false;
        if (!writeCharacter(data, serviceUUid, writeCharacteristicUUid)) return false;

        writeReadCallbackMap.put(writeCharacteristicUUid, callback);

        return true;
    }


    public boolean receiveData(String serviceUUid, String readCharacteristicUUid, Callback callback) {
        LogUtil.i(TAG, "receiveData -> serviceUUid:[" + serviceUUid + "] , readCharacteristicUUid:[" + readCharacteristicUUid + "]");

        if (!checkConnected()) return false;
        if (!readCharacter(serviceUUid, readCharacteristicUUid)) return false;

        writeReadCallbackMap.put(readCharacteristicUUid, callback);
        return true;
    }


    public boolean sendDataSynchronized(byte[] data, String serviceUUid, String writeCharacteristicUUid) {
        LogUtil.i(TAG, "sendData -> serviceUUid:[" + serviceUUid + "] , writeCharacteristicUUid:[" + writeCharacteristicUUid + "] ");

        if (!checkConnected()) return false;

        ReturnData returnData = new ReturnData();
        writeReadMap.put(writeCharacteristicUUid, returnData);

        if (!writeCharacter(data, serviceUUid, writeCharacteristicUUid)) return false;
//        final boolean[] timeout = new boolean[1];

//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                timeout[0] = true;
//            }
//        }, 5000);

        int timeOut = 0;
        while (true) {
//            if (timeout[0]) {
//                LogUtil.i(TAG, "time out");
//                return false;
//            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (returnData.valueEnable == 1) {
                LogUtil.i(TAG, "write success");
                return true;
            } else if (returnData.valueEnable == -1) {
                LogUtil.i(TAG, "write fail");
                return false;
            }
            timeOut++;
            if (timeOut == 5) {
                LogUtil.i(TAG, "write time out");
                return false;
            }
        }
//
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        return true;
    }


    public byte[] receiveDataSynchronized(String serviceUUid, String readCharacteristicUUid) {
        LogUtil.i(TAG, "receiveData -> serviceUUid:[" + serviceUUid + "] , readCharacteristicUUid:[" + readCharacteristicUUid + "]");

        if (!checkConnected()) return null;

        ReturnData returnData = new ReturnData();
        writeReadMap.put(readCharacteristicUUid, returnData);

        if (!readCharacter(serviceUUid, readCharacteristicUUid)) return null;


//        final boolean[] timeout = new boolean[1];
//
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                timeout[0] = true;
//            }
//        }, 5000);
        int timeOut = 0;
        while (true) {
//            if (timeout[0]) {
//                LogUtil.i(TAG, "time out");
//                return null;
//            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (returnData.valueEnable == 1) {
                LogUtil.i(TAG, "read success");
                return returnData.data;
            } else if (returnData.valueEnable == -1) {
                LogUtil.i(TAG, "read fail");
                return null;
            }
            timeOut++;
            if (timeOut == 5) {
                LogUtil.i(TAG, "read time out");
                return null;
            }
        }
    }


    public boolean notifyData(String serviceUUid, String notifyCharacteristicUUid, Callback callback) {
        LogUtil.i(TAG, "notifyData -> serviceUUid:[" + serviceUUid + "] , notifyCharacteristicUUid:[" + notifyCharacteristicUUid + "]");
        if (!checkConnected()) return false;

        BluetoothGattCharacteristic notifyCharacteristic = findCharacter(serviceUUid, notifyCharacteristicUUid);
        if (notifyCharacteristic == null) {
            return false;
        }

        BluetoothGattDescriptor descriptor = notifyCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
        if (descriptor == null) {
            LogUtil.i(TAG, "can not find descriptor.");
            return false;
        }
        LogUtil.i(TAG, "descriptor found.");
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);
        bluetoothGatt.setCharacteristicNotification(notifyCharacteristic, true);
        LogUtil.i(TAG, "notify characteristic.");
        notificationMap.put(notifyCharacteristicUUid, callback);


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

        notificationMap.remove(notifyCharacteristicUUid);


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


    class ReturnData {
        int valueEnable;
        byte[] data;
    }
}
