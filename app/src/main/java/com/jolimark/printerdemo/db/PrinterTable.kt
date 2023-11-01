package com.jolimark.printerdemo.db

import com.jolimark.printerdemo.db.base.DBColumn
import com.jolimark.printerdemo.db.base.DBTable

class PrinterTable : DBTable() {

    companion object {
        const val tableName = "printer"
    }

    override fun getDbName(): String {
        return PrinterDBHelper.dbName
    }

    override fun getTableName(): String {
        return Companion.tableName
    }

    override fun getTableColumns(): List<DBColumn> {
        return mutableListOf<DBColumn>().apply {
            add(DBColumn("type", DBColumn.Type.String))
            add(DBColumn("info", DBColumn.Type.String))
        }
    }
}