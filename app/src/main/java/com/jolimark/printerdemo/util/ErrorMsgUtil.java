package com.jolimark.printerdemo.util;

import android.content.Context;

import com.jolimark.printerdemo.PrinterDemoApp;
import com.jolimark.printerdemo.R;
import com.jolimark.sdk.common.MsgCode;

public class ErrorMsgUtil {

    public static String getErrorMsg(int code) {
        Context context = PrinterDemoApp.context;
        String msg = "";
        switch (code) {
            case MsgCode.ER_PRINTER_NOT_CONNECT: {
                msg = context.getString(R.string.printer_not_connect);
                break;
            }
            case MsgCode.ER_WIFI_ADDRESS_NULL: {
                msg = context.getString(R.string.wifi_address_null);
                break;
            }
            case MsgCode.ER_WIFI_CONNECT_FAIL: {
                msg = context.getString(R.string.wifi_connect_fail);
                break;
            }
            case MsgCode.ER_WIFI_SEND_FAIL: {
                msg = context.getString(R.string.wifi_send_fail);
                break;
            }
            case MsgCode.ER_WIFI_READ_FAIL: {
                msg = context.getString(R.string.wifi_read_fail);
                break;
            }
            case MsgCode.ER_WIFI_UDP_SOCKET_CREATE_FAIL: {
                msg = context.getString(R.string.wifi_udp_socket_create_fail);
                break;
            }
            case MsgCode.ER_BT_ADDRESS_NULL: {
                msg = context.getString(R.string.bt_address_null);
                break;
            }
            case MsgCode.ER_BT_CONNECT_FAIL: {
                msg = context.getString(R.string.bt_connect_fail);
                break;
            }
            case MsgCode.ER_BT_SEND_FAIL: {
                msg = context.getString(R.string.bt_send_fail);
                break;
            }
            case MsgCode.ER_BT_RECEIVE: {
                msg = context.getString(R.string.bt_receive);
                break;
            }
            case MsgCode.ER_USB_DEVICE_null: {
                msg = context.getString(R.string.usb_device_null);
                break;
            }
            case MsgCode.ER_USB_DEVICE_CONNECT_FAIL: {
                msg = context.getString(R.string.usb_device_connect_fail);
                break;
            }
            case MsgCode.ER_USB_SEND_FAIL: {
                msg = context.getString(R.string.usb_send_fail);
                break;
            }
            case MsgCode.ER_USB_RECEIVE_FAIL: {
                msg = context.getString(R.string.usb_receive_fail);
                break;
            }
            case MsgCode.ER_PRINTER_RECEIVE_WRONG: {
                msg = context.getString(R.string.printer_receive_wrong);
                break;
            }
            case MsgCode.ER_PRINTER_VERIFY: {
                msg = context.getString(R.string.printer_verify);
                break;
            }
            case MsgCode.ER_PRINTER_RECEIVE_TIMEOUT: {
                msg = context.getString(R.string.printer_receive_timeout);
                break;
            }
            case MsgCode.ER_PRINTER_TYPE: {
                msg = context.getString(R.string.GET_PRINTER_TYPE_FAIL);
                break;
            }
            case MsgCode.CLIENT_CODE_INVALID: {
                msg = context.getString(R.string.CLIENT_CODE_INVALID);
                break;
            }
            case MsgCode.PDF_CONVERT_FAIL: {
                msg = context.getString(R.string.PDF_CONVERT_FAIL);
                break;
            }
            case MsgCode.IMG_CONVERT_FAIL: {
                msg = context.getString(R.string.IMG_CONVERT_FAIL);
                break;
            }
            case MsgCode.PDF_PATH_EMPTY: {
                msg = context.getString(R.string.PDF_PATH_EMPTY);
                break;
            }
            case MsgCode.PRINTER_TYPE_NULL: {
                msg = context.getString(R.string.PRINTER_TYPE_NULL);
                break;
            }

        }
        return msg;
    }

}
