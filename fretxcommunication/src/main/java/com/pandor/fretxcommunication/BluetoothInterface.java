package com.pandor.fretxcommunication;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 09/06/17 02:29.
 */

interface BluetoothInterface {
    void connect();
    void disconnect();
    boolean isConnected();
    void send(byte data[]);
    void registerBluetoothListener(BluetoothListener listener);
    void unregisterBluetoothListener(BluetoothListener listener);
}
