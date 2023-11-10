package com.jolimark.printerdemo.bean

import android.graphics.Bitmap

class ImgItem {
    var resourceId: Int
    var printData: Bitmap

    constructor(resourceId: Int, printData: Bitmap) {
        this.resourceId = resourceId
        this.printData = printData
    }

}