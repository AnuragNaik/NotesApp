package com.android.anurag.notesapp;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.anurag.notesapp.gcm.GcmUtil;
import com.android.anurag.notesapp.gcm.ServerUtilities;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by anurag on 24/2/16.
 */
public class ChatActivity extends FragmentActivity implements MessagesFragment.OnFragmentInteractionListener {

    private EditText msgEdit;
    private Button sendBtn;
    private String profileId, profileName, profileEmail;
    private GcmUtil gcmUtil;
    String TAG="mainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);
        Log.d(TAG, "Common.PROFILE_ID= " + Common.PROFILE_ID);
        Log.d("MessagesFragment", "ChatActivity()" );
		//got this profile Id (which is just index _id field in DB)  from intent. Since this activity launched by clicking the contact in main activity
        profileId = getIntent().getStringExtra(Common.PROFILE_ID);

        msgEdit = (EditText) findViewById(R.id.msg_edit);//Entered Message
        sendBtn = (Button) findViewById(R.id.send_btn);	//Send bttn

        sendBtn.setOnClickListener(new View.OnClickListener() {	//if Send button clicked
            @Override
            public void onClick(View v) {
                if(isOnline()){
                    send(msgEdit.getText().toString());			//send the message
                    msgEdit.setText(null);						//set the message field null in UI
                }else{
                    Toast.makeText(ChatActivity.this, "Check Internet Connection!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ActionBar actionBar = this.getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Cursor c = getContentResolver().query(Uri.withAppendedPath(DataProvider.CONTENT_URI_PROFILE, profileId), null, null, null, null);
        if (c.moveToFirst()) {
            profileName = c.getString(c.getColumnIndex(DataProvider.COL_NAME));
            profileEmail = c.getString(c.getColumnIndex(DataProvider.COL_EMAIL));
            Common.setCurrentChat(profileEmail);
            actionBar.setTitle(profileName);
        }
        c.close();

      //  actionBar.setSubtitle("connecting ...");

        registerReceiver(registrationStatusReceiver, new IntentFilter(Common.ACTION_REGISTER));
        gcmUtil = new GcmUtil(getApplicationContext());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public String getProfileEmail() {
        return profileEmail;
    }

    @Override
    protected void onPause() {
        //reset new messages count
        ContentValues values = new ContentValues(1);
        values.put(DataProvider.COL_COUNT, 0);
        getContentResolver().update(Uri.withAppendedPath(DataProvider.CONTENT_URI_PROFILE, profileId), values, null, null);
        super.onPause();
    }

    private void send(final String txt) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {

                    /**
                     * Insert the message in messages table
                     */

                    ContentValues values = new ContentValues(3);
                    values.put(DataProvider.COL_MSG, txt);
                    values.put(DataProvider.COL_TO, getProfileEmail());

                    /**
                     * After insertion in Database insert() will return Uri of newly added tuple as base_uri/id
                     * so we need to fetch lastPathSegment() to get id from resultUri
                     */

                    Uri resultUri=getContentResolver().insert(DataProvider.CONTENT_URI_MESSAGES, values);
                    String id= resultUri.getLastPathSegment();
                    Log.d(TAG, "insertUri= "+resultUri+" ,id= " +id);

                    /**
                     * Now sending message Id with payload to track the delivery and status of message
                     */

                    ServerUtilities SU= new ServerUtilities();
                    SU.send(getApplicationContext(),txt, getProfileEmail(), id);

                } catch (IOException ex) {
                    msg = "Message could not be sent";
                }
                /**
                 * this 'msg' will be returned to onPostExecute(String args)
                 * returning the msg
                 */
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                if (!TextUtils.isEmpty(msg)) {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                }
            }
        }.execute(null, null, null);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(registrationStatusReceiver);
        gcmUtil.cleanup();
        super.onDestroy();
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private BroadcastReceiver registrationStatusReceiver = new  BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && Common.ACTION_REGISTER.equals(intent.getAction())) {
                switch (intent.getIntExtra(Common.EXTRA_STATUS, 100)) {
                    case Common.STATUS_SUCCESS:
                        getActionBar().setSubtitle("online");
                        break;

                    case Common.STATUS_FAILED:
                        getActionBar().setSubtitle("offline");
                        break;
                }
            }
        }
    };

    public boolean isOnline() {

        Runtime runtime = Runtime.getRuntime();
        try {

            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }


}

