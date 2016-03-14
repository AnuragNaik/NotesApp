package com.android.anurag.notesapp.gcm;

/**
 * Created by anurag on 5/3/16.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.anurag.notesapp.MainActivity;
import com.android.anurag.notesapp.PopUp;
import com.android.anurag.notesapp.R;
import com.google.android.gms.gcm.GcmListenerService;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    private Context ctx;
    private ContentResolver cr;

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     *             In data{
     *                     frm: sender's mobile number
     *                     to: receiver's number
     *                     msg: message
     *             }
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        ctx = getApplicationContext();
        cr = ctx.getContentResolver();

        String message = data.getString("msg");
        String frm= data.getString("frm");
        String to=data.getString("to");

        Log.d(TAG, "From: " + frm);
        Log.d(TAG, "Message: " + message);
        Log.d(TAG, "to: " + to);

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
            Log.d(TAG, "Normal downstream message: " );
        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */
  //      Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Intent intent= new Intent(this, PopUp.class);
        intent.putExtra("msg",message);
        intent.putExtra("from", from);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification(message);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("GCM Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
