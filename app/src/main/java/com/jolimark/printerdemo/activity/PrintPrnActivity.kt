package com.jolimark.printerdemo.activity

import android.view.View
import com.jolimark.printer.callback.Callback
import com.jolimark.printer.printer.BasePrinter
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.databinding.ActivityPrintPrnBinding
import java.io.IOException
import java.io.InputStream

class PrintPrnActivity : PrintBaseActivity<ActivityPrintPrnBinding>() {

    private var prn: String? = null
    override fun onPrinterSelect(printer: BasePrinter) {
        showProgress(getString(R.string.tip_printing))
        var `is`: InputStream? = null
        var bytes: ByteArray? = null
        try {
            `is` = assets.open(prn!!)
            bytes = ByteArray(`is`.available())
            `is`.read(bytes)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            `is`?.close()
        }

        printer.print(bytes, object : Callback {
            override fun onSuccess() {
                hideProgress()
            }

            override fun onFail(code: Int, msg: String) {
                hideProgress()
                if (printer.isAntiMode) {
                    showAntiLossRetryDialog(printer, msg)
                } else {
                    toast(msg)
                }
            }
        })
    }


    override fun onViewClick(v: View?) {
        when (v?.id) {
            R.id.btn_back -> {
                finish()
            }

            R.id.btn_print -> {
                prn = "110.prn"
                selectPrinter()
            }

            R.id.btn_print2 -> {
                prn = "TSPL.prn"
                selectPrinter()
            }
        }
    }

    override fun initView() {
    }

    override fun initData() {

    }
}