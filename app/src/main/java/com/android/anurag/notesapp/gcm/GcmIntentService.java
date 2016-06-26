/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.anurag.notesapp.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.android.anurag.notesapp.AlertDialog;
import com.android.anurag.notesapp.DataProvider;
import com.android.anurag.notesapp.DateTimeUtils;
import com.android.anurag.notesapp.DbQueries;
import com.android.anurag.notesapp.MainActivity;
import com.android.anurag.notesapp.R;
import com.android.anurag.notesapp.SendNoteApplication;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    private Context ctx;
    private ContentResolver cr;
    private String msg;
    private String to;
    private String from;
    private String contactName;
    private String msgId;
    private String ack;
    private String timer;
    private DbQueries dbQueries;
    private Cursor cursor;
    SendNoteApplication app;
    private SharedPreferences sharedPreferences;

    public String getTimer() {
        return timer;
    }

    public void setTimer(String timer) {
        this.timer = timer;
    }

    public String getMsgId() {
        return msgId;
    }

    public String getAck() {
        return ack;
    }

    public String getContactName() {
        return contactName;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getMsg() {
        return msg;
    }

    public void setAck(String ack) {
        this.ack = ack;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public GcmIntentService() {
        super("GcmIntentService");
    }
    public static final String TAG = "GCM Demo";
    ServerUtilities serverUtilities= new ServerUtilities();

    @Override
    protected void onHandleIntent(Intent intent) {
        app = (SendNoteApplication) getApplication();
        ctx = this;
        Context context= ctx;
        cr = context.getContentResolver();
        PowerManager mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.acquire();
        sharedPreferences = getSharedPreferences(getPackageName(),Context.MODE_PRIVATE);

        try {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);

            String messageType = gcm.getMessageType(intent);
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error", false);
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server", false);
            } else {
                setTo(intent.getStringExtra(SendNoteApplication.TO));
                setMsgId(intent.getStringExtra(SendNoteApplication.MSG_ID));
                setAck(intent.getStringExtra(SendNoteApplication.ACK));
                setTimer(intent.getStringExtra(SendNoteApplication.TIMER));
                Log.i(TAG, "ack: " + ack + " msgId: " + msgId + " to: " + to + "...");
                if (ack.equals("SENT")) {
                    //message is received from other client
                    setMsg(intent.getStringExtra(SendNoteApplication.MSG));
                    setFrom(intent.getStringExtra(SendNoteApplication.FROM));

                    try {
                        //send delivery report to the client
                        serverUtilities.sendDeliveryReport(ctx, from, msgId);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    /**
                    check if contact is already there in your database or not
                    if(there){
                        insert message to the contact
                    }
                    else{
                        create new contact and add to database and then add the message
                    }
                     */
                    if (getContactIfAvailable(from, context) == null) {
                        insertContactIntoDatabase(context, from);
                        Log.i(TAG, "inserting profile data into database");
                    }

                    insertMessageIntoDatabase(msg, from, getTimer(),Integer.parseInt(getMsgId()));
                    setContactName(from);
                    Log.d(TAG, "current chat= " + SendNoteApplication.getCurrentChat());
                    if (!(from.equals(SendNoteApplication.getCurrentChat()) && !to.equals(SendNoteApplication.getCurrentChat()))) {

                        if (SendNoteApplication.isNotify()) {
                            sendNotification(contactName + ": " + msg, true);
                           if(sharedPreferences.getBoolean("IS_NOTE_MODE",false)) {
                               showNotificationPopUp(from, msg, getTimer());
                           }
                            incrementMessageCount(context, from, to);
                        }
                    }

                } else if (ack.equals("DELIVERY_REPORT")) {
                        //Delivery report
                        Log.i(TAG, "delivery Report inserting in database");
                        ContentValues contentValues = new ContentValues(1);
                        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/M/yyyy hh:mm:ss");//dd/MM/yyyy
                        Date now = new Date();
                        String strDate = sdfDate.format(now);
                        contentValues.put(DataProvider.COL_DELIVERED, strDate);
                        ctx.getContentResolver().update(Uri.withAppendedPath(DataProvider.CONTENT_URI_MESSAGES, msgId), contentValues, null, null);
                        Log.i(TAG, "Delivery Status updated in database ");
                } else if (ack.equals("READ")) {
                    //TODO implement Read ack
                }
            }
        }
        finally{
            mWakeLock.release();
        }
    }

    /**
     * \brief shows notification popup
     */
    public void showNotificationPopUp(String from, String msg, String timer){
        Intent dialogIntent= new Intent(ctx, AlertDialog.class);
        dialogIntent.putExtra("sender_name", from);
        dialogIntent.putExtra("msg", msg);
        dialogIntent.putExtra("timer",timer);
        dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);
    }



    public ArrayList getNotificationText(){
        ArrayList list = new ArrayList();
        cursor = cr.query(
               DataProvider.CONTENT_URI_MESSAGES,
               new String[]{DataProvider.COL_FROM, DataProvider.COL_MSG},
               DataProvider.COL_READ+" is null AND "+DataProvider.COL_FROM+" is not null",
               null,
               DataProvider.COL_AT+" ASC");
        if(cursor != null){
            while(cursor.moveToNext()){
                list.add(getContactNameFromContactNumber(cursor.getString(0))+" : "+cursor.getString(1)+"\n");
            }
            cursor.close();
        }
        return list;
    }

    /**
     * Query Database for contact Availability
     * @param context context variable
     * @param mobileNumber contact number to query
     * @return contact number/null
     */
    public String getContactIfAvailable(String mobileNumber, Context context){

        Cursor c = context.getContentResolver().query(
                DataProvider.CONTENT_URI_PROFILE,
                new String[]{DataProvider.COL_USER_NAME},
                DataProvider.COL_USER_ID + " = ?",
                new String[]{mobileNumber},
                null);

        if (c != null && c.getCount()>0) {
            if (c.moveToFirst()) {
                mobileNumber = c.getString(0);
                Log.i(TAG, "contactName: "+mobileNumber);
            }
            c.close();
            return mobileNumber;
        }else {
            Log.i(TAG, "contact not found");
            return null;
        }
    }

    /**
     * Inserts a provided contact into database
     * @throws SQLException
     * @param context context variable
     * @param contact contact number to insert into database
     */
    public void insertContactIntoDatabase(Context context, String contact){
        String name=getContactNameFromContactNumber(contact);
        try {
            ContentValues values = new ContentValues(2);
            values.put(DataProvider.COL_USER_NAME, name);
            values.put(DataProvider.COL_USER_ID, contact);
            context.getContentResolver().insert(DataProvider.CONTENT_URI_PROFILE, values);
        } catch (SQLException sqle) {
            Log.e(TAG, sqle+": Inserting in databse failed");
        }
    }

    /**
     * Gives display name back from contact
     * @param contact : contact to extract name
     * @return display name
     */
    public String getContactNameFromContactNumber(String contact){
        return contact.substring(0, contact.indexOf('@'));
    }

    /**
     * Inserts message to message table
     * @param msg: message to be inserted
     * @param from: sender of message
     */
    public void insertMessageIntoDatabase(String msg, String from , String timer, int theirMsgId){
        Log.i(TAG,"inserting received msg in database");

        String strDate = DateTimeUtils.getCurrentDateTime();

        ContentValues values = new ContentValues(6);
        values.put(DataProvider.COL_MSG, msg);
        values.put(DataProvider.COL_FROM, from);
        values.put(DataProvider.COL_TO, "");
        values.put(DataProvider.COL_AT,strDate);
        values.put(DataProvider.COL_TIMER, timer);
        values.put(DataProvider.COL_THEIR_MSG_ID, theirMsgId);
        cr.insert(DataProvider.CONTENT_URI_MESSAGES, values);
        Log.i(TAG, "inserted received msg in database");
    }

    //	public static int ctr=1;
    private void sendNotification(String text, boolean launchApp) {
        ArrayList msgList = getNotificationText();
        //	NotificationManagerCompat mNotificationManager = (NotificationManagerCompat) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        Log.i(TAG, "notification");
        String GROUP_KEY="key";
        //	final  String GROUP_KEY_EMAILS = "group_key_emails";

        // Create an InboxStyle notification
        NotificationManager mNotificationManager =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.send_note)
                .setContentTitle("New Notes Received")
                .setContentText("Notes Received")
                .setGroup(GROUP_KEY)
                .setGroupSummary(true)
                .setSound(Uri.parse(SendNoteApplication.getRingtone()), AudioAttributes.USAGE_NOTIFICATION);
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        inboxStyle.setBigContentTitle(msgList.size()+" New Notes");
        for(int i=0; i<msgList.size(); i++){
            inboxStyle.addLine(msgList.get(i).toString());
        }

        mBuilder.setStyle(inboxStyle);
        //	Random rn= new Random();
        //	int notificationId= rn.nextInt();
        int notificationId=1;
        	//mNotificationManager.notify(notificationId, mBuilder.build());


        if (!TextUtils.isEmpty(SendNoteApplication.getRingtone())) {
            mBuilder.setSound(Uri.parse(SendNoteApplication.getRingtone()), AudioAttributes.USAGE_NOTIFICATION);
        }

        if (launchApp) {
            Intent intent = new Intent(ctx, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pi = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pi);
        }

        mNotificationManager.notify(notificationId, mBuilder.getNotification());
    }

    /**
     * Increments new message count by 1
     * @param context: Context Variable
     * @param from: Sender
     * @param to: Receiver
     */
    private void incrementMessageCount(Context context, String from, String to) {
        String chatId;
        if (!SendNoteApplication.getChatId().equals(to)) {//group
            chatId = to;
        } else {
            chatId = from;
        }

        String selection = DataProvider.COL_USER_ID+" = ?";
        String[] selectionArgs = new String[]{chatId};
        Cursor c = cr.query(DataProvider.CONTENT_URI_PROFILE,
                new String[]{DataProvider.COL_MSG_COUNT},
                selection,
                selectionArgs,
                null);

        if (c != null) {
            if (c.moveToFirst()) {
                int count = c.getInt(0);

                ContentValues cv = new ContentValues(1);
                cv.put(DataProvider.COL_MSG_COUNT, count+1);
                cr.update(DataProvider.CONTENT_URI_PROFILE, cv, selection, selectionArgs);
            }
            c.close();
        }
    }

}

