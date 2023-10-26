package com.jolimark.printer.trans.wifi.search;

public interface SearchDeviceCallback {
    void deviceFound(DeviceInfo deviceInfo);

    void searchFinish();
}
