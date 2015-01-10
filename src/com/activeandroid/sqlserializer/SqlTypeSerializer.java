package com.activeandroid.sqlserializer;

import android.content.ContentValues;
import android.database.Cursor;

public interface SqlTypeSerializer<T> {

    T unpack(Cursor c, String name);

    void pack(T object, ContentValues cv, String name);

    String toSql(T object);

    SqlType getSqlType();

}
