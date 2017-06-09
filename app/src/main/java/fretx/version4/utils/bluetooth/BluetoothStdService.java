package fretx.version4.utils.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 09/06/17 02:11.
 */

public class BluetoothStdService extends Service {
    private final static String TAG = "KJKP6_BLUETOOTH";

    private static final UUID HC05_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private enum State {IDLE, ENABLING, SCANNING, CONNECTING, CONNECTED}
    private State state = State.IDLE;
    private final static int SCAN_DELAY_MS = 3000;
    private final Handler handler = new Handler();
    private final ArrayList<BluetoothListener> bluetoothListeners = new ArrayList<>();
    private String deviceName;

    private final IBinder mBinder = new LocalBinder();

    private final SparseArray<BluetoothDevice> devices = new SparseArray<>();
    private BluetoothAdapter adapter;
    private BluetoothSocket btSocket;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service started");
        adapter = ((BluetoothManager) getSystemService(BLUETOOTH_SERVICE)).getAdapter();
        if (adapter == null) {
            Log.d(TAG, "No bluetooth adapter");
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

        @Override
        public void connectDevice(String deviceName) {
            iDisconnect();
            iConnectDeviceNamed(deviceName);
        }

        @Override
        public void onTurnedOn() {
            iOnTurnedOn();
        }

        @Override
        public void onTurnedOff() {
            iOnTurnedOff();
        }

        @Override
        public void send(byte data[]) {
            iSend(data);
        }

        @Override
        public void registerBluetoothListener(BluetoothListener listener) {
            iRegisterBluetoothListener(listener);
        }

        @Override
        public void unregisterBluetoothListener(BluetoothListener listener) {
            iUnregisterBluetoothListener(listener);
        }

        @Override
        public boolean isConnected() {
            return btSocket != null;
        }

        @Override
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
        if (btSocket == null)
            return;
        try {
            btSocket.getOutputStream().write(data);
        } catch (IOException e) {
            Log.d(TAG, "setMatrix failed");
        }
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

    /* = = = = = = = = = = = = = = = = = = = SCANNING = = = = = = = = = = = = = = = = = = = = = */
    public void scan() {
        Log.d(TAG, "scanning...");
        if (deviceName == null) {
            Log.d(TAG, "device name not set before scanning!");
        } else {
            state = State.SCANNING;
            devices.clear();
            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            adapter.startDiscovery();
            handler.postDelayed(endOfScan, SCAN_DELAY_MS);
        }
    }

    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name = device.getName();
                if (name != null && name.equals(deviceName)
                        && device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "new device: " + name);
                    devices.append(device.hashCode(), device);
                }
            }
        }
    };

    private Runnable endOfScan = new Runnable() {
        @Override
        public void run() {
            adapter.cancelDiscovery();
            unregisterReceiver(bReceiver);
            if (devices.size() == 1) {
                handler.removeCallbacksAndMessages(null);
                connect(devices.valueAt(0));
            } else if (devices.size() > 1) {
                Log.d(TAG, "Too many devices found");
                notifyScanFailure();
                state = State.IDLE;
            } else {
                Log.d(TAG, "No device found");
                notifyScanFailure();
                state = State.IDLE;
            }
        }
    };

    /* = = = = = = = = = = = = = = = = = = = CONNECTING = = = = = = = = = = = = = = = = = = = = = */
    private void connect(BluetoothDevice device) {
        Log.d(TAG, "connecting");
        state = State.CONNECTING;
        try {
            BluetoothDevice remoteDevice = adapter.getRemoteDevice(device.getAddress());
            btSocket = remoteDevice.createInsecureRfcommSocketToServiceRecord(HC05_UUID);
            btSocket.connect();
        } catch (IOException e) {
            notifyFailure();
            state = State.IDLE;
        }
        state = State.CONNECTED;
        notifyConnection();
    }

    public void iDisconnect() {
        if (btSocket != null) {
            try {
                btSocket.close();
                btSocket = null;
                notifyDisconnection();
                state = State.IDLE;
            } catch (IOException e) {
                Log.d(TAG, "Socket disconnection failed");
                notifyFailure();
                state = State.IDLE;
                btSocket = null;
            }
        }
    }

    /* = = = = = = = = = = = = = = = = = = = LISTENERS = = = = = = = = = = = = = = = = = = = = = */
    private void notifyScanFailure() {
        for (BluetoothListener listener: bluetoothListeners) {
            listener.onScanFailure();
        }
    }

    private void notifyFailure() {
        for (BluetoothListener listener: bluetoothListeners) {
            listener.onFailure();
        }
    }

    private void notifyConnection() {
        for (BluetoothListener listener: bluetoothListeners) {
            listener.onConnect();
        }
    }

    private void notifyDisconnection() {
        for (BluetoothListener listener: bluetoothListeners) {
            listener.onDisconnect();
        }
    }
}
