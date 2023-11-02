package com.jolimark.printerdemo.db.base;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.List;

public abstract class BaseDBHelper extends SQLiteOpenHelper {

    public BaseDBHelper(@Nullable Context context, @Nullable String name, int version) {
        super(context, name, null, version);
    }

    protected abstract List<DBTable> getTables();

    @Override
    public void onCreate(SQLiteDatabase db) {
        List<DBTable> list = getTables();
        for (DBTable table : list) {
            createTable(db, table);
        }
    }

    private void createTable(SQLiteDatabase db, DBTable table) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE " + table.tableName + "(");
        sb.append(" _id INTEGER PRIMARY KEY AUTOINCREMENT");
        sb.append(",");
        sb.append("_uuid TEXT");
        List<DBColumn> list = table.columnList;
        for (DBColumn dbColumn : list) {
            sb.append(",");
            sb.append(dbColumn.name + " ");
            switch (dbColumn.type) {
                case Int: {
                    sb.append("INTEGER");
                    break;
                }
                case String: {
                    sb.append("TEXT");
                    break;
                }
                case Float: {
                    sb.append("REAL");
                    break;
                }
            }
        }
        sb.append(")");
        db.execSQL(sb.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
