package com.saitorhan.harmfulconnections.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbProcessor extends SQLiteOpenHelper {
    public DbProcessor(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE urls(_id TEXT PRIMARY KEY, url TEXT, description TEXT, source TEXT,description_default TEXT)");
        sqLiteDatabase.execSQL("CREATE TABLE xmlinfo(_id TEXT PRIMARY KEY, updated TEXT, author TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (newVersion <= oldVersion) {
            return;
        }

        if (oldVersion == 1) {
            sqLiteDatabase.execSQL("ALTER TABLE urls ADD description_default TEXT");
        }
    }
}
