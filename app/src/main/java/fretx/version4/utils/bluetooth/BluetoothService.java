package fretx.version4.utils.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.UUID;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

/**
 * BluetoothRework for FretX
 * Created by pandor on 07/07/17 10:13.
 */

public class BluetoothService extends Service implements BluetoothInterface {
    private final static String TAG = "KJKP6_BLE_SERVICE";
    private static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    private final IBinder binder = new LocalBinder();
    private Bluetooth bt;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic rx;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        return Service.START_NOT_STICKY;
    }

    private class LocalBinder extends Binder implements BluetoothBinderInterface {
        public BluetoothInterface getService() {
            return BluetoothService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void connect(Bluetooth bt, BluetoothDevice device) {
        Log.d(TAG, "connecting...");
        this.bt = bt;
        gatt = device.connectGatt(this, true, gattCallback, TRANSPORT_LE);
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
                bt.state = Bluetooth.State.NOT_CONNECTED;
                bt.notifyDisconnection();
                BluetoothService.this.stopSelf();
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "failure, disconnecting: " + status);
                gatt.close();
                bt.state = Bluetooth.State.NOT_CONNECTED;
                bt.notifyFailure("error code " + status);
                BluetoothService.this.stopSelf();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status != BluetoothGatt.GATT_SUCCESS) {
                bt.notifyFailure("service discovery failed: " + status);
                return;
            }

            final BluetoothGattService RxService = BluetoothService.this.gatt.getService(RX_SERVICE_UUID);
            rx = RxService.getCharacteristic(RX_CHAR_UUID);
            if (rx == null) {
                Log.d(TAG, "rx is null");
                bt.notifyFailure("no rx characteristic");
                return;
            }
            bt.state = Bluetooth.State.CONNECTED;
            Log.d(TAG, "Connected");
            bt.notifyConnection();
        }
    };

    @Override
    public void disconnect() {
        gatt.disconnect();
        //gatt.close();
        //stopSelf();
    }

    private void send(byte data[]) {
        //BluetoothAnimator.getInstance().stopAnimation();
        if (gatt == null || rx == null)
            return;
        rx.setValue(data);
        gatt.writeCharacteristic(rx);
    }
}
