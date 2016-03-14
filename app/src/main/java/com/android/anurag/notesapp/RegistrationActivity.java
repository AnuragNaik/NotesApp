package com.android.anurag.notesapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.android.anurag.notesapp.gcm.GcmUtil;

public class RegistrationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
    }

    public void registration(View view){
        EditText editText1= (EditText) findViewById(R.id.mobile_number);
        EditText editText2= (EditText) findViewById(R.id.name);

        String mobile=editText1.getText().toString();
        String name=editText2.getText().toString();

        GcmUtil gcmUtil= new GcmUtil(this);
        gcmUtil.register(this, mobile, name);
     }

}
