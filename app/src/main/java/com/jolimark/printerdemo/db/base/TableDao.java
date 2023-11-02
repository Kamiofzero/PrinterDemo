package com.jolimark.printerdemo.db.base;


import com.jolimark.printer.util.LogUtil;

import java.util.List;

public abstract class TableDao<T extends DbBean> extends BaseDao<T> {
    private String TAG = getClass().getSimpleName();
    protected String tableName;

    public TableDao(BaseDBHelper helper) {
        super(helper);
        tableName = getTableName();
    }


    protected abstract String getTableName();

    public void insert(T t) {
        LogUtil.i(TAG, "insert " + t.toString());
        create(tableName, t);
    }

    public void delete(T t) {
        LogUtil.i(TAG, "delete " + t.toString());
        this.delete(tableName, "_uuid=?", new String[]{t._uuid});
    }

    public T query(String uuid) {
        String selection = "_uuid=?";
        String[] selectionArgs = new String[]{uuid};
        List<T> list = this.get(tableName, selection, selectionArgs);
        return list.size() > 0 ? (T) list.get(0) : null;
    }

    public List<T> queryAll() {
        LogUtil.i(TAG, "queryAll ");
        List<T> list = this.get(tableName, null, null, null, null, null, null, null);
        for (T t : list) {
            LogUtil.i(TAG, t.toString());
        }
        return list;
    }

    public int update(T t) {
        return this.update(tableName, t, "_uuid=?", new String[]{t._uuid});
    }

}
