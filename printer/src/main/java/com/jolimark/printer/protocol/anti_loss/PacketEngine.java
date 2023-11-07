package com.jolimark.printer.protocol.anti_loss;



import com.jolimark.printer.common.MsgCode;
import com.jolimark.printer.protocol.anti_loss.object.PrinterStatusInfo;
import com.jolimark.printer.protocol.anti_loss.object.ReceiveHolder;
import com.jolimark.printer.protocol.anti_loss.object.ReceivePackageInfo;
import com.jolimark.printer.util.ByteArrayUtil;
import com.jolimark.printer.util.LogUtil;

import java.util.zip.CRC32;

/**
 * 数据包处理类
 *
 * @author zhrjian
 */
public class PacketEngine {
    private static final String TAG = "PacketEngine";

    /**
     * 封装普通数据包
     * <p>
     * 1A 17 SEQ N1 N2 D1.......Dn CHK1 CHK2 CHK3 CHK4
     * 说明：
     * 1A 17:包头
     * SEQ：包序号（从0开始，顺序增加，达到253后复位为0，重发的数据包序号不变，255预留，254校验）
     * N1，N2:  长度（包括D1…Dn和CHK1…CHK4，两个字节，低字节在前）
     * D1…Dn: 数据包内容
     * CHK1 CHK2 CHK3 CHK4：包头至数据包内容的CRC32校验，低字节在前
     *
     * @param data       原始数据内容
     * @param packageSeq 数据包序号
     * @return
     */
    public static byte[] pack(byte[] data, int packageSeq) {
        byte[] tempData = new byte[data.length + 9];
        // 长度
        int dataLength = data.length + 4;
        tempData[0] = 0x1A; // 包头
        tempData[1] = 0x17;
        tempData[2] = (byte) packageSeq; // 包序号
        tempData[3] = (byte) (dataLength & 0xFF); // 长度
        tempData[4] = (byte) ((dataLength >> 8) & 0xFF);
        System.arraycopy(data, 0, tempData, 5, data.length);
        // 计算校验码
        CRC32 cc = new CRC32();
        try {
            cc.update(tempData, 0, tempData.length - 4);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 发送的数据的CRC32 校验码
        long crcValue = cc.getValue();
        LogUtil.i(TAG, "packageSeq ->" + packageSeq);
        LogUtil.i(TAG, "crc ->" + crcValue + ", " + Long.toHexString(crcValue));
        tempData[data.length + 5] = (byte) (crcValue & 0xFF);
        tempData[data.length + 6] = (byte) ((crcValue >> 8) & 0xFF);
        tempData[data.length + 7] = (byte) ((crcValue >> 16) & 0xFF);
        tempData[data.length + 8] = (byte) ((crcValue >> 24) & 0xFF);
        return tempData;
    }


    public static byte[] pack(byte[] data, int packageSeq, int type) {
        byte[] tempData = new byte[data.length + 9];
        // 长度
        int dataLength = data.length + 4;
        byte b = 0;
        if (type == 0) {
            b = 0x17;
        } else if (type == 1) {
            b = 0x13;
        } else if (type == 2) {
            b = 0x14;
        } else if (type == 3) {
            b = 0x15;
        }
        tempData[0] = 0x1A; // 包头
        tempData[1] = b;
        tempData[2] = (byte) packageSeq; // 包序号
        tempData[3] = (byte) (dataLength & 0xFF); // 长度
        tempData[4] = (byte) ((dataLength >> 8) & 0xFF);
        System.arraycopy(data, 0, tempData, 5, data.length);
        // 计算校验码
        CRC32 cc = new CRC32();
        try {
            cc.update(tempData, 0, tempData.length - 4);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 发送的数据的CRC32 校验码
        long crcValue = cc.getValue();
        LogUtil.i(TAG, "packageSeq ->" + packageSeq);
        LogUtil.i(TAG, "crc ->" + crcValue + ", " + Long.toHexString(crcValue));
        tempData[data.length + 5] = (byte) (crcValue & 0xFF);
        tempData[data.length + 6] = (byte) ((crcValue >> 8) & 0xFF);
        tempData[data.length + 7] = (byte) ((crcValue >> 16) & 0xFF);
        tempData[data.length + 8] = (byte) ((crcValue >> 24) & 0xFF);
        return tempData;
    }

    /**
     * 分析byte数据封装数据包对象
     *
     * @param recData    完整数据包字节数组
     * @param packageSeq 数据包当前序号（用于校验接收到的序号是否一致）
     * @return
     */
    public static ReceivePackageInfo unPack(byte[] recData, int packageSeq) {

        LogUtil.i(TAG, ByteArrayUtil.toArrayString(recData, recData.length));
        // 校验数据
        CRC32 recCrc = new CRC32();
        int tolRet = recData.length;

        try {
            recCrc.update(recData, 0, tolRet - 4);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long localCrcValue = recCrc.getValue();// 接收后的crc校验，手机端计算的值
        long recCrcValue = ((long) recData[tolRet - 4] & 0xFF) + (((long) recData[tolRet - 3] & 0xFF) << 8)
                + (((long) recData[tolRet - 2] & 0xFF) << 16) + (((long) recData[tolRet - 1] & 0xFF) << 24);// 数据包中的CRC值

        // 比对校验码是否一致
        if (localCrcValue != recCrcValue) {
            LogUtil.i(TAG, "local CrcValue -> " + localCrcValue + ", " + Long.toHexString(localCrcValue));
            LogUtil.i(TAG, "rec CrcValue -> " + recCrcValue + ", " + Long.toHexString(recCrcValue));
            LogUtil.i(TAG, "decode response packet -> packet verification not pass.");
            MsgCode.setLastErrorCode(MsgCode.ER_BIDIRECTIONAL_CRC_NOT_MATCH);
            return null;
        }
        // 检查包序号是否正确
        int recPackageNum = recData[2] & 0xFF;
        if (recPackageNum == packageSeq || recPackageNum == -1) {
            // 不操作
        } else {
            LogUtil.i(TAG, "local packageSeq ->" + packageSeq);
            LogUtil.i(TAG, "rec packageSeq ->" + recPackageNum);
            LogUtil.i(TAG, "decode response packet -> packet sn incorrect.");
            MsgCode.setLastErrorCode(MsgCode.ER_BIDIRECTIONAL_SN_NOT_MATCH);
            return null;
        }

        ReceivePackageInfo info = new ReceivePackageInfo();
        //打印机通讯结果信息
        switch (recData[5]) {
            case 0x00:
                info.result = ReceivePackageInfo.STATUS_OK;
                // 数据内容,现在只有查询打印机类型以及查询打印机状态的指令会返回带有数据内容的回应
                //其他指令的回应都只需分析通讯结果位以及故障状态位即可
                if (tolRet > 11) {
                    byte[] data = new byte[tolRet - 11];
                    for (int i = 7; i < tolRet - 4; i++) {
                        data[i - 7] = recData[i];
                    }
                    info.contentData = data;
                }
                break;
            case 0x01:
                info.result = ReceivePackageInfo.STATUS_HEAD_ERROR;
                break;
            case 0x02:
                info.result = ReceivePackageInfo.STATUS_PRINTER_CHECKOUT_ERROR;
                break;
            case 0x03:
                info.result = ReceivePackageInfo.STATUS_RECEIVE_BUFFER_FULL;
                break;
            case 0x04:
                info.result = ReceivePackageInfo.STATUS_RECEIVE_TIMEOUT;
                break;
            case 0x05:
                info.result = ReceivePackageInfo.STATUS_SAME_PACKET_NUM;
                break;
        }

        /**
         * Bit0 – 1=联机，0=脱机
         * Bit1 – 1=钱箱打开，0=钱箱闭合
         * Bit2 – 1=上盖打开，0=上盖闭合
         * Bit3 – 1=纸尽，0=正常
         * Bit4 – 1=纸将尽，0=正常
         * Bit5 – 1=切刀故障，0=切刀正常
         * Bit6 – 1=其它故障，0=无其它故障
         */
        //打印机状态信息
        info.isConnect = (((int) recData[6] & 0x01) == 1) ? true : false;
        info.isMoneyBoxOpen = ((((int) recData[6] >> 1) & 0x01) == 1) ? true : false;
        info.isCoverOpen = ((((int) recData[6] >> 2) & 0x01) == 1) ? true : false;
        info.isOutOfPaper = ((((int) recData[6] >> 3) & 0x01) == 1) ? true : false;
        info.isAlmostOutOfPaper = ((((int) recData[6] >> 4) & 0x01) == 1) ? true : false;
        info.isKnifeError = ((((int) recData[6] >> 5) & 0x01) == 1) ? true : false;
        info.isOtherError = ((((int) recData[6] >> 6) & 0x01) == 1) ? true : false;

        LogUtil.i(TAG, "decode response packet -> response packet correct.");

        return info;
    }


    public static boolean checkPackageResponse(ReceivePackageInfo info) {
        if (info == null)
            return false;
        if (!checkCommunication(info))
            return false;
        if (!checkPrinterStatus(info))
            return false;
        return true;
    }

    public static boolean checkCommunication(ReceivePackageInfo info) {
        boolean flag = false;
        if (info != null) {
            switch (info.result) {
                case ReceivePackageInfo.STATUS_OK:
                    flag = true;
                    break;
                case ReceivePackageInfo.STATUS_HEAD_ERROR:
                    LogUtil.i(TAG, "decode response packet -> communication error : header error. ");
                    MsgCode.setLastErrorCode(MsgCode.ER_BIDIRECTIONAL_HEAD_ERROR);
                    break;
                case ReceivePackageInfo.STATUS_PRINTER_CHECKOUT_ERROR:
                    LogUtil.i(TAG, "decode response packet -> communication error : printer checkout error. ");
                    MsgCode.setLastErrorCode(MsgCode.ER_BIDIRECTIONAL_PRINTER_CHECKOUT_ERROR);

                    break;
                case ReceivePackageInfo.STATUS_RECEIVE_BUFFER_FULL:
                    LogUtil.i(TAG, "decode response packet -> communication error : printer receive buffer full. ");
                    MsgCode.setLastErrorCode(MsgCode.ER_BIDIRECTIONAL_RECEIVE_BUFFER_FULL);

                    break;
                case ReceivePackageInfo.STATUS_RECEIVE_TIMEOUT:
                    LogUtil.i(TAG, "decode response packet -> communication error : printer receive timeout. ");
                    MsgCode.setLastErrorCode(MsgCode.ER_BIDIRECTIONAL_RECEIVE_TIMEOUT);

                    break;
                case ReceivePackageInfo.STATUS_SAME_PACKET_NUM:
                    LogUtil.i(TAG, "decode response packet -> communication error : printer same packet number. ");
                    MsgCode.setLastErrorCode(MsgCode.ER_BIDIRECTIONAL_SAME_PACKET_NUM);

                    break;
                default:
                    break;
            }
        }
        return flag;
    }

    public static boolean checkPrinterStatus(ReceivePackageInfo info) {
        boolean flag = false;
        if (info != null) {
            StringBuilder sb = new StringBuilder();
            if (info.isCoverOpen) {
                flag = true;
                sb.append("[cover open] ");
                MsgCode.setLastErrorCode(MsgCode.ER_STATUS_COVER_OPEN);
            }
            if (info.isMoneyBoxOpen) {
                flag = true;
                sb.append("[money box open] ");
                MsgCode.setLastErrorCode(MsgCode.ER_STATUS_MONEY_BOX_OPEN);
            }
            if (info.isOutOfPaper) {
                flag = true;
                sb.append("[out of paper] ");
                MsgCode.setLastErrorCode(MsgCode.ER_STATUS_OUT_OF_PAPER);
            }
            if (info.isAlmostOutOfPaper) {
                flag = true;
                sb.append("[almost out of paper] ");
                MsgCode.setLastErrorCode(MsgCode.ER_STATUS_ALMOST_OUT_OF_PAPER);
            }
            if (info.isKnifeError) {
                flag = true;
                sb.append("[paper knife error] ");
                MsgCode.setLastErrorCode(MsgCode.ER_STATUS_KNIFE_ERROR);
            }
            if (info.isOtherError) {
                flag = true;
                sb.append("[other paper]");
                MsgCode.setLastErrorCode(MsgCode.ER_STATUS_OTHER_ERROR);
            }
            if (flag) {
                LogUtil.i(TAG, "decode response packet -> printer status abnormal : " + sb);
            }
            flag = !flag;
        }
        return flag;
    }

    public static PrinterStatusInfo AnalyseStatusData(byte status) {

        PrinterStatusInfo printerStatusInfo = new PrinterStatusInfo();
        printerStatusInfo.isNoPaper = (status & 0x01) == 1 ? true : false;
        printerStatusInfo.isOverHeat = ((status >> 1) & 0x01) == 1 ? true : false;
        printerStatusInfo.isCoverOpen = ((status >> 2) & 0x01) == 1 ? true : false;
        printerStatusInfo.isKnifeError = ((status >> 3) & 0x01) == 1 ? true : false;
        printerStatusInfo.isRecBufferError = ((status >> 4) & 0x01) == 1 ? true : false;
        printerStatusInfo.isFinishTaskFlag = ((status >> 5) & 0x01) == 1 ? true : false;
        printerStatusInfo.isHadPrinterError = ((status >> 6) & 0x01) == 1 ? true : false;
        printerStatusInfo.isPrinting = ((status >> 7) & 0x01) == 1 ? true : false;

        return printerStatusInfo;
    }


    public static boolean locateHeader(ReceiveHolder rHolder) {
        int length = rHolder.dataBuffer.length;
        //每轮循环查找目标字节：1A -> 1B
        //如果任意一步不满足，则跳过这轮，重新从1A开始找
        for (int i = 0; ; ) {
            if (i >= length) {
                break;
            }
            if (rHolder.dataBuffer[i++] != 0x1A) {
                continue;
            }
            if (i >= length) {
                //如果最后一字节是0x1A,则把下标回位到0x1A处，待后续数据合并后从0x1A处重新解析
                rHolder.index = i--;
                break;
            }
            if (rHolder.dataBuffer[i] != 0x18) {
                //下标不递增，让下一轮重新检测该字节是否为0x1A
                continue;
            }
            //记录应答包有效数据在缓存中的起始位置
            rHolder.packageOffset = i - 1;
            rHolder.index = i + 1;
            LogUtil.i(TAG, "handle response packet -> locate header.");
            return true;
        }
        return false;
    }


    public static int getPackageLength(ReceiveHolder rHolder) {
        //跳过一个字节的包序号，分析后面两个字节的包长度
        rHolder.packageLength = (rHolder.dataBuffer[rHolder.index + 1] & 0xff) + ((rHolder.dataBuffer[rHolder.index + 2] & 0xff) << 8);
        LogUtil.i(TAG, "handle response packet -> packet length：" + rHolder.packageLength + " bytes");
        rHolder.index = rHolder.index + 2;
        return rHolder.packageLength;
    }

    public static ReceivePackageInfo decodeContent(ReceiveHolder rHolder, int packageSeq) {
        LogUtil.i(TAG, "handle response packet -> get whole packet.");
        byte[] data = ByteArrayUtil.subArray(rHolder.dataBuffer, rHolder.packageOffset, rHolder.packageLength + 5);
        ReceivePackageInfo info = PacketEngine.unPack(data, packageSeq);
        return info;
    }


//    public static byte[] pack(byte[] data, int packageSeq, int secondNumber) {
//        // 发送的数据的CRC32 校验码
//        long crcValue;
//        // 封装数据包 1A 17 SEQ N1 N2 D1.......Dn CHK1 CHK2 CHK3 CHK4
//        byte[] tempData = new byte[data.length + 9];
//        // 长度 包括数据D1…Dn和校验CHK1…CHK4
//        int dataLength = data.length + 4;
//        tempData[0] = 0x1A; // 包头
//        tempData[1] = (byte) secondNumber;
//        tempData[2] = (byte) packageSeq; // 包序号
//        tempData[3] = (byte) (dataLength & 0xFF); // 长度
//        tempData[4] = (byte) ((dataLength >> 8) & 0xFF);
//        System.arraycopy(data, 0, tempData, 5, data.length);
//        // 计算校验码
//        CRC32 cc = new CRC32();
//        try {
//            cc.update(tempData, 0, tempData.length - 4);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        crcValue = cc.getValue();
//        LogUtil.i(TAG, "-------------- 包序号：" + packageSeq);
//        LogUtil.i(TAG, "-------------- crc：" + crcValue);
//        tempData[data.length + 5] = (byte) (crcValue & 0xFF);
//        tempData[data.length + 6] = (byte) ((crcValue >> 8) & 0xFF);
//        tempData[data.length + 7] = (byte) ((crcValue >> 16) & 0xFF);
//        tempData[data.length + 8] = (byte) ((crcValue >> 24) & 0xFF);
//        return tempData;
//    }

    /**
     * 判断是否是重复包
     *
     * @param list
     * @param rec
     * @return
     */
//    public static boolean isExited(List<ReceivePackageInfo> list, ReceivePackageInfo rec) {
//        if (list != null && rec != null) {
//            return list.contains(rec);
//        } else {
//            return false;
//        }
//    }
}
