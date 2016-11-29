package fretx.version4.fretxapi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.text.SimpleDateFormat;

import android.util.Log;
import android.content.Context;
import android.net.ConnectivityManager;

import com.google.api.client.util.DateTime;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;



import cz.msebera.android.httpclient.Header;

public class FretxApi {

    static JSONArray       songlist;
    static File            index_cache;
    static String          apiBase       = "http://fretx.herokuapp.com/";
    static AsyncHttpClient client        = new AsyncHttpClient();
    static Context         context       = null;
    static boolean         network_ready = false;

    public static void    setContext(Context c) { context = c; }
    public static boolean noContext()           { return context == null; }

    public static void initialize(Context c) {
        setContext(c);
        if( isNetworkConnected() ) { getListFromServer(); }
        else                       { getListFromCache();  }
    }

    //////////////////////////// REQUESTS ///////////////////////////////////

    public static void getListFromServer() {
        get("/songs/index.json", new JsonHttpResponseHandler() {
            @Override public void onSuccess(int status, Header[] headers, JSONArray list) {
                songlist      = list;
                Log.d("FRETX API", String.format("got index: %s", songlist ));
                saveListToCache();
                checkForSongsInCache();
            }
        });
    }

    public static void getSongFromServer(final String youtube_id) {
        String path = apiBase + String.format("/songs/%s.txt",youtube_id);
        client.get(path, new AsyncHttpResponseHandler() {
           @Override public void onSuccess(int status, Header[] headers, byte[] body) {
               saveToCache(youtube_id + ".txt", body);
           }

           @Override public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
               Log.d("FRETX API", e.toString());
           }
        });
    }

    public static void get(String path, JsonHttpResponseHandler handler) {
        try                { client.get(apiBase + path, null, handler); }
        catch(Exception e) { Log.d("FRETX API", e.toString());    }
    }

    //////////////////////////// REQUESTS ///////////////////////////////////

    //////////////////////////// CACHING ///////////////////////////////////

    private static void saveListToCache() {
        if(index_cache == null) index_cache = new File(context.getCacheDir(), "index.json");
        try {
            FileOutputStream fos = new FileOutputStream(index_cache);
            fos.write(songlist.toString().getBytes());
            fos.close();
            Log.d("FRETX API", "Saved To Cache");
        }   catch(Exception e) { Log.d("FRETX API", "Failed Saving To Cache"); }
    }

    private static Boolean getListFromCache() {
        if(index_cache == null) index_cache = new File(context.getCacheDir(), "index.json");
        try {
            int i; String buff = "";
            FileInputStream fis = new FileInputStream(index_cache);
            while( (i=fis.read() ) != -1 ) { buff += (char)i; }
            songlist = new JSONArray(buff);
            Log.d("FRETX API", String.format("Retrieved From Cache: %s", buff ));
        }   catch(Exception e) { Log.d("FRETX API", "Failed Retrieving From Cache"); return false; }
        return true;
    }

    private static void checkForSongsInCache() {
        try {
            for(int i = 0; i < songlist.length(); i++) {
                JSONObject row         = songlist.getJSONObject(i);
                String     filename    = row.getString("youtube_id") + ".txt";
                File       songfile    = new File(context.getCacheDir(), filename);
                DateTime   uploaded_on = new DateTime(row.getString("uploaded_on"));
                Boolean    is_latest   = songfile.lastModified() > uploaded_on.getValue();

                if( songfile.exists() && is_latest ) {
                    Log.d("FRETX API", "Getting Song From Cache: " + row.getString("title"));
                    continue;
                }

                Log.d("FRETX API", "Getting Song From Server: " + row.getString("title"));
                getSongFromServer( row.getString("youtube_id") );
            }
        }   catch (Exception e) {
            Log.d("FRETX API", String.format("Failed Checking Songs In Cache\r\n%s", e.toString()));
        }
    }

    private static String getFromCache(String path) {
        try {
            int i; String buff = "";
            File f = new File(context.getCacheDir(), path);
            FileInputStream fis = new FileInputStream(index_cache);
            while ((i = fis.read()) != -1) { buff += (char) i; }
            return buff;
        }   catch (Exception e) { Log.d("FRETX API", String.format("Failed Getting From Cache %s\r\n%s", path, e.toString())); return ""; }
    }

    private static void saveToCache(String path, byte[] body) {
        try {
            File songfile = new File(context.getCacheDir(), path);
            if( songfile.exists() ) songfile.delete();
            songfile = new File(context.getCacheDir(), path);
            FileOutputStream fos = new FileOutputStream(songfile);
            fos.write(body);
            fos.close();
            Log.d("FRETX API", String.format("Saved To Cache %s", path));
        }   catch (Exception e) { Log.d("FRETX API", String.format("Failed Saving To Cache %s\r\n%s", path, e.toString())); }
    }

    //////////////////////////// CACHING ///////////////////////////////////

    ///////////////////////// CONNECTIVITY /////////////////////////////////

    private static boolean isNetworkConnected() {
        if( noContext() ) return false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        network_ready = cm.getActiveNetworkInfo() != null;
        return network_ready;
    }

    ///////////////////////// CONNECTIVITY /////////////////////////////////

}
