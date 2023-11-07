package com.jolimark.printer.printer;

import static com.jolimark.printer.callback.Callback.FAIL;
import static com.jolimark.printer.callback.Callback.SUCCESS;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import com.jolimark.printer.bean.PrinterConfig;
import com.jolimark.printer.bean.PrinterInfo;
import com.jolimark.printer.callback.Callback;
import com.jolimark.printer.common.MsgCode;
import com.jolimark.printer.protocol.Comm;
import com.jolimark.printer.protocol.CommBase;
import com.jolimark.printer.protocol.anti_loss.Comm2;
import com.jolimark.printer.trans.TransBase;
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

    public abstract String getDeviceInfo();

    private String name;


    protected void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private ExecutorService executorService;
    private Comm comm;
    private Comm2 comm2;
    private CommBase commBase;

    private Handler mainHandler;

    private PrinterConfig config;
    private boolean antiLoss;

    public BasePrinter() {
        executorService = Executors.newSingleThreadExecutor();
        config = new PrinterConfig();
        config.packageSize = initPackageSize();
        config.sendDelay = initSendDelay();
        comm = new Comm(getTransBase());
        comm.setConfig(config);
        commBase = comm;
        mainHandler = new Handler(Looper.getMainLooper());
    }


    protected abstract TransBase getTransBase();


    protected abstract int initPackageSize();

    protected abstract int initSendDelay();


    public void setPackageSize(int size) {
        config.packageSize = size;
        commBase.setConfig(config);
    }


    public void setSendDelay(int sendDelay) {
        config.sendDelay = sendDelay;
        commBase.setConfig(config);
    }

    public void enableVerification(boolean enable) {
        config.enableVerification = enable;
        commBase.setConfig(config);
    }

    public void enableAntiLossMode(boolean enable) {
        if (antiLoss != enable) {
            if (commBase.isConnected())
                commBase.disconnect();

            antiLoss = enable;
            if (antiLoss) {
                if (comm2 == null) comm2 = new Comm2(getTransBase());
                commBase = comm2;
            } else
                commBase = comm;

            commBase.setConfig(config);
        }
    }

    public boolean isAntiMode() {
        return antiLoss;
    }

    public PrinterInfo getPrinterInfo() {
        return commBase.getPrinterInfo();
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
        if (!commBase.connect()) {
            callback(callback, FAIL);
            return;
        }
        if (!commBase.sendData(bytes)) {
            callback(callback, FAIL);
        } else {
            callback(callback, SUCCESS);
        }
        commBase.disconnect();
    }

    /**
     * 续打，需要设置防丢单模式后才能正常调用
     *
     * @param callback
     */
    public void resumePrint(Callback callback) {
        if (!antiLoss) {
            MsgCode.setLastErrorCode(MsgCode.ER_BIDIRECTIONAL_NOT_SWITCH_PROTOCOL);
            callback(callback, FAIL);
            return;
        }
        executorService.execute(() -> {
            if (!commBase.connect()) {
                callback(callback, FAIL);
                return;
            }
            if (!((Comm2) commBase).resumeSend()) {
                callback(callback, FAIL);
            } else {
                callback(callback, SUCCESS);
            }
            commBase.disconnect();
        });
    }


    /**
     * 重打，需要设置防丢单模式后才能正常调用
     *
     * @param callback
     */
    public void rePrint(Callback callback) {
        if (!antiLoss) {
            MsgCode.setLastErrorCode(MsgCode.ER_BIDIRECTIONAL_NOT_SWITCH_PROTOCOL);
            callback(callback, FAIL);
            return;
        }
        executorService.execute(() -> {
            if (!commBase.connect()) {
                callback(callback, FAIL);
                return;
            }
            if (!((Comm2) commBase).resend()) {
                callback(callback, FAIL);
            } else {
                callback(callback, SUCCESS);
            }
            commBase.disconnect();
        });
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
        return commBase.connect();
    }

    /**
     * 关闭连接
     */
    public void disconnect() {
        commBase.disconnect();
    }

    public boolean print(byte[] bytes) {
        return commBase.sendData(bytes);
    }

    public boolean printText(String str) {
        byte[] bytes = ByteArrayUtil.stringToByte(str);
        return commBase.sendData(bytes);
    }

    public boolean printImg(Bitmap bitmap) {
        byte[] bytes = ImageTransformer.imageToData(bitmap);
        return commBase.sendData(bytes);
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
