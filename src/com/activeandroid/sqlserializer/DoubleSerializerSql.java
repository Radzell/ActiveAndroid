package com.activeandroid.sqlserializer;

import android.content.ContentValues;
import android.database.Cursor;

public class DoubleSerializerSql implements SqlTypeSerializer<Double> {

    @Override
    public Double unpack(Cursor c, String name) {
        return c.getDouble(c.getColumnIndexOrThrow(name));
    }

    @Override
    public void pack(Double object, ContentValues cv, String name) {
        cv.put(name, object);
    }

    @Override
    public String toSql(Double object) {
        return String.valueOf(object);
    }

    @Override
    public SqlType getSqlType() {
        return SqlType.REAL;
    }

}
