package fretx.version4;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import rocks.fretx.audioprocessing.*;

/**
 * Created by AsmodeusStudio on 29/12/2015.
 */
public class BluetoothClass {

    public static String tag = "debug";
    public static BluetoothSocket mmSocket;
    static final int SUCCESS_CONNECT = 0;
    static final int MESSAGE_READ = 1;
    static final int ARDUINO = 2;
    static ConnectedThread connectedThread = null;
    static Handler.Callback callback = new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            Log.i("debug", "in handler");
            switch (msg.what) {
                case BluetoothClass.SUCCESS_CONNECT:
                    BluetoothClass.connectedThread = new BluetoothClass.ConnectedThread((BluetoothSocket) msg.obj);
                    break;
                case BluetoothClass.ARDUINO:
                    if (Config.bBlueToothActive == true){
                        BluetoothClass.connectedThread.write((byte[])msg.obj);
                    }
                    Log.d("BT",msg.toString());
                    break;
            }
            return false;
        }
    };
    public static Handler mHandler = new Handler(callback);

    static public class ConnectedThread extends Thread {
        //private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            //mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(tag, "tmpIn or tmpOut");
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer;  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    buffer = new byte[1024];
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(tag, "mmOutStream");
            }
        }
    }


    public static void sendToFretX(Object o){
        byte[] bluetoothArray = new byte[] {Byte.valueOf("0")};
        if(o instanceof byte[]){
            bluetoothArray = (byte[])o;
            //TODO: make consistent with methods that already add a terminating 0
        }
        else if(o instanceof Integer){
            bluetoothArray = new byte[]  {Byte.valueOf( Integer.toString((int)o) ),Byte.valueOf("0")};
        } else if (o instanceof int[]){
            int[] intArray = ((int[]) o);
            bluetoothArray = new byte[intArray.length+1];
            for (int i = 0; i < intArray.length; i++) {
                bluetoothArray[i] = Byte.valueOf(Integer.toString(intArray[i]));
            }
            bluetoothArray[intArray.length] = Byte.valueOf("0");
        } else if (o instanceof String){
            bluetoothArray = new byte[] {Byte.valueOf((String)o),Byte.valueOf("0")};
        } else if (o instanceof String[]){
            String[] stringArray = (String[])o;
            bluetoothArray = new byte[stringArray.length+1];
            for (int i = 0; i < stringArray.length; i++) {
                bluetoothArray[i] = Byte.valueOf(stringArray[i]);
            }
            bluetoothArray[stringArray.length] = Byte.valueOf("0");
        } else {
            Log.d("sendToFretX","data type could not be resolved, turning off lights");
        }
        ConnectThread connectThread = new ConnectThread(bluetoothArray);
        connectThread.run();
    }

    public static void lightsOff(){
        sendToFretX(Util.str2array("{0}"));
    }

    /////////////////////////////////BlueToothConnection/////////////////////////
    static private class ConnectThread extends Thread {
        byte[] array;

        public ConnectThread(byte[] tmp) {
            array = tmp;
        }

        public void run() {
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                Util.startViaData(array);
            } catch (Exception connectException) {
                Log.i(BluetoothClass.tag, "connect failed");
                // Unable to connect; close the socket and get out
                try {
                    BluetoothClass.mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(BluetoothClass.tag, "mmSocket.close");
                }
                return;
            }
            // Do work to manage the connection (in a separate thread)
            if (BluetoothClass.mHandler == null)
                Log.v("debug", "mHandler is null @ obtain message");
            else
                Log.v("debug", "mHandler is not null @ obtain message");
        }
    }
}