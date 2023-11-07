package com.jolimark.printerdemo.activity

import android.view.View
import android.widget.EditText
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
                }.saveSetting(context)
                finish()
            }
        }
    }

    override fun initView() {
        vb.etWifiPackageSize.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                (v as EditText).apply {
                    if (text.toString().isEmpty() || text.toString().toInt() < 512)
                        setText("512")
                }
            }
        }
        vb.etWifiSendDelay.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus)
                (v as EditText).apply {
                    if (text.toString().isEmpty())
                        setText("0")
                }
        }

        vb.etBtPackageSize.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                (v as EditText).apply {
                    if (text.toString().isEmpty() || text.toString().toInt() < 512)
                        setText("512")
                }
            }
        }
        vb.etBtSendDelay.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus)
                (v as EditText).apply {
                    if (text.toString().isEmpty())
                        setText("0")
                }
        }

        vb.etUsbPackageSize.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                (v as EditText).apply {
                    if (text.toString().isEmpty() || text.toString().toInt() < 3840)
                        setText("3840")
                }
            }
        }
        vb.etUsbSendDelay.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus)
                (v as EditText).apply {
                    if (text.toString().isEmpty())
                        setText("0")
                }
        }
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
    }

}