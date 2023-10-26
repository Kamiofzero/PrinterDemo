package com.jolimark.printer.printer;

import com.jolimark.printer.direction.Comm;
import com.jolimark.printer.trans.wifi.WifiBase;

public class WifiPrinter extends BasePrinter {
    private WifiBase wifiBase;

    @Override
    protected Comm getComm() {
        wifiBase = new WifiBase();
        return new Comm(wifiBase);
    }

    @Override
    protected int initPackageSize() {
        return 1024;
    }

    public void setAddress(String ip, int port) {
        wifiBase.setAddress(ip, port);
    }
}
