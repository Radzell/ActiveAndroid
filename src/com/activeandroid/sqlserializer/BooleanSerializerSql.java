package com.activeandroid.sqlserializer;

import android.content.ContentValues;
import android.database.Cursor;

public class BooleanSerializerSql implements SqlTypeSerializer<Boolean> {

    @Override
    public Boolean unpack(Cursor c, String name) {
        return c.getInt(c.getColumnIndexOrThrow(name)) > 0;
    }

    @Override
    public void pack(Boolean object, ContentValues cv, String name) {
        cv.put(name, object ? 1 : 0);
    }

    @Override
    public String toSql(Boolean object) {
        return object ? "1" : "0";
    }

    @Override
    public SqlType getSqlType() {
        return SqlType.INTEGER;
    }

}
