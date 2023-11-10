package com.jolimark.printer.printer;

import com.jolimark.printer.trans.TransBase;
import com.jolimark.printer.trans.TransType;
import com.jolimark.printer.trans.wifi.WifiBase;

public class WifiPrinter extends BasePrinter {
    private WifiBase wifiBase;

    @Override
    public String getDeviceInfo() {
        return "[ip:" + wifiBase.getIp() + ", port:" + wifiBase.getPort() + "]";
    }

    @Override
    protected TransBase getTransBase() {
        wifiBase = new WifiBase();
        return wifiBase;
    }

    public WifiPrinter(String ip, int port) {
        super();
        transtype = TransType.WIFI;
        wifiBase.setIpAndPort(ip, port);
        setName("wifi/" + ip + "/" + port);
    }


    @Override
    protected int initPackageSize() {
        return 1024;
    }

    @Override
    protected int initSendDelay() {
        return 50;
    }

//    public void setIpAndPort(String ip, int port) {
//        wifiBase.setIpAndPort(ip, port);
//        if (getName() == null || getName().isEmpty()) {
//            setName("wifi[ip:" + ip + ", port:" + port + "]");
//        }
//    }

    public String getIp() {
        return wifiBase.getIp();
    }

    public int getPort() {
        return wifiBase.getPort();
    }
}
