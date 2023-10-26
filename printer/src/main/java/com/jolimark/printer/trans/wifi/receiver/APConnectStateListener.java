package com.jolimark.printer.trans.wifi.receiver;

public interface APConnectStateListener {

    void onConnecting();

    void onConnected(String ssid);

    void onConnectFail();
}
