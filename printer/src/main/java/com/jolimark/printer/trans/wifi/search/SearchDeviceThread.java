package com.jolimark.printer.trans.wifi.search;

import static com.jolimark.printer.common.MsgCode.ER_WIFI_UDP_SOCKET_CREATE_FAIL;

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

public class SearchDeviceThread extends Thread {

    private final String TAG = "SearchDeviceThread";


    private Callback callback;
    private static final int LOCAL_PORT = 5040; // 本地端口号
    private static final String BROADCAST_IP = "255.255.255.255";// 广播地址
    private static final int BROADCAST_PORT = 3040; // UDP广播的端口号
    private byte label[] = null;// 两字节辨识码
    private DatagramSocket udpSocket = null;
    private int timeout = 10000;// 最大搜索时间/毫秒


    private boolean flag_cancel;
    private boolean flag_timeout;
    private Timer timer;

    private ReentrantLock lock_cancel = new ReentrantLock();

    public SearchDeviceThread(Callback callback) {
        this.callback = callback;
    }

    public void stopSearching() {
        lock_cancel.lock();
        flag_cancel = true;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        LogUtil.i(TAG, "cancel searching.");
        lock_cancel.unlock();
    }


    @Override
    public void run() {
        try {
            udpSocket = new DatagramSocket(LOCAL_PORT);
            udpSocket.setSoTimeout(5000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if (udpSocket == null) {
            LogUtil.i(TAG, "udp socket create fail.");
            MsgCode.setLastErrorCode(ER_WIFI_UDP_SOCKET_CREATE_FAIL);
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

        flag_cancel = false;
        LogUtil.i(TAG, "start searching...");
        while (!flag_timeout && !flag_cancel) {
            sendBroadcast();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            receiveBroadcast();
        }
        closeSocket();
        LogUtil.i(TAG, "searching finish.");
        callback.onSearchEnd();
    }


    private void closeSocket() {
        if (udpSocket != null && !udpSocket.isClosed()) {
            try {
                udpSocket.close();
                LogUtil.i(TAG, "udp socket close.");
            } catch (Exception e) {
                e.printStackTrace();
            }
            udpSocket = null;
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
            LogUtil.i(TAG, "send udp broadcast.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void receiveBroadcast() {
        if (udpSocket == null)
            return;

        byte[] data = new byte[256];
        DatagramPacket recDataPacket = new DatagramPacket(data, data.length);
        data[2] = 0;
        data[3] = 0;
        try {
            udpSocket.receive(recDataPacket);
            LogUtil.i(TAG, "receive udp package.");
            if (data[2] == label[0] && data[3] == label[1]) { // 辨识码校对
                data[255] = 0;
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
                LogUtil.i(TAG, "find printer " + deviceInfo.toString());

                lock_cancel.lock();
                if (!flag_cancel) {
                    if (callback != null)
                        callback.onDeviceFound(deviceInfo);
                }
                lock_cancel.unlock();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public interface Callback {
        void onDeviceFound(DeviceInfo info);

        void onSearchEnd();
    }
}
