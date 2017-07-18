package fretx.version4.utils.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fretx.version4.activities.BaseActivity;
import fretx.version4.utils.Preference;
import rocks.fretx.audioprocessing.Chord;
import rocks.fretx.audioprocessing.FingerPositions;
import rocks.fretx.audioprocessing.MusicUtils;
import rocks.fretx.audioprocessing.Scale;

import static android.content.Context.BLUETOOTH_SERVICE;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 08/06/17 10:31.
 */

public class Bluetooth implements ServiceConnection {
    private static final String TAG = "KJKP6_BLUETOOTH";
    private static final String DEVICE_NAME = "FretX";
    private static final int SCAN_DELAY_MS = 3000;

    static final byte[] CORRECT_INDICATOR = new byte[]{1, 11, 21, 31, 41, 6, 16, 26, 36, 46, 0};
    static final byte[] F0 = new byte[] {1, 2, 3, 4, 5, 6, 0};
    static final byte[] F1 = new byte[] {11, 12, 13, 14, 15, 16, 0};
    static final byte[] F2 = new byte[] {21, 22, 23, 24, 25, 26, 0};
    static final byte[] F3 = new byte[] {31, 32, 33, 34, 35, 36, 0};
    static final byte[] F4 = new byte[] {41, 42, 43, 44, 45, 46, 0};
    static final byte[] S1 = new byte[] {1, 11, 21, 31, 41, 0};
    static final byte[] S2 = new byte[] {2, 12, 22, 32, 42, 0};
    static final byte[] S3 = new byte[] {3, 13, 23, 33, 43, 0};
    static final byte[] S4 = new byte[] {4, 14, 24, 34, 44, 0};
    static final byte[] S5 = new byte[] {5, 15, 25, 35, 45, 0};
    static final byte[] S6 = new byte[] {6, 16, 26, 36, 46, 0};
    static final byte[] S1_NO_F0 = new byte[] {11, 21, 31, 41, 0};
    static final byte[] S2_NO_F0 = new byte[] {12, 22, 32, 42, 0};
    static final byte[] S3_NO_F0 = new byte[] {13, 23, 33, 43, 0};
    static final byte[] S4_NO_F0 = new byte[] {14, 24, 34, 44, 0};
    static final byte[] S5_NO_F0 = new byte[] {15, 25, 35, 45, 0};
    static final byte[] S6_NO_F0 = new byte[] {16, 26, 36, 46, 0};
    static final byte[] BLANK = new byte[] {0};

    private final SparseArray<BluetoothDevice> devices = new SparseArray<>();
    private final ArrayList<BluetoothListener> bluetoothListeners = new ArrayList<>();
    private final Handler handler = new Handler();

    private BluetoothDevice connectDevice;
    private HashMap<String,FingerPositions> chordFingerings;
    enum State {NOT_CONNECTED, CONNECTING, CONNECTED}
    private LocationManager manager = null;
    State state = State.NOT_CONNECTED;
    private BluetoothAdapter adapter;
    private boolean enabled;
    private BluetoothInterface service;
    private boolean locationAsked;

    /* = = = = = = = = = = = = = = = = = SINGLETON PATTERN = = = = = = = = = = = = = = = = = = = */
    private static class Holder {
        private static final Bluetooth instance = new Bluetooth();
    }

    private Bluetooth() {
        Log.d(TAG, "init bluetooth LE");
        final AppCompatActivity activity = BaseActivity.getActivity();
        boolean ble = false;
        if (activity == null) {
            Log.d(TAG, "Base activity is null");
        } else {
            adapter = ((BluetoothManager) activity.getSystemService(BLUETOOTH_SERVICE)).getAdapter();
            manager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
            ble = activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        }
        if (adapter == null) {
            Log.d(TAG, "No bluetooth adapter");
        } else if (!ble) {
            Log.d(TAG, "No BluetoothLE low energy");
        } else {
            Log.d(TAG, "Bluetooth adapter ok!");
            enabled = true;
        }
        chordFingerings = MusicUtils.parseChordDb();
    }

