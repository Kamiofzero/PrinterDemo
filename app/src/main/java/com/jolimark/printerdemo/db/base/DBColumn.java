package com.jolimark.printerdemo.db.base;

public class DBColumn {
    String name;
    Type type;

    public DBColumn(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public enum Type {
        Int, String, Float
    }


}
