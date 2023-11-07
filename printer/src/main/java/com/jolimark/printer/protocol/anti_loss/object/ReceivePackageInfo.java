package com.jolimark.printer.protocol.anti_loss.object;

import java.io.Serializable;

/**
 * 接收到的数据包解析出来的内容
 *
 * @author zhrjian
 */
public class ReceivePackageInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 数据包返回结果，先判断
     */
    public static final int STATUS_OK = 0x11; // 返回结果正确
    public static final int STATUS_HEAD_ERROR = 0x12; // 包头错误
    public static final int STATUS_PRINTER_CHECKOUT_ERROR = 0x13; // 打印机校验错误
    public static final int STATUS_RECEIVE_BUFFER_FULL = 0x14; // 接收缓冲区满
    public static final int STATUS_RECEIVE_TIMEOUT = 0x15;// 接收超时
    public static final int STATUS_SAME_PACKET_NUM = 0x16; // 相同的包序号


    /**
     * 打印机状态信息 Bit0 – 1=联机，0=脱机 Bit1 – 1=钱箱打开，0=钱箱闭合 Bit2 – 1=上盖打开，0=上盖闭合 Bit3 –
     * 1=纸尽，0=正常 Bit4 – 1=纸将尽，0=正常 Bit5 – 1=切刀故障，0=切刀正常 Bit6 – 1=其它故障，0=无其它故障
     */
    public boolean isConnect = false; // 是否联机
    public boolean isMoneyBoxOpen = false; // 是否钱箱打开
    public boolean isCoverOpen = false; // 是否上盖打开
    public boolean isOutOfPaper = false; // 是否纸尽
    public boolean isAlmostOutOfPaper = false; // 是否纸将尽
    public boolean isKnifeError = false; // 是否切刀故障
    public boolean isOtherError = false; // 是否其它故障
    /**
     * 数据内容 contentData
     */
    public byte[] contentData = null;
    /**
     * 数据包返回结果
     */
    public int result = -1;

    public boolean isPrinterAbnormal() {
        if (isCoverOpen || isOutOfPaper || isKnifeError || isOtherError) {
            return true;
        }
        return false;
    }

    public boolean isCommunicationAbnormal() {
        return result != 0x11;
    }

    /**
     * 数据包类型（应答包/数据包）
     */
//	public int packageType = -1;

    /**
     * CRC校验码
     */
    public byte[] CRC = null;

    @Override
    public boolean equals(Object o) {
        // 通过CRC码比较是否相等
        byte[] temp = ((ReceivePackageInfo) o).CRC;
        if (this.CRC != null && temp != null) {
            if (this.CRC.length == temp.length) {
                for (int i = 0; i < this.CRC.length; i++) {
                    if (this.CRC[i] == temp[i]) {
                        continue;
                    } else {
                        return false;// 不相等
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}