package com.android.anurag.notesapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.anurag.notesapp.gcm.GcmUtil;

public class RegistrationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
    }

    public void registration(View view){
        EditText email= (EditText) findViewById(R.id.mobile_number);
        EditText editText2= (EditText) findViewById(R.id.name);

        String mobile=email.getText().toString();
        String name=editText2.getText().toString();

        AddContactDialog addContactDialog=new AddContactDialog();
        if(!addContactDialog.isEmailValid(mobile) || TextUtils.isEmpty(name)) {
            if(!addContactDialog.isEmailValid(mobile)){
                Toast.makeText(this, "Invalid Email.", Toast.LENGTH_LONG).show();
                email.setText("");
            }
            else{
                Toast.makeText(this, "Invalid Name.", Toast.LENGTH_LONG).show();
                editText2.setText("");
            }
        }
        else{
            GcmUtil gcmUtil = new GcmUtil(this);
            gcmUtil.register(this, mobile, name);
        }
     }

}
