package com.android.anurag.notesapp;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by anurag on 10/6/16.
 */
public class DbQueries {
    public static final String TAG = "DbQueries";
    private Context context;
    private DataProvider dataProvider;
    private Cursor cursor;
    SendNoteApplication app;
    public DbQueries(Context ctx){
        this.dataProvider = new DataProvider();
        this.context =ctx;
    }

    public void deleteMessageByID(String id){
        context.getContentResolver().delete(DataProvider.CONTENT_URI_MESSAGES,
                DataProvider.COL_ID +"= ?",
                new String[]{id}
        );
    }

    public void deleteChat(String profileHandle){
        context.getContentResolver().delete(DataProvider.CONTENT_URI_MESSAGES,
                DataProvider.COL_TO+"=? OR "+DataProvider.COL_FROM+"=?",
                new String[]{profileHandle, profileHandle});
    }

    public String getMessageById(String id){
        cursor = dataProvider.query(DataProvider.CONTENT_URI_MESSAGES,
                    new String[]{DataProvider.COL_MSG},
                    DataProvider.COL_ID+" = ?",
                    new String[]{id},
                    null);
        return cursor.getString(cursor.getColumnIndex(DataProvider.COL_MSG));
    }

    public void markAsReadInDatabase(String profileEmail){
        ContentValues contentValues = new ContentValues(1);
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        contentValues.put(DataProvider.COL_READ, strDate);
        int c=context.getContentResolver().update(DataProvider.CONTENT_URI_MESSAGES,
                contentValues,
                DataProvider.COL_FROM+"=? AND "+DataProvider.COL_READ+" is null",
                new String[]{profileEmail}
        );
        Log.d(TAG, "count = "+c);
    }

}
