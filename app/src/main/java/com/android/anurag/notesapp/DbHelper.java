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
        String CREATE_MSG_TABLE= "create table "+DataProvider.TABLE_MESSAGES +
                "("+DataProvider.COL_ID + " integer primary key autoincrement," +
                DataProvider.COL_MSG + " text," +
                DataProvider.COL_FROM + " text," +
                DataProvider.COL_TO + " text," +
                DataProvider.COL_AT + " datetime default current_timestamp," +
                DataProvider.COL_SENT + " datetime default null," +
                DataProvider.COL_DELIVERED + " datetime default null," +
                DataProvider.COL_READ + " datetime default null)" +
                ";";

        String CREATE_PROFILE_TABLE="create table " + DataProvider.TABLE_PROFILE +
                "(" + DataProvider.COL_ID + " integer primary key autoincrement," +
                DataProvider.COL_USER_NAME + "  text," +
                DataProvider.COL_USER_ID + " text unique," +
                DataProvider.COL_MSG_COUNT + " integer default 0)" +
                ";";

        db.execSQL(CREATE_MSG_TABLE);
        db.execSQL(CREATE_PROFILE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    //nothing to do here for now...
    }
}
