package com.android.anurag.notesapp;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.anurag.notesapp.gcm.GcmUtil;
import com.android.anurag.notesapp.gcm.ServerUtilities;

import java.io.IOException;

/**
 * Created by anurag on 24/2/16.
 */
public class ChatActivity extends FragmentActivity implements MessagesFragment.OnFragmentInteractionListener {

    private EditText msgEdit;
    private Button sendBtn;
    private String profileId, profileName, profileEmail;
    private GcmUtil gcmUtil;
    private ServerUtilities SU;
    private ContentResolver cr;
    private SendNoteApplication app;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Menu menu;
    private DbQueries dbQueries;
    String TAG = "ChatActivity";

    public ChatActivity(){
    }

    public void init(){
        app = (SendNoteApplication) getApplicationContext();
        app.setChatActivity(this);
        sharedPreferences = getSharedPreferences(getPackageName(),Context.MODE_PRIVATE);
        dbQueries = app.getDbQueries();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setContentView(R.layout.activity_chat);
        Log.d(TAG, "SendNoteApplication.PROFILE_ID= " + SendNoteApplication.PROFILE_ID);

        //got this profile Id (which is just index _id field in DB)  from intent. Since this activity launched by clicking the contact in main activity
        profileId = getIntent().getStringExtra(SendNoteApplication.PROFILE_ID);
        cr= getApplicationContext().getContentResolver();
        msgEdit = (EditText) findViewById(R.id.msg_edit);//Entered Message
        sendBtn = (Button) findViewById(R.id.send_btn);    //Send bttn

        sendBtn.setOnClickListener(new View.OnClickListener() {    //if Send button clicked
            @Override
            public void onClick(View v) {
                if (!msgEdit.getText().toString().equals("")) {
                    Log.d(TAG, "Sending msg");
                    InsertInDatabaseAndSend(msgEdit.getText().toString(), getProfileEmail());            //send the message
                    msgEdit.setText(null);                        //set the message field null in UI
                } else {
                    Toast.makeText(ChatActivity.this, "Type in your message", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ActionBar actionBar = this.getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Cursor c = cr.query(Uri.withAppendedPath(DataProvider.CONTENT_URI_PROFILE, profileId), null, null, null, null);
        if (c.moveToFirst()) {
            profileName = c.getString(c.getColumnIndex(DataProvider.COL_USER_NAME));
            profileEmail = c.getString(c.getColumnIndex(DataProvider.COL_USER_ID));
            SendNoteApplication.setCurrentChat(profileEmail);
            actionBar.setTitle(profileName);
        }
        c.close();
        dbQueries.markAsReadInDatabase(profileEmail);
        registerReceiver(registrationStatusReceiver, new IntentFilter(SendNoteApplication.ACTION_REGISTER));
        gcmUtil = new GcmUtil(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        this.menu = menu;
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        initChatModeMenuItem();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.action_mode:
                if(sharedPreferences.getBoolean("IS_NOTE_MODE", false)) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("IS_NOTE_MODE", false);
                    editor.apply();
                    Toast.makeText(this, "Note Mode Disabled", Toast.LENGTH_SHORT).show();
                }
                else{
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("IS_NOTE_MODE", true);
                    editor.apply();
                    Toast.makeText(this, "Note Mode Enabled", Toast.LENGTH_SHORT).show();
                }
                initChatModeMenuItem();
                break;

            case R.id.action_delete_chat:
               final ProgressDialog pd = ProgressDialog.show(this,"Delete chat","Deleting...",true, false);
                this.dbQueries.deleteChat(profileEmail);
                Toast.makeText(this,"chat deleted successfully",Toast.LENGTH_SHORT).show();
                pd.dismiss();
                break;
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
        values.put(DataProvider.COL_MSG_COUNT, 0);
        cr.update(Uri.withAppendedPath(DataProvider.CONTENT_URI_PROFILE, profileId), values, null, null);
        super.onPause();
    }

    private void initChatModeMenuItem(){
        MenuItem mItem= menu.findItem(R.id.action_mode);
        if(sharedPreferences.getBoolean("IS_NOTE_MODE", false)){
            mItem.setTitle("Disable Note Mode");
        }
        else{
            mItem.setTitle("Enable Note Mode");
        }
    }

    public void InsertInDatabaseAndSend(String txt, String to) {
        String msg = "";
        try {
            /**
             * Insert the message in messages table
             */
            ContentValues values = new ContentValues(3);
            values.put(DataProvider.COL_MSG, txt);
            values.put(DataProvider.COL_TO, to);

            /**
             * After insertion in Database insert() will return Uri of newly added tuple as base_uri/id
             * so we need to fetch lastPathSegment() to get id from resultUri
             */
            cr = getContentResolver();
            Uri resultUri = cr.insert(DataProvider.CONTENT_URI_MESSAGES, values);
            String id = resultUri.getLastPathSegment();
            Log.d(TAG, "insertUri= " + resultUri + " ,id= " + id);

            /**
             * Now sending message Id with payload to track the delivery and status of message
             */

            SU = new ServerUtilities();
            SU.send(getApplicationContext(), txt, getProfileEmail(), id);

        } catch (IOException ex) {
            msg = "Message could not be sent";
        }
        if (!TextUtils.isEmpty(msg)) {
            Toast.makeText(this, "Unable to send the message", Toast.LENGTH_SHORT);
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(registrationStatusReceiver);
        gcmUtil.cleanup();
        super.onDestroy();
    }

    private BroadcastReceiver registrationStatusReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && SendNoteApplication.ACTION_REGISTER.equals(intent.getAction())) {
                switch (intent.getIntExtra(SendNoteApplication.EXTRA_STATUS, 100)) {
                    case SendNoteApplication.STATUS_SUCCESS:
                        getActionBar().setSubtitle("online");
                        break;

                    case SendNoteApplication.STATUS_FAILED:
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
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

}

