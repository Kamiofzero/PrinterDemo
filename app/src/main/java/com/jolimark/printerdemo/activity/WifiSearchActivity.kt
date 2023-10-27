package com.jolimark.printerdemo.activity

import android.view.View
import android.widget.ArrayAdapter
import com.jolimark.printer.printer.JmPrinter
import com.jolimark.printer.printer.WifiPrinter
import com.jolimark.printer.trans.TransType
import com.jolimark.printer.trans.wifi.WifiUtil
import com.jolimark.printer.trans.wifi.search.DeviceInfo
import com.jolimark.printer.trans.wifi.search.SearchDeviceCallback
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.databinding.ActivityWifiSearchBinding

class WifiSearchActivity : BaseActivity<ActivityWifiSearchBinding>() {

    private lateinit var foundDevicesArrayAdapters: ArrayAdapter<String>
    private val foundDevices = ArrayList<DeviceInfo>()


    override fun onViewClick(v: View?) {
        when (v?.id) {
            R.id.btn_back -> {
                finish()
            }

            R.id.btn_search -> {
                foundDevices.clear()
                foundDevicesArrayAdapters.clear()
                wifiUtil.stopSearchPrinter()
                wifiUtil.searchPrinter(object : SearchDeviceCallback {
                    override fun deviceFound(deviceInfo: DeviceInfo?) {

                    }

                    override fun searchFinish() {
                        vb.pb.visibility = View.VISIBLE
                    }
                })
            }
        }
    }

    override fun initView() {
        vb.pb.visibility = View.INVISIBLE
        foundDevicesArrayAdapters = ArrayAdapter<String>(this, R.layout.item_bt);
        vb.foundDevices.adapter = foundDevicesArrayAdapters
        vb.foundDevices.setOnItemClickListener { _, _, position, _ ->
            var info = foundDevices[position]
            var ip = info.ip
            var port = info.port
            (JmPrinter.createPrinter(
                TransType.WIFI,
                "jolimark[$ip:$port]"
            ) as WifiPrinter).apply { setIpAndPort(ip, port.toInt()) }
            setResult(RESULT_OK)
            finish()
        }
    }

    private lateinit var wifiUtil: WifiUtil
    override fun initData() {
        wifiUtil = WifiUtil()
    }

    override fun onDestroy() {
        super.onDestroy()
        wifiUtil.stopSearchPrinter()
    }

}