package com.jolimark.printerdemo.db

import com.jolimark.printer.printer.BasePrinter
import com.jolimark.printer.printer.BluetoothPrinter
import com.jolimark.printer.printer.UsbPrinter
import com.jolimark.printer.printer.WifiPrinter
import com.jolimark.printer.trans.TransType
import com.jolimark.printerdemo.PrinterDemoApp
import com.jolimark.printerdemo.db.base.DbBean

class PrinterBean : DbBean {


    var type: String? = null
    var info: String? = null

    constructor()
    constructor(basePrinter: BasePrinter) {
        var type = ""
        var info = ""
        when (basePrinter.transtype) {
            TransType.WIFI -> {
                type = "wifi"
                var wifiPrinter = basePrinter as WifiPrinter
                info = "${wifiPrinter.ip}/${wifiPrinter.port}"
            }

            TransType.BLUETOOTH -> {
                type = "bluetooth"
                var bluetoothPrinter = basePrinter as BluetoothPrinter
                info = "${bluetoothPrinter.mac}/"
            }

            TransType.USB -> {
                type = "usb"
                var usbPrinter = basePrinter as UsbPrinter
                info = "${usbPrinter.vid}/${usbPrinter.pid}"
            }
        }
        this.type = type
        this.info = info
        this._uuid = "$type/$info"
    }

    fun toPrinter(): BasePrinter? {
        var printer: BasePrinter? = null
        var array = info?.split("/")
        when (type) {
            "wifi" -> {
                printer = WifiPrinter(array?.get(0), array?.get(1)!!.toInt())
            }

            "bluetooth" -> {
                printer = BluetoothPrinter(array?.get(0))
            }

            "usb" -> {
                printer = UsbPrinter(
                    PrinterDemoApp.context,
                    array?.get(0)!!.toInt(),
                    array?.get(1)!!.toInt()
                )
            }
        }
        return printer
    }

    override fun toString(): String {
        return _uuid
    }
}