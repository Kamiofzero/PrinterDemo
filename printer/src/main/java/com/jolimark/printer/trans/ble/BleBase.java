package com.jolimark.printer.trans.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Build;

import com.jolimark.printer.common.MsgCode;
import com.jolimark.printer.trans.TransBase;
import com.jolimark.printer.trans.ble.util.ByteArrayUtils;
import com.jolimark.printer.util.LogUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BleBase extends TransBase {
    private final String TAG = getClass().getSimpleName();
    private BluetoothDevice btDev;
    private BluetoothGatt bluetoothGatt;
    private Context context;

    private int connectTryCount;

    private HashMap<String, Callback> notificationMap = new HashMap<>();
    private HashMap<String, ReturnData> writeReadMap = new HashMap<>();


    private final int STATE_DISCONNECTED = 0;
    private final int STATE_CONNECTING = 1;
    private final int STATE_CONNECTED = 2;

    private int state = STATE_DISCONNECTED;

    private String btAddress;

    private int connectRet;


    public void setContext(Context context) {
        this.context = context;
    }

    public void setBtAddress(String address) {
        this.btAddress = address;
    }

    public String getBtAddress() {
        return btAddress;
    }

    private String serviceUUid = "000000ff-0000-1000-8000-00805f9b34fb";
    private String characteristicUUid = "0000ff01-0000-1000-8000-00805f9b34fb";
    private BluetoothGattCharacteristic writeCharacteristic;

    private Object lock = new Object();
    private Object writeLock = new Object();

    @Override
    public boolean connect() {
        if (btAddress == null) {
            LogUtil.i(TAG, "bt address null");
            MsgCode.setLastErrorCode(MsgCode.ER_BLE_ADDRESS_NULL);
            return false;
        }
        LogUtil.i(TAG, "connect address: " + btAddress);
        btDev = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(btAddress);
        connectTryCount = 3;
        state = STATE_CONNECTING;
        connectRet = 0;
        while (true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                bluetoothGatt = btDev.connectGatt(context, false, bleCallback, BluetoothDevice.TRANSPORT_LE);
            } else {
                bluetoothGatt = btDev.connectGatt(context, false, bleCallback);
            }
            timeoutTask = new ConnectTimeoutTask();
            timeoutTimer.schedule(timeoutTask, 10000);
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            connectTryCount--;
            if (connectRet == 1 || connectTryCount == 0) {
                break;
            }
            connectRet = 0;
            LogUtil.i(TAG, "remain try count: " + connectTryCount);
        }

        if (connectRet == -1) {
            MsgCode.setLastErrorCode(MsgCode.ER_BLE_CONNECT_FAIL);
            state = STATE_DISCONNECTED;
            return false;
        } else if (connectRet == -2) {
            MsgCode.setLastErrorCode(MsgCode.ER_BLE_CONNECT_TIMEOUT);
            state = STATE_DISCONNECTED;
            return false;
        }
        LogUtil.i(TAG, "connect success, to discover services");
        connectRet = 0;
        bluetoothGatt.discoverServices();
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if (connectRet == -3) {
            MsgCode.setLastErrorCode(MsgCode.ER_BLE_SERVICE_NOT_FOUND);
            state = STATE_DISCONNECTED;
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            return false;
        } else if (connectRet == -4) {
            MsgCode.setLastErrorCode(MsgCode.ER_BLE_CHARACTERISTIC_NOT_FOUND);
            state = STATE_DISCONNECTED;
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            return false;
        }
        LogUtil.i(TAG, "connect finish");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean ret = bluetoothGatt.requestMtu(256);
            LogUtil.i(TAG, "requestMtu :" + ret);
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        state = STATE_CONNECTED;
        return true;
    }

    /**
     * 连接超时计时任务
     */
    private ConnectTimeoutTask timeoutTask;
    /**
     * 连接超时计时器
     */
    private Timer timeoutTimer = new Timer();

    private class ConnectTimeoutTask extends TimerTask {

        @Override
        public void run() {
            synchronized (lock) {
                if (connectRet == 0) {
                    LogUtil.i(TAG, "connect timeout");
                    connectRet = -2;
                    bluetoothGatt.disconnect();
                    bluetoothGatt.close();
//                    lock.notify();
                }
            }
        }
    }


    private BluetoothGattCallback bleCallback = new BluetoothGattCallback() {

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            LogUtil.i(TAG, "onMtuChanged-> status:" + status + " , mtu: " + mtu);
            if(status==BluetoothGatt.GATT_SUCCESS){
                LogUtil.i(TAG, "requestMtu success");
            }
            synchronized (lock) {
                lock.notify();
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            LogUtil.i(TAG, "onConnectionStateChange -> status: " + status + " , newState: " + newState);

            if (newState == BluetoothGatt.STATE_CONNECTED) {
                synchronized (lock) {
                    if (connectRet == 0) {
                        if (timeoutTask != null)
                            timeoutTask.cancel();
                        connectRet = 1;
                        lock.notify();
                    }
                }
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                gatt.disconnect();
                gatt.close();
                //如果是连接中，则表示连接失败
                if (state == STATE_CONNECTING) {
                    synchronized (lock) {
                        if (connectRet == 0) {
                            LogUtil.i(TAG, "connect fail this run");
                            if (timeoutTask != null)
                                timeoutTask.cancel();
                            connectRet = -1;
                        } else if (connectRet == -2) {
                        }
                        lock.notify();
                    }
                }
                //如果是已连接，则表示连接断开
                else {
                    LogUtil.i(TAG, "disconnected");
                    state = STATE_DISCONNECTED;
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
            LogUtil.i(TAG, sb.toString());
            int ret = 0;
            BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(serviceUUid));
            if (service == null) {
                LogUtil.i(TAG, "can not find service.");
                MsgCode.setLastErrorCode(MsgCode.ER_BLE_SERVICE_NOT_FOUND);
                ret = -3;
            } else {
                LogUtil.i(TAG, "service found.");
                writeCharacteristic = service.getCharacteristic(UUID.fromString(characteristicUUid));
                if (writeCharacteristic == null) {
                    LogUtil.i(TAG, "can not find characteristic.");
                    MsgCode.setLastErrorCode(MsgCode.ER_BLE_CHARACTERISTIC_NOT_FOUND);
                    ret = -4;
                } else
                    LogUtil.i(TAG, "characteristic found.");
            }

            synchronized (lock) {
                connectRet = ret;
                lock.notify();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            LogUtil.i(TAG, "onCharacteristicWrite uuid: " + characteristic.getUuid().toString() + " status: " + status);
            LogUtil.i(TAG, "characteristic uuid " + characteristic.getUuid().toString());

            synchronized (writeLock) {
                ReturnData returnData = writeReadMap.get(characteristic.getUuid().toString());
                if (returnData != null && returnData.valueEnable == 0) {
                    LogUtil.i(TAG, " returnData: " + returnData);
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        returnData.valueEnable = 1;
                        LogUtil.i(TAG, "[" + characteristic.getUuid() + "]: write success.");
                    } else {
                        returnData.valueEnable = -1;
                        LogUtil.i(TAG, "[" + characteristic.getUuid() + "]: write fail.");
                    }
                    if (writeTimeoutTask != null) writeTimeoutTask.cancel();
                    writeLock.notify();
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            LogUtil.i(TAG, "onCharacteristicRead uuid: " + characteristic.getUuid().toString() + " status: " + status);
            ReturnData returnData = writeReadMap.get(characteristic.getUuid().toString());
            LogUtil.i(TAG, "returnData: " + returnData);

            if (status == BluetoothGatt.GATT_SUCCESS) {

                byte[] buff = characteristic.getValue();
                if (buff != null) {
                    LogUtil.i(TAG, "[" + characteristic.getUuid() + "]: read -> " + ByteArrayUtils.toArrayString(buff, buff.length));
                    if (returnData != null) {
                        returnData.valueEnable = 1;
                        returnData.data = buff;
                    }
                } else {
                    LogUtil.i(TAG, "[" + characteristic.getUuid() + "]: read none.");
                    if (returnData != null) returnData.valueEnable = -1;
                }

            } else {
                LogUtil.i(TAG, "[" + characteristic.getUuid() + "]: read fail.");
                if (returnData != null) returnData.valueEnable = -1;
            }
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
    };


    /**
     * 连接超时计时任务
     */
    private WriteTimeoutTask writeTimeoutTask;
    /**
     * 连接超时计时器
     */
    private Timer writeTimer = new Timer();

    private class WriteTimeoutTask extends TimerTask {
        private ReturnData returnData;

        public WriteTimeoutTask(ReturnData returnData) {
            this.returnData = returnData;
        }

        @Override
        public void run() {
            synchronized (writeLock) {
                if (returnData.valueEnable == 0) {
                    returnData.valueEnable = -2;
                    writeLock.notify();
                }
            }
        }
    }

    @Override
    public boolean sendData(byte[] bytes) {

        LogUtil.i(TAG, "sendData -> serviceUUid:[" + serviceUUid + "] , writeCharacteristicUUid:[" + characteristicUUid + "] ");
        if (state != STATE_CONNECTED) {
            LogUtil.i(TAG, "bluetoothGatt not connect.");
            return false;
        }

        ReturnData returnData = new ReturnData();
        writeReadMap.put(characteristicUUid, returnData);

        if (!writeCharacter(bytes)) return false;

        writeTimeoutTask = new WriteTimeoutTask(returnData);
        writeTimer.schedule(writeTimeoutTask, 60000);

        synchronized (writeLock) {
            try {
                writeLock.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        writeReadMap.remove(characteristicUUid);
        if (returnData.valueEnable == 1) {
            LogUtil.i(TAG, "write success");
            return true;
        } else if (returnData.valueEnable == -1) {
            LogUtil.i(TAG, "write fail");
            return false;
        } else if (returnData.valueEnable == -2) {
            LogUtil.i(TAG, "write timeout");
            return false;
        }
        return false;

//        int timeOut = 0;
//        while (true) {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            if (returnData.valueEnable == 1) {
//                LogUtil.i(TAG, "write success");
//                return true;
//            } else if (returnData.valueEnable == -1) {
//                LogUtil.i(TAG, "write fail");
//                return false;
//            }
//            timeOut++;
//            if (timeOut == 5) {
//                LogUtil.i(TAG, "write time out");
//                return false;
//            }
//        }
    }

    private boolean writeCharacter(byte[] data) {
        LogUtil.i(TAG, "data: " + data.length + " bytes, " + ByteArrayUtils.toArrayString(data, data.length));
        writeCharacteristic.setValue(data);
        writeCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        bluetoothGatt.writeCharacteristic(writeCharacteristic);
        LogUtil.i(TAG, "write characteristic.");
        return true;
    }

    class ReturnData {
        int valueEnable;
        byte[] data;
    }

    @Override
    public int receiveData(byte[] buffer, int timeout) {
        return 0;
    }

    @Override
    public void disconnect() {
        if (state >= STATE_CONNECTING) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            state = STATE_DISCONNECTED;
            LogUtil.i(TAG, "bluetoothGatt disconnect");
        }
    }
}
