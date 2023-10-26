package com.jolimark.printerdemo.activity

import android.content.Intent
import android.view.View
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.databinding.ActivityDeviceAddBinding

class DeviceAddActivity : BaseActivity<ActivityDeviceAddBinding>() {
    private val REQUEST_WIFI = 1
    private val REQUEST_BLUETOOTH = 2
    private val REQUEST_USB = 3

    override fun onViewClick(v: View?) {
        when (v?.id) {
            R.id.btn_wifi -> {
                launchActivityForResult(WifiDevicesActivity::class.java, REQUEST_BLUETOOTH)

            }

            R.id.btn_bluetooth -> {
                launchActivityForResult(BluetoothDevicesActivity::class.java, REQUEST_BLUETOOTH)
            }

            R.id.btn_usb -> {
                launchActivityForResult(UsbDevicesActivity::class.java, REQUEST_BLUETOOTH)

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
            when (requestCode) {
                REQUEST_WIFI -> "wifi"
                REQUEST_BLUETOOTH -> "bluetooth"
                REQUEST_USB -> "usb"
                else -> ""
            }.also {
                setResult(RESULT_OK, intent.apply { putExtra("type", it) })
                finish()
            }

        }
    }
}