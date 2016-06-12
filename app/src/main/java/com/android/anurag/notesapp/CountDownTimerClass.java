package com.android.anurag.notesapp;

import android.os.CountDownTimer;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

/**
 * Created by anurag on 8/6/16.
 */
public class CountDownTimerClass extends CountDownTimer {

    private TextView textViewTime;
    public CountDownTimerClass(long millisInFuture, long countDownInterval, TextView timerTextView ) {
        super(millisInFuture, countDownInterval);
        this.textViewTime = timerTextView;
        timerTextView.setText("03:00:00");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onTick(long millisUntilFinished) {
        // TODO Auto-generated method stub

        long millis = millisUntilFinished;
        String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        System.out.println(hms);
        textViewTime.setText(hms);
    }

    @Override
    public void onFinish() {
        // TODO Auto-generated method stub
        textViewTime.setText("Completed.");
    }

}
