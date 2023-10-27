package com.jolimark.printerdemo.activity

import android.view.View
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.databinding.ActivityWifiSearchBinding

class WifiSearchActivity : BaseActivity<ActivityWifiSearchBinding>() {

    override fun onViewClick(v: View?) {
        when (v?.id) {
            R.id.btn_back -> {
                finish()
            }

            R.id.btn_search -> {

            }
        }
    }

    override fun initView() {
    }

    override fun initData() {
    }
}