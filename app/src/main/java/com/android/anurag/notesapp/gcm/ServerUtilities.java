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

import android.os.AsyncTask;
import android.util.Log;

import com.android.anurag.notesapp.Common;

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

    private static final int MAX_ATTEMPTS = 10;
    private static final int BACKOFF_MILLI_SECONDS = 2000;
    private static final Random random = new Random();

    public String UrlToSendMsg="http://athena.nitc.ac.in/anuragkumar_b130974cs/notesapp/send.php";
    public String UrlToRegister="http://athena.nitc.ac.in/anuragkumar_b130974cs/notesapp/register.php";
    public String UrlToUnRegister="http://athena.nitc.ac.in/anuragkumar_b130974cs/notesapp/unregister.php";
    /**
     * Register this account/device pair within the server.
     */
    public String register(final String mobile, final String regId) {
        Log.i(TAG, "registering device (regId = " + regId + ")");

        // Once GCM returns a registration id, we need to register it in the
        // demo server. As the server might be down, we will retry it a couple
        // times.
        JSONObject post_dict = new JSONObject();

        try {
            post_dict.put(Common.USER_NAME, mobile);
            post_dict.put(Common.REG_ID, regId);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (post_dict.length() > 0) {
            new SendJsonDataToServer().execute(String.valueOf(post_dict),UrlToRegister);
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
            post_dict.put(Common.FROM, mobile);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (post_dict.length() > 0) {
            new SendJsonDataToServer().execute(String.valueOf(post_dict),UrlToUnRegister);
        }
    }

    public  void send(String msg, String to) throws IOException{
        JSONObject post_dict = new JSONObject();

        try {
            Log.i(TAG,"chat id= "+Common.getChatId());
            post_dict.put(Common.TO , to);
            post_dict.put(Common.FROM, Common.getChatId());
            post_dict.put(Common.MSG, msg);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (post_dict.length() > 0) {
            new SendJsonDataToServer().execute(String.valueOf(post_dict), UrlToSendMsg);
        }
    }

    static class SendJsonDataToServer extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... params) {
            String JsonResponse = null;
            String JsonDATA = params[0];
            String Url= params[1];
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
            for (int i = 1; i <= MAX_ATTEMPTS; i++) {
                Log.d(TAG, "Attempt #" + i + " to send");
                try {
                    URL url = new URL(Url);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoOutput(true);
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
                        buffer.append(inputLine + "\n");
                    if (buffer.length() == 0) {
                        // Stream was empty. No point in parsing.
                        return null;
                    }
                    JsonResponse = buffer.toString();
                    //response data
                    //     Toast.makeText(getApplicationContext(),"Json Response="+JsonResponse,Toast.LENGTH_LONG).show();
                    Log.i(TAG, JsonResponse);
                    //send to post execute
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
        protected void onPostExecute(String s) {
        }
    }
}