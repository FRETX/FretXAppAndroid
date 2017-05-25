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
import com.google.firebase.crash.FirebaseCrash;
import com.greysonparrelli.permiso.IOnComplete;
import com.greysonparrelli.permiso.IOnPermissionResult;
import com.greysonparrelli.permiso.IOnRationaleProvided;
import com.greysonparrelli.permiso.Permiso;
import com.greysonparrelli.permiso.ResultSet;

import fretx.version4.R;
import fretx.version4.utils.audio.Audio;
import fretx.version4.utils.bluetooth.BluetoothLE;
import fretx.version4.utils.bluetooth.BluetoothListener;
import fretx.version4.utils.audio.Midi;
import fretx.version4.utils.firebase.Analytics;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class SplashScreen extends BaseActivity {
    private static final String TAG = "KJKP6_PERMISO";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/GothamRoundedBook.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        setContentView(R.layout.activity_splash_screen);
        Permiso.getInstance().setActivity(this);

        //Permiso callback called when the permission process is done
        Permiso.getInstance().setOnComplete(new IOnComplete() {
            public void onComplete(){
                Log.d(TAG, "Complete!");
                if (BluetoothLE.getInstance().isEnabled()) {
                    BluetoothLE.getInstance().scan();
                } else {
                    onInitComplete();
                }
            }
        });

        //bluetooth callback called when the scan is done

        BluetoothLE.getInstance().setListener(new BluetoothListener() {
            @Override
            public void onConnect() {
                Log.d(TAG, "Success!");
                onInitComplete();
            }

            @Override
            public void onDisconnect() {
                Log.d(TAG, "Failure!");
                onInitComplete();
            }

            @Override
            public void onScanFailure() {
                Log.d(TAG, "Failure!");
                onInitComplete();
            }

            public void onFailure() {
                Log.d(TAG, "Failure!");
                onInitComplete();
            }
        });

        //ask for permissions
        Permiso.getInstance().requestPermissions(
                new IOnPermissionResult() {
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
                            Log.d(TAG,"Location permissions granted");
                            initBluetooth();
                        }
                    }
                    @Override
                    public void onRationaleRequested(IOnRationaleProvided callback, String... permissions) {
                        Permiso.getInstance().showRationaleInDialog("FretX Permissions",
                                "These permissions are requested to connect to the FretX, to detect your chords and play sounds", null, callback);
                    }
                },
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        //initialize midi
        if (!Midi.getInstance().isEnabled()) {
            Midi.getInstance().init();
            Midi.getInstance().start();
        }

        //initialize firebase
        if (!Analytics.getInstance().isEnabled()) {
            Analytics.getInstance().init();
            Analytics.getInstance().start();
        }
    }

    private void onInitComplete() {
        BluetoothLE.getInstance().setListener(null);
        BluetoothLE.getInstance().clearMatrix();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }
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
        Permiso.getInstance().setActivity(this);
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
                && !BluetoothLE.getInstance().isEnabled()) {
            BluetoothLE.getInstance().init();
            BluetoothLE.getInstance().start();
        }
    }
}