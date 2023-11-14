package com.jolimark.printerdemo.activity

import android.content.Intent
import android.view.View
import com.jolimark.printer.callback.Callback
import com.jolimark.printer.printer.BasePrinter
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.databinding.ActivityPrintFileBinding
import com.jolimark.printerdemo.printContent.PrintContent
import java.io.File

class PrintFileActivity : PrintBaseActivity<ActivityPrintFileBinding>() {


    override fun onPrinterSelect(printer: BasePrinter) {
        print(selectFilePath!!, printer)
    }

    override fun onViewClick(v: View?) {
        when (v?.id) {
            R.id.btn_back -> {
                finish()
            }

            R.id.btn_selectFile -> {
                launchActivityForResult(SelectFileActivity::class.java, 1)
            }

            R.id.btn_print -> {
                if (selectFilePath.isNullOrEmpty())
                    toast(getString(R.string.tip_noSelectFile))
                else
                    selectPrinter()
            }
        }
    }

    override fun initView() {

    }

    private fun print(filePath: String, basePrinter: BasePrinter) {
        if (filePath.endsWith(".prn")) {
            printPrn(filePath, basePrinter)
        } else if (filePath.endsWith(".txt")) {
            printText(filePath, basePrinter)
        } else if (filePath.endsWith(".png", true)
            || filePath.endsWith(".jpg", true)
            || filePath.endsWith(".jpeg", true)
        ) {
            printImg(filePath, basePrinter)
        }
    }

    private fun printText(filePath: String, printer: BasePrinter) {
        showProgress(getString(R.string.tip_printing))
        PrintContent.getLocalText(filePath)?.apply {
            printer.printText(this, object : Callback {
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
    }

    private fun printPrn(filePath: String, printer: BasePrinter) {
        showProgress(getString(R.string.tip_printing))
        PrintContent.getLocalPrn(filePath)?.apply {
            printer.print(this, object : Callback {
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
    }

    private fun printImg(filePath: String, printer: BasePrinter) {
        showProgress(getString(R.string.tip_printing))
        PrintContent.getLocalBitmap(filePath)?.apply {
            printer.printImg(this, object : Callback {
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
    }


    override fun initData() {
        vb.tvSelected.text = getString(R.string.tip_noSelectFile)
    }

    private var selectFilePath: String? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            selectFilePath = data?.getStringExtra("file")?.apply {
                File(this).apply {
                    if (exists()) vb.tvSelected.text = name
                }
            }
        }
    }

}