package com.jolimark.printer.common;


public final class MsgCode {
    private static int errCode;

    //打印机忙
    public static final int ER_PRINTER_BUSY = 0;
    //接收打印机返回有误
    public static final int ER_PRINTER_RECEIVE_WRONG = 1;
    //打印机校验不通过
    public static final int ER_PRINTER_VERIFY = 2;
    //接收打印机返回超时
    public static final int ER_PRINTER_RECEIVE_TIMEOUT = 3;
    //获取打印机类型失败
    public static final int ER_PRINTER_TYPE = 4;
    //打印机客户编码无效（客户化限制连接时使用）
    public static final int CLIENT_CODE_INVALID = 5;

    //发送数据为空
    public static final int ER_DATA_NULL = 6;
    //接收数据buffer为空
    public static final int ER_RECEIVE_BUFFER_NULL = 7;


    //打印机未连接
    public static final int ER_PRINTER_NOT_CONNECT = 10;

    //wifi地址空
    public static final int ER_WIFI_ADDRESS_NULL = 101;
    //wifi连接失败
    public static final int ER_WIFI_CONNECT_FAIL = 102;
    //wifi发送失败
    public static final int ER_WIFI_SEND_FAIL = 103;
    //wifi接收失败
    public static final int ER_WIFI_READ_FAIL = 104;
    //wifi udp创建失败
    public static final int ER_WIFI_UDP_SOCKET_CREATE_FAIL = 105;

    //蓝牙地址空
    public static final int ER_BT_ADDRESS_NULL = 201;
    //蓝牙连接失败
    public static final int ER_BT_CONNECT_FAIL = 202;
    //蓝牙发送失败
    public static final int ER_BT_SEND_FAIL = 203;
    //蓝牙接收失败
    public static final int ER_BT_RECEIVE = 204;

    //usb设备空
    public static final int ER_USB_DEVICE_null = 300;
    //usb设备未找到
    public static final int ER_USB_DEVICE_NOT_FOUND = 301;
    //usb连接失败
    public static final int ER_USB_DEVICE_CONNECT_FAIL = 302;
    //usb发送失败
    public static final int ER_USB_SEND_FAIL = 303;
    //usb接收失败
    public static final int ER_USB_RECEIVE_FAIL = 304;
    public static final int ER_USB_PERMISSION_DENIED = 305;


    //打印机未切换至双向通讯模式
    public static final int ER_BIDIRECTIONAL_NOT_SWITCH_PROTOCOL = 501;

    //双向通讯数据包头部有误
    public static final int ER_BIDIRECTIONAL_HEAD_ERROR = 502;

    //双向通讯数据包校验有误
    public static final int ER_BIDIRECTIONAL_PRINTER_CHECKOUT_ERROR = 503;

    //双向通讯数据缓冲区满
    public static final int ER_BIDIRECTIONAL_RECEIVE_BUFFER_FULL = 504;

    //双向通讯接收超时
    public static final int ER_BIDIRECTIONAL_RECEIVE_TIMEOUT = 505;

    //双向通讯数据包重复
    public static final int ER_BIDIRECTIONAL_SAME_PACKET_NUM = 506;

    //双向通讯续打，步骤异常，必须先调用正常打印一次，才能调用续打方法
    public static final int ER_BIDIRECTIONAL_STAGE_ERROR_RESUME_PRINT = 507;
    //双向通讯重打，步骤异常，必须先调用正常打印一次，才能调用重打方法
    public static final int ER_BIDIRECTIONAL_STAGE_ERROR_REPRINT = 508;
    //打印机回复包，SN与发送包不一致
    public static final int ER_BIDIRECTIONAL_SN_NOT_MATCH = 509;
    //打印机回复包，CRC校验不通过
    public static final int ER_BIDIRECTIONAL_CRC_NOT_MATCH = 510;


