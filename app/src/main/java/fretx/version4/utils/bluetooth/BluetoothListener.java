package fretx.version4.utils.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.util.SparseArray;

/**
 * FretXapp for FretX
 * Created by pandor on 24/04/17 19:46.
 */

public interface BluetoothListener {
    void onMultipleScanResult(SparseArray<BluetoothDevice> results);
    void onScanFailure(String errorMessage);
    void onFailure(String errorMessage);
    void onConnect();
    void onDisconnect();
}
