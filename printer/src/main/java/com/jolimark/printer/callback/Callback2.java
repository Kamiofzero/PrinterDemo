package com.jolimark.printer.callback;

public interface Callback2<T> {
    void onConnecting(T t);

    void onConnected(T t);
    void onConnectFail(T t);

    void onDisconnect(T t);


}
