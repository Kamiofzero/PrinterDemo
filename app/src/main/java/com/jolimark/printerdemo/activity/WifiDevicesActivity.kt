package com.jolimark.printerdemo.activity

import android.content.Intent
import android.view.View
import com.jolimark.printer.trans.wifi.WifiUtil
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.databinding.ActivityWifiDevicesBinding

class WifiDevicesActivity : BaseActivity<ActivityWifiDevicesBinding>(), WifiUtil.Callback {

    override fun onViewClick(v: View?) {
        when (v?.id) {
            R.id.btn_back -> {
                finish()
            }

            R.id.btn_setIp -> {
                launchActivityForResult(IpSetActivity::class.java, 1)

            }

            R.id.btn_search -> {
                launchActivityForResult(WifiSearchActivity::class.java, 2)
            }

            R.id.btn_connectAp -> {

            }

        }
    }

    private lateinit var wifiUtil: WifiUtil
    override fun initView() {

    }

    override fun initData() {
        wifiUtil = WifiUtil().apply {
            var ret = isWifiEnable(context)
            vb.btnConnectAp.visibility = if (ret) View.GONE else View.VISIBLE
            vb.btnSetIp.visibility = if (ret) View.VISIBLE else View.GONE
            vb.btnSearch.visibility = if (ret) View.VISIBLE else View.GONE

            registerReceiver(context)
            setCallback(this@WifiDevicesActivity)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun onConnectWifiAp(isConnect: Boolean) {
        vb.btnConnectAp.visibility = if (isConnect) View.GONE else View.VISIBLE
        vb.btnSetIp.visibility = if (isConnect) View.VISIBLE else View.GONE
        vb.btnSearch.visibility = if (isConnect) View.VISIBLE else View.GONE
    }
}