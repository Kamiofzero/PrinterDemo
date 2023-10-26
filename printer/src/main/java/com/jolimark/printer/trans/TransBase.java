package com.jolimark.printer.trans;

public interface TransBase {
    boolean connect();

    boolean sendData(byte[] bytes);

    int receiveData(byte[] buffer, int timeout);

    void disconnect();
}
