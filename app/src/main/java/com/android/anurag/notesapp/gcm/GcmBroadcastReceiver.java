package com.android.anurag.notesapp.gcm;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.android.anurag.notesapp.Common;
import com.android.anurag.notesapp.DataProvider;
import com.android.anurag.notesapp.MainActivity;
import com.android.anurag.notesapp.PopUp;
import com.android.anurag.notesapp.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmBroadcastReceiver extends BroadcastReceiver {
	
	private static final String TAG = "GcmBroadcastReceiver";
	
	private Context ctx;
	private ContentResolver cr;

	@Override
	public void onReceive(Context context, Intent intent) {
		ctx = context;
		cr = context.getContentResolver();
		String msg, to, from, contactName;
		PowerManager mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		WakeLock mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		mWakeLock.acquire();


		try {
			GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
			
			String messageType = gcm.getMessageType(intent);
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
				sendNotification("Send error", false);
				
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
				sendNotification("Deleted messages on server", false);
				
			} else {

				msg = intent.getStringExtra(Common.MSG);
				from = intent.getStringExtra(Common.FROM);
				to = intent.getStringExtra(Common.TO);

				contactName=getContactIfAvailable(from, context);
				Log.i(TAG, "msg= " + msg + "  from=" + from + "..." + " to: " + to + "...");

				if(contactName == null){
					insertContactIntoDatabase(context, contactName);
				}

				insertMessageIntoDatabase(msg,from);

				Log.d(TAG, "current chat= "+ Common.getCurrentChat());
				if ((!from.equals(Common.getCurrentChat()) &&!to.equals(Common.getCurrentChat()))) {
					if (Common.isNotify()) {
						sendNotification(contactName + ": " + msg, true);
						Intent intnt= new Intent(context, PopUp.class);
						intnt.putExtra("msg", msg);
						intnt.putExtra("from", from);
						intnt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intnt);
					}
					incrementMessageCount(context, from, to);
				}
			}
			setResultCode(Activity.RESULT_OK);
			
		} finally {
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

		if (c != null) {
			if (c.moveToFirst()) {
				mobileNumber = c.getString(0);
				Log.i(TAG, "contactName: "+mobileNumber);
			}
			c.close();
			return mobileNumber;
		}
		return null;
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
			ctx.getContentResolver().insert(DataProvider.CONTENT_URI_PROFILE, values);
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

		ContentValues values = new ContentValues(2);
		values.put(DataProvider.COL_MSG, msg);
		values.put(DataProvider.COL_FROM, from);
		values.put(DataProvider.COL_TO, "");
		cr.insert(DataProvider.CONTENT_URI_MESSAGES, values);

	}

	public static int ctr=1;
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
						.setBigContentTitle(ctr + " new Messages")
						.setSummaryText(ctr + " new Messages"))
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
