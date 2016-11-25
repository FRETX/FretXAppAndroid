package fretx.version4.fretxapi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;

import java.net.InetAddress;

import static com.loopj.android.http.AsyncHttpClient.log;

public class FretxApi {

    static String ApiBase = "http://fretx.herokuapp.com/";
    static AsyncHttpClient client = new AsyncHttpClient();
    static Context ctx = null;
    static boolean network_ready = false;
    static boolean api_ready = false;

    public static void setContext(Context c) { ctx = c; }
    public static boolean noContext() {
        if (ctx == null) {
            log.e("No Context", "Context Not Set for FretxApi");
            return true;
        }
        return false;
    }

    public static void Init(Context c) {
        setContext(c);
        network_ready = isNetworkConnected();
        if(!network_ready) return;
        api_ready = isApiAvailable();
    }

    public static void get(String path, FretxResponseHandler handler) {
        try                { client.get(ApiBase + path, null, handler); }
        catch(Exception e) { Log.d("result", e.toString()); }
    }

    private static boolean isNetworkConnected() {
        if(noContext()) return false;
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(ctx.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public static boolean isApiAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName(ApiBase);
            return !ipAddr.equals("");
        }
        catch (Exception e) { return false; }
    }

}
