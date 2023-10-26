package com.jolimark.printer.printer;

import static com.jolimark.printer.callback.Callback.FAIL;
import static com.jolimark.printer.callback.Callback.SUCCESS;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import com.jolimark.printer.bean.PrinterInfo;
import com.jolimark.printer.callback.Callback;
import com.jolimark.printer.common.MsgCode;
import com.jolimark.printer.direction.Comm;
import com.jolimark.printer.trans.TransType;
import com.jolimark.printer.util.ByteArrayUtil;
import com.jolimark.printer.util.ImageTransformer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BasePrinter {

    protected TransType transtype;

    public TransType getTranstype() {
        return transtype;
    }

    protected String deviceInfo;

    public String getDeviceInfo() {
        return deviceInfo;
    }

    private String name;


    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private ExecutorService executorService;

    protected Comm comm;
    private Handler mainHandler;

    public BasePrinter() {
        executorService = Executors.newSingleThreadExecutor();
        comm = getComm();
        comm.setPackageSize(initPackageSize());
        mainHandler = new Handler(Looper.getMainLooper());
    }

    protected abstract Comm getComm();

    protected abstract int initPackageSize();


    public void setPackageSize(int size) {
        comm.setPackageSize(size);
    }


    public void setSendDelay(long sendDelay) {
        comm.setSendDelay(sendDelay);
    }

    public void enableVerification(boolean enable) {
        comm.enableVerification(enable);
    }

    public PrinterInfo getPrinterInfo() {
        return comm.getPrinterInfo();
    }

    /**
     * 打印，发送字节数据
     * 异步线程中运行
     * 包括连接，发送，关闭连接
     *
     * @param bytes
     */
    public void print(byte[] bytes, Callback callback) {
        executorService.execute(() -> {
            printWithCallback(bytes, callback);
        });
    }

    /**
     * 打印文字
     * 异步线程中运行
     * 包括连接，发送，关闭连接
     *
     * @param str
     * @param callback
     */
    public void printText(String str, Callback callback) {
        executorService.execute(() -> {
            byte[] bytes = ByteArrayUtil.stringToByte(str);
            printWithCallback(bytes, callback);
        });
    }

    /**
     * 打印图片
     * 异步线程中运行
     * 包括连接，发送，关闭连接
     *
     * @param bitmap
     * @param callback
     */
    public void printImg(Bitmap bitmap, Callback callback) {
        executorService.execute(() -> {
            byte[] bytes = ImageTransformer.imageToData(bitmap);
            printWithCallback(bytes, callback);
        });
    }

    private void printWithCallback(byte[] bytes, Callback callback) {
        if (!comm.connect()) {
            callback(callback, FAIL);
            return;
        }
        if (!comm.sendData(bytes)) {
            callback(callback, FAIL);
        } else {
            callback(callback, SUCCESS);
        }
        comm.disconnect();
    }

    /**
     * 释放资源
     */
    public void release() {
        executorService.shutdown();
    }

    /**
     * 连接打印机
     *
     * @return
     */
    public boolean connect() {
        return comm.connect();
    }

    /**
     * 关闭连接
     */
    public void disconnect() {
        comm.disconnect();
    }

    public boolean print(byte[] bytes) {
        return comm.sendData(bytes);
    }

    public boolean printText(String str) {
        byte[] bytes = ByteArrayUtil.stringToByte(str);
        return comm.sendData(bytes);
    }

    public boolean printImg(Bitmap bitmap) {
        byte[] bytes = ImageTransformer.imageToData(bitmap);
        return comm.sendData(bytes);
    }

    protected void callback(final Callback callback, int key) {
        if (callback == null)
            return;
        switch (key) {
            case Callback.SUCCESS: {
                mainHandler.post(() -> callback.onSuccess());
                break;
            }
            case FAIL: {
                final int code = MsgCode.getLastErrorCode();
                final String msg = MsgCode.getLastErrorMsg();
                MsgCode.clear();
                mainHandler.post(() -> callback.onFail(code, msg));
                break;
            }
        }
    }

}
