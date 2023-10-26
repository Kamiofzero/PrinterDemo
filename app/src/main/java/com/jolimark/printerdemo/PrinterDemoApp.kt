package com.jolimark.printerdemo

import android.app.Application
import android.content.Context
import com.jolimark.printer.util.LogUtil

class PrinterDemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        context = this
        LogUtil.enable_debug_log(true)
    }

    companion object {
        lateinit var context: Context
    }
}