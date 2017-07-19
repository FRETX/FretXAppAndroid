package fretx.version4;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import fretx.version4.utils.bluetooth.Bluetooth;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 10/07/17 12:44.
 */

public class FretXApp extends MultiDexApplication {
    private static final String TAG = "KJKP6_FRETXAPP";
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "location provider enabled");
            Bluetooth.getInstance().connectFretX();
        }

        @Override
        public void onProviderDisabled(String provider) {}
    };
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            Log.d(TAG, "register bluetooth action");
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "bluetooth is enabled");
                        Bluetooth.getInstance().connectFretX();
                        break;
                }

            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        final IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(broadcastReceiver, filter);
        Log.d(TAG, "register bluetooth broadcast receiver");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "unregister bluetooth broadcast receiver");
        unregisterReceiver(broadcastReceiver);
    }

    public void setLocationListener() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location notification enabled");
            final LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 100, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10, 100, locationListener);
        }
    }


}
