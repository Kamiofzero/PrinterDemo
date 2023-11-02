package com.jolimark.printerdemo.db.base

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

abstract class BaseDao<T>(private val helper: SQLiteOpenHelper) {
    private fun openReader(): SQLiteDatabase {
        return helper.readableDatabase
    }

    private fun openWriter(): SQLiteDatabase {
        return helper.writableDatabase
    }

    private fun closeDatabase(database: SQLiteDatabase?, cursor: Cursor?) {
        if (cursor != null && !cursor.isClosed) {
            cursor.close()
        }
        if (database != null && database.isOpen) {
            database.close()
        }
    }

    fun count(tableName: String): Int {
        return countColumn(tableName, "id")
    }

    fun countColumn(tableName: String, columnName: String): Int {
        val sql = "SELECT COUNT(?) FROM $tableName"
        val database = openReader()
        var cursor: Cursor? = null
        try {
            database.beginTransaction()
            cursor = database.rawQuery(sql, arrayOf(columnName))
            var count = 0
            if (cursor.moveToNext()) {
                count = cursor.getInt(0)
            }
            database.setTransactionSuccessful()
            return count
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            database.endTransaction()
            closeDatabase(database, cursor)
        }
        return 0
    }

    fun deleteAll(tableName: String): Int {
        return delete(tableName, null, null)
    }

    fun delete(tableName: String, whereClause: String?, whereArgs: Array<String?>?): Int {
        val database = openWriter()
        try {
            database.beginTransaction()
            val result = database.delete(tableName, whereClause, whereArgs)
            database.setTransactionSuccessful()
            return result
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            database.endTransaction()
            closeDatabase(database, null )
        }
        return 0
    }


    fun getAll(tableName: String): ArrayList<T> {
        return get(tableName, null, null)
    }

    fun get(tableName: String, selection: String?, selectionArgs: Array<String?>?): ArrayList<T> {
        return get(tableName, null, selection, selectionArgs, null, null, null, null)
    }

    fun get(
        tableName: String,
        columns: Array<String?>?,
        selection: String?,
        selectionArgs: Array<String?>?,
        groupBy: String?,
        having: String?,
        orderBy: String?,
        limit: String?
    ): ArrayList<T> {
        val database = openReader()
        val list = ArrayList<T>()
        var cursor: Cursor? = null
        try {
            database.beginTransaction()
            cursor = database.query(
                tableName,
                columns,
                selection,
                selectionArgs,
                groupBy,
                having,
                orderBy,
                limit
            )
            while (!cursor.isClosed && cursor.moveToNext()) {
                list.add(parseCursorToBean(cursor))
            }
            database.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            database.endTransaction()
            closeDatabase(database, cursor)
        }
        return list
    }

    fun replace(tableName: String, t: T): Long {
        val database = openWriter()
        try {
            database.beginTransaction()
            val id = database.replace(tableName, null as String?, getContentValues(t))
            database.setTransactionSuccessful()
            return id
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            database.endTransaction()
            closeDatabase(database, null)
        }
        return 0L
    }

    fun create(tableName: String, t: T): Long {
        val database = openWriter()
        try {
            database.beginTransaction()
            val id = database.insert(tableName, null , getContentValues(t))
            database.setTransactionSuccessful()
            return id
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            database.endTransaction()
            closeDatabase(database, null)
        }
        return 0L
    }

    fun update(tableName: String, t: T, whereClause: String?, whereArgs: Array<String?>?): Int {
        val database = openWriter()
        try {
            database.beginTransaction()
            //根据列条件筛选出项，再根据ContentValue的键值对逐一对应修改该项的列
            val count = database.update(tableName, getContentValues(t), whereClause, whereArgs)
            database.setTransactionSuccessful()
            return count
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            database.endTransaction()
            closeDatabase(database, null )
        }
        return 0
    }

    abstract fun parseCursorToBean(cursor: Cursor): T
    abstract fun getContentValues(t: T): ContentValues
}