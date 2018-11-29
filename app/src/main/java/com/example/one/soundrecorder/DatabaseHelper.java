package com.example.one.soundrecorder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DBName = "recorder.db";
    private static final String TBName = "recorder_info";

    private static final String CREATE_TABLE = "create table " + TBName + "(id integer primary key autoincrement,createtime integer, duration integer, filename VARCHAR(255), filepath VARCHAR(255), size integer)";



    public DatabaseHelper(Context context, int version) {
        super(context, DBName, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
