package com.jolimark.printerdemo.activity

import android.content.Intent
import android.view.View
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.databinding.ActivityDeviceAddBinding

class DeviceAddActivity : BaseActivity<ActivityDeviceAddBinding>() {

    override fun onViewClick(v: View?) {
        when (v?.id) {
            R.id.btn_back -> {
                finish()
            }

            R.id.btn_wifi -> {
                launchActivityForResult(WifiDevicesActivity::class.java, 1)

            }

            R.id.btn_bluetooth -> {
                launchActivityForResult(BluetoothDevicesActivity::class.java, 2)
            }

            R.id.btn_usb -> {
                launchActivityForResult(UsbDevicesActivity::class.java, 3)

            }

            R.id.btn_ble -> {
                launchActivityForResult(BleDevicesActivity::class.java, 4)

            }
        }
    }

    override fun initView() {

    }

    override fun initData() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            setResult(RESULT_OK)
            finish()
        }
    }
}