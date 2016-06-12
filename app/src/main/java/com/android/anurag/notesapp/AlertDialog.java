package com.android.anurag.notesapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.anurag.notesapp.gcm.ServerUtilities;

import java.io.IOException;

/**
 * Created by anurag on 30/4/16.
 */
public class AlertDialog extends Activity {
    String TAG = "alertDialog";
    private String senderName, message;
    private Dialog dialog;
    TextView msgTextView;
    EditText replyEditText;
    Button replyButton, cancelButton;
    SendNoteApplication app;
    private ChatActivity chatActivity;
    private TextView timerTextView;
    private CountDownTimerClass timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (SendNoteApplication) getApplicationContext();
        chatActivity = app.getChatActivity();
        Intent intent = getIntent();
        senderName = intent.getStringExtra("sender_name");
        message = intent.getStringExtra("msg");

        createDialogAndTimer(senderName, message).show();
        timer.start();
    }

    private Dialog createDialogAndTimer(String senderName, String message){
        dialog = new Dialog(AlertDialog.this);
        dialog.setContentView(R.layout.alert_dialog);
        dialog.setTitle(senderName);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        //  dialog.setCanceledOnTouchOutside(false);
        timerTextView =(TextView) dialog.findViewById(R.id.timer_text_view);
        timer = new CountDownTimerClass(180000, 1000, timerTextView);

        msgTextView = (TextView) dialog.findViewById(R.id.messageTextView);
        msgTextView.setText(message);
        replyButton = (Button) dialog.findViewById(R.id.button_reply);
        cancelButton = (Button) dialog.findViewById(R.id.button_cancel);

        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        replyEditText = (EditText) dialog.findViewById(R.id.replyEditText);

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
        return dialog;
    }

    private void replyClicked(){
        timer.cancel();
        message = replyEditText.getText().toString();
        if (!TextUtils.isEmpty(message)) {
            Log.d(TAG, "sender: " + senderName);
            chatActivity.InsertInDatabaseAndSend(message, senderName);
            dialog.dismiss();
        }
    }

    private void cancelClicked(){
        timer.cancel();
        dialog.dismiss();
    }
}