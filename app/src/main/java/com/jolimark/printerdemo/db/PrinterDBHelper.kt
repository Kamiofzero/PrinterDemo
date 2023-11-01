package com.jolimark.printerdemo.db

import android.content.Context
import com.jolimark.printerdemo.db.base.BaseDBHelper
import com.jolimark.printerdemo.db.base.DBTable

class PrinterDBHelper(context: Context?) : BaseDBHelper(context, dbName, version) {
    override fun getTables(): List<DBTable> {
        val dbTable: DBTable = PrinterTable()
        return mutableListOf<DBTable>().apply { add(dbTable) }
    }

    companion object {
        var dbName = "printer.db"
        var version = 1
    }
}