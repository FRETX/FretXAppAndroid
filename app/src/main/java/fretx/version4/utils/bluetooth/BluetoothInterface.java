package fretx.version4.utils.bluetooth;

import android.bluetooth.BluetoothDevice;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 09/06/17 02:29.
 */

interface BluetoothInterface {
    void connect(Bluetooth bt, BluetoothDevice device);
    void send(byte data[]);
}
