package com.jolimark.printerdemo.activity

import android.view.View
import android.widget.ArrayAdapter
import com.jolimark.printer.printer.JmPrinter
import com.jolimark.printer.trans.wifi.WifiUtil
import com.jolimark.printer.trans.wifi.search.DeviceInfo
import com.jolimark.printer.trans.wifi.search.SearchCallback
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.databinding.ActivityWifiSearchBinding
import com.jolimark.printerdemo.db.PrinterBean
import com.jolimark.printerdemo.db.PrinterTableDao

class WifiSearchActivity : BaseActivity<ActivityWifiSearchBinding>() {

    private lateinit var foundDevicesArrayAdapters: ArrayAdapter<String>
    private val foundDevices = ArrayList<DeviceInfo>()

    private var timeMark: Long = 0

    override fun onViewClick(v: View?) {
        when (v?.id) {
            R.id.btn_back -> {
                finish()
            }

            R.id.btn_search -> {
                var timeMarkTemp = System.currentTimeMillis()
                timeMark = timeMarkTemp
                vb.pb.visibility = View.VISIBLE
                foundDevices.clear()
                foundDevicesArrayAdapters.clear()
                wifiUtil.stopSearchPrinter()
                wifiUtil.searchPrinter(object : SearchCallback {

                    override fun onDeviceFound(info: DeviceInfo?) {
                        if (!foundDevices.contains(info)) {
                            foundDevices.add(info!!)
                            foundDevicesArrayAdapters!!.add(
                                """
                   ${info.ip}
                   ${info.port}
                    """.trimIndent()
                            ) // 添加找到的蓝牙设备
                            foundDevicesArrayAdapters!!.notifyDataSetChanged()
                        }
                    }

                    override fun onSearchEnd() {
                        if (timeMarkTemp == timeMark)
                            vb.pb.visibility = View.INVISIBLE
                    }

                    override fun onSearchFail(msg: String?) {
                        if (msg != null) {
                            toast(msg)
                        }
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

            var printer = JmPrinter.getWifiPrinter(ip, port.toInt())
//            var printer = JmPrinter.createPrinter(
//                TransType.WIFI,
//                "Jolimark[$ip:$port]"
//            ) as WifiPrinter
//            printer.setIpAndPort(ip, port.toInt())
            PrinterTableDao.INSTANCE.insert(PrinterBean(printer))
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
        wifiUtil.destroy()
    }

}