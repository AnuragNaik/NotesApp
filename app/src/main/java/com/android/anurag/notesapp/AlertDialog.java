package com.android.anurag.notesapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by anurag on 30/4/16.
 */
public class AlertDialog extends Activity {
    String TAG= "alertDialog";
    private String senderName, message;
    private Dialog dialog;
    TextView msgTextView;
    EditText replyEditText;
    Button replyButton , cancelButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent= getIntent();
        senderName=intent.getStringExtra("sender_name");
        message= intent.getStringExtra("msg");

        this.setFinishOnTouchOutside(false);
        dialog= new Dialog(AlertDialog.this);
        dialog.setTitle(senderName);
        dialog.setContentView(R.layout.alert_dialog);

        replyEditText = (EditText) dialog.findViewById(R.id.replyEditText);
        msgTextView = (TextView) dialog.findViewById(R.id.messageTextView);
        msgTextView.setText(message);

        replyButton = (Button) dialog.findViewById(R.id.button_reply);
        cancelButton = (Button) dialog.findViewById(R.id.button_cancel);
        dialog.show();

        replyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatActivity chatActivity = new ChatActivity();
                message=replyEditText.getText().toString();
                if(!TextUtils.isEmpty(message)) {
                    Log.d(TAG, "sender: "+senderName);
                    chatActivity.sendFromDialog(message, senderName);
                    Toast.makeText(getApplicationContext(), "reply clicked!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "cancel clicked!",Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });


    }
}
