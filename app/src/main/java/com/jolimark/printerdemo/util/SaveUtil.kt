package com.jolimark.printerdemo.util

import android.content.Context
import com.jolimark.printer.util.LogUtil
import com.jolimark.printerdemo.config.Config

object SettingUtil {

    private val TAG = "SettingUtil"
    private var config = Config()


    val connectVerify: Boolean
        get() = config.connectVerify
    val antiLost: Boolean
        get() = config.preventLost
    val usbPrinterSendDelay: Int
        get() = config.usbPrinterSendDelay
    val usbPrinterPackageSize: Int
        get() = config.usbPrinterPackageSize

    val bluetoothPrinterSendDelay: Int
        get() = config.bluetoothPrinterSendDelay
    val bluetoothPrinterPackageSize: Int
        get() = config.bluetoothPrinterPackageSize

    val wifiPrinterSendDelay: Int
        get() = config.wifiPrinterSendDelay

    val wifiPrinterPackageSize: Int
        get() = config.wifiPrinterPackageSize

    fun loadSetting(context: Context) {
        context.getSharedPreferences("config", Context.MODE_PRIVATE).apply {
            config.connectVerify = getBoolean("connectVerify", true)
            config.preventLost = getBoolean("preventLost", false)
            config.wifiPrinterPackageSize = getInt("wifiPrinterPackageSize", 1024)
            config.bluetoothPrinterPackageSize = getInt("bluetoothPrinterPackageSize", 1024)
            config.usbPrinterPackageSize = getInt("usbPrinterPackageSize", 1024)
            config.wifiPrinterSendDelay = getInt("wifiPrinterSendDelay", 100)
            config.bluetoothPrinterSendDelay = getInt("bluetoothPrinterSendDelay", 100)
            config.usbPrinterSendDelay = getInt("usbPrinterSendDelay", 0)
        }
    }


    fun saveSetting(context: Context) {
        context.getSharedPreferences("config", Context.MODE_PRIVATE).edit().apply {
            putBoolean("connectVerify", config.connectVerify)
            putBoolean("preventLost", config.preventLost)
            putInt("wifiPrinterPackageSize", config.wifiPrinterPackageSize)
            putInt("bluetoothPrinterPackageSize", config.bluetoothPrinterPackageSize)
            putInt("usbPrinterPackageSize", config.usbPrinterPackageSize)
            putInt("wifiPrinterSendDelay", config.wifiPrinterSendDelay)
            putInt("bluetoothPrinterSendDelay", config.bluetoothPrinterSendDelay)
            putInt("usbPrinterSendDelay", config.usbPrinterSendDelay)
            commit()
        }
    }


    fun setConnectVerify(boolean: Boolean) {
        config.connectVerify = boolean
    }

    fun setPreventLost(boolean: Boolean) {
        config.preventLost = boolean
    }


    fun setWifiPrinterPackageSize(size: Int) {
        config.wifiPrinterPackageSize = size
    }

    fun setBluetoothPrinterPackageSize(size: Int) {
        config.bluetoothPrinterPackageSize = size
        LogUtil.i(TAG, "setBluetoothPrinterPackageSize:$size")
    }

    fun setUsbPrinterPackageSize(size: Int) {
        config.usbPrinterPackageSize = size

    }

    fun setWifiPrinterSendDelay(delayMs: Int) {
        config.wifiPrinterSendDelay = delayMs
    }

    fun setBluetoothPrinterSendDelay(delayMs: Int) {
        config.bluetoothPrinterSendDelay = delayMs
        LogUtil.i(TAG, "setBluetoothPrinterSendDelay:$delayMs")
    }

    fun setUsbPrinterSendDelay(delayMs: Int) {
        config.usbPrinterSendDelay = delayMs

    }


}