    public static Bluetooth getInstance() {
        return Holder.instance;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /* = = = = = = = = = = = = = = = = ENABLING / SCANNING = = = = = = = = = = = = = = = = = = = */
    public void connectFretX() {
        if (!enabled || adapter == null) {
            Log.d(TAG, "Bluetooth connect request rejected!");
        } else if (!adapter.isEnabled()) {
            Log.d(TAG, "Bluetooth enabling adapter!");
            adapter.enable();
        } else if (state != State.NOT_CONNECTED) {
            Log.d(TAG, "Scan or connection already ongoing!");
        } else if (manager != null && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (locationAsked)
                return;
            locationAsked = true;
            final AppCompatActivity activity = BaseActivity.getActivity();
            if (activity == null) {
                Log.d(TAG, "Base activity is null");
            } else {
                activity.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        } else {
            locationAsked = false;
            state = State.CONNECTING;
            Log.d(TAG, "Bluetooth scanning devices!");
            devices.clear();
            final ScanSettings settings = new ScanSettings.Builder().build();
            final ScanFilter filter = new ScanFilter.Builder().build();
            final List<ScanFilter> filters = new ArrayList<>();
            filters.add(filter);
            adapter.getBluetoothLeScanner().startScan(filters, settings, scanCallback);
            handler.postDelayed(endOfScan, SCAN_DELAY_MS);
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            //super.onScanResult(callbackType, result);
            final BluetoothDevice device = result.getDevice();
            if (device == null) {
                Log.d(TAG, "connectDevice is null");
            } else {
                final String name = device.getName();
                if (name == null) {
                    Log.d(TAG, "name is null");
                } else if (name.equals(DEVICE_NAME)) {
                    Log.d(TAG, "New FRETX connectDevice");
                    devices.put(device.hashCode(), device);
                } else {
                    Log.d(TAG, "New OTHER connectDevice: " + device.getName());
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.d(TAG, "New BLE Devices");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            if (errorCode == SCAN_FAILED_ALREADY_STARTED) {
                Log.d(TAG, "Scan failed: SCAN_FAILED_ALREADY_STARTED");
            } else if (errorCode == SCAN_FAILED_APPLICATION_REGISTRATION_FAILED) {
                Log.d(TAG, "Scan failed: SCAN_FAILED_APPLICATION_REGISTRATION_FAILED");
            } else if (errorCode == SCAN_FAILED_FEATURE_UNSUPPORTED) {
                Log.d(TAG, "Scan failed: SCAN_FAILED_FEATURE_UNSUPPORTED");
            } else if (errorCode == SCAN_FAILED_INTERNAL_ERROR) {
                Log.d(TAG, "Scan failed: SCAN_FAILED_INTERNAL_ERROR");
            } else {
                Log.d(TAG, "Scan failed: UNKNOWN_ERROR (" + errorCode + ")");
            }
            state = State.NOT_CONNECTED;
        }
    };

    private Runnable endOfScan = new Runnable() {
        @Override
        public void run() {
            adapter.getBluetoothLeScanner().stopScan(scanCallback);
            state = State.NOT_CONNECTED;
            switch (devices.size()) {
                case 0:
                    Log.d(TAG, "no FretX found");
                    notifyScanFailure("no FretX found");
                    break;
                case 1:
                    Log.d(TAG, devices.size() + " Fretx found");
                    connect(devices.valueAt(0));
                    break;
                default:
                    state = State.NOT_CONNECTED;
                    Log.d(TAG, "Too many FretX");
                    notifyMultipleScanResults(devices);
            }
            state = State.NOT_CONNECTED;
        }
    };

    /* = = = = = = = = = = = = = = = = = = CONNECTING = = = = = = = = = = = = = = = = = = = = = = */
    public void connect(BluetoothDevice device) {
        final AppCompatActivity activity = BaseActivity.getActivity();
        if (state == State.CONNECTING) {
            Log.d(TAG, "connect request canceled, already connecting");
        } else if (activity != null) {
            Log.d(TAG, "start connection service");
            state = State.CONNECTING;
            this.connectDevice = device;
            final Context appContext = activity.getApplication();
            final Intent intent = new Intent(appContext, BluetoothLEService.class);
            appContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
        Log.d(TAG, "service connected");
        final BluetoothBinderInterface binder = (BluetoothBinderInterface) serviceBinder;
        service = binder.getService();
        service.connect(Bluetooth.this, connectDevice);
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        Log.d(TAG, "service disconnected");
        adapter.disable();
        service = null;
        state = State.NOT_CONNECTED;
    }

    public void disconnect() {
        Log.d(TAG, "disconnect");
        final AppCompatActivity activity = BaseActivity.getActivity();
        if (activity != null && state == State.CONNECTED) {
            final Context appContext = activity.getApplication();
            appContext.unbindService(this);
        }
    }

    public boolean isConnected() {
        return state == State.CONNECTED;
    }
    public boolean isConnecting() {
        return state == State.CONNECTING;
    }

    /* = = = = = = = = = = = = = = = = = = LISTENING = = = = = = = = = = = = = = = = = = = = = = */
    public void registerBluetoothListener(BluetoothListener listener) {
        if (!bluetoothListeners.contains(listener)) {
            bluetoothListeners.add(listener);
        }
    }

    public void unregisterBluetoothListener(BluetoothListener listener) {
        if (bluetoothListeners.contains(listener)) {
            bluetoothListeners.remove(bluetoothListeners.indexOf(listener));
        }
    }

    private void notifyScanFailure(String errorMessage) {
        if (bluetoothListeners.size() > 0)
            for (BluetoothListener listener: bluetoothListeners)
                listener.onScanFailure(errorMessage);
        else
            Log.d(TAG, "No listener to notify scan failure");
    }

    private void notifyMultipleScanResults(SparseArray<BluetoothDevice> devices) {
        if (bluetoothListeners.size() > 0)
            for (BluetoothListener listener: bluetoothListeners)
                listener.onMultipleScanResult(devices);
        else
            Log.d(TAG, "No listener to notify scan failure");
    }

    void notifyFailure(String errorMessage) {
        if (bluetoothListeners.size() > 0)
            for (BluetoothListener listener: bluetoothListeners)
                listener.onFailure(errorMessage);
        else
            Log.d(TAG, "No listener to notify scan failure");
    }

    void notifyConnection() {
        if (bluetoothListeners.size() > 0)
            for (BluetoothListener listener: bluetoothListeners)
                listener.onConnect();
        else
            Log.d(TAG, "No listener to notify scan failure");
    }

    void notifyDisconnection() {
        if (bluetoothListeners.size() > 0)
            for (BluetoothListener listener: bluetoothListeners)
                listener.onDisconnect();
        else
            Log.d(TAG, "No listener to notify scan failure");
    }

    /* = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = */
    public void setMatrix(Chord chord) {
        if (service == null || chord == null)
            return;
        BluetoothAnimator.getInstance().stopAnimation();
        byte[] bluetoothArray = MusicUtils.getBluetoothArrayFromChord(chord.toString(), chordFingerings);
        if (Preference.getInstance().isLeftHanded())
            bluetoothArray = convertToLeftHanded(bluetoothArray);
        service.send(bluetoothArray);
    }

    public void setMatrix(byte[] fingerings) {
        if (service == null || fingerings == null)
            return;
        BluetoothAnimator.getInstance().stopAnimation();
        if (Preference.getInstance().isLeftHanded())
            fingerings = convertToLeftHanded(fingerings);
        service.send(fingerings);
    }

    public void setMatrix(Scale scale) {
        if (service == null || scale == null)
            return;
        BluetoothAnimator.getInstance().stopAnimation();
        byte[] bluetoothArray = MusicUtils.getBluetoothArrayFromChord(scale.toString(), chordFingerings);
        if (Preference.getInstance().isLeftHanded())
            bluetoothArray = convertToLeftHanded(bluetoothArray);
        service.send(bluetoothArray);
    }

    public void clearMatrix() {
        if (service == null)
            return;
        BluetoothAnimator.getInstance().stopAnimation();
        service.send(BLANK);
    }

    public void setString(int string) {
        if (service == null)
            return;
        BluetoothAnimator.getInstance().stopAnimation();
        byte data[];
        switch (string) {
            case 1:
                data = S1_NO_F0;
                if (Preference.getInstance().isLeftHanded())
                    data = S6_NO_F0;
                break;
            case 2:
                data = S2_NO_F0;
                if (Preference.getInstance().isLeftHanded())
                    data = S5_NO_F0;
                break;
            case 3:
                data = S3_NO_F0;
                if (Preference.getInstance().isLeftHanded())
                    data = S4_NO_F0;
                break;
            case 4:
                data = S4_NO_F0;
                if (Preference.getInstance().isLeftHanded())
                    data = S3_NO_F0;
                break;
            case 5:
                data = S5_NO_F0;
                if (Preference.getInstance().isLeftHanded())
                    data = S2_NO_F0;
                break;
            case 6:
                data = S6_NO_F0;
                if (Preference.getInstance().isLeftHanded())
                    data = S1_NO_F0;
                break;
            default:
                data = BLANK;
        }
        service.send(data);
    }

    public void lightMatrix() {
        if (service == null)
            return;
        BluetoothAnimator.getInstance().stopAnimation();
        service.send(CORRECT_INDICATOR);
    }

    private byte[] convertToLeftHanded(byte bluetoothArray[]) {
        byte convertedArray[] = new byte[bluetoothArray.length];
        for (int index = 0; index < convertedArray.length; ++index) {
            int b = bluetoothArray[index];
            if (b == 0) {
                convertedArray[index] = 0;
            } else if (b < 10){
                int s = b;
                s = 7 - s;
                convertedArray[index] = (byte)(s);
            } else {
                int s = b % 10;
                int f = b - s;
                s = 7 - s;
                convertedArray[index] = (byte)(f + s);
            }
        }
        return convertedArray;
    }
}
