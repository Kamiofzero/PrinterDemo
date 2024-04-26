package com.jolimark.printerdemo.activity

import android.view.View
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.databinding.ActivitySettingBinding
import com.jolimark.printerdemo.util.SettingUtil

class SettingActivity : BaseActivity<ActivitySettingBinding>() {
    override fun onViewClick(v: View?) {
        when (v?.id) {
            R.id.btn_back -> {
                finish()
            }

            R.id.btn_confirm -> {
                if (!checkConfig()) return
                SettingUtil.apply {
                    setConnectVerify(vb.swVerification.isChecked)
                    setPreventLost(vb.swPreventLost.isChecked)
                    setWifiPrinterPackageSize(
                        vb.etWifiPackageSize.text.toString().toInt()
                    )
                    setWifiPrinterSendDelay(
                        vb.etWifiSendDelay.text.toString().toInt()
                    )
                    setBluetoothPrinterPackageSize(
                        vb.etBtPackageSize.text.toString().toInt()

                    )
                    setBluetoothPrinterSendDelay(
                        vb.etBtSendDelay.text.toString().toInt()
                    )
                    setUsbPrinterPackageSize(
                        vb.etUsbPackageSize.text.toString().toInt()
                    )
                    setUsbPrinterSendDelay(
                        vb.etUsbSendDelay.text.toString().toInt()
                    )
                    setBlePrinterPackageSize(
                        vb.etBlePackageSize.text.toString().toInt()
                    )
                    setBlePrinterSendDelay(
                        vb.etBleSendDelay.text.toString().toInt()
                    )
                    setBlePrinterMTUSize(
                        vb.etBleMtu.text.toString().toInt()
                    )
                }.saveSetting(context)
                finish()
            }
        }
    }

    override fun initView() {
//        vb.etWifiPackageSize.setOnFocusChangeListener { v, hasFocus ->
//            if (!hasFocus) {
//                (v as EditText).apply {
//                    if (text.toString().isEmpty() || text.toString().toInt() < 128)
//                        setText("128")
//                }
//            }
//        }
//        vb.etWifiSendDelay.setOnFocusChangeListener { v, hasFocus ->
//            if (!hasFocus)
//                (v as EditText).apply {
//                    if (text.toString().isEmpty())
//                        setText("0")
//                }
//        }
//
//        vb.etBtPackageSize.setOnFocusChangeListener { v, hasFocus ->
//            if (!hasFocus) {
//                (v as EditText).apply {
//                    if (text.toString().isEmpty() || text.toString().toInt() < 128)
//                        setText("128")
//                }
//            }
//        }
//        vb.etBtSendDelay.setOnFocusChangeListener { v, hasFocus ->
//            if (!hasFocus)
//                (v as EditText).apply {
//                    if (text.toString().isEmpty())
//                        setText("0")
//                }
//        }
//
//        vb.etUsbPackageSize.setOnFocusChangeListener { v, hasFocus ->
//            if (!hasFocus) {
//                (v as EditText).apply {
//                    if (text.toString().isEmpty() || text.toString().toInt() < 3840)
//                        setText("3840")
//                }
//            }
//        }
//        vb.etUsbSendDelay.setOnFocusChangeListener { v, hasFocus ->
//            if (!hasFocus)
//                (v as EditText).apply {
//                    if (text.toString().isEmpty())
//                        setText("0")
//                }
//        }
//        vb.etBlePackageSize.apply {
//            setOnFocusChangeListener { v, hasFocus ->
//                if (!hasFocus) {
//                    (v as EditText).apply {
//                        if (text.toString().isEmpty() || text.toString().toInt() < 20)
//                            setText("20")
//                    }
//                }
//            }
//        }
//
//
//        vb.etBleSendDelay.setOnFocusChangeListener { v, hasFocus ->
//            if (!hasFocus)
//                (v as EditText).apply {
//                    if (text.toString().isEmpty())
//                        setText("0")
//                }
//        }
    }

    override fun initData() {
        vb.swVerification.isChecked = SettingUtil.connectVerify
        vb.swPreventLost.isChecked = SettingUtil.antiLost
        vb.etWifiPackageSize.setText("${SettingUtil.wifiPrinterPackageSize}")
        vb.etWifiSendDelay.setText("${SettingUtil.wifiPrinterSendDelay}")
        vb.etBtPackageSize.setText("${SettingUtil.bluetoothPrinterPackageSize}")
        vb.etBtSendDelay.setText("${SettingUtil.bluetoothPrinterSendDelay}")
        vb.etUsbPackageSize.setText("${SettingUtil.usbPrinterPackageSize}")
        vb.etUsbSendDelay.setText("${SettingUtil.usbPrinterSendDelay}")
        vb.etBlePackageSize.setText("${SettingUtil.blePackageSize}")
        vb.etBleSendDelay.setText("${SettingUtil.bleSendDelay}")
        vb.etBleMtu.setText("${SettingUtil.bleMtu}")

    }

    private fun checkConfig(): Boolean {
        vb.etWifiPackageSize.text.also {
            if (it.toString().isEmpty()) {
                dialog("WiFi传输分包大小不能为空")
                return false
            }
            var value = it.toString().toInt()
            if (value == 0) {
                dialog("WiFi传输分包大小不能为0")
                return false
            }
        }
        vb.etWifiSendDelay.text.also {
            if (it.toString().isEmpty()) {
                dialog("WiFi分包发送延迟不能为空")
                return false
            }
        }
        vb.etBtPackageSize.text.also {
            if (it.toString().isEmpty()) {
                dialog("蓝牙传输分包大小不能为空")
                return false
            }
            var value = it.toString().toInt()
            if (value == 0) {
                dialog("蓝牙传输分包大小不能为0")
                return false
            }
        }
        vb.etBtSendDelay.text.also {
            if (it.toString().isEmpty()) {
                dialog("蓝牙分包发送延迟不能为空")
                return false
            }
        }
        vb.etUsbPackageSize.text.also {
            if (it.toString().isEmpty()) {
                dialog("USB传输分包大小不能为空")
                return false
            }
            var value = it.toString().toInt()
            if (value == 0) {
                dialog("USB传输分包大小不能为0")
                return false
            } else if (value > 16384) {
                dialog("USB传输分包大小不超过16384")
                return false
            }
        }
        vb.etUsbSendDelay.text.also {
            if (it.toString().isEmpty()) {
                dialog("USB分包发送延迟不能为空")
                return false
            }
        }
        vb.etBleMtu.text.also {
            var value = it.toString().toInt()
            if (value == 0) {
                dialog("Ble MTU大小不能为0")
                return false
            }
        }
        vb.etBlePackageSize.text.also {
            if (it.toString().isEmpty()) {
                dialog("Ble传输分包大小不能为空")
                return false
            }
            var value = it.toString().toInt()
            var mtu = vb.etBleMtu.text.toString().toInt()
            if (value == 0) {
                dialog("Ble传输分包大小不能为0")
                return false
            } else if (value > mtu) {
                dialog("Ble传输分包大小不超过MTU大小")
                return false
            }
        }
        vb.etBleSendDelay.text.also {
            if (it.toString().isEmpty()) {
                dialog("Ble分包发送延迟不能为空")
                return false
            }
        }
        return true
    }


}