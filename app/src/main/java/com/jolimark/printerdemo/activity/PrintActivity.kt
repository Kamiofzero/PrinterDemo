package com.jolimark.printerdemo.activity

import android.view.View
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.databinding.ActivityPrintBinding

class PrintActivity : BaseActivity<ActivityPrintBinding>() {

    override fun onViewClick(v: View?) {
        when (v?.id) {
            R.id.btn_printText -> {}
            R.id.btn_printImage -> {}
            R.id.btn_printPrn -> {}
            R.id.btn_printFile -> {}
        }
    }

    override fun initView() {
    }

    override fun initData() {
    }




}