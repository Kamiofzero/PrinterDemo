package com.jolimark.printer.direction.anti_loss.object;


import com.jolimark.printer.common.MsgCode;

/**
 * Created by jsliang on 2016/8/29.
 */
public class PrinterStatusInfo {
    public boolean isNoPaper = false; // 是否纸尽
    public boolean isOverHeat = false; // 是否过热
    public boolean isCoverOpen = false; // 是否上盖打开
    public boolean isKnifeError = false; // 是否切刀故障
    public boolean isRecBufferError = false; // 是否接收缓冲区满
    public boolean isFinishTaskFlag = false; // 任务结束标志
    public boolean isHadPrinterError = false; //打印途中是否出现过错误
    public boolean isPrinting = false; //是否正在打印

    public boolean isNormal() {
        if (isCoverOpen) {
            MsgCode.setLastErrorCode(MsgCode.ER_STATUS_COVER_OPEN);
            return false;
        }
        if (isNoPaper) {
            MsgCode.setLastErrorCode(MsgCode.ER_STATUS_OUT_OF_PAPER);
            return false;
        }
        if (isOverHeat) {
            MsgCode.setLastErrorCode(MsgCode.ER_STATUS_OVER_HEAT);
            return false;
        }
        if (isKnifeError) {
            MsgCode.setLastErrorCode(MsgCode.ER_STATUS_KNIFE_ERROR);
            return false;
        }
        if (isRecBufferError) {
            MsgCode.setLastErrorCode(MsgCode.ER_BIDIRECTIONAL_RECEIVE_BUFFER_FULL);
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PrinterStatusInfo{" +
                "isNoPaper=" + isNoPaper +
                ",\n isOverHeat=" + isOverHeat +
                ",\n isCoverOpen=" + isCoverOpen +
                ",\n isKnifeError=" + isKnifeError +
                ",\n isRecBufferError=" + isRecBufferError +
                ",\n isFinishTaskFlag=" + isFinishTaskFlag +
                ",\n isHadPrinterError=" + isHadPrinterError +
                ",\n isPrinting=" + isPrinting +
                '}';
    }

    public String toZHString() {
        return "{" +
                "缺纸=" + (isNoPaper ? "是" : "否") +
                ",\n 过热=" + (isOverHeat ? "是" : "否") +
                ",\n  开盖=" + (isCoverOpen ? "是" : "否") +
                ",\n  切刀错误=" + (isKnifeError ? "是" : "否") +
                ",\n  接收缓冲满=" + (isRecBufferError ? "是" : "否") +
                ",\n  打印结束标志=" + (isFinishTaskFlag ? "是" : "否") +
                ",\n  打印途中出错=" + (isHadPrinterError ? "是" : "否") +
                ",\n  正在打印=" + (isPrinting ? "是" : "否") +
                '}';
    }
}
