package com.android.anurag.notesapp;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Patterns;

import com.android.anurag.notesapp.gcm.Constants;
import com.android.anurag.notesapp.gcm.GcmUtil;

import java.util.ArrayList;
import java.util.List;

public class SendNoteApplication extends Application {

    public static final String PROFILE_ID = "profile_id";
    public static final String ACTION_REGISTER = "com.android.anurag.notesapp.REGISTER";
    public static final String EXTRA_STATUS = "status";
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_FAILED = 0;
    public static final String FROM= "frm";
    public static final String REG_ID= "reg_id";
    public static final String TO="to";
    public static final String MSG="msg";
    public static final String USER_NAME="mobile_no";
    public static final String MSG_ID="msg_id";
    public static final String ACK="ack";

    public static String[] email_arr;
    private static SharedPreferences prefs;
    public static String CHAT_ID="";
    public static String CURRENT_CHAT_ID="";
    private ChatActivity chatActivity;
    private DbQueries dbQueries;

    @Override
    public void onCreate() {
        super.onCreate();
        sendBroadcast(new Intent("com.google.android.intent.action.GTALK_HEARTBEAT"));
        sendBroadcast(new Intent("com.google.android.intent.action.MCS_HEARTBEAT"));
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        List<String> emailList = getEmailList();
        email_arr = emailList.toArray(new String[emailList.size()]);
        dbQueries = new DbQueries(this);
    }

    public ChatActivity getChatActivity() {
        return chatActivity;
    }

    public void setChatActivity(ChatActivity chatActivity) {
        this.chatActivity = chatActivity;
    }

    public DbQueries getDbQueries() {
        return dbQueries;
    }

    private List<String> getEmailList() {
        List<String> lst = new ArrayList<String>();
        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            if (Patterns.EMAIL_ADDRESS.matcher(account.name).matches()) {
                lst.add(account.name);
            }
        }
        return lst;
    }

    public static String getPreferredEmail() {
        return prefs.getString("chat_email_id", email_arr.length==0 ? "" : email_arr[0]);
    }

    public static String getDisplayName(String email) {
        return prefs.getString("display_name", email.substring(0, email.indexOf('@')));
    }

    public static boolean isNotify() {
        return prefs.getBoolean("notifications_new_message", true);
    }

    public static String getRingtone() {
        return prefs.getString("notifications_new_message_ringtone", android.provider.Settings.System.DEFAULT_NOTIFICATION_URI.toString());
    }

    public static String getServerUrl() {
        return prefs.getString("server_url_pref", Constants.SERVER_URL);
    }
//implemented by me forcely
    public static String getSenderId() {
        return Constants.SENDER_ID;
    }

   public static String getChatId(){
         return prefs.getString(GcmUtil.PROPERTY_CHAT_ID, "");
    }

    public static void setChatId(String mobile){
        String TAG="SendNoteApplication";
        SendNoteApplication.CHAT_ID=mobile;
        Log.i(TAG, "Saving ChatId on preference file :" + mobile);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(GcmUtil.PROPERTY_CHAT_ID, mobile);
        editor.apply();
    }

    public static String getCurrentChat(){
        return SendNoteApplication.CURRENT_CHAT_ID;
    }

    public static void setCurrentChat(String currentChat){
        SendNoteApplication.CURRENT_CHAT_ID= currentChat;
    }

}
