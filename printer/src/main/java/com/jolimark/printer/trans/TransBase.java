package com.jolimark.printer.trans;

public abstract class TransBase {
    public abstract boolean connect();

    public abstract boolean sendData(byte[] bytes);

    public abstract int receiveData(byte[] buffer, int timeout);

    public abstract void disconnect();

    protected boolean isConnected;

    public boolean isConnected() {
        return isConnected;
    }
}
