package com.jolimark.printerdemo.activity

import android.view.View
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.databinding.ActivityPrintBinding

class PrintActivity : BaseActivity<ActivityPrintBinding>() {
    override fun onViewClick(v: View?) {
        when (v?.id) {
            R.id.btn_back -> {
                finish()
            }

            R.id.btn_printText -> {
                launchActivity(PrintTextActivity::class.java)
            }

            R.id.btn_printImage -> {
                launchActivity(PrintImgActivity::class.java)
            }

            R.id.btn_printPrn -> {
            }

            R.id.btn_printFile -> {
                launchActivity(PrintFileActivity::class.java)

            }
        }
    }

    override fun initView() {
    }

    override fun initData() {
    }
}