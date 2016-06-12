package com.android.anurag.notesapp;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.android.anurag.notesapp.gcm.GcmUtil;

public class MainActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private final String TAG="mainActivity";
    private SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context=this;

        Intent ii= new Intent(this, AlertDialog.class );
       // startActivity(ii);
        context.sendBroadcast(new Intent("com.google.android.intent.action.GTALK_HEARTBEAT"));
        context.sendBroadcast(new Intent("com.google.android.intent.action.MCS_HEARTBEAT"));

        GcmUtil gcmUtil= new GcmUtil(this);
        if(gcmUtil.getRegistrationId(this).equals("")){
            Intent i= new Intent(this, RegistrationActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }

        adapter = new SimpleCursorAdapter(this,
                R.layout.main_list_item,
                null,
                new String[]{DataProvider.COL_USER_NAME, DataProvider.COL_MSG_COUNT},
                new int[]{R.id.text1, R.id.text2},
                0);
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

                switch (view.getId()) {
                    case R.id.text2:
                        int count = cursor.getInt(columnIndex);
                        if (count > 0) {
                            ((TextView) view).setText(String.format("%d new message%s", count, count == 1 ? "" : "s"));
                            Log.i(TAG, "setView ActivityMain");
                        }
                        return true;
                }
                return false;
            }
        });
        setListAdapter(adapter);

        //  ActionBar actionBar = getActionBar();
        //actionBar.setDisplayShowTitleEnabled(true);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Log.d(TAG, "ListView= " + String.valueOf(l) + " view=" + String.valueOf(v) + " position= " + position + " id= " + String.valueOf(id));
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(SendNoteApplication.PROFILE_ID, String.valueOf(id));
        Log.d(TAG, "SendNoteApplication.PROFILE_ID= "+String.valueOf(id));
        this.startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(this,
                DataProvider.CONTENT_URI_PROFILE,
                new String[]{DataProvider.COL_ID, DataProvider.COL_USER_NAME, DataProvider.COL_MSG_COUNT},
                null,
                null,
               DataProvider.COL_ID + " DESC");
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    //setting menu options.....
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                AddContactDialog dialog = new AddContactDialog();
                dialog.show(getFragmentManager(), "AddContactDialog");
                return true;

            case R.id.action_settings:
           //     Intent intent = new Intent(this, SettingsActivity.class);
           //     startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}