package com.android.anurag.notesapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.anurag.notesapp.gcm.ServerUtilities;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import github.ankushsachdeva.emojicon.EmojiconEditText;
import github.ankushsachdeva.emojicon.EmojiconTextView;

/**
 * Created by anurag on 30/4/16.
 */
public class AlertDialog extends Activity {
    String TAG = "alertDialog";
    private ContentResolver cr;
    private String senderName, message;
    private Dialog dialog;
    EmojiconTextView msgTextView;
    EmojiconEditText replyEditText;
    Button replyButton, cancelButton;
    SendNoteApplication app;
    private ChatActivity chatActivity;
    private EmojiconTextView timerTextView;
    private CountDownTimerClass timer;
    private String msgTimer;
    DateTimeUtils.Timer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (SendNoteApplication) getApplicationContext();
        chatActivity = app.getChatActivity();
        Intent intent = getIntent();
        senderName = intent.getStringExtra("sender_name");
        message = intent.getStringExtra("msg");
        msgTimer = intent.getStringExtra("timer");
        createDialogAndTimer(senderName, message);
        timer.start();
    }

    private void createDialogAndTimer(String senderName, String message){
        dialog = new Dialog(AlertDialog.this);
        dialog.setContentView(R.layout.alert_dialog);
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
      //  lp.dimAmount= 0.0f;           //Dont allow dialog background to dim
        dialog.setTitle(senderName);
        dialog.setCancelable(false);  //Dont allow user to dismiss dialog using Back Button
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);  //Dont allow user to dismiss dialog to dismiss by touching outside window
        timerTextView =(EmojiconTextView) dialog.findViewById(R.id.timer_text_view);
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");//dd/MM/yyyy
        Date strDate = new Date();
        DateTimeUtils dtu = new DateTimeUtils(strDate, msgTimer);
        countDownTimer = dtu.getDifference();

        timer = new CountDownTimerClass(countDownTimer, 1000, timerTextView, dialog);

        msgTextView = (EmojiconTextView) dialog.findViewById(R.id.messageTextView);
        msgTextView.setText(message);
        replyButton = (Button) dialog.findViewById(R.id.button_reply);
        cancelButton = (Button) dialog.findViewById(R.id.button_cancel);

        replyEditText = (EmojiconEditText) dialog.findViewById(R.id.replyEditText);
        replyEditText.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   replyEditText.setFocusableInTouchMode(true);
                   replyEditText.requestFocus();
                   final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                   inputMethodManager.showSoftInput(replyEditText, InputMethodManager.SHOW_IMPLICIT);
               }
        });
        replyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replyClicked();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelClicked();
            }
        });
    }

    private void replyClicked(){
        timer.cancel();
        message = replyEditText.getText().toString();

        if (!TextUtils.isEmpty(message)) {
            Log.d(TAG, "sender: " + senderName);
            if(chatActivity!=null) {
                chatActivity.InsertInDatabaseAndSend(message, senderName, null);
            }
            else{
               InsertInDatabaseAndSend(message, senderName, null);
            }
            this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); //prevent background to be dim when dialog is cancelled
            dialog.cancel();
        }
    }

    private void cancelClicked(){
        timer.cancel();
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.cancel();
    }


    public void InsertInDatabaseAndSend(String txt, String to, String timer) {
        String msg = "";
        try {
            /**
             * Insert the message in messages table
             */
            String strDate = DateTimeUtils.getCurrentDateTime();
            ContentValues values = new ContentValues(4);
            values.put(DataProvider.COL_MSG, txt);
            values.put(DataProvider.COL_TO, to);
            values.put(DataProvider.COL_AT, strDate);
            values.put(DataProvider.COL_TIMER, timer);
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

          ServerUtilities  SU = new ServerUtilities();
            SU.send(getApplicationContext(), txt, to, id, timer);

        } catch (IOException ex) {
            msg = "Message could not be sent";
        }
        if (!TextUtils.isEmpty(msg)) {
            Toast.makeText(this, "Unable to send the message", Toast.LENGTH_SHORT);
        }
    }


}
