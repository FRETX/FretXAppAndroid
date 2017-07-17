package fretx.version4.utils.firebase;

import android.support.annotation.NonNull;
import android.support.v7.appcompat.BuildConfig;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.Batch;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import fretx.version4.R;
import fretx.version4.activities.BaseActivity;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 09/06/17 11:13.
 */

public class FirebaseConfig {
    private final static String TAG = "KJKP6_CONFIG";
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    public final static String SKIP_USER_INFO = "skipUserInfo";
    public final static String SKIP_HARDWARE_SETUP = "skipHardwareSetup";
    public final static String SETUP_URLS = "setup_urls";
    public final static String TUNER_URL = "tuner_url";
    public final static String PLAY_URL = "play_url";

    /* = = = = = = = = = = = = = = = = = SINGLETON PATTERN = = = = = = = = = = = = = = = = = = = */
    private static class Holder {
        private static final FirebaseConfig instance = new FirebaseConfig();
    }

    private FirebaseConfig() {
    }

    public static FirebaseConfig getInstance() {
        return Holder.instance;
    }

    /* = = = = = = = = = = = = = = = = = = = FIREBASE = = = = = = = = = = = = = = = = = = = = = */
    public void init() {
        Log.d(TAG, "init");
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
//                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        long cacheExpiration = mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled() ? 0 : 3600;
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(BaseActivity.getActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFirebaseRemoteConfig.activateFetched();
                            Log.d(TAG,"Firebase Remote Config activate fetched");
                        }
                    }
                });
    }

    public boolean isUserInfoSkipable() {
        if (mFirebaseRemoteConfig == null)
            return false;
        return mFirebaseRemoteConfig.getBoolean(SKIP_USER_INFO);
    }

    public boolean isHardwareSetupSkipable() {
        if (mFirebaseRemoteConfig == null)
            return false;
        return mFirebaseRemoteConfig.getBoolean(SKIP_HARDWARE_SETUP);
    }

    public ArrayList<String> getSetupUrls() {
        if (mFirebaseRemoteConfig == null)
            return new ArrayList<>();
        final String urls = mFirebaseRemoteConfig.getString(SETUP_URLS);
        Log.d(TAG, "urls: " + urls);
        if (urls == null)
            return null;
        return new ArrayList<>(Arrays.asList(urls.split("\\s+")));
    }

    public String getTunerUrl() {
        if (mFirebaseRemoteConfig == null)
            return "";
        final String url = mFirebaseRemoteConfig.getString(TUNER_URL);
        Log.d(TAG, "url: " + url);
        if (url == null)
            return "";
        return url;
    }

    public String getPlayUrl() {
        return "P7fcILR75NQ";
//        if (mFirebaseRemoteConfig == null)
//            return "";
//        final String url = mFirebaseRemoteConfig.getString(PLAY_URL);
//        Log.d(TAG, "url: " + url);
//        if (url == null)
//            return "";
//        return url;
    }
}
