package com.jolimark.printer.trans.ble;

import android.content.Context;

import com.jolimark.printer.util.LogUtil;


public class BleTrans {
    private static final String TAG = "BleTrans";

    private static BleTrans bleTrans;

    BleBase2 bleBase;
//    ExecutorService executorService;


    boolean isReleased;

    private BleTrans() {
        bleBase = new BleBase2();
//        executorService = Executors.newSingleThreadExecutor();
    }

    public static BleTrans getInstance() {
        synchronized (BleTrans.class) {
            if (bleTrans == null)
                bleTrans = new BleTrans();
        }
        return bleTrans;
    }


    public void connect(Context context, String address, Callback callback) {
        if (isReleased) {
            LogUtil.i(TAG, "bleTrans already release.");
            return;
        }
//        executorService.execute(new Runnable() {
//            @Override
//            public void run() {
////                try {
////                    Thread.sleep(3000);
////                } catch (InterruptedException e) {
////                    e.printStackTrace();
////                }
////                callback.onSuccess("");
//
//            }
//        });

        if (!bleBase.openTrans(context, address, new Callback() {
            @Override
            public void onSuccess(Object o) {
                if (callback != null) {
                    callback.onSuccess("");
                }
            }

            @Override
            public void onFail(String msg) {
                if (callback != null)
                    callback.onFail("connect fail.");
            }
        })) {
            if (callback != null)
                callback.onFail("address null.");
        }
    }

    public void disconnect() {
        if (isReleased) {
            LogUtil.i(TAG, "bleTrans already release.");
            return;
        }
        bleBase.closeTrans();
    }

    public void release() {
        LogUtil.i(TAG, "release");
//        executorService.shutdown();
        if (bleTrans.isConnected())
            bleBase.closeTrans();
        bleBase.release();
        bleTrans = null;
        isReleased = true;
    }

    public void sendData(byte[] data, String serviceUUid, String writeCharacteristicUUid, Callback callback) {
        if (isReleased) {
            LogUtil.i(TAG, "bleTrans already release.");
            return;
        }
//        executorService.execute(new Runnable() {
//            @Override
//            public void run() {
//
//
//            }
//        });

        boolean ret = bleBase.sendData(data, serviceUUid, writeCharacteristicUUid, new Callback() {
            @Override
            public void onSuccess(Object o) {
                if (callback != null) {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onFail(String msg) {
                if (callback != null)
                    callback.onFail(msg);
            }
        });
        if (!ret) {
            if (callback != null)
                callback.onFail("can not find service or characteristic.");
        }
    }

    public void receiveData(String serviceUUid, String readCharacteristicUUid, Callback<byte[]> callback) {
        if (isReleased) {
            LogUtil.i(TAG, "bleTrans already release.");
            return;
        }
//        executorService.execute(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        });
        boolean ret = bleBase.receiveData(serviceUUid, readCharacteristicUUid, new Callback<byte[]>() {

            @Override
            public void onSuccess(byte[] bytes) {
                if (callback != null) {
                    callback.onSuccess(bytes);
                }
            }

            @Override
            public void onFail(String msg) {
                if (callback != null)
                    callback.onFail(msg);
            }
        });
        if (!ret) {
            if (callback != null)
                callback.onFail("can not find service or characteristic.");
        }
    }


    public void notifyData(String serviceUUid, String notifyCharacteristicUUid, Callback<byte[]> callback) {
        if (isReleased) {
            LogUtil.i(TAG, "bleTrans already release.");
            return;
        }
//        executorService.execute(new Runnable() {
//            @Override
//            public void run() {
//
//
//            }
//        });

        boolean ret = bleBase.notifyData(serviceUUid, notifyCharacteristicUUid, new Callback<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                if (callback != null) {
                    callback.onSuccess(bytes);
                }
            }

            @Override
            public void onFail(String msg) {
                if (callback != null)
                    callback.onFail(msg);
            }
        });
        if (!ret) {
            if (callback != null)
                callback.onFail("can not find service or characteristic.");
        }
    }


    public void cancelNotifyData(String serviceUUid, String notifyCharacteristicUUid) {
        if (isReleased) {
            LogUtil.i(TAG, "bleTrans already release.");
            return;
        }
//        executorService.execute(new Runnable() {
//            @Override
//            public void run() {
//            }
//        });
        bleBase.cancelNotifyData(serviceUUid, notifyCharacteristicUUid);

    }


    public boolean sendData(byte[] data, String serviceUUid, String writeCharacteristicUUid) {
        if (isReleased) {
            LogUtil.i(TAG, "bleTrans already release.");
            return false;
        }
        return bleBase.sendDataSynchronized(data, serviceUUid, writeCharacteristicUUid);
    }

    public byte[] receiveData(String serviceUUid, String readCharacteristicUUid) {
        if (isReleased) {
            LogUtil.i(TAG, "bleTrans already release.");
            return null;
        }
        return bleBase.receiveDataSynchronized(serviceUUid, readCharacteristicUUid);
    }



    public boolean isConnected() {
        if (isReleased) {
            LogUtil.i(TAG, "bleTrans already release.");
            return false;
        }
        return bleBase.isConnected();
    }


    public void setListener(ConnectionListener connectionListener) {
        if (isReleased) {
            LogUtil.i(TAG, "bleTrans already release.");
            return;
        }
        bleBase.setListener(connectionListener);
    }
}
