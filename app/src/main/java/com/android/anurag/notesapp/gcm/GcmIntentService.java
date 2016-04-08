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
import android.database.Cursor;
import android.database.SQLException;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.android.anurag.notesapp.Common;
import com.android.anurag.notesapp.DataProvider;
import com.android.anurag.notesapp.MainActivity;
import com.android.anurag.notesapp.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.text.SimpleDateFormat;
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

    public GcmIntentService() {
        super("GcmIntentService");
    }
    public static final String TAG = "GCM Demo";
    ServerUtilities serverUtilities= new ServerUtilities();
    @Override
    protected void onHandleIntent(Intent intent) {
        ctx = this;
        Context context= ctx;
        cr = context.getContentResolver();
        String msg, to, from, contactName, msgId, ack;
        PowerManager mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.acquire();


        try {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);

            String messageType = gcm.getMessageType(intent);
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error", false);

            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server", false);

            } else {

                to = intent.getStringExtra(Common.TO);
                msgId = intent.getStringExtra(Common.MSG_ID);
                ack = intent.getStringExtra(Common.ACK);

                Log.i(TAG, "ack: "+ack+" msgId: "+msgId+ " to: " + to + "...");
                if (ack.equals("SENT")) {

                    msg = intent.getStringExtra(Common.MSG);
                    from = intent.getStringExtra(Common.FROM);

                    try{
                        serverUtilities.sendDeliveryReport(ctx, from, msgId);
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }

                    if (getContactIfAvailable(from, context) == null) {
                        insertContactIntoDatabase(context, from);
                        Log.i(TAG, "inserting profile data into database");
                    }

                    insertMessageIntoDatabase(msg, from);
                    contactName = from;
                    Log.d(TAG, "current chat= " + Common.getCurrentChat());
                    if ((!from.equals(Common.getCurrentChat()) && !to.equals(Common.getCurrentChat()))) {
                        if (Common.isNotify()) {
                            sendNotification(contactName + ": " + msg, true);
					/*	Intent intnt= new Intent(context, PopUp.class);
						intnt.putExtra("msg", msg);
						intnt.putExtra("from", from);
						intnt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intnt);*/
                        }
                        incrementMessageCount(context, from, to);
                    }
                }
                else if(ack.equals("DELIVERY_REPORT")){
                    //Delivery report
                    Log.i(TAG, "delivery Report inserting in database");
                    ContentValues contentValues= new ContentValues(1);
                    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
                    Date now = new Date();
                    String strDate = sdfDate.format(now);
                    contentValues.put(DataProvider.DELIVERED, strDate);
                    ctx.getContentResolver().update(Uri.withAppendedPath(DataProvider.CONTENT_URI_MESSAGES, msgId), contentValues, null, null);
                    Log.i(TAG, "Delivery Status updated in database ");
                }
                else if(ack.equals("READ")){
                    //Delivery report
                    Log.i(TAG, "delivery Report inserting in database");
                    ContentValues contentValues= new ContentValues(1);
                    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
                    Date now = new Date();
                    String strDate = sdfDate.format(now);
                    contentValues.put(DataProvider.READ, strDate);
                    ctx.getContentResolver().update(Uri.withAppendedPath(DataProvider.CONTENT_URI_MESSAGES, msgId), contentValues, null, null);
                    Log.i(TAG, "Delivery Status updated in database ");
                }
                //setResultCode(Activity.RESULT_OK);
            }
        }finally{
            mWakeLock.release();
        }


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
                new String[]{DataProvider.COL_NAME},
                DataProvider.COL_EMAIL + " = ?",
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
        String name=getContactNameFromConactNumber(contact);
        try {
            ContentValues values = new ContentValues(2);
            values.put(DataProvider.COL_NAME, name);
            values.put(DataProvider.COL_EMAIL, contact);
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
    public String getContactNameFromConactNumber(String contact){
        return contact.substring(0, contact.indexOf('@'));
    }

    /**
     * Inserts message to message table
     * @param msg: message to be inserted
     * @param from: sender of message
     */
    public void insertMessageIntoDatabase(String msg, String from){
        Log.i(TAG,"inserting received msg in databse");
        ContentValues values = new ContentValues(2);
        values.put(DataProvider.COL_MSG, msg);
        values.put(DataProvider.COL_FROM, from);
        values.put(DataProvider.COL_TO, "");
        cr.insert(DataProvider.CONTENT_URI_MESSAGES, values);
        Log.i(TAG, "inserted received msg in database");
    }

    //	public static int ctr=1;
    private void sendNotification(String text, boolean launchApp) {
        //	NotificationManagerCompat mNotificationManager = (NotificationManagerCompat) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        String GROUP_KEY="key";
        //	final  String GROUP_KEY_EMAILS = "group_key_emails";

        // Create an InboxStyle notification
        NotificationManager mNotificationManager =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(ctx.getString(R.string.app_name))
                .setStyle(new NotificationCompat.InboxStyle()
                        .addLine(text)
                        .setBigContentTitle(/*ctr + */" new Messages")
                        .setSummaryText(/*ctr + */" new Messages"))
                .setGroup(GROUP_KEY)
                .setGroupSummary(true)
                .setSound(Uri.parse(Common.getRingtone()), AudioAttributes.USAGE_NOTIFICATION);


        //	Random rn= new Random();
        //	int notificationId= rn.nextInt();
        int notificationId=1;
        //	mNotificationManager.notify(notificationId, mBuilder.build());


        if (!TextUtils.isEmpty(Common.getRingtone())) {
            mBuilder.setSound(Uri.parse(Common.getRingtone()), AudioAttributes.USAGE_NOTIFICATION);
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
        if (!Common.getChatId().equals(to)) {//group
            chatId = to;
        } else {
            chatId = from;
        }

        String selection = DataProvider.COL_EMAIL+" = ?";
        String[] selectionArgs = new String[]{chatId};
        Cursor c = cr.query(DataProvider.CONTENT_URI_PROFILE,
                new String[]{DataProvider.COL_COUNT},
                selection,
                selectionArgs,
                null);

        if (c != null) {
            if (c.moveToFirst()) {
                int count = c.getInt(0);

                ContentValues cv = new ContentValues(1);
                cv.put(DataProvider.COL_COUNT, count+1);
                cr.update(DataProvider.CONTENT_URI_PROFILE, cv, selection, selectionArgs);
            }
            c.close();
        }
    }
}
