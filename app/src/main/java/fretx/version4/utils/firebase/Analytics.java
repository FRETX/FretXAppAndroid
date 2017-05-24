package fretx.version4.utils.firebase;

import android.os.Bundle;
import android.util.Log;

import fretx.version4.activities.BaseActivity;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 15/05/17 15:25.
 */

public class Analytics {

    private final static String TAG = "KJKP6_ANALYTICS";
    private com.google.firebase.analytics.FirebaseAnalytics analytics;
    private boolean enabled;

    /* = = = = = = = = = = = = = = = = = SINGLETON PATTERN = = = = = = = = = = = = = = = = = = = */
    private static class Holder {
        private static final Analytics instance = new Analytics();
    }

    private Analytics() {
    }

    public static Analytics getInstance() {
        return Holder.instance;
    }

    /* = = = = = = = = = = = = = = = = = = = FIREBASE = = = = = = = = = = = = = = = = = = = = = */
    public void init() {
        Log.d(TAG, "init");
        analytics = com.google.firebase.analytics.FirebaseAnalytics.getInstance(BaseActivity.getActivity());
        if (analytics != null)
            enabled = true;
        else
            enabled = false;
    }

    public void start() {
        if (!enabled)
            return;
        Log.d(TAG, "start");
    }

    public void stop(){
        if (!enabled)
            return;
        Log.d(TAG, "stop");
    }

    public boolean isEnabled() {
        return enabled;
    }

    /* = = = = = = = = = = = = = = = = = = = EVENT = = = = = = = = = = = = = = = = = = = = = */
    public void logSelectEvent(String content_type, String item_id) {
        if (!enabled)
            return;
        Bundle bundle = new Bundle();
        bundle.putString(com.google.firebase.analytics.FirebaseAnalytics.Param.CONTENT_TYPE, content_type);
        bundle.putString(com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_ID, item_id);
        analytics.logEvent(com.google.firebase.analytics.FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
}
