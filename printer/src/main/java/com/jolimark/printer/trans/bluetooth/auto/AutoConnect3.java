package com.jolimark.printer.trans.bluetooth.auto;

import static java.lang.Thread.sleep;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;

import com.jolimark.printer.callback.Callback;
import com.jolimark.printer.callback.Callback2;
import com.jolimark.printer.common.MsgCode;
import com.jolimark.printer.protocol.CommBase;
import com.jolimark.printer.trans.bluetooth.BluetoothUtil;
import com.jolimark.printer.trans.bluetooth.listener.BTDeviceAclListener;
import com.jolimark.printer.trans.bluetooth.listener.BluetoothStateListener;
import com.jolimark.printer.util.ByteArrayUtil;
import com.jolimark.printer.util.ImageTransformer;
import com.jolimark.printer.util.LogUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

public class AutoConnect3 {
    private final String TAG = "AutoConnect3";

    private Context context;

    private CommBase base;

    private ExecutorService service;
    private Callback2 callback;

    private Handler handler;
    private AutoCore core;
    private String info;
    private BluetoothUtil util;

    public AutoConnect3(Context context, String info, CommBase base, ExecutorService service, Handler handler, Callback2 callback) {
        this.context = context;
        this.info = info;
        this.base = base;
        this.service = service;
        this.handler = handler;
        this.callback = callback;
        util = new BluetoothUtil();
    }

    public void destroy() {
        util.unregisterBluetoothReceiver(context);
        core.stop();
        service.shutdown();
    }


    public void autoConnect() {
        core = new AutoCore();
        service.execute(core);
        util.registerBluetoothReceiver(context);
        util.setBTDeviceAclListener(btDeviceAclListener);
        util.setBluetoothStateListener(bluetoothStateListener);
        if (util.isBluetoothEnabled())
            core.signalBtEnable();
    }

    private BluetoothStateListener bluetoothStateListener = new BluetoothStateListener() {
        @Override
        public void onBluetoothEnabled() {
            core.signalBtEnable();
        }

        @Override
        public void onBluetoothDisabled() {
            core.signalBtDisable();
        }
    };


    private BTDeviceAclListener btDeviceAclListener = new BTDeviceAclListener() {
        @Override
        public void onAclConnected(BluetoothDevice device) {

        }

        @Override
        public void onAclConnectRequest(BluetoothDevice device) {

        }

        @Override
        public void onAclDisConnected(BluetoothDevice device) {
            if (device.getAddress().equals(info)) {
                core.signalDisconnect();
            }
        }
    };


    public void print(byte[] bytes, Callback callback) {
        PrintTask task = new PrintTask(bytes, callback);
        core.enqueuePrintTask(task);
    }


    public void printText(String text, Callback callback) {
        byte[] bytes = ByteArrayUtil.stringToByte(text);
        PrintTask task = new PrintTask(bytes, callback);
        core.enqueuePrintTask(task);
    }

    public void printImg(Bitmap bitmap, Callback callback) {
        byte[] bytes = ImageTransformer.imageToData(bitmap);
        PrintTask task = new PrintTask(bytes, callback);
        core.enqueuePrintTask(task);
    }


    class PrintTask {
        byte[] data;
        Callback callback;

        public PrintTask(byte[] data, Callback callback) {
            this.data = data;
            this.callback = callback;
        }
    }


    class AutoCore implements Runnable {

        private Object lock;
        private boolean loop;
        private boolean isConnect;
        private boolean isEnable;

        private boolean isWait;

        public AutoCore() {
            lock = this;
            loop = true;
        }

        private LinkedBlockingQueue<PrintTask> printTasks = new LinkedBlockingQueue<>();

        public void enqueuePrintTask(PrintTask task) {

            synchronized (lock) {
                if (!isEnable || !isConnect) {
                    Callback cb = task.callback;
                    int code = MsgCode.ER_PRINTER_NOT_CONNECT;
                    MsgCode.setLastErrorCode(code);
                    String msg = MsgCode.getLastErrorMsg();
                    MsgCode.clear();
                    if (cb != null) {
                        handler.post(() -> cb.onFail(code, msg));
                    }
                    return;
                }
                try {
                    printTasks.put(task);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (isWait)
                    lock.notify();
            }
        }

        public void signalDisconnect() {
            synchronized (lock) {
                if (isEnable) {
                    isConnect = false;
                    LogUtil.i(TAG, "device [" + info + "] disconnected");
                    if (isWait)
                        lock.notify();
                    if (loop)
                        handler.post(() -> callback.onDisconnect(info));
                }
            }
        }

        public void signalBtEnable() {
            LogUtil.i(TAG, "device [" + info + "] enabled");
            synchronized (lock) {
                isEnable = true;
                if (isWait)
                    lock.notify();
            }
        }

        public void signalBtDisable() {
            isConnect = false;
            synchronized (lock) {
                isEnable = false;
                LogUtil.i(TAG, "device [" + info + "] disabled");
//                if (isWait)
//                    lock.notify();
                if (loop)
                    handler.post(() -> callback.onDisconnect(info));
            }
        }


        @Override
        public void run() {
            LogUtil.i(TAG, "core [" + info + "] run");
            while (loop) {
                synchronized (lock) {
                    if (!isEnable) {
                        waitFor();
                        continue;
                    }
                }

                if (!isConnect) {
                    doConnectJob();
                    continue;
                }

                PrintTask task;
                synchronized (lock) {
                    task = printTasks.poll();
                    if (task == null) {
                        waitFor();
                        continue;
                    }
                }
                LogUtil.i(TAG, "core [" + info + "] receive job");
                doPrintJob(task);
            }
            LogUtil.i(TAG, "core [" + info + "] stop");
        }

        private void waitFor() {
            try {
                LogUtil.i(TAG, "core [" + info + "] wait");
                isWait = true;
                wait();
            } catch (InterruptedException e) {
                LogUtil.i(TAG, e.getMessage());
            }
            isWait = false;
            LogUtil.i(TAG, "core [" + info + "] wait up");
        }

        private void doPrintJob(PrintTask task) {
            LogUtil.i(TAG, "device [" + info + "] printing");
            byte[] bytes = task.data;
            boolean ret = base.sendData(bytes);
            Callback cb = task.callback;
            if (cb != null)
                synchronized (lock) {
                    if (loop)
                        if (ret)
                            handler.post(() -> cb.onSuccess());
                        else {
                            final int code = MsgCode.getLastErrorCode();
                            final String msg = MsgCode.getLastErrorMsg();
                            MsgCode.clear();
                            handler.post(() -> {
                                cb.onFail(code, msg);
                                callback.onDisconnect(info);
                            });
                        }
                }

        }

        private void doConnectJob() {
            LogUtil.i(TAG, "device [" + info + "] connecting");
            synchronized (lock) {
                if (loop)
                    handler.post(() -> callback.onConnecting(info));
            }
            boolean ret = base.connect();
            if (ret) {
                LogUtil.i(TAG, "device [" + info + "] connected");
                synchronized (lock) {
                    if (loop)
                        handler.post(() -> callback.onConnected(info));
                }
                isConnect = true;
            } else {
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    LogUtil.i(TAG, e.getMessage());
                }
            }
        }

        public void stop() {
            base.release();
            synchronized (lock) {
                loop = false;
                lock.notify();
            }
        }
    }

}
