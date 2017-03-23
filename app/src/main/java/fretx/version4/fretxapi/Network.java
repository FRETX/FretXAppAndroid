package fretx.version4.fretxapi;

import android.content.Context;
import android.net.ConnectivityManager;

public class Network {

    private static Context context;

    public static void initialize(Context c) { context = c; }

    public static boolean isConnected() {
        if( context == null ) return false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

}
