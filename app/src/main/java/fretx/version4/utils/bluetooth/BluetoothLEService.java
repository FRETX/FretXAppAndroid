package fretx.version4.utils.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 06/06/17 17:20.
 */

public class BluetoothLEService extends Service {
    private final static String TAG = "KJKP6_BLE_SERVICE";
    private static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    private enum State {IDLE, ENABLING, SCANNING, CONNECTING, CONNECTED}
    private State state = State.IDLE;
    private final static int SCAN_DELAY_MS = 3000;
    private final Handler handler = new Handler();
    private final ArrayList<BluetoothListener> bluetoothListeners = new ArrayList<>();
    private String deviceName;

    private final IBinder mBinder = new LocalBinder();

    private final SparseArray<BluetoothDevice> devices = new SparseArray<>();
    private BluetoothAdapter adapter;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic rx;


    @Override
    public void onCreate() {
        Log.d(TAG, "Service started");
        adapter = ((BluetoothManager) getSystemService(BLUETOOTH_SERVICE)).getAdapter();
        if (adapter == null) {
            Log.d(TAG, "No bluetooth adapter");
        } else if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.d(TAG, "No BluetoothLE low energy");
        } else {
            Log.d(TAG, "Bluetooth adapter ok!");
        }
        super.onCreate();
    }

    /* = = = = = = = = = = = = = = = = = = = = BINDING = = = = = = = = = = = = = = = = = = = = = */
    private class LocalBinder extends Binder implements BluetoothInterface{
        /* public BluetoothLEService getServiceInstance(){
            return BluetoothLEService.this;
        } */

        public void connectDevice(String deviceName) {
            iDisconnect();
            iConnectDeviceNamed(deviceName);
        }

        public void onTurnedOn() {
            iOnTurnedOn();
        }

        public void onTurnedOff() {
            iOnTurnedOff();
        }

        public void send(byte data[]) {
            iSend(data);
        }

        public void registerBluetoothListener(BluetoothListener listener) {
            iRegisterBluetoothListener(listener);
        }

        public void unregisterBluetoothListener(BluetoothListener listener) {
            iUnregisterBluetoothListener(listener);
        }

        public boolean isConnected() {
            return gatt != null;
        }

        public void disconnect(){
            iDisconnect();
        }
    }

    private void iConnectDeviceNamed(String deviceName) {
        this.deviceName = deviceName;
        if (enable()) {
            scan();
        }
    }

    private void iOnTurnedOff() {
        if (state == State.SCANNING) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    private void iOnTurnedOn() {
        scan();
    }

    private void iSend(byte data[]) {
        //BluetoothAnimator.getInstance().stopAnimation();
        if (gatt == null || rx == null)
            return;
        rx.setValue(data);
        gatt.writeCharacteristic(rx);
    }

    private void iRegisterBluetoothListener(BluetoothListener listener) {
        if (!bluetoothListeners.contains(listener)) {
            bluetoothListeners.add(listener);
        }
    }

    private void iUnregisterBluetoothListener(BluetoothListener listener) {
        if (bluetoothListeners.contains(listener)) {
            bluetoothListeners.remove(bluetoothListeners.indexOf(listener));
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "Service binded");
        return mBinder;
    }

    /* = = = = = = = = = = = = = = = = = = = = ENABLING = = = = = = = = = = = = = = = = = = = = = */
    private boolean enable() {
        if (state == State.IDLE) {
            if (!adapter.isEnabled()) {
                Log.d(TAG, "enabling...");
                state = State.ENABLING;
                adapter.enable();
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    /* = = = = = = = = = = = = = = = = = = = = SCANNING = = = = = = = = = = = = = = = = = = = = = */
    private void scan() {
        if (deviceName == null) {
            Log.d(TAG, "device name not set before scanning!");
        } else {
            Log.d(TAG, "scanning...");
            state = State.SCANNING;
            devices.clear();
            final ScanSettings settings = new ScanSettings.Builder().build();
            final ScanFilter filter = new ScanFilter.Builder().build();
            //final ScanFilter filter = new ScanFilter.Builder().setDeviceName(deviceName).build();
            final List<ScanFilter> filters = new ArrayList<>();
            filters.add(filter);
            adapter.getBluetoothLeScanner().startScan(filters, settings, scanCallback);
            handler.postDelayed(endOfScan, SCAN_DELAY_MS);
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            final BluetoothDevice device = result.getDevice();
            if (device.getName().equals(deviceName)) {
                Log.d(TAG, "New FRETX: " + device.getName());
                devices.put(device.hashCode(), device);
            } else {
                Log.d(TAG, "New OTHER: " + device.getName());
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

            state = State.IDLE;
            if (errorCode == SCAN_FAILED_ALREADY_STARTED) {
                Log.d(TAG, "Scan failed: SCAN_FAILED_ALREADY_STARTED");
                notifyScanFailure("SCAN_FAILED_ALREADY_STARTED");
            } else if (errorCode == SCAN_FAILED_APPLICATION_REGISTRATION_FAILED) {
                Log.d(TAG, "Scan failed: SCAN_FAILED_APPLICATION_REGISTRATION_FAILED");
                notifyScanFailure("SCAN_FAILED_APPLICATION_REGISTRATION_FAILED");
            } else if (errorCode == SCAN_FAILED_FEATURE_UNSUPPORTED) {
                Log.d(TAG, "Scan failed: SCAN_FAILED_FEATURE_UNSUPPORTED");
                notifyScanFailure("SCAN_FAILED_FEATURE_UNSUPPORTED");
            } else if (errorCode == SCAN_FAILED_INTERNAL_ERROR) {
                Log.d(TAG, "Scan failed: SCAN_FAILED_INTERNAL_ERROR");
                notifyScanFailure("SCAN_FAILED_INTERNAL_ERROR");
            }
        }
    };

    private Runnable endOfScan = new Runnable() {
        @Override
        public void run() {
            adapter.getBluetoothLeScanner().stopScan(scanCallback);
            if (devices.size() == 1) {
                handler.removeCallbacksAndMessages(null);
                connect(devices.valueAt(0));
            } else if (devices.size() > 1) {
                Log.d(TAG, "too many devices found");
                notifyScanFailure("too many devices found");
                state = State.IDLE;
            } else {
                Log.d(TAG, "No device found");
                notifyScanFailure("no device found");
                state = State.IDLE;
            }
        }
    };

    /* = = = = = = = = = = = = = = = = = = = CONNECTING = = = = = = = = = = = = = = = = = = = = = */
    private void connect(BluetoothDevice device) {
        Log.d(TAG, "connecting...");
        state = State.CONNECTING;
        gatt = device.connectGatt(this, false, gattCallback);
    }

    private void iDisconnect() {
        if (gatt != null) {
            Log.d(TAG, "disconnecting");
            gatt.disconnect();
            gatt.close();
            gatt = null;
            state = State.IDLE;
        }
    }

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        //TODO: strum_right_handed all strings to @strings and use Resources.getSystem().getString()
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "discovering...");
                gatt.discoverServices();
            } else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "disconnected");
                gatt.close();
                BluetoothLEService.this.gatt = null;
                state = State.IDLE;
                notifyDisconnection();
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "failure, disconnecting");
                gatt.close();
                BluetoothLEService.this.gatt = null;
                state = State.IDLE;
                notifyFailure("mysterious failure");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            final BluetoothGattService RxService = BluetoothLEService.this.gatt.getService(RX_SERVICE_UUID);
            rx = RxService.getCharacteristic(RX_CHAR_UUID);
            state = State.CONNECTED;
            BluetoothAnimator.getInstance().stringFall();
            notifyConnection();
        }
    };

    /* = = = = = = = = = = = = = = = = = = = LISTENERS = = = = = = = = = = = = = = = = = = = = = */
    private void notifyScanFailure(String errorMessage) {
        if (bluetoothListeners.size() == 0)
            Log.d(TAG, "no listener registered");
        for (BluetoothListener listener: bluetoothListeners) {
            listener.onScanFailure(errorMessage);
        }
    }

    private void notifyFailure(String errorMessage) {
        if (bluetoothListeners.size() == 0)
            Log.d(TAG, "no listener registered");
        for (BluetoothListener listener: bluetoothListeners) {
            listener.onFailure(errorMessage);
        }
    }

    private void notifyConnection() {
        if (bluetoothListeners.size() == 0)
            Log.d(TAG, "no listener registered");
        for (BluetoothListener listener: bluetoothListeners) {
            listener.onConnect();
        }
    }

    private void notifyDisconnection() {
        if (bluetoothListeners.size() == 0)
            Log.d(TAG, "no listener registered");
        for (BluetoothListener listener: bluetoothListeners) {
            listener.onDisconnect();
        }
    }
}
