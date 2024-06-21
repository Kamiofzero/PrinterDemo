package com.jolimark.printer.trans.bluetooth.auto;

import static java.lang.Thread.sleep;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;

import com.jolimark.printer.callback.Callback;
import com.jolimark.printer.callback.Callback2;
import com.jolimark.printer.common.MsgCode;
import com.jolimark.printer.protocol.CommBase;
import com.jolimark.printer.util.ByteArrayUtil;
import com.jolimark.printer.util.ImageTransformer;
import com.jolimark.printer.util.LogUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

public class AutoConnect2 {
    private final String TAG = "AutoConnect2";

    private Context context;

    private CommBase base;

    private ExecutorService service;
    private Callback2 callback;

    private Handler handler;
    private AutoCore core;
    private String info;

    public AutoConnect2(Context context, String info, CommBase base, ExecutorService service, Handler handler, Callback2 callback) {
        this.context = context;
        this.info = info;
        this.base = base;
        this.service = service;
        this.handler = handler;
        this.callback = callback;
    }

    public void destroy() {
        core.stop();
        service.shutdown();
    }


    public void autoConnect() {
        core = new AutoCore();
        service.execute(core);
    }


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

        public AutoCore() {
            lock = this;
            loop = true;
        }

        private LinkedBlockingQueue<PrintTask> printTasks = new LinkedBlockingQueue<>();

        public void enqueuePrintTask(PrintTask task) {

            synchronized (lock) {
                if (!isConnect) {
                    Callback cb = task.callback;
                    int code = MsgCode.ER_PRINTER_NOT_CONNECT;
                    MsgCode.setLastErrorCode(code);
                    String msg = MsgCode.getLastErrorMsg();
                    MsgCode.clear();
                    if (cb != null) {
                        cb.onFail(code, msg);
                    }
                    return;
                }
                try {
                    printTasks.put(task);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                lock.notify();
            }
        }

        @Override
        public void run() {
            LogUtil.i(TAG, "core [" + info + "] run");
            while (loop) {
                if (!isConnect)
                    doConnectJob();
                else {
                    PrintTask task;
                    synchronized (lock) {
                        task = printTasks.poll();
                    }
                    if (task == null) {
                        doHeartBeatJob();
                        synchronized (lock) {
                            task = printTasks.poll();
                            if (task == null)
                                try {
                                    wait(2000);
                                } catch (InterruptedException e) {
                                    LogUtil.i(TAG, e.getMessage());
                                }
                        }
                        continue;
                    }
                    doPrintJob(task);
                }
            }
            LogUtil.i(TAG, "core [" + info + "] stop");
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
                            handler.post(() -> cb.onFail(code, msg));
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

        private void doHeartBeatJob() {
            LogUtil.i(TAG, "device [" + info + "] heartbeat test");
//            byte[] heartBit = new byte[]{0x01};
//            boolean ret = base.sendData(heartBit);
            boolean ret =   base.isConnected();
//            boolean ret = (base.receiveData(new byte[1], 100) != -1);
            if (!ret) {
                LogUtil.i(TAG, "device [" + info + "] heartbeat test fail, disconnected");
                handler.post(() -> callback.onDisconnect(info));
                isConnect = false;
            }
        }

        public void stop() {
            synchronized (lock) {
                loop = false;
            }
            base.release();
        }
    }

}
