package fretx.version4.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.greysonparrelli.permiso.IOnPermissionComplete;
import com.greysonparrelli.permiso.IOnPermissionResult;
import com.greysonparrelli.permiso.IOnRationaleProvided;
import com.greysonparrelli.permiso.Permiso;
import com.greysonparrelli.permiso.ResultSet;

import fretx.version4.R;
import fretx.version4.utils.audio.Audio;
import fretx.version4.utils.bluetooth.Bluetooth;
import fretx.version4.utils.audio.Midi;
import fretx.version4.utils.bluetooth.ServiceListener;
import fretx.version4.utils.firebase.Analytics;
import fretx.version4.utils.firebase.FirebaseConfig;
import io.intercom.android.sdk.Intercom;
import io.intercom.android.sdk.UserAttributes;
import io.intercom.android.sdk.identity.Registration;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class SplashScreen extends BaseActivity {
    private static final String TAG = "KJKP6_PERMISO";
    private boolean initialized;
    private boolean permission;
    private boolean binded;

    private IOnPermissionComplete onCompleteListener = new IOnPermissionComplete() {
        @Override
        public void onComplete(){
            Log.d(TAG, "Complete!");
            permission = true;
            complete();
        }
    };
    private ServiceListener serviceListener = new ServiceListener() {
        @Override
        public void onBind() {
            binded = true;
            Bluetooth.getInstance().unregisterServiceListener(serviceListener);
            complete();
        }
    };
    private IOnPermissionResult onPermissionResult = new IOnPermissionResult() {
        @Override
        public void onPermissionResult(ResultSet resultSet) {
            if (resultSet.isPermissionGranted(Manifest.permission.RECORD_AUDIO)) {
                Log.d(TAG,"Record Audio permissions granted");
                // Audio permission granted!
                initAudio();
            }
            if (resultSet.isPermissionGranted(Manifest.permission.READ_PHONE_STATE)) {
                Log.d(TAG,"Phone permissions granted");
                // Phone permission granted!
                initAudio();
            }
            if (resultSet.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Location permission granted!
                initBluetooth();
                Log.d(TAG,"Location permissions granted");
            }
        }
        @Override
        public void onRationaleRequested(IOnRationaleProvided callback, String... permissions) {
            Permiso.getInstance().showRationaleInDialog("FretX Permissions",
                    "These permissions are requested to connect to the FretX, to detect your chords and play sounds", null, callback);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/GothamRoundedBook.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        //ask permissions at runtime
        Permiso.getInstance().setActivity(this);
        Permiso.getInstance().setOnComplete(onCompleteListener);
        Permiso.getInstance().requestPermissions(onPermissionResult,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        //initialize midi
        if (!Midi.getInstance().isEnabled()) {
            Midi.getInstance().init();
            Midi.getInstance().start();
        }
        //initialize Firebase analytics
        if (!Analytics.getInstance().isEnabled()) {
            Analytics.getInstance().init();
            Analytics.getInstance().start();
        }
        //initialize intercom
        Intercom.initialize(getApplication(), "android_sdk-073d0705faff270ed9274399ebff4d4c55c58d67", "p1olv87a");
        //initialize Firebase remote config
        FirebaseConfig.getInstance().init();

        initialized = true;
        complete();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Permiso.getInstance().setActivity(this);
    }

    //redirect callback to Permiso
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Permiso.getInstance().onRequestPermissionResult(requestCode, permissions, grantResults);
        //Permiso.getInstance().setActivity(this);
    }

    //init audio
    private void initAudio() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                && !Audio.getInstance().isEnabled()) {
            Audio.getInstance().init();
            Audio.getInstance().start();
        }
    }

    //init bluetooth
    private void initBluetooth() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && !Bluetooth.getInstance().isEnabled()) {
            Bluetooth.getInstance().registerServiceListener(serviceListener);
            Bluetooth.getInstance().init();
        }
    }

    private void complete() {
        if (!initialized || !permission || !binded)
            return;

        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fUser != null) {
            //intercom
            Registration registration = Registration.create().withUserId(fUser.getUid());
            Intercom.client().registerIdentifiedUser(registration);
            UserAttributes userAttributes = new UserAttributes.Builder()
                    .withName(fUser.getDisplayName())
                    .withEmail(fUser.getEmail())
                    .build();
            Intercom.client().updateUser(userAttributes);

            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        } else {
            final Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }
    }
}