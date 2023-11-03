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
    }

    override fun initData() {
        vb.swVerification.isChecked = SettingUtil.connectVerify
        vb.swPreventLost.isChecked = SettingUtil.preventLost
        vb.etWifiPackageSize.setText("${SettingUtil.wifiPrinterPackageSize}")
        vb.etWifiSendDelay.setText("${SettingUtil.wifiPrinterSendDelay}")
        vb.etBtPackageSize.setText("${SettingUtil.bluetoothPrinterPackageSize}")
        vb.etBtSendDelay.setText("${SettingUtil.bluetoothPrinterSendDelay}")
        vb.etUsbPackageSize.setText("${SettingUtil.usbPrinterPackageSize}")
        vb.etUsbSendDelay.setText("${SettingUtil.usbPrinterSendDelay}")
    }

}