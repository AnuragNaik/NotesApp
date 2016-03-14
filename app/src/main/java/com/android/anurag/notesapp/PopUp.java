package com.android.anurag.notesapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class PopUp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pop_up);

        final String TAG="popup";
        Intent intent =this.getIntent();
        String msg =intent.getStringExtra("msg");
        String from=intent.getStringExtra("from");
        Toast.makeText(this, "message= "+msg+" from: "+from, Toast.LENGTH_LONG).show();
        TextView textView= (TextView) findViewById(R.id.popup);
        textView.setText(from +": "+msg+" \n Note: Press back button to Exit.");
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width=dm.widthPixels;
        int height=dm.heightPixels;
        getWindow().setLayout((int) (width*.7), (int) (height*.6));
        Log.d(TAG,"Pop-up Shown Successfully!");
    }

}

