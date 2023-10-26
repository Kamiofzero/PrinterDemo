package com.jolimark.printerdemo.activity

import android.content.Intent
import android.view.View
import com.jolimark.printer.printer.JmPrinter
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val REQUEST_DEVICES = 1
    override fun onViewClick(v: View?) {
        when (v?.id) {
            R.id.btn_print -> {
                if (!checkDeviceSetting()) {
                    dialog(getString(R.string.tip_set_device_first))
                    return
                }
                launchActivity(PrintActivity::class.java)
            }

            R.id.btn_devices -> {
                launchActivityForResult(DeviceAddActivity::class.java, REQUEST_DEVICES)
            }
        }

    }


    private fun checkDeviceSetting(): Boolean {
        return !JmPrinter.isDevicesEmpty()
    }

    override fun initView() {
        vb.btnPrint.icon = getDrawable(R.mipmap.forbid)
    }

    override fun initData() {
    }

    override fun onDestroy() {
        super.onDestroy()
        JmPrinter.release()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
//            REQUEST_DEVICES -> {
//                if (resultCode == RESULT_OK) {
//                    data?.getStringExtra("type")?.also { s ->
//                        var str = when (s) {
//                            "wifi" ->
//                                "${getString(R.string.device)}\n[${WifiPrinter.getInstance().address}]"
//
//                            "bluetooth" ->
//                                "${getString(R.string.device)}\n[${BluetoothPrinter.getInstance().address}]"
//
//
//                            "usb" ->
//                                "${getString(R.string.device)}\n[${UsbPrinter.getInstance().usbDevice?.let { "${it.vendorId},${it.productId}" }}]"
//
//                            else -> ""
//                        }
//                        vb.btnDevices.text = str
//                    }
//                    vb.btnPrint.icon = getDrawable(R.mipmap.allow)
//
//                }
//            }
        }
    }

}