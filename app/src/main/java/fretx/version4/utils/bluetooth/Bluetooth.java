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
    public final ArrayList<ServiceListener> serviceListeners = new ArrayList<>();
    public static final byte[] F0 = new byte[] {1, 2, 3, 4, 5, 6, 0};
    public static final byte[] F1 = new byte[] {11, 12, 13, 14, 15, 16, 0};
    public static final byte[] F2 = new byte[] {21, 22, 23, 24, 25, 26, 0};
    public static final byte[] F3 = new byte[] {31, 32, 33, 34, 35, 36, 0};
    public static final byte[] F4 = new byte[] {41, 42, 43, 44, 45, 46, 0};
    public static final byte[] S1 = new byte[] {1, 11, 21, 31, 41, 0};
    public static final byte[] S2 = new byte[] {2, 12, 22, 32, 42, 0};
    public static final byte[] S3 = new byte[] {3, 13, 23, 33, 43, 0};
    public static final byte[] S4 = new byte[] {4, 14, 24, 34, 44, 0};
    public static final byte[] S5 = new byte[] {5, 15, 25, 35, 45, 0};
    public static final byte[] S6 = new byte[] {6, 16, 26, 36, 46, 0};
    public static final byte[] S1_NO_F0 = new byte[] {11, 21, 31, 41, 0};
    public static final byte[] S2_NO_F0 = new byte[] {12, 22, 32, 42, 0};
    public static final byte[] S3_NO_F0 = new byte[] {13, 23, 33, 43, 0};
    public static final byte[] S4_NO_F0 = new byte[] {14, 24, 34, 44, 0};
    public static final byte[] S5_NO_F0 = new byte[] {15, 25, 35, 45, 0};
    public static final byte[] S6_NO_F0 = new byte[] {16, 26, 36, 46, 0};
    public static final byte[] BLANK = new byte[] {0};

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
        initBluetoothLE();
    }

    public void initBluetoothStd(){
        Log.d(TAG, "init bluetooth Standard");
        final Intent intent = new Intent(getActivity(), BluetoothStdService.class);
        getActivity().getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        //// TODO: 08/06/17 find a place to unregister this
        chordFingerings = MusicUtils.parseChordDb();
    }

    public void initBluetoothLE(){
        Log.d(TAG, "init bluetooth LE");
        final Intent intent = new Intent(getActivity(), BluetoothLEService.class);
        getActivity().getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        //// TODO: 08/06/17 find a place to unregister this
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
        BluetoothAnimator.getInstance().stopAnimation();
        byte[] bluetoothArray = MusicUtils.getBluetoothArrayFromChord(chord.toString(), chordFingerings);
        service.send(bluetoothArray);
    }

    public void setMatrix(byte[] fingerings) {
        BluetoothAnimator.getInstance().stopAnimation();
        service.send(fingerings);
    }

    public void setMatrix(Scale scale) {
        BluetoothAnimator.getInstance().stopAnimation();
        byte[] bluetoothArray = MusicUtils.getBluetoothArrayFromChord(scale.toString(), chordFingerings);
        service.send(bluetoothArray);
    }

    public void clearMatrix() {
        BluetoothAnimator.getInstance().stopAnimation();
        service.send(clear);
    }

    public void setString(int string) {
        BluetoothAnimator.getInstance().stopAnimation();
        byte data[] = null;
        switch (string) {
            case 1:
                data = S1_NO_F0;
                break;
            case 2:
                data = S2_NO_F0;
                break;
            case 3:
                data = S3_NO_F0;
                break;
            case 4:
                data = S4_NO_F0;
                break;
            case 5:
                data = S5_NO_F0;
                break;
            case 6:
                data = S6_NO_F0;
                break;
            default:
        }
        service.send(data);
    }

    public void lightMatrix() {
        BluetoothAnimator.getInstance().stopAnimation();
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
