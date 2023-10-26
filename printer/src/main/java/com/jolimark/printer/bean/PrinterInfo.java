package com.jolimark.printer.bean;

public class PrinterInfo {
    public static final int PRINTER_TYPE_9DOT = 10;
    public static final int PRINTER_TYPE_24DOT = 11;
    public static final int PRINTER_TYPE_THERMAL = 12;

    public int clientCode;
    public String printerModel;
    public int printerType;
}
