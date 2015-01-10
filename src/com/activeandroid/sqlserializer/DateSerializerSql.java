package com.activeandroid.sqlserializer;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.Date;

public class DateSerializerSql implements SqlTypeSerializer<Date> {

    @Override
    public Date unpack(Cursor c, String name) {
        return new Date(c.getLong(c.getColumnIndexOrThrow(name)));
    }

    @Override
    public void pack(Date object, ContentValues cv, String name) {
        cv.put(name, object.getTime());
    }

    @Override
    public String toSql(Date object) {
        return String.valueOf(object.getTime());
    }

    @Override
    public SqlType getSqlType() {
        return SqlType.INTEGER;
    }

}
