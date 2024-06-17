package com.jolimark.printerdemo.bean

import android.util.Log
import com.jolimark.printer.util.LogUtil

class TextItem {

    var resourceId: Int
    var printData: String

    constructor(resourceId: Int, printData: String) {
        this.resourceId = resourceId
        this.printData = printData
        Log.i("tag",printData)
    }
}