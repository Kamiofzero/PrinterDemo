package com.jolimark.printer.printer;

import android.content.Context;
import android.hardware.usb.UsbDevice;

import com.jolimark.printer.direction.Comm;
import com.jolimark.printer.trans.TransType;
import com.jolimark.printer.trans.usb.UsbBase;

public class UsbPrinter extends BasePrinter {

    private UsbBase usbBase;

    @Override
    protected Comm getComm() {
        usbBase = new UsbBase();
        return new Comm(usbBase);
    }

    @Override
    public String getDeviceInfo() {
        return "[vid:" + usbBase.getDevice().getDeviceId() + ", pid:" + usbBase.getDevice().getProductId() + "]";
    }

    public UsbPrinter() {
        super();
        transtype = TransType.USB;
    }

    @Override
    protected int initPackageSize() {
        return 3840;
    }

    public void initContext(Context context) {
        usbBase.setContext(context);
    }

    public void setDevice(UsbDevice usbDevice) {
        usbBase.setUsbDevice(usbDevice);
    }

    public void setId(int vid, int pid) {
        usbBase.setId(vid, pid);
    }

    public int getVid() {
        return usbBase.getVid();

    }

    public int getPid() {
        return usbBase.getPid();
    }

    public UsbDevice getDevice() {
        return usbBase.getDevice();
    }
}
