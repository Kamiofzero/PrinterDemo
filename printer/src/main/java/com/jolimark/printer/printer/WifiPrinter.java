package com.jolimark.printer.printer;

import com.jolimark.printer.direction.Comm;
import com.jolimark.printer.trans.TransType;
import com.jolimark.printer.trans.wifi.WifiBase;

public class WifiPrinter extends BasePrinter {
    private WifiBase wifiBase;

    @Override
    protected Comm getComm() {
        wifiBase = new WifiBase();
        return new Comm(wifiBase);
    }

    @Override
    public String getDeviceInfo() {
        return "[ip:" + wifiBase.getIp() + ", port:" + wifiBase.getPort() + "]";
    }

    public WifiPrinter() {
        super();
        transtype = TransType.WIFI;
    }


    @Override
    protected int initPackageSize() {
        return 1024;
    }

    public void setIpAndPort(String ip, int port) {
        wifiBase.setIpAndPort(ip, port);
    }

    public String getIp() {
        return wifiBase.getIp();
    }

    public int getPort() {
        return wifiBase.getPort();
    }
}
