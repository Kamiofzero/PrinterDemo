package com.jolimark.printer.trans.wifi.receiver;

public interface WifiStateListener {

    void onEnabling();

    void onEnabled();

    void onDisabling();

    void onDisabled();

}
