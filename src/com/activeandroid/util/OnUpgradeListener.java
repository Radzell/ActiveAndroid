package com.activeandroid.util;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by radzell on 11/18/14.
 */
public interface OnUpgradeListener {
    void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
}
