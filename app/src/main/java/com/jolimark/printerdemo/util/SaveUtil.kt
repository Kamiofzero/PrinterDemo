package com.jolimark.printerdemo.util

import android.content.Context
import com.jolimark.printer.printer.BasePrinter
import com.jolimark.printer.printer.BluetoothPrinter
import com.jolimark.printer.printer.UsbPrinter
import com.jolimark.printer.printer.WifiPrinter
import com.jolimark.printer.trans.TransType
import com.jolimark.printer.util.LogUtil

object SaveUtil {

    fun savePrinter(context: Context, basePrinter: BasePrinter) {
        context.getSharedPreferences("printer", Context.MODE_PRIVATE).edit().apply {
            var str = ""
            when (basePrinter.transtype) {
                TransType.WIFI -> {
                    var wifiPrinter = basePrinter as WifiPrinter
                    str = "wifi/${wifiPrinter.ip}/${wifiPrinter.port}"
                }

                TransType.BLUETOOTH -> {
                    var bluetoothPrinter = basePrinter as BluetoothPrinter
                    str = "bluetooth/${bluetoothPrinter.mac}"
                }

                TransType.USB -> {
                    var usbPrinter = basePrinter as UsbPrinter
                    str = "usb/${usbPrinter.device?.vendorId}/${usbPrinter.device?.productId}"
                }
            }
            LogUtil.i("SaveUtil", "save: $str")
            putString("info", str)
            commit()
        }
    }

    fun loadPrinters(): MutableList<BasePrinter> {
        var list = mutableListOf<BasePrinter>()


        return list
    }
}