package com.jolimark.printerdemo.db.base;


import java.util.List;

public abstract class TableDao<T extends DbBean> extends BaseDao<T> {


    protected String tableName;

    public TableDao(BaseDBHelper helper) {
        super(helper);
        tableName = getTableName();
    }


    protected abstract String getTableName();

    public void insert(T t) {
        long id = create(tableName, t);
        t.id = id;
    }

    public void delete(T t) {
        this.delete(tableName, "id=?", new String[]{String.valueOf(t.id)});
    }

    public T query(long id) {
        String selection = "id=?";
        String[] selectionArgs = new String[]{String.valueOf(id)};
        List<T> list = this.get(tableName, selection, selectionArgs);
        return list.size() > 0 ? (T) list.get(0) : null;
    }

    public List<T> queryAll() {
        return this.get(tableName, null, null, null, null, null, "id ASC", null);
    }

    public int update(T t) {
        return this.update(tableName, t, "id=?", new String[]{String.valueOf(t.id)});
    }

}
