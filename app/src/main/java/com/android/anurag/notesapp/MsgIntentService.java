package com.android.anurag.notesapp;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.android.anurag.notesapp.gcm.ServerUtilities;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MsgIntentService extends IntentService {
    private ChatActivity chatActivity;
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private static final String TAG="MsgIntentService";
    private static final String ACTION_SEND_IN_BACKGROUND="action.SEND_IN_BACKGROUND";
    private String MSG_TO, MSG_FROM, MSG, COL_ID;

    public MsgIntentService() {
        super("MsgIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(TAG, "Service Started by BroadcastReceiver!");
        dbHelper= new DbHelper(getApplicationContext());
        db = dbHelper.getReadableDatabase();
        if (intent != null) {
            final String action = intent.getAction();
            if(action.equals(ACTION_SEND_IN_BACKGROUND)){
                tryToSend();
            }
        }
    }

    private void tryToSend(){
        Cursor cursor = getPendingMessages();
        Log.d(TAG, "cursor length: "+cursor.getCount());

        while(cursor.moveToNext()){
            MSG=cursor.getString(cursor.getColumnIndex(DataProvider.COL_MSG));
            MSG_FROM= cursor.getString(cursor.getColumnIndex(DataProvider.COL_FROM));
            MSG_TO= cursor.getString(cursor.getColumnIndex(DataProvider.COL_TO));
            COL_ID= cursor.getString(cursor.getColumnIndex(DataProvider.COL_ID));
            ServerUtilities  SU= new ServerUtilities();
            String k= "k";
            try {
                SU.send(getApplicationContext(), MSG, MSG_TO, COL_ID);
            }catch (IOException ex){
                ex.printStackTrace();
            }

          //  chatActivity = new ChatActivity();
          //  chatActivity.sendFromBackGround(MSG, MSG_TO, COL_ID);
        }
        cursor.close();
    }

    private Cursor getPendingMessages(){
       return db.rawQuery("SELECT * FROM messages WHERE sent is null AND email2 is not null ORDER BY _id ASC", null);
     /*   return db.query(
                DataProvider.TABLE_MESSAGES,
                null,
                DataProvider.COL_SENT+"= ?",
                new String[]{"is null"},
                null,
                null,
                DataProvider.COL_ID + " DESC"
        );*/

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public boolean hasActiveInternetConnection(Context context) {
        if (isNetworkAvailable()) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                Log.e(TAG, "Error checking internet connection", e);
            }
        } else {
            Log.d(TAG, "No network available!");
        }
        return false;
    }
}
