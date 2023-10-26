package com.jolimark.printer.callback;

public interface Callback {
    int SUCCESS = 1;

    int FAIL = 2;


    void onSuccess();

    void onFail(int code, String msg);
}
