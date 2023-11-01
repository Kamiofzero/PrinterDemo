package com.jolimark.printerdemo.db.base;

import java.util.List;

public abstract class DBTable {
    public String dbName;
    public String tableName;
    public List<DBColumn> columnList;

    protected abstract String getDbName();

    protected abstract String getTableName();

    protected abstract List<DBColumn> getTableColumns();

    public DBTable() {
        dbName = getDbName();
        tableName = getTableName();
        columnList = getTableColumns();
    }
}
