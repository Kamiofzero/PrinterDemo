package com.jolimark.printer.protocol;

import com.jolimark.printer.bean.PrinterConfig;
import com.jolimark.printer.bean.PrinterInfo;
import com.jolimark.printer.trans.TransBase;
import com.jolimark.printer.util.ByteArrayUtil;

import java.util.List;

public abstract class CommBase {
    protected TransBase transBase;

    protected PrinterInfo printerInfo;

    protected PrinterConfig config;


    public CommBase(TransBase transBase) {
        this.transBase = transBase;
        config = new PrinterConfig();
    }

    public abstract boolean connect();

    public abstract void disconnect();

    public abstract boolean sendData_(List<byte[]> bytesList);


    public boolean sendData(byte[] bytes) {
        List<byte[]> bytesList = ByteArrayUtil.splitArray(bytes, config.packageSize);
        return sendData_(bytesList);
    }


    public void setConfig(PrinterConfig config) {
        this.config = config;
    }

//    public void setPackageSize(int packageSize) {
//        this.packageSize = packageSize;
//    }

//    public void setSendDelay(int sendDelay) {
//        this.sendDelay = sendDelay;
//    }

    public PrinterInfo getPrinterInfo() {
        return printerInfo;
    }


//    public void enableVerification(boolean enable) {
//        enableVerification = enable;
//    }

//    public void setClientCode(int clientCode) {
//        this.clientCode = clientCode;
//    }


    public boolean isConnected() {
        return transBase.isConnected();
    }

}
