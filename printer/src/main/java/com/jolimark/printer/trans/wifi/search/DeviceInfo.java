package com.jolimark.printer.trans.wifi.search;

import java.io.Serializable;

public class DeviceInfo implements Serializable {

    public String ip; // 打印机ip
    public String port;// 打印机端口
    public String type;// 打印机型号
    public String mac;// Mac地址

    @Override
    public boolean equals(Object o) {
        if (mac.equals(((DeviceInfo) o).mac)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "[ip :" + ip + ", port :" + port + ", mac " + mac + ", type :" + type + "]";
    }
}
