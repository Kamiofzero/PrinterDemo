package com.jolimark.printer.trans.usb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.jolimark.printer.util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class UsbUtil {
    private final String TAG = "UsbUtil";

    private final int VendorId = 7072;
    private final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";


    private UsbPermissionRequestListener usbPermissionRequestListener;

    private UsbDevice requestUsbDevice;


    public List<UsbDevice> getUsbDevices(Context context) {
        ArrayList<UsbDevice> usbDeviceList = new ArrayList<>();
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        LogUtil.i(TAG, "usb devices list:");
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            LogUtil.i(TAG, "device  [vid " + device.getVendorId() + "]");
            if (device.getVendorId() == VendorId) {
                usbDeviceList.add(device);
                break;
            }
//            UsbInterface usbInterface = device.getInterface(0);
//            if (usbInterface != null && usbInterface.getInterfaceClass() == 7) {
//                usbDevice = device;
//                   LogUtil.i(TAG, "found usbDevice.");
//                break;
//            }
//        }
        }
        return usbDeviceList;
    }

    public UsbDevice getUsbDevice(Context context, int pid, int vid) {
        ArrayList<UsbDevice> usbDeviceList = new ArrayList<>();
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        LogUtil.i(TAG, "usb devices list:");
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            LogUtil.i(TAG, "device  [vid " + device.getVendorId() + "]");
            if (device.getVendorId() == vid &&
                    device.getProductId() == pid) {
                return device;
            }
        }
        return null;
    }


    public boolean checkUsbPermission(Context context, UsbDevice usbDevice) {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        if (!usbManager.hasPermission(usbDevice)) {
            return false;
        }
        return true;
    }

    public void requestUsbPermission(Context context, final UsbDevice usbDevice, final UsbPermissionRequestListener listener) {
        final UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        if (!usbManager.hasPermission(usbDevice)) {
            requestUsbDevice = usbDevice;
            usbPermissionRequestListener = listener;
            LogUtil.i(TAG, "has no permission of this device，request permission.");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
            usbManager.requestPermission(usbDevice, pendingIntent);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            context.registerReceiver(mUsbReceiver, filter);
        } else {
            LogUtil.i(TAG, "already has permission of this device.");
            if (listener != null) {
                listener.onRequestGranted();
            }
        }
    }


    public void requestUsbPermissionForCustomSystem(Context context, final UsbDevice usbDevice, final UsbPermissionRequestListener listener) {
        final UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        if (!usbManager.hasPermission(usbDevice)) {
            LogUtil.i(TAG, "has no permission of this device，request permission.");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
            usbManager.requestPermission(usbDevice, pendingIntent);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int count = 30;
                    while (true) {
                        if (usbManager.hasPermission(usbDevice)) {
                            if (listener != null) {
                                listener.onRequestGranted();
                            }

                            LogUtil.i(TAG, "permission granted for device " + usbDevice);
                            return;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        count--;
                        if (count <= 0) {
                            if (listener != null) {
                                listener.onRequestDenied();
                            }

                            LogUtil.i(TAG, "permission denied for device " + usbDevice);
                            return;
                        }
                    }
                }
            }).start();


        } else {
            LogUtil.i(TAG, "already has permission of this device.");
            if (listener != null) {
                listener.onRequestGranted();
            }
        }
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            context.unregisterReceiver(this);
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    LogUtil.i(TAG, "permission access for device " + device);
                    if (device != null && requestUsbDevice != null && device.getDeviceId() == requestUsbDevice.getDeviceId()) {
                        if (usbPermissionRequestListener != null)
                            usbPermissionRequestListener.onRequestGranted();
                    }
                } else {
                    if (device != null && requestUsbDevice != null && device.getDeviceId() == requestUsbDevice.getDeviceId()) {
                        LogUtil.i(TAG, "permission denied for device " + device);
                        if (usbPermissionRequestListener != null) {
                            usbPermissionRequestListener.onRequestDenied();
                        }
                    }
                }
            }
        }
    };


    public interface UsbPermissionRequestListener {

        void onRequestGranted();

        void onRequestDenied();

    }


}
