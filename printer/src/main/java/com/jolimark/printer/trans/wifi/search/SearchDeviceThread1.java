package com.jolimark.printer.trans.wifi.search;


import static com.jolimark.printer.common.MsgCode.ER_WIFI_UDP_SOCKET_CREATE_FAIL;

import com.jolimark.printer.common.MsgCode;
import com.jolimark.printer.util.ByteArrayUtil;
import com.jolimark.printer.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

public class SearchDeviceThread1 extends Thread {

    private final String TAG = "SearchDeviceThread1";


    private Callback callback;
    private static final int LOCAL_PORT = 10002; // 本地端口号
    private static final String BROADCAST_IP = "255.255.255.255";// 广播地址
    private static final int BROADCAST_PORT = 10002; // UDP广播的端口号
    private byte label[] = null;// 两字节辨识码
    private DatagramSocket udpSocket = null;
    private int timeout = 10000;// 最大搜索时间/毫秒


    private boolean flag_cancel;
    private boolean flag_timeout;
    private Timer timer;

    private ReentrantLock lock_cancel = new ReentrantLock();

    public SearchDeviceThread1(Callback callback) {
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
            udpSocket.setSoTimeout(1000);
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
        //udp广播数据
        byte[] bytes = new byte[]{(byte) 0xBC, 0x01, 0x0B, 0x00, 0x00, 0x00};
        bytes = ByteArrayUtil.mergeArrays(bytes, "{\"tp\":1001}".getBytes());
        DatagramPacket dataPacket = null;
        try {
            dataPacket = new DatagramPacket(bytes, bytes.length,
                    InetAddress.getByName(BROADCAST_IP), BROADCAST_PORT);

            //发送广播
            udpSocket.send(dataPacket);
            LogUtil.i(TAG, "searchType1 send package:" + ByteArrayUtil.toHex(bytes, bytes.length));

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void receiveBroadcast() {
        if (udpSocket == null)
            return;

        // UDP接收数据缓存
        byte[] buff = new byte[256];
        DatagramPacket recDataPacket = new DatagramPacket(buff, buff.length);

        try {

            //接收广播
            udpSocket.receive(recDataPacket);
            LogUtil.i(TAG, "receive package");
            if ((buff[0] & 0xff) == 0xBC && buff[1] == 0x01) {

                int len1 = (buff[5] & 0xff) << 24;
                int len2 = (buff[4] & 0xff) << 16;
                int len3 = (buff[3] & 0xff) << 8;
                int len4 = (buff[2] & 0xff);
                int len = len1 + len2 + len3 + len4;
                LogUtil.i(TAG, "len: " + len);
                if (len <= buff.length - 6) {
                    String jsonString = new String(buff, 6, len);
                    LogUtil.i(TAG, "jsonString: " + jsonString);


                    try {
                        JSONObject jsonObject = new JSONObject(jsonString);
                        String tp = jsonObject.getString("tp");
                        if (!tp.equals("1001")) {
                            String ip = jsonObject.getString("ip");
                            String printerModel = jsonObject.getString("mdl");
                            String mac = jsonObject.getString("mac");
                            DeviceInfo deviceInfo = new DeviceInfo();
                            deviceInfo.ip = ip;
                            deviceInfo.type = printerModel;
                            deviceInfo.mac = mac;
                            deviceInfo.port = "19100";
                            LogUtil.i(TAG, "find printer " + deviceInfo.toString());

                            lock_cancel.lock();
                            if (!flag_cancel) {
                                if (callback != null)
                                    callback.onDeviceFound(deviceInfo);
                            }
                            lock_cancel.unlock();
                        } else {
                            LogUtil.i(TAG, "package drop");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.i(TAG, "udp receive error:" + e.getMessage());
        }
    }


    public interface Callback {
        void onDeviceFound(DeviceInfo info);

        void onSearchEnd();
    }
}
