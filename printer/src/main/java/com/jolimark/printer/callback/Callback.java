package com.jolimark.printer.callback;

import androidx.annotation.NonNull;

public interface Callback {
    int SUCCESS = 1;

    int FAIL = 2;


    void onSuccess();

    void onFail(int code, @NonNull String msg);
}
