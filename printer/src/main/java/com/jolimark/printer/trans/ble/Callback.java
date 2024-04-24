package com.jolimark.printer.trans.ble;

public interface Callback<T> {

    void onSuccess(T t);

    void onFail(String msg);

}
