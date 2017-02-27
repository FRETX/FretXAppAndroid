package fretx.version4.activities;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.view.MaterialIntroView;
import fretx.version4.Config;
import fretx.version4.R;

public class BluetoothActivity extends AppCompatActivity{
    private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    public static BluetoothGatt mBluetoothGatt;
    private boolean dialoguing_state = false;
    private boolean scanning_state = false;
    private boolean connection_state = false;
    private static final int SCAN_PERIOD = 10000;
    private static final int DISC = 1;
    public static final int FRET = 2;
    /** Services for Nordic **/
    public static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    /** Requests **/
    private static final int REQUEST_BLUETOOTH_ACTIVATION = 1;
    private final Runnable scanPeriodExpired = new Runnable() {
        @Override
        public void run() {
            scanning_state = false;
            btAdapter.stopLeScan(mLeScanCallback);
            Toast.makeText(getApplicationContext(), "No fretx found", Toast.LENGTH_LONG).show();
            finish();
        }
    };

    private static Handler.Callback callback = new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            byte[] writeBuf = (byte[]) msg.obj;
            switch (msg.what) {
                case BluetoothActivity.FRET:
                    String data = new String(writeBuf);
                    byte[] send;
                    try {
                        send = data.getBytes("UTF-8");
                        writeRXCharacteristic(send);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                case BluetoothActivity.DISC:
                    Config.bBlueToothActive = false;
                    mBluetoothGatt.disconnect();
                    break;
            }
            return false;
        }
    };
    public static Handler mHandler = new Handler(Looper.getMainLooper(), callback);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_activity);
        showTutorial();
    }

    private void showTutorial(){
        new MaterialIntroView.Builder(this)
                .enableDotAnimation(false)
                .enableIcon(false)
                .setFocusGravity(FocusGravity.CENTER)
                .setFocusType(Focus.ALL)
                .setDelayMillis(300)
                .enableFadeAnimation(true)
                .performClick(true)
                .setInfoText("Tap on FretX to connect.\n" +
                        "(If you don't see your device, turn it off and on again.)")
                .setTarget(findViewById(R.id.target))
                .setUsageId("tutorialChooseFretxFromBluetoothList") //THIS SHOULD BE UNIQUE ID
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!dialoguing_state) {
            if(btAdapter == null){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("No bluetooth on your device");
                builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialoguing_state = false;
                    }
                });
                AlertDialog dialog = builder.create();
                dialoguing_state = true;
                dialog.show();
            } else if (!btAdapter.isEnabled()) {
                Intent intent =new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                dialoguing_state = true;
                startActivityForResult(intent, REQUEST_BLUETOOTH_ACTIVATION);
            } else {
                scanning_state = true;
                scanLeDevice(true);
                Toast.makeText(getApplicationContext(), "Start scanning", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_BLUETOOTH_ACTIVATION) {
            if (resultCode == RESULT_OK)
                dialoguing_state = false;
            else
                finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (scanning_state) {
            scanning_state = false;
            scanLeDevice(false);
            Toast.makeText(getApplicationContext(), "Stop scanning", Toast.LENGTH_SHORT).show();
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {

            mHandler.postDelayed(scanPeriodExpired, SCAN_PERIOD);
            scanning_state = true;
            btAdapter.startLeScan(mLeScanCallback);
        } else {
            scanning_state = false;
            mHandler.removeCallbacks(scanPeriodExpired);
            btAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device,final int rssi, byte[] scanRecord) {
                        runOnUiThread(new Runnable() {
                        @Override
                        public void run()
                        {
                            Toast.makeText(getApplicationContext(), device.getName(),
                                    Toast.LENGTH_SHORT).show();
                            btAdapter.stopLeScan(mLeScanCallback);
                            mBluetoothGatt = device.connectGatt(getApplicationContext(), false, mGattCallback);
                        }
                    });
                }
            };

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt,int status,int newState) {

            if (status == BluetoothGatt.GATT_SUCCESS
                    && newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
                connection_state = true;
                finish();
            } else if (status == BluetoothGatt.GATT_SUCCESS
                    && newState == BluetoothProfile.STATE_DISCONNECTED) {
                connection_state = false;
            }
        }
    };

    public static void writeRXCharacteristic(byte[] value)
    {
        BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID);
        if (RxService == null) {
            return;
        }
        BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(RX_CHAR_UUID);
        if (RxChar == null) {
            return;
        }
        RxChar.setValue(value);
        mBluetoothGatt.writeCharacteristic(RxChar);
    }
}

