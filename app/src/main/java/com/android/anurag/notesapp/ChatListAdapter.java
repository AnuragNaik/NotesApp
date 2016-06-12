package com.android.anurag.notesapp;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.media.MediaBrowserCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

/**
 * Created by anurag on 11/6/16.
 */
public class ChatListAdapter extends CursorAdapter {


    public ChatListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }
}
