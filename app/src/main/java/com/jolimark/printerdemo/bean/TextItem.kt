package com.jolimark.printerdemo.bean

class TextItem {

    var resourceId: Int
    var printData: String

    constructor(resourceId: Int, printData: String) {
        this.resourceId = resourceId
        this.printData = printData
    }
}