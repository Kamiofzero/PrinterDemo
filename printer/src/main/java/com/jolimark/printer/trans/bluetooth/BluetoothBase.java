package com.jolimark.printer.trans.bluetooth;

/**
 * 蓝牙传输方式
 */

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;

import com.jolimark.printer.common.MsgCode;
import com.jolimark.printer.trans.TransBase;
import com.jolimark.printer.trans.bluetooth.util.SystemUtils;
import com.jolimark.printer.util.LogUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

@SuppressLint("MissingPermission")
public class BluetoothBase implements TransBase {
    private static final String TAG = "BluetoothBase";

    private static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private BluetoothSocket btSocket;
    private BluetoothDevice btDev;


    private DataInputStream ins;
    private DataOutputStream out;


    private String btDevAddress;

    public void setBtDevAddress(String btDevAddress) {
        this.btDevAddress = btDevAddress;
    }


    @Override
    public boolean connect() {
        if (btDevAddress == null) {
            LogUtil.i(TAG, "bt address not set");
            MsgCode.setLastErrorCode(MsgCode.ER_BT_ADDRESS_NULL);
            return false;
        }
        if (btSocket != null) {
            disconnect();
        }
        btDev = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(btDevAddress);
        LogUtil.i(TAG, "bt socket connecting to " + btDevAddress + " ...");
        boolean flag_connect = false;
        if (connectRFCommSocket()) {
            flag_connect = true;
            LogUtil.i(TAG, "connectRFCommSocket success");
        }
        if (!flag_connect) {
            LogUtil.i(TAG, "connectRFCommSocket fail");

            try {
                Thread.sleep(300);

                if (btSocket != null)
                    btSocket.close();

            } catch (IOException e) {
                LogUtil.i(TAG, e.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (connectWithChannel()) {
                flag_connect = true;
                LogUtil.i(TAG, "connectWithChannel 6 success");
            } else
                LogUtil.i(TAG, "connectWithChannel 6 fail");
        }


        if (!flag_connect) {
            LogUtil.i(TAG, "bt socket connect fail");
            MsgCode.setLastErrorCode(MsgCode.ER_BT_CONNECT_FAIL);
            return false;
        }

        try {
            out = new DataOutputStream(btSocket.getOutputStream());
            ins = new DataInputStream(btSocket.getInputStream());
        } catch (IOException e) {
            out = null;
            ins = null;
            LogUtil.i(TAG, e.getMessage());
            LogUtil.i(TAG, "bt socket get IO stream fail");
            MsgCode.setLastErrorCode(MsgCode.ER_BT_CONNECT_FAIL);
            return false;
        }

        LogUtil.i(TAG, "bt socket connect success");
        return true;
    }


    private boolean connectRFCommSocket() {
        LogUtil.i(TAG, "connectRFCommSocket");
        UUID uuid = UUID.fromString(SPP_UUID);
        Class<? extends BluetoothDevice> cls = BluetoothDevice.class;
        Method m = null;
        if (Build.VERSION.SDK_INT >= 10 && !SystemUtils.isMediatekPlatform()) {
            try {
                m = cls.getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            } catch (NoSuchMethodException var9) {
                var9.printStackTrace();
            }
            if (m != null) {
                try {
                    btSocket = (BluetoothSocket) m.invoke(btDev, uuid);
                } catch (IllegalAccessException e) {
                    btSocket = null;
                    LogUtil.i(TAG, e.getMessage());
                } catch (InvocationTargetException e) {
                    btSocket = null;
                    LogUtil.i(TAG, e.getMessage());
                }
            }
        } else {
            try {
                btSocket = btDev.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                btSocket = null;
                LogUtil.i(TAG, e.getMessage());
            }
        }
        if (btSocket == null) {
            LogUtil.i(TAG, "bt socket create fail");
            MsgCode.setLastErrorCode(MsgCode.ER_BT_CONNECT_FAIL);
            return false;
        }

        if (SystemUtils.isMediatekPlatform()) {
            try {
                LogUtil.i(TAG, "it is MTK platform");
                Thread.sleep(3000);
            } catch (InterruptedException var6) {
                var6.printStackTrace();
            }
        }

        int tryCount = 2;
        boolean result;
        while (true) {
            try {
                btSocket.connect();
                result = true;
            } catch (IOException e) {
                LogUtil.i(TAG, "connect failed , try count " + tryCount);
                result = false;
                LogUtil.i(TAG, e.getMessage());
                if (--tryCount > 0)
                    continue;
            }
            break;
        }
        return result;
    }

    private boolean connectWithChannel() {
        LogUtil.i(TAG, "connectWithChannel");
        Class<? extends BluetoothDevice> cls = BluetoothDevice.class;
        Method m = null;

        try {
            m = cls.getMethod("createRfcommSocket", Integer.TYPE);
        } catch (NoSuchMethodException e) {
            LogUtil.i(TAG, e.getMessage());
            LogUtil.i(TAG, "bt socket create fail");
        }

        if (m != null) {
            try {
                btSocket = (BluetoothSocket) m.invoke(btDev, 6);
            } catch (IllegalAccessException e) {
                btSocket = null;
                LogUtil.i(TAG, e.getMessage());
            } catch (InvocationTargetException e) {
                btSocket = null;
                LogUtil.i(TAG, e.getMessage());
            }
        }

        if (btSocket == null) {
            LogUtil.i(TAG, "bt socket create fail");
            MsgCode.setLastErrorCode(MsgCode.ER_BT_CONNECT_FAIL);
            return false;
        }

        boolean result;
        try {
            btSocket.connect();
            result = true;
        } catch (IOException e) {
            result = false;
            LogUtil.i(TAG, e.getMessage());
        }
        return result;
    }


    @Override
    public boolean sendData(byte[] data) {
        if (out == null) {
            LogUtil.i(TAG, "bluetooth not connect.");
            MsgCode.setLastErrorCode(MsgCode.ER_PRINTER_NOT_CONNECT);
            return false;
        }
        if (data == null) {
            LogUtil.i(TAG, "data to send is null.");
            MsgCode.setLastErrorCode(MsgCode.ER_DATA_NULL);
            return false;
        }
        try {
            out.write(data);
            out.flush();
        } catch (IOException e) {
            LogUtil.i(TAG, e.getMessage());
            LogUtil.i(TAG, "bt socket write fail");
            MsgCode.setLastErrorCode(MsgCode.ER_BT_SEND_FAIL);
            return false;
        }
        LogUtil.i(TAG, "send " + data.length + " bytes.");
        return true;
    }


    @Override
    public int receiveData(byte[] buffer, int timeout) {
        if (ins == null) {
            LogUtil.i(TAG, "bluetooth not connect.");
            MsgCode.setLastErrorCode(MsgCode.ER_PRINTER_NOT_CONNECT);
            return -1;
        }
        if (buffer == null) {
            LogUtil.i(TAG, "receive buffer is null.");
            MsgCode.setLastErrorCode(MsgCode.ER_RECEIVE_BUFFER_NULL);
            return -1;
        }
        int len = 0;
        try {
            if (ins.available() > 0) {
                len = ins.read(buffer);
            }
        } catch (IOException e) {
            LogUtil.i(TAG, e.getMessage());
            LogUtil.i(TAG, "bt socket read fail.");
            MsgCode.setLastErrorCode(MsgCode.ER_BT_RECEIVE);
            return -1;
        }
        LogUtil.i(TAG, "receive data " + len + " bytes.");
        return len;
    }

    @Override
    public void disconnect() {
        try {
            if (out != null) {
                out.flush();
                out.close();
                out = null;
            }
            if (ins != null) {
                ins.close();
                ins = null;
            }
            if (btSocket != null) {
                btSocket.close();
                btSocket = null;
            }
            LogUtil.i(TAG, "bt socket close");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getBtDevAddress() {
        return btDevAddress;
    }
}
