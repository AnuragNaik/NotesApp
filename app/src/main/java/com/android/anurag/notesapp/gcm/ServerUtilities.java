/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.anurag.notesapp.gcm;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.android.anurag.notesapp.DataProvider;
import com.android.anurag.notesapp.DateTimeUtils;
import com.android.anurag.notesapp.SendNoteApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;


/**
 * Helper class used to communicate with the demo server.
 */
public final class ServerUtilities {

    private static final String TAG = "ServerUtilities";
    private static final String BASE_URL="http://notesapp.comxa.com/";
  //  private static final String BASE_URL="http://athena.nitc.ac.in/anuragkumar_b130974cs/"
    private static final int MAX_ATTEMPTS = 1;
    private static final int BACKOFF_MILLI_SECONDS = 2000;
    private static final Random random = new Random();

    public String UrlToSendMsg=BASE_URL+"notesapp/send.php";
    public String UrlToRegister=BASE_URL+"notesapp/register.php";
    public String UrlToUnRegister=BASE_URL+"notesapp/unregister.php";
    public String UrlToSendDeliveryReport=BASE_URL+"notesapp/ack.php";
    /**
     * Register this account/device pair within the server.
     */
    public String register(Context context, final String mobile, final String regId) {
        Log.i(TAG, "registering device (regId = " + regId + ")");

        // Once GCM returns a registration id, we need to register it in the
        // demo server. As the server might be down, we will retry it a couple
        // times.
        JSONObject post_dict = new JSONObject();

        try {
            post_dict.put(SendNoteApplication.USER_NAME, mobile);
            post_dict.put(SendNoteApplication.REG_ID, regId);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (post_dict.length() > 0) {
            new SendJsonDataToServer(context).execute(String.valueOf(post_dict),UrlToRegister);
        }
        return mobile;
    }

    /**
     * Unregister this account/device pair within the server.
    */
    public void unregister(final String mobile) {
        Log.i(TAG, "unregistering device (mobile = " + mobile + ")");
        JSONObject post_dict = new JSONObject();
        try {
            post_dict.put(SendNoteApplication.FROM, mobile);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (post_dict.length() > 0) {
           // new SendJsonDataToServer().execute(String.valueOf(post_dict),UrlToUnRegister);
        }
    }

    public void send(Context context, String msg, String to, String messageId, String timer) throws IOException{
        JSONObject post_dict = new JSONObject();

        try {
            Log.i(TAG,"chat id= "+ SendNoteApplication.getChatId());
            post_dict.put(SendNoteApplication.TO, to);
            post_dict.put(SendNoteApplication.FROM, SendNoteApplication.getChatId());
            post_dict.put(SendNoteApplication.MSG, msg);
            post_dict.put(SendNoteApplication.MSG_ID, messageId);
            post_dict.put(SendNoteApplication.TIMER, timer);
            post_dict.put(SendNoteApplication.ACK,"SENT");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (post_dict.length() > 0) {
            new SendJsonDataToServer(context).execute(String.valueOf(post_dict), UrlToSendMsg);
        }

    }

    public void sendDeliveryReport(Context context, String to, String messageId, String strDate) throws IOException{
        JSONObject post_dict = new JSONObject();
        try {
            post_dict.put(SendNoteApplication.TO, to);
            post_dict.put(SendNoteApplication.MSG_ID, messageId);
            post_dict.put(SendNoteApplication.ACK,"DELIVERY_REPORT");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (post_dict.length() > 0) {
            new SendJsonDataToServer(context).execute(String.valueOf(post_dict), UrlToSendDeliveryReport);
        }
    }

    public void sendReadAckReport(String msgId){

    }

    static class SendJsonDataToServer extends AsyncTask<String,String,String> {
         private Context mContext;

        public SendJsonDataToServer(Context context){
            mContext=context;
        }

        @Override
        protected String doInBackground(String... params) {
            String JsonResponse = null;
            String JsonDATA = params[0];
            String Url= params[1];

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
            for (int i = 1; i <= MAX_ATTEMPTS; i++) {
                Log.i(TAG, "Attempt #" + i + " to send to "+Url);
                try {
                    URL url = new URL(Url);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoOutput(true);
                    urlConnection.setReadTimeout(4000);
                    // is output buffer writter
                    Log.i(TAG, "trying to connect...");
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Accept", "application/json");
                    Log.i(TAG, "connected...");
                    //set headers and method
                    Writer writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
                    writer.write(JsonDATA);
                    // json data
                    Log.i(TAG, "data sent....");
                    Log.i(TAG, JsonDATA);
                    writer.close();
                    InputStream inputStream = urlConnection.getInputStream();
                    //input stream
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        // Nothing to do.
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String inputLine;
                    while ((inputLine = reader.readLine()) != null)
                        buffer.append(inputLine);
                    if (buffer.length() == 0) {
                        // Stream was empty. No point in parsing.
                        return null;
                    }
                    JsonResponse = buffer.toString();
                    //response data
                    //     Toast.makeText(getApplicationContext(),"Json Response="+JsonResponse,Toast.LENGTH_LONG).show();
                    Log.i(TAG, JsonResponse);
                    //send to post execute
                    publishProgress(JsonResponse);
                    return JsonResponse;
                } catch (IOException e) {
                    e.printStackTrace();
                    if (i == MAX_ATTEMPTS) {
                        break;
                    }
                    try {
                        Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
                        Thread.sleep(backoff);
                    } catch (InterruptedException e1) {
                        // Activity finished before we complete - exit.
                        Log.d(TAG, "Thread interrupted: abort remaining retries!");
                        Thread.currentThread().interrupt();
                    }
                    // increase backoff exponentially
                    backoff *= 2;
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e(TAG, "Error closing stream", e);
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
           String JsonResponse= values[0];
            Log.i(TAG, "JsonReasponse: " + JsonResponse);
            if(!JsonResponse.equals("1")) {
                Log.i(TAG, "updating data ");
                ContentValues dataToInsert = new ContentValues(1);
                String strDate = DateTimeUtils.getCurrentDateTime();
                Log.i(TAG,strDate );
                dataToInsert.put(DataProvider.COL_SENT, strDate);
                mContext.getContentResolver().update(Uri.withAppendedPath(DataProvider.CONTENT_URI_MESSAGES, JsonResponse), dataToInsert, null, null);

                Log.i(TAG, "data updated");
            }
        }

        @Override
        protected void onPostExecute(String s) {
           // (new MessagesFragment()).setAsSentInMessageTable(s);
        }
    }
}