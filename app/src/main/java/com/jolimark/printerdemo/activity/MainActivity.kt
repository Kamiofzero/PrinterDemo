package com.jolimark.printerdemo.activity

import android.content.Intent
import android.view.View
import com.jolimark.printer.printer.JmPrinter
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.databinding.ActivityMainBinding
import com.jolimark.printerdemo.db.PrinterTableDao
import com.jolimark.printerdemo.util.SettingUtil

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
                launchActivityForResult(DevicesActivity::class.java, REQUEST_DEVICES)
            }

            R.id.btn_setting -> {
                launchActivity(SettingActivity::class.java)
            }

            R.id.btn_about -> {
                packageManager.getPackageInfo(packageName, 0).apply {

                    dialog(getString(R.string.version) + ":${this.versionName}\r\n")
                }
            }
        }

    }


    private fun checkDeviceSetting(): Boolean {
        return !JmPrinter.isDevicesEmpty()
    }

    override fun initView() {

    }

    override fun initData() {
        loadPrinters()
        SettingUtil.loadSetting(context)
    }

    private fun loadPrinters() {
        var list = PrinterTableDao.INSTANCE.queryAll()
        list.forEach {
            JmPrinter.addPrinter(it.toPrinter())
        }
        vb.btnPrint.icon =
            if (list.size > 0) getDrawable(R.mipmap.allow) else getDrawable(R.mipmap.forbid)
    }

    override fun onDestroy() {
        super.onDestroy()
        JmPrinter.release()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_DEVICES -> {
                if (!JmPrinter.isDevicesEmpty())
                    vb.btnPrint.icon = getDrawable(R.mipmap.allow)

            }
        }
    }

}