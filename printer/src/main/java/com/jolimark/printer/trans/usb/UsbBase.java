package com.jolimark.printer.trans.usb;

import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import com.jolimark.printer.common.MsgCode;
import com.jolimark.printer.trans.TransBase;
import com.jolimark.printer.util.LogUtil;

/**
 * Created by ljbin on 2018/1/31.
 */

public class UsbBase implements TransBase {

    private final String TAG = "UsbBase";

    private UsbDeviceConnection mConnection;
    private UsbInterface mUsbInterface;
    private UsbEndpoint mEndpointOut, mEndpointIn;

    private int sendTimeout = -1;

    private Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    private UsbDevice usbDevice;

    public void setUsbDevice(UsbDevice usbDevice) {
        this.usbDevice = usbDevice;
    }

    @Override
    public boolean connect() {
        if (usbDevice == null) {
            LogUtil.i(TAG, "usbDevice not set.");
            MsgCode.setLastErrorCode(MsgCode.ER_USB_DEVICE_null);
            return false;
        }
        if (context == null) {
            return false;
        }
        if (mConnection != null) {
            disconnect();
        }
        LogUtil.i(TAG, "device: [vid: " + usbDevice.getVendorId() + " , pid: " + usbDevice.getProductId() + " , did: " + usbDevice.getDeviceId() + "]");

        LogUtil.i(TAG, "get usb printer interface.");
        LogUtil.i(TAG, "interface count: " + usbDevice.getInterfaceCount());
        //InterfaceClass代表usb设备类型，7为打印机
        for (int i = 0; i < usbDevice.getInterfaceCount(); i++) {
            UsbInterface usbInterface = usbDevice.getInterface(i);
            LogUtil.i(TAG, "  interface : [num: " + i + " , class: " + usbInterface.getInterfaceClass() + "]");
            if (usbInterface != null && usbInterface.getInterfaceClass() == 7) {
                mUsbInterface = usbInterface;
                LogUtil.i(TAG, "found usb printer interface.");
                break;
            }
        }
        if (mUsbInterface == null) {
            LogUtil.i(TAG, "get printer interface fail.");
            MsgCode.setLastErrorCode(MsgCode.ER_USB_DEVICE_CONNECT_FAIL);
            return false;
        }
        LogUtil.i(TAG, "open device connection.");
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        mConnection = usbManager.openDevice(usbDevice);
        if (mConnection == null) {
            LogUtil.i(TAG, "open device connection fail.");
            MsgCode.setLastErrorCode(MsgCode.ER_USB_DEVICE_CONNECT_FAIL);
            return false;
        }

        //一般一个设备对应一个接口，而一个接口有多个端口，其中包括控制端口0和其他传输端口，端口0可输入可输出，其他端口可以是输入或者输出。
//        LogUtil.i(TAG, "get device interface.");
//        mUsbInterface = usbDevice.getInterface(0);
//        if (mUsbInterface == null) {
//            LogUtil.i(TAG, "get device interface fail.");
//            MsgCode.setLastErrorCode(MsgCode.ER_USE_DEVICE_CONNECT_FAIL);
//            return false;
//        }

        LogUtil.i(TAG, "claim device interface.");
        boolean ret = mConnection.claimInterface(mUsbInterface, true);
        if (!ret) {
            LogUtil.i(TAG, "claim device interface fail.");
            MsgCode.setLastErrorCode(MsgCode.ER_USB_DEVICE_CONNECT_FAIL);
            return false;
        }

        LogUtil.i(TAG, "get device endpoints.");
        for (int i = 0; i < mUsbInterface.getEndpointCount(); i++) {
            if (mUsbInterface.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_OUT) {
                mEndpointOut = mUsbInterface.getEndpoint(i);
            } else if (mUsbInterface.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN) {
                mEndpointIn = mUsbInterface.getEndpoint(i);
            }
        }
        if (mEndpointOut == null) {
            LogUtil.i(TAG, "get device out_endpoint fail.");
            MsgCode.setLastErrorCode(MsgCode.ER_USB_DEVICE_CONNECT_FAIL);
            return false;
        }
        if (mEndpointIn == null) {
            LogUtil.i(TAG, "get device in_endpoint fail.");
            MsgCode.setLastErrorCode(MsgCode.ER_USB_DEVICE_CONNECT_FAIL);
            return false;
        }

        return true;
    }

    /**
     * 打开usb连接
     *
     * @return
     */
    public boolean open(UsbManager usbManager, UsbDevice usbDevice) {

    }


    @Override
    public boolean sendData(byte[] bytes) {
        if (mEndpointOut == null || mConnection == null) {
            LogUtil.i(TAG, "usb not connect.");
            MsgCode.setLastErrorCode(MsgCode.ER_PRINTER_NOT_CONNECT);
            return false;
        }
        if (bytes == null) {
            LogUtil.i(TAG, "data to send is null.");
            MsgCode.setLastErrorCode(MsgCode.ER_DATA_NULL);
            return false;
        }

        int len = 0;
        try {
            len = mConnection.bulkTransfer(mEndpointOut, bytes, bytes.length, sendTimeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (len != bytes.length) {
            LogUtil.i(TAG, "usb send data fail.");
            MsgCode.setLastErrorCode(MsgCode.ER_USB_SEND_FAIL);
            return false;
        }
        LogUtil.i(TAG, "send data success.");
        return true;
    }


    /**
     * 接收数据
     *
     * @param buffer
     * @param timeout
     * @return
     */
    @Override
    public int receiveData(byte[] buffer, int timeout) {
//        LogUtil.i(TAG, "receive data.");
        if (mEndpointIn == null || mConnection == null) {
            LogUtil.i(TAG, "usb not connect.");
            MsgCode.setLastErrorCode(MsgCode.ER_PRINTER_NOT_CONNECT);
            return -1;
        }
        if (buffer == null) {
            LogUtil.i(TAG, "receive buffer is null.");
            MsgCode.setLastErrorCode(MsgCode.ER_RECEIVE_BUFFER_NULL);
            return -1;
        }
        int ret = 0;
        try {
            ret = mConnection.bulkTransfer(mEndpointIn, buffer, buffer.length, timeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ret == -1) {
            LogUtil.i(TAG, "usb receive data fail.");
            MsgCode.setLastErrorCode(MsgCode.ER_USB_RECEIVE_FAIL);
        } else {
            LogUtil.i(TAG, "receive data " + ret + " bytes.");
        }
        return ret;
    }

    @Override
    public void disconnect() {
        try {
            if (mConnection != null) {
                mConnection.releaseInterface(mUsbInterface);
                mConnection.close();
                LogUtil.i(TAG, "close device connection.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mConnection = null;
            mEndpointOut = null;
            mEndpointIn = null;
        }
    }


    /**
     * 获取打印机状态码
     */
    public int getPrinterStatus() {
        if (mConnection == null) {
            LogUtil.i(TAG, "usb not connect.");
            MsgCode.setLastErrorCode(MsgCode.ER_PRINTER_NOT_CONNECT);
            return -1;
        }
        byte[] buffer = new byte[1];
        int ret = 0;
        try {
            ret = mConnection.controlTransfer(0xA1, 0x01, 0x00, 0x00, buffer, buffer.length, 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ret == -1) {
            return -1;
        }
//        LogUtil.i( "printer status code -> " + "buffer->" + new Integer(buffer[0]));
        return new Integer(buffer[0]);
    }


    public void setSendTimeout(int sendTimeout) {
        this.sendTimeout = sendTimeout;
    }
}
