package com.jolimark.printer.trans.wifi;


import com.jolimark.printer.common.MsgCode;
import com.jolimark.printer.trans.TransBase;
import com.jolimark.printer.util.LogUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ljbin on 2018/2/23.
 */

public class WifiBase implements TransBase {

    private final String TAG = "WifiBase";

    private final int mPort = 9100; // 端口号

    private static Socket wifiSocket = null;
    private DataOutputStream out = null;
    private DataInputStream in = null;

    private String ip;
    private int port;

    public void setIpAndPort(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public boolean connect() {
        if (ip == null) {
            LogUtil.i(TAG, "ip not set.");
            MsgCode.setLastErrorCode(MsgCode.ER_WIFI_ADDRESS_NULL);
            return false;
        }
        if (wifiSocket != null) {
            disconnect();
        }
        try {
            LogUtil.i(TAG, "socket connecting to [" + "ip: " + ip + " , port: " + mPort + "] ...");
//            wifiSocket = new Socket();
//            wifiSocket.setTcpNoDelay(true);//禁用Nagle算法
//            wifiSocket.setSoTimeout(5000);// 读5S超时
//            wifiSocket.setKeepAlive(true);
//            wifiSocket.connect(new InetSocketAddress(ipAddr, mPort), 5000);

            wifiSocket = new Socket(ip, port == -1 ? mPort : port);

            out = new DataOutputStream(wifiSocket.getOutputStream());
            in = new DataInputStream(wifiSocket.getInputStream());
        } catch (UnknownHostException e) {
            wifiSocket = null;
            out = null;
            in = null;
            e.printStackTrace();
            LogUtil.i(TAG, "socket connect fail.");
            MsgCode.setLastErrorCode(MsgCode.ER_WIFI_CONNECT_FAIL);
            return false;
        } catch (IOException e) {
            wifiSocket = null;
            out = null;
            in = null;
            e.printStackTrace();
            LogUtil.i(TAG, "socket io stream get fail.");
            MsgCode.setLastErrorCode(MsgCode.ER_WIFI_CONNECT_FAIL);
            return false;
        }
        LogUtil.i(TAG, "socket connect success.");
        return true;
    }


    /**
     * 发送数据
     *
     * @param bytes
     * @return
     */
    @Override
    public boolean sendData(byte[] bytes) {
        if (out == null) {
            LogUtil.i(TAG, "socket not connect.");
            MsgCode.setLastErrorCode(MsgCode.ER_PRINTER_NOT_CONNECT);
            return false;
        }
        if (bytes == null) {
            LogUtil.i(TAG, "data to send is null.");
            MsgCode.setLastErrorCode(MsgCode.ER_DATA_NULL);
            return false;
        }
        try {
            out.write(bytes);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.i(TAG, "socket send fail.");
            MsgCode.setLastErrorCode(MsgCode.ER_WIFI_SEND_FAIL);
            return false;
        }
        LogUtil.i(TAG, "socket send success.");
        return true;
    }

    /**
     * 接收数据
     *
     * @param buffer
     * @param timeout
     * @return
     */
    @Override
    public int receiveData(byte[] buffer, int timeout) {
        if (in == null) {
            LogUtil.i(TAG, "socket not connect.");
            MsgCode.setLastErrorCode(MsgCode.ER_PRINTER_NOT_CONNECT);
            return -1;
        }
        if (buffer == null) {
            LogUtil.i(TAG, "receive buffer is null.");
            MsgCode.setLastErrorCode(MsgCode.ER_RECEIVE_BUFFER_NULL);
            return -1;
        }
        final boolean[] flag_timeout = {false};
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                flag_timeout[0] = true;
            }
        }, timeout);
        int len = 0;
        try {
            while (!flag_timeout[0]) {
                if (in.available() > 0) {
                    len = in.read(buffer);
                    timer.cancel();
                    break;
                }
                Thread.sleep(10);
            }
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.i(TAG, "receive data fail.");
            MsgCode.setLastErrorCode(MsgCode.ER_WIFI_READ_FAIL);
            return -2;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        LogUtil.i(TAG, "receive data " + len + " bytes.");
        return len;
    }

    @Override
    public void disconnect() {
        try {
            if (wifiSocket != null) {
                wifiSocket.close();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            LogUtil.i(TAG, "socket close.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            in = null;
            out = null;
            wifiSocket = null;
        }
    }


    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
