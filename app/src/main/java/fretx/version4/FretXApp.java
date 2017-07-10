package fretx.version4;

import android.*;
import android.content.Context;
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
    private static final String TAG = "KJKP6_Global";
    private LocationListener locationListener = new LocationListener() {
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
