package com.jolimark.printerdemo.activity

import android.view.View
import com.jolimark.printer.printer.JmPrinter
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.databinding.ActivityIpSetBinding
import com.jolimark.printerdemo.db.PrinterBean
import com.jolimark.printerdemo.db.PrinterTableDao

class IpSetActivity : BaseActivity<ActivityIpSetBinding>() {

    override fun onViewClick(v: View?) {
        when (v?.id) {
            R.id.btn_back -> {
                finish()
            }

            R.id.btn_confirm -> {
                var ip = vb.etIp.text.toString()
                var port = vb.etPort.text.toString()
//                var printer = JmPrinter.createPrinter(
//                    TransType.WIFI,
//                    "jolimark[$ip:$port]"
//                ) as WifiPrinter
//                printer.setIpAndPort(ip, port.toInt())

                var printer = JmPrinter.getWifiPrinter(ip, port.toInt())
                PrinterTableDao.INSTANCE.insert(PrinterBean(printer))
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    override fun initView() {
    }

    override fun initData() {
    }
}