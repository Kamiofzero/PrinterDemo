package com.jolimark.printerdemo.activity

import android.view.View
import com.jolimark.printer.printer.JmPrinter
import com.jolimark.printer.printer.WifiPrinter
import com.jolimark.printer.trans.TransType
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.databinding.ActivityIpSetBinding

class IpSetActivity : BaseActivity<ActivityIpSetBinding>() {

    override fun onViewClick(v: View?) {
        when (v?.id) {
            R.id.btn_back -> {
                finish()
            }

            R.id.btn_confirm -> {
                var ip = vb.etIp.text.toString()
                var port = vb.etPort.text.toString()
                (JmPrinter.createPrinter(
                    TransType.WIFI,
                    "jolimark[$ip:$port]"
                ) as WifiPrinter).apply { setIpAndPort(ip, port.toInt()) }
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