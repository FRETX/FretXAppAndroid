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

import fretx.version4.R;
import fretx.version4.activities.BaseActivity;
import fretx.version4.activities.MainActivity;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 09/06/17 11:13.
 */

public class FirebaseConfig {
    private final static String TAG = "KJKP6_CONFIG";
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    public final static String SKIP_USER_INFO = "skipUserInfo";
    public final static String SKIP_HARDWARE_SETUP = "skipHardwareSetup";

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
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
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
                        }
                    }
                });
    }

    public boolean isUserInfoSkipable() {
        return mFirebaseRemoteConfig.getBoolean(SKIP_USER_INFO);
    }

    public boolean isHardwareSetupSkipable() {
        return mFirebaseRemoteConfig.getBoolean(SKIP_HARDWARE_SETUP);
    }
}
