package com.jolimark.printer.trans.wifi.search;


import static com.jolimark.printer.common.MsgCode.ER_WIFI_UDP_SOCKET_CREATE_FAIL;

import android.text.TextUtils;

import com.jolimark.printer.common.MsgCode;
import com.jolimark.printer.util.LogUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;


public class SearchDevice implements Runnable {

    private final String TAG = getClass().getSimpleName();


    private SearchCallback callback;
    private static final int LOCAL_PORT = 5040; // 本地端口号
    private static final String BROADCAST_IP = "255.255.255.255";// 广播地址
    private static final int BROADCAST_PORT = 3040; // UDP广播的端口号
    private byte label[] = null;// 两字节辨识码
    private DatagramSocket udpSocket = null;
    private int timeout = 10000;// 最大搜索时间/毫秒


    /**
     * 有时候线程执行到循环前需要若干时间
     * 而可能存在开启线程后，马上调用中断的情况
     * 这种情况下，如果在循环前才把循环标识置为可循环，那么可能中断早就调用，也已经把循环标识
     * <p>
     * 中断标识应该在线程构造时就设置为可工作状态，而不是循环前
     * 因为有时候线程任务中循环前有一定量操作，如果此时外部已经调用中断，修改了标识，那这次修改就没被读取到，
     * 到循环前照样设置为可工作状态并循环
     */
    private boolean loop = true;
    private boolean flag_timeout;
    private Timer timer;

    private ReentrantLock lock = new ReentrantLock();

    public SearchDevice(SearchCallback callback) {
        this.callback = callback;
    }

    /**
     * 1.flag_cancel用于循环标识，设为true即可打断循环
     * 2.循环内用到sleep做延时，想立即响应用interrupt
     * 3.使用了timer作为超时监督，需要取消它
     * 4.不想回调响应，把callback置空（callback调用处做同步）
     */
    public void stopSearching() {
        lock.lock();
        try {
            loop = false;
            Thread.currentThread().interrupt();
            callback = null;
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            if (udpSocket != null)
                udpSocket.close();
            LogUtil.i(TAG, "cancel searching.");
        } finally {
            lock.unlock();
        }
    }