    //打印机开盖
    public static final int ER_STATUS_COVER_OPEN = 600;
    //打印机钱箱打开
    public static final int ER_STATUS_MONEY_BOX_OPEN = 601;
    //打印机缺纸
    public static final int ER_STATUS_OUT_OF_PAPER = 602;
    //打印机纸将尽
    public static final int ER_STATUS_ALMOST_OUT_OF_PAPER = 603;
    //打印机切刀错误
    public static final int ER_STATUS_KNIFE_ERROR = 604;
    //打印机其他错误
    public static final int ER_STATUS_OTHER_ERROR = 605;
    //打印机过热
    public static final int ER_STATUS_OVER_HEAT = 606;

    //pdf转换
    public static final int PDF_CONVERT_FAIL = 701;
    public static final int IMG_CONVERT_FAIL = 702;
    public static final int PDF_PATH_EMPTY = 703;
    public static final int PRINTER_TYPE_NULL = 704;//打印机类型不确定，无法进行转换


    public static int getLastErrorCode() {
        return errCode;
    }

    public static void setLastErrorCode(int LastErrCode) {
        errCode = LastErrCode;
    }

    public static void clear() {
        errCode = 0;
    }


    public static String getLastErrorMsg() {
        String msg = "";
        switch (errCode) {
            case ER_PRINTER_RECEIVE_WRONG: {
                msg = "接收打印机数据错误";
                break;
            }
            case ER_PRINTER_RECEIVE_TIMEOUT: {
                msg = "接收打印机数据超时";
                break;
            }
            case ER_DATA_NULL: {
                msg = "发送数据为空";
                break;
            }
            case ER_RECEIVE_BUFFER_NULL: {
                msg = "接收数据buffer为空";
                break;
            }
            case ER_PRINTER_NOT_CONNECT: {
                msg = "打印机未连接";
                break;
            }
            case ER_USB_DEVICE_null: {
                msg = "未找到打印机";
                break;
            }
            case ER_USB_DEVICE_CONNECT_FAIL: {
                msg = "连接打印机失败";
                break;
            }
            case ER_USB_SEND_FAIL: {
                msg = "发送数据失败";
                break;
            }
            case ER_USB_RECEIVE_FAIL: {
                msg = "接收数据失败";
                break;
            }
            case ER_STATUS_COVER_OPEN: {
                msg = "开盖";
                break;
            }
            case ER_STATUS_OUT_OF_PAPER: {
                msg = "缺纸";
                break;
            }
            case ER_BIDIRECTIONAL_HEAD_ERROR: {
                msg = "打印机回复：数据包包头错误";
                break;
            }
            case ER_BIDIRECTIONAL_PRINTER_CHECKOUT_ERROR: {
                msg = "打印机回复：数据包校验错误";
                break;
            }
            case ER_BIDIRECTIONAL_RECEIVE_BUFFER_FULL: {
                msg = "打印机回复：接收缓冲区满";
                break;
            }
            case ER_BIDIRECTIONAL_RECEIVE_TIMEOUT: {
                msg = "打印机回复：通讯超时";
                break;
            }
            case ER_BIDIRECTIONAL_SAME_PACKET_NUM: {
                msg = "打印机回复：包序号相同";
                break;
            }
            case ER_BIDIRECTIONAL_STAGE_ERROR_RESUME_PRINT: {
                msg = "续打异常，需调用打印后且该次打印未能完成，才能打印续打";
                break;
            }
            case ER_BIDIRECTIONAL_STAGE_ERROR_REPRINT: {
                msg = "重打异常，请先调用打印";
                break;
            }
            case ER_BIDIRECTIONAL_SN_NOT_MATCH: {
                msg = "打印机回复包SN与主机发送包不一致";
                break;
            }
            case ER_BIDIRECTIONAL_CRC_NOT_MATCH: {
                msg = "打印机回复包CRC校验不通过";
                break;
            }
            default: {
                msg = "错误码：【" + errCode + "】";
                break;
            }
        }
        return msg;
    }


}
