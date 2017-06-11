package fretx.version4.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import fretx.version4.activities.BaseActivity;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 11/06/17 17:35.
 */

public class Network {
    private final static String TAG = "KJKP6_NETWORK";
    private boolean connected;
    private ConnectivityManager connectivityManager;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                checkConnection();
            }
        }
    };

    /* = = = = = = = = = = = = = = = = = SINGLETON PATTERN = = = = = = = = = = = = = = = = = = = */
    private static class Holder {
        private static final Network instance = new Network();
    }

    private Network() {
    }

    public static Network getInstance() {
        return Holder.instance;
    }

    public void init() {
        connectivityManager = (ConnectivityManager) BaseActivity.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE );
        BaseActivity.getActivity().getApplicationContext().registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        checkConnection();
    }

    private void checkConnection() {
        final NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        connected = netInfo != null && netInfo.isConnectedOrConnecting();
        if (connected) {
            Log.d(TAG, "connected!");
        } else {
            Log.d(TAG, "disconnected!");
        }
    }

    public boolean isConnected() {
        return connected;
    }
}
