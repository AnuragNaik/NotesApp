package com.android.anurag.notesapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private boolean isConnected = false;
    private static final String TAG= "NetworkChangeReceiver";
    public NetworkChangeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "Received notification about network status change!");
        if(isNetworkAvailable(context)){
            Intent i = new Intent(context, MsgIntentService.class);
            i.setAction("action.SEND_IN_BACKGROUND");
            context.startService(i);
        }
    }

    private boolean isNetworkAvailable(Context context){
        NetworkInfo info;
        NetworkInfo[] networkInfo;
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivity != null){
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP) {
                Network[] networks = connectivity.getAllNetworks();
                for(Network mNetwork : networks){
                    info= connectivity.getNetworkInfo(mNetwork);
                    if(info.getState().equals(NetworkInfo.State.CONNECTED)){
                        if(!isConnected) {
                            Log.v(TAG, "connected to internet");
                            return true;
                        }
                    }
                }
            }
            else{
                networkInfo = connectivity.getAllNetworkInfo();
                if(networkInfo != null){
                    for(NetworkInfo mInfo: networkInfo){
                        if (mInfo.getState().equals(NetworkInfo.State.CONNECTED)){
                            if(!isConnected) {
                                Log.v(TAG, "connected to internet");
                                return true;
                            }
                        }
                    }
                }
            }
        }
        Log.v(TAG, "You are not connected to internet!");
        isConnected = false;
        return false;
    }
}
