package fretx.version4.utils.bluetooth;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import fretx.version4.activities.BaseActivity;
import rocks.fretx.audioprocessing.Chord;
import rocks.fretx.audioprocessing.FingerPositions;
import rocks.fretx.audioprocessing.MusicUtils;
import rocks.fretx.audioprocessing.Scale;

import static android.content.Context.BLUETOOTH_SERVICE;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 03/05/17 22:30.
 */

public class BluetoothStd {
    private static final String TAG = "KJKP6_BT_STD_UTIL";

    //delays
    private static final int SCAN_DELAY = 3000;
    private static final int TURN_ON_INTERVAL = 100;
    private static final int TURN_ON_DELAY = 2000;

    //states
    private boolean enabled;
    private boolean scanning;
    private boolean wasScanning;

    private BluetoothAdapter adapter;
    private final SparseArray<BluetoothDevice> devices = new SparseArray<>();
    private BluetoothSocket btSocket;
    private static final UUID HC05_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final int PROGRESS_MSG = 1;
    private static final int PROGRESS_DISMISS = 2;
    private static final int TOAST = 3;
    private static final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case PROGRESS_MSG:
                    progress.setMessage((String) message.obj);
                    break;
                case PROGRESS_DISMISS:
                    progress.dismiss();
                    break;
                case TOAST:
                    Toast.makeText(BaseActivity.getActivity(), (String) message.obj, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(message);
            }
        }
    };

    private static ProgressDialog progress;
    private BluetoothListener listener;
    private int turn_on_try;

    //fretx specific
    private static final String DEVICE_NAME = "HC-05";
    private HashMap<String,FingerPositions> chordFingerings;
    private final byte[] clear = new byte[]{0};

    /* = = = = = = = = = = = = = = = = = SINGLETON PATTERN = = = = = = = = = = = = = = = = = = = */
    private static class Holder {
        private static final BluetoothStd instance = new BluetoothStd();
    }

    private BluetoothStd() {
    }

    public static BluetoothStd getInstance() {
        return BluetoothStd.Holder.instance;
    }

    /* = = = = = = = = = = = = = = = = = = = BLUETOOTH = = = = = = = = = = = = = = = = = = = = = */
    public void init() {
        Log.d(TAG, "init");
        enabled = true;
        chordFingerings = MusicUtils.parseChordDb();
        BluetoothManager manager = (BluetoothManager) BaseActivity.getActivity().getSystemService(BLUETOOTH_SERVICE);
        adapter = manager.getAdapter();
        if (adapter == null) {
            Log.d(TAG, "No bluetooth adapter");
            enabled = false;
        } else if (!BaseActivity.getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.d(TAG, "No BluetoothLE low energy");
            enabled = false;
        } else {
            enabled = true;
        }
    }

    public void start() {
        if (!enabled)
            return;
        Log.d(TAG, "start");
        if (wasScanning && !scanning) {
            wasScanning = false;
            scan();
        }
    }

    public void stop(){
        if (!enabled)
            return;
        handler.removeCallbacksAndMessages(null);
        if (scanning) {
            adapter.cancelDiscovery();
            BaseActivity.getActivity().unregisterReceiver(bReceiver);
            wasScanning = true;
            scanning = false;
        }
        progress.dismiss();
        Log.d(TAG, "stop");
    }

    public boolean isEnabled() {
        return enabled;
    }

    /* = = = = = = = = = = = = = = = = = = = SCANNING = = = = = = = = = = = = = = = = = = = = = */
    public void scan() {
        progress = new ProgressDialog(BaseActivity.getActivity());
        progress.setIndeterminate(true);
        progress.setCancelable(false);

        handler.obtainMessage(PROGRESS_MSG, "Enabling").sendToTarget();
        progress.show();

        Log.d(TAG, "enabling...");
        if(!adapter.isEnabled()) {
            adapter.enable();
            turn_on_try = 0;
            handler.postDelayed(enable, TURN_ON_INTERVAL);
        } else {
            startScan();
        }
    }

    //// TODO: 21/04/17 use 2 runnable different to avoid if forest
    private Runnable enable = new Runnable() {
        @Override
        public void run() {
            if (adapter.getBluetoothLeScanner() == null) {
                ++turn_on_try;
                if (turn_on_try * TURN_ON_INTERVAL <= TURN_ON_DELAY) {
                    Log.d(TAG, "Postpone startScan");
                    handler.removeCallbacksAndMessages(null);
                    handler.postDelayed(enable, TURN_ON_INTERVAL);
                } else {
                    Log.d(TAG, "Enable failed");
                    progress.dismiss();
                    if (listener != null)
                        listener.onScanFailure();
                }
            } else {
                startScan();
            }
        }
    };

    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name = device.getName();
                if (name != null && name.equals(DEVICE_NAME)
                        && device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "new device: " + name);
                    devices.append(device.hashCode(), device);
                }
            }
        }
    };

    private void startScan() {
        Log.d(TAG, "scanning...");
        handler.obtainMessage(PROGRESS_MSG, "Scanning").sendToTarget();
        devices.clear();
        scanning = true;
        adapter.startDiscovery();
        BaseActivity.getActivity().registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        handler.postDelayed(endOfScan, SCAN_DELAY);
    }

    private Runnable endOfScan = new Runnable() {
        @Override
        public void run() {
            adapter.cancelDiscovery();
            BaseActivity.getActivity().unregisterReceiver(bReceiver);
            scanning = false;
            if (devices.size() == 1) {
                handler.removeCallbacksAndMessages(null);
                connect(devices.valueAt(0));
            } else if (devices.size() > 1) {
                Log.d(TAG, "Too many devices found");
                progress.dismiss();
                if (listener != null) {
                    listener.onScanFailure();
                }
            } else {
                Log.d(TAG, "No device found");
                progress.dismiss();
                if (listener != null) {
                    listener.onScanFailure();
                }
            }
        }
    };

    /* = = = = = = = = = = = = = = = = = = = CONNECTING = = = = = = = = = = = = = = = = = = = = = */
    private void connect(BluetoothDevice device) {
        Log.d(TAG, "connecting");
        handler.obtainMessage(PROGRESS_MSG, "Connecting").sendToTarget();

        try {
            BluetoothDevice remoteDevice = adapter.getRemoteDevice(device.getAddress());
            btSocket = remoteDevice.createInsecureRfcommSocketToServiceRecord(HC05_UUID);
            btSocket.connect();
        } catch (IOException e) {
            handler.obtainMessage(PROGRESS_DISMISS, null).sendToTarget();
            if (listener != null) {
                listener.onFailure();
            }
        }
        handler.obtainMessage(PROGRESS_DISMISS, null).sendToTarget();
        if (listener != null) {
            listener.onConnect();
        }
    }

    public void disconnect() {
        if (btSocket != null) {
            try {
                btSocket.close();
                btSocket = null;
                if (listener != null) {
                    listener.onDisconnect();
                }
            } catch (IOException e) {
                Log.d(TAG, "Socket disconnection failed");
                if (listener != null) {
                    listener.onFailure();
                }
            }
        }
    }

    public boolean isConnected() {
        return !(btSocket == null);
    }

    /* = = = = = = = = = = = = = = = = = = = = = MATRIX = = = = = = = = = = = = = = = = = = = = = */
    public void setMatrix(Chord chord) {
        if (btSocket == null)
            return;
        byte[] bluetoothArray = MusicUtils.getBluetoothArrayFromChord(chord.toString(), chordFingerings);
        try {
            btSocket.getOutputStream().write(bluetoothArray);
        } catch (IOException e) {
            Log.d(TAG, "setMatrix failed");
        }
    }

    public void setMatrix(byte[] fingerings) {
        if (btSocket == null)
            return;
        try {
            btSocket.getOutputStream().write(fingerings);
        } catch (IOException e) {
            Log.d(TAG, "setMatrix failed");
        }
    }

    public void setMatrix(Scale scale) {
        if (btSocket == null)
            return;
        byte[] bluetoothArray = MusicUtils.getBluetoothArrayFromChord(scale.toString(), chordFingerings);
        try {
            btSocket.getOutputStream().write(bluetoothArray);
        } catch (IOException e) {
            Log.d(TAG, "setMatrix failed");
        }
    }

    public void clearMatrix() {
        if (btSocket == null)
            return;
        try {
            btSocket.getOutputStream().write(clear);
        } catch (IOException e) {
            Log.d(TAG, "setMatrix failed");
        }
    }

    /* = = = = = = = = = = = = = = = = = = = LISTENERS = = = = = = = = = = = = = = = = = = = = = */
    public void setListener(@Nullable BluetoothListener listener) {
        this.listener = listener;
    }
}
