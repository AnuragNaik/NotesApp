package com.android.anurag.notesapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by anurag on 23/2/16.
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "notesapp.db";
    private static final int DATABASE_VERSION = 1;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        //getting tables  created and initialized
        String CREATE_MSG_TABLE= "create table messages " +
                "(_id integer primary key autoincrement," +
                " msg text," +
                " email text," +
                " email2 text," +
                " at datetime default current_timestamp," +
                " sent datetime default null," +
                " delivered datetime default null," +
                " read datetime default null)" +
                ";";

        String CREATE_PROFILE_TABLE="create table profile " +
                "(_id integer primary key autoincrement," +
                " name text," +
                " email text unique," +
                " count integer default 0)" +
                ";";

        db.execSQL(CREATE_MSG_TABLE);
        db.execSQL(CREATE_PROFILE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    //nothing to do here for now...
    }
}
