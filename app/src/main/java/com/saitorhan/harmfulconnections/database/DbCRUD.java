package com.saitorhan.harmfulconnections.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.saitorhan.harmfulconnections.R;

public class DbCRUD {
    public SQLiteDatabase database;
    DbProcessor dbProcessor;

    public DbCRUD(Context context, boolean writable) {
        int dbVersion = Integer.parseInt(context.getString(R.string.dbVersion));

        dbProcessor = new DbProcessor(context, context.getString(R.string.dbName), null, dbVersion);
        database = writable ? dbProcessor.getWritableDatabase() : dbProcessor.getReadableDatabase();
    }
}
