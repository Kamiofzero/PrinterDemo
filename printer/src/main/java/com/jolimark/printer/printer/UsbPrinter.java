package com.jolimark.printer.printer;

import android.content.Context;
import android.hardware.usb.UsbDevice;

import com.jolimark.printer.direction.Comm;
import com.jolimark.printer.trans.usb.UsbBase;

public class UsbPrinter extends BasePrinter {

    private UsbBase usbBase;

    @Override
    protected Comm getComm() {
        usbBase = new UsbBase();
        return new Comm(usbBase);
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

}
