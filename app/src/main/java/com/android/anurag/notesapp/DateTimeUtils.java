package com.android.anurag.notesapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by anurag on 21/6/16.
 */
public class DateTimeUtils {
    public class Timer{
        private long days;
        private long hours;
        private long minutes;
        private long seconds;
        private long difference;

        public void setDays(long days) {
            this.days = days;
        }

        public void setHours(long hours) {
            this.hours = hours;
        }

        public void setMinutes(long minutes) {
            this.minutes = minutes;
        }

        public void setSeconds(long seconds) {
            this.seconds = seconds;
        }

        public long getHours() {
            return hours;
        }

        public long getMinutes() {
            return minutes;
        }

        public long getSeconds() {
            return seconds;
        }

        public long getDifference() {
            return difference;
        }

        public void setDifference(long difference) {
            this.difference = difference;
        }

        public long getDays() {
            return days;
        }
    }

    private Date startDate;
    private Date endDate;
    public DateTimeUtils(String startDateTime, String  endDateTime){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/M/yyyy hh:mm:ss");
        try{
            startDate = simpleDateFormat.parse(startDateTime);
            endDate = simpleDateFormat.parse(endDateTime);
        } catch (ParseException e){
            e.printStackTrace();
        }

    }

    public DateTimeUtils.Timer getDifference(){
        //milliseconds
        DateTimeUtils.Timer timer = new DateTimeUtils.Timer();
        long difference = endDate.getTime() - startDate.getTime();
        System.out.println("startDate : "+ startDate);
        System.out.println("endDate : " + endDate);
        System.out.println("difference : " + difference);

        timer.setDifference(difference);

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = difference / daysInMilli;
        difference = difference % daysInMilli;
        timer.setDays(elapsedDays);

        long elapsedHours = difference / hoursInMilli;
        difference = difference % hoursInMilli;
        timer.setHours(elapsedHours);

        long elapsedMinutes = difference / minutesInMilli;
        difference = difference % minutesInMilli;
        timer.setMinutes(elapsedMinutes);

        long elapsedSeconds = difference / secondsInMilli;
        timer.setSeconds(elapsedSeconds);

        System.out.printf("%d days , %d hours, %d minutes, %d seconds%n", elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds);
        return timer;
    }

    public static void main(String arg[]){
        DateTimeUtils dtu = new DateTimeUtils( "21/06/2016 21:00:00","23/06/2016 10:30:00" );
        dtu.getDifference();
    }

    public static String getCurrentDateTime(){
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/M/yyyy hh:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public static String get12HourFormatTime(String time){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
        Date date = new Date(time);
        String changedTime = simpleDateFormat.format(date);
        return changedTime;
    }
}


