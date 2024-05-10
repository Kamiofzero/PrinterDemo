package com.jolimark.printer.trans.wifi.search;

public interface SearchCallback {
 void onDeviceFound(DeviceInfo info);

 void onSearchEnd();

 void onSearchFail(String msg);
}