    @Override
    public void run() {
        try {
            udpSocket = new DatagramSocket(LOCAL_PORT);
            udpSocket.setSoTimeout(1000);
        } catch (SocketException e) {
            e.printStackTrace();
            LogUtil.i(TAG, e.getMessage());
            LogUtil.i(TAG, "udp socket create fail.");
            MsgCode.setLastErrorCode(ER_WIFI_UDP_SOCKET_CREATE_FAIL);
            callback(-1, MsgCode.getLastErrorMsg());
            return;
        }
        LogUtil.i(TAG, "udp socket create success.");
        // 搜索超时
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                flag_timeout = true;
            }
        }, timeout);

        LogUtil.i(TAG, "start searching...");
        while (!flag_timeout && loop) {
            sendBroadcast();
            if (!delay()) break;
            receiveBroadcast();
        }
        closeSocket();
        LogUtil.i(TAG, "searching finish.");
        callback(1, null);
    }


    private boolean delay() {
        lock.lock();
        try {
            if (loop)
                Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
            LogUtil.i(TAG, e.getMessage());
            return false;
        } finally {
            lock.unlock();
        }
        return true;
    }

    private void closeSocket() {
        lock.lock();
        try {
            if (udpSocket != null && !udpSocket.isClosed()) {
                try {
                    udpSocket.close();
                    LogUtil.i(TAG, "udp socket close.");
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtil.i(TAG, e.getMessage());
                }
                udpSocket = null;
            }
        } finally {
            lock.unlock();
        }
    }

    private void sendBroadcast() {
        if (udpSocket == null)
            return;
        // 发送广播
        label = new byte[2];// 两字节辨识码
        // UDP广播的数据
        byte[] SData = new byte[4];
        SData[0] = 0x0;
        SData[1] = 0x0;
        label[0] = SData[2] = (byte) (Math.abs(new Random().nextInt(255)) * System.currentTimeMillis() % 256);
        label[1] = SData[3] = (byte) (Math.abs(new Random().nextInt(255)) * System.currentTimeMillis() % 256);
        // 创建socket发送广播
        DatagramPacket dataPacket = null;

        try {
            dataPacket = new DatagramPacket(SData, SData.length, InetAddress.getByName(BROADCAST_IP),
                    BROADCAST_PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            udpSocket.send(dataPacket);
//            LogUtil.i(TAG, "send udp broadcast [" + ByteArrayUtil.toHex(SData, SData.length) + "]");
        } catch (IOException e) {
            LogUtil.i(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 在不停地开始中断开始搜索过程里面，中断的时机可能发生在发送后接收前，因为这里面有一点延时，
     * 这样就会多次发送，而不接收，在socket的缓存中会有数据，
     * 虽然下次是重建socket对象，但因为ip、port一样，短时间之内底层并没回收对应资源，而是由把资源与新的socket关联上，
     * 那么新的socket就会读到之前的数据，
     * 一次广播发送后，本来就会收到两次回信，两次回信都必须消耗掉，否则影响下次接收（校验码对不上，影响判断）
     * 而现在更是有大量的错位回信，需要消耗的次数可以非常多，因此应该死循环消耗
     */
    private void receiveBroadcast() {
//        int tryCount = 5;
        DeviceInfo deviceInfo = null;
        DeviceInfo temp;
        do {
            temp = receivePacket();
            //接收失败的情况，可能是连接异常了，或者已经没有东西可接收，退出循环
            if (temp == null) break;
                //处理错位的情况，或者错误的信息，消耗完
            else if (TextUtils.isEmpty(temp.ip)) {
            }
            //正确获取的情况，可能重复，只取最后一次
            else
                deviceInfo = temp;
        } while (true);
        if (deviceInfo != null) {
            LogUtil.i(TAG, "find printer " + deviceInfo);
            callback(0, deviceInfo);
        }
    }


    private DeviceInfo receivePacket() {
        if (udpSocket == null)
            return null;

        byte[] data = new byte[128];
        DatagramPacket recDataPacket = new DatagramPacket(data, data.length);
        data[2] = 0;
        data[3] = 0;
        try {
            udpSocket.receive(recDataPacket);
            int length = recDataPacket.getLength();
//            LogUtil.i(TAG, "receive udp package " + length + " bytes -> [" + ByteArrayUtil.toHex(data, data.length) + "]");
            if (data[2] == label[0] && data[3] == label[1]) { // 辨识码校对
                // 获取Mac地址
                StringBuffer macString = new StringBuffer();
                for (int i = 14; i < 20; i++) {
                    macString.append(Integer.toHexString((int) (data[i] & 0xFF)));
                    if (i != 19) {
                        macString.append(":");
                    }
                }
                // 获取ip
                StringBuffer ipString = new StringBuffer();
                for (int i = 20; i < 24; i++) {
                    ipString.append((int) (data[i] & 0xFF));
                    if (23 != i) {
                        ipString.append(".");
                    }
                }
                // 获取型号字节数组长度
                int i = 0;
                while (data[i + 32] != 0) {
                    i++;
                }
                DeviceInfo deviceInfo = new DeviceInfo();
                deviceInfo.ip = ipString.toString();
                deviceInfo.port = "9100";
                deviceInfo.type = new String(data, 32, i);
                deviceInfo.mac = macString.toString();

//                callback(0, deviceInfo);
                return deviceInfo;
            }
        } catch (IOException e) {
            LogUtil.i(TAG, e.getMessage());
            e.printStackTrace();
            return null;
        }
        return new DeviceInfo();
    }

    private void callback(int what, Object obj) {
        lock.lock();
        try {
            if (callback != null)
                switch (what) {
                    case -1: {
                        callback.onSearchFail((String) obj);
                        break;
                    }
                    case 0: {
                        callback.onDeviceFound((DeviceInfo) obj);
                        break;
                    }
                    case 1: {
                        callback.onSearchEnd();
                        break;
                    }
                }
        } finally {
            lock.unlock();
        }
    }
}
