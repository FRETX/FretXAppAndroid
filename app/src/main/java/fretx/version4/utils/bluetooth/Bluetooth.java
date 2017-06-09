package fretx.version4.utils.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import rocks.fretx.audioprocessing.Chord;
import rocks.fretx.audioprocessing.FingerPositions;
import rocks.fretx.audioprocessing.MusicUtils;
import rocks.fretx.audioprocessing.Scale;

import static fretx.version4.activities.BaseActivity.getActivity;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 08/06/17 10:31.
 */

public class Bluetooth {
    private final static String TAG = "KJKP6_BLUETOOTH";
    private HashMap<String,FingerPositions> chordFingerings;
    private static final String DEVICE_NAME = "FretX";
    private BluetoothInterface service;
    private boolean enabled;
    private final byte[] clear = new byte[]{0};
    private final byte[] correctIndicator = new byte[]{
            1,11,21,31,41,
            6,16,26,36,46, 0};
    private final ArrayList<ServiceListener> serviceListeners = new ArrayList<>();

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int btState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (btState) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "Bluetooth off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "Turning Bluetooth off... (by external event)");
                        service.onTurnedOff();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "Bluetooth on");
                        service.onTurnedOn();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "Turning Bluetooth on...");
                        break;
                }
            }
        }
    };

    /* = = = = = = = = = = = = = = = = = SINGLETON PATTERN = = = = = = = = = = = = = = = = = = = */
    private static class Holder {
        private static final Bluetooth instance = new Bluetooth();
    }

    private Bluetooth() {
    }

    public static Bluetooth getInstance() {
        return Holder.instance;
    }

    /* = = = = = = = = = = = = = = = = = = = BLUETOOTH = = = = = = = = = = = = = = = = = = = = = */
    public void init() {
        Log.d(TAG, "init");
        final Intent intent = new Intent(getActivity(), BluetoothLEService.class);
        getActivity().getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        //// TODO: 08/06/17 find a place to unregister this
        getActivity().getApplicationContext().registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        chordFingerings = MusicUtils.parseChordDb();
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
            service = (BluetoothInterface) serviceBinder;
            for (ServiceListener listener: serviceListeners) {
                listener.onBind();
            }
            enabled = true;
            service.connectDevice(DEVICE_NAME);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    public void registerBluetoothListener(BluetoothListener listener) {
        service.registerBluetoothListener(listener);
    }

    public void unregisterBluetoothListener(BluetoothListener listener) {
        service.unregisterBluetoothListener(listener);
    }

    public void registerServiceListener(ServiceListener listener) {
        if (!serviceListeners.contains(listener)) {
            serviceListeners.add(listener);
        }
    }

    public void unregisterServiceListener(ServiceListener listener) {
        if (serviceListeners.contains(listener)) {
            serviceListeners.remove(serviceListeners.indexOf(listener));
        }
    }

    public void setMatrix(Chord chord) {
        byte[] bluetoothArray = MusicUtils.getBluetoothArrayFromChord(chord.toString(), chordFingerings);
        service.send(bluetoothArray);
    }

    public void setMatrix(byte[] fingerings) {
        service.send(fingerings);
    }

    public void setMatrix(Scale scale) {
        byte[] bluetoothArray = MusicUtils.getBluetoothArrayFromChord(scale.toString(), chordFingerings);
        service.send(bluetoothArray);
    }

    public void clearMatrix() {
        service.send(clear);
    }

    public void lightMatrix() {
        service.send(correctIndicator);
    }

    public boolean isConnected() {
        return service.isConnected();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void disconnect() {
        service.disconnect();
    }

    public void connect() {
        service.connectDevice(DEVICE_NAME);
    }
}
