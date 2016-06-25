package com.android.anurag.notesapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by anurag on 21/6/16.
 */
public class DateTimeUtils {
    private Date startDate;
    private Date endDate;
    public DateTimeUtils(String date1, String  date2){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/M/yyyy hh:mm:ss");
        try{
            startDate = simpleDateFormat.parse(date1);
            endDate = simpleDateFormat.parse(date2);
        } catch (ParseException e){
            e.printStackTrace();
        }

    }

    public void getDifference(){
        //milliseconds
        long difference = endDate.getTime() - startDate.getTime();
        System.out.println("startDate : "+ startDate);
        System.out.println("endDate : " + endDate);
        System.out.println("difference : " + difference);

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = difference / daysInMilli;
        difference = difference % daysInMilli;

        long elapsedHours = difference / hoursInMilli;
        difference = difference % hoursInMilli;

        long elapsedMinutes = difference / minutesInMilli;
        difference = difference % minutesInMilli;

        long elapsedSeconds = difference / secondsInMilli;

        System.out.printf("%d days , %d hours, %d minutes, %d seconds%n", elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds);
    }

    public static void main(String arg[]){
        DateTimeUtils dtu = new DateTimeUtils( "21/06/2016 21:00:00","23/06/2016 10:30:00" );
        dtu.getDifference();
    }
}


