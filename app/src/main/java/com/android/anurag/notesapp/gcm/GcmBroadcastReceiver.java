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
				String msg = intent.getStringExtra(Common.MSG);
				String from = intent.getStringExtra(Common.FROM);
				String to = intent.getStringExtra(Common.TO);


				//find contact
			//	from="ashwin@gmail.com";
				Log.i(TAG, "msg= "+msg+"  from="+from+"..."+" to: "+to+"...");
				String contactName = null;
				Cursor c = context.getContentResolver().query(
						DataProvider.CONTENT_URI_PROFILE, 
						new String[]{DataProvider.COL_NAME}, 
						DataProvider.COL_EMAIL+" = ?",
						new String[]{from},
						null);
				if (c != null) {
					if (c.moveToFirst()) {
						contactName = c.getString(0);
						Log.i(TAG, "contactName: "+contactName);
					}
					c.close();
				}



                //contact not found
				if (contactName == null) return;
				ContentValues values = new ContentValues(2);
				values.put(DataProvider.COL_MSG, msg);
				values.put(DataProvider.COL_FROM, from);
				values.put(DataProvider.COL_TO, "");
				cr.insert(DataProvider.CONTENT_URI_MESSAGES, values);

				if ((!from.equals(Common.getCurrentChat()) && !to.equals(Common.getCurrentChat()))) {
					if (Common.isNotify()) {
						sendNotification(contactName + ": " + msg, true);
						Intent intnt= new Intent(context, PopUp.class);
						intnt.putExtra("msg",msg);
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
	public static int ctr=1;
	private void sendNotification(String text, boolean launchApp) {
	//	NotificationManagerCompat mNotificationManager = (NotificationManagerCompat) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

		String GROUP_KEY="key";
	//	final  String GROUP_KEY_EMAILS = "group_key_emails";

		// Create an InboxStyle notification
		NotificationManager mNotificationManager =
				(NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

		String[] notice= new String[10];
		notice[0]="ashwin:hii";
		notice[1]="ashwin:kaisa hai tu";
		notice[2]="aur bata";

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
