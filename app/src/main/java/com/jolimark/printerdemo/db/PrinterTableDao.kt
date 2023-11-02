package com.jolimark.printerdemo.db

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.jolimark.printerdemo.PrinterDemoApp
import com.jolimark.printerdemo.db.base.TableDao

class PrinterTableDao(context: Context) : TableDao<PrinterBean>(PrinterDBHelper(context)) {


    override fun getContentValues(t: PrinterBean): ContentValues {
        return ContentValues().apply {
            put("_uuid", t._uuid)
            put("type", t.type)
            put("info", t.info)
        }
    }

    @SuppressLint("Range")
    override fun parseCursorToBean(cursor: Cursor): PrinterBean {
        return PrinterBean().apply {
            this._uuid = cursor.getString(cursor.getColumnIndex("_uuid"))
            this.type = cursor.getString(cursor.getColumnIndex("type"))
            this.info = cursor.getString(cursor.getColumnIndex("info"))
        }
    }

    override fun getTableName(): String {
        return PrinterTable.tableName
    }

    companion object {
        private var dao: PrinterTableDao? = null
        val INSTANCE: PrinterTableDao
            @Synchronized get() {
                return dao ?: PrinterTableDao(PrinterDemoApp.context).also { dao = it }
            }
    }
}