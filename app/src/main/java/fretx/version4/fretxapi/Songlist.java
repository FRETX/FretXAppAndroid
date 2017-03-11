package fretx.version4.fretxapi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.util.DateTime;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;



import cz.msebera.android.httpclient.Header;
import fretx.version4.Config;
import fretx.version4.R;

public class Songlist {

    static String          apiBase       = Config.apiBase;
    static JSONArray       index         = new JSONArray();
    static AsyncHttpClient async_client  = new AsyncHttpClient();
    static Boolean         ready         = false;

    public static void initialize(final Context ctx) {
        fireBusy();

        if(!getIndexFromCache()){
            if (Network.isConnected()) {
                getIndexFromServer();
            } else{
                AlertDialog dialog = createConnectionRetryDialog(ctx);
                dialog.show();
            }
        }

        if( Network.isConnected() ) { getIndexFromServer(); }
        else {
            boolean success = getIndexFromCache();
            if(!success){
                fireReady();
            }
        }
    }

    //////////////////////////// INDEXING ///////////////////////////////////

    public static void getIndexFromServer() {
        async_client.setTimeout(5);
        async_client.get(apiBase + "/songs/index.json", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int status, Header[] headers, JSONArray data) {
                index = data;
                saveIndexToCache();
                fireReady();
                Log.d("FRETX API", String.format("got index: %s", index ));
                checkSongsInCache();
            }
        });
    }

    private static Boolean getIndexFromCache() {
        String data = AppCache.getFromCache("index.json");
        try { index = new JSONArray(data); } catch (Exception e) { return false; }
        fireReady();
        return true;
    }

    private static void saveIndexToCache() {
        byte[] index_bytes = index.toString().getBytes();
        AppCache.saveToCache("index.json", index_bytes);
    }

    public static void forceDownloadIndexFromServer() {
        async_client.get(apiBase + "/songs/index.json", new JsonHttpResponseHandler() {
            @Override public void onSuccess(int status, Header[] headers, JSONArray data) {
                index = data;
                saveIndexToCache();
                fireReady();
                Log.d("FRETX API", String.format("got index: %s", index ));
                try {
                    for(int i = 0; i < index.length(); i++) {

                        JSONObject entry       = index.getJSONObject(i);
                        String     youtube_id  = entry.getString("youtube_id");
                        String     fretx_id = entry.getString("fretx_id");
                        DateTime   uploaded_on = new DateTime(entry.getString("uploaded_on"));
                        Boolean    is_latest   = AppCache.last_modified(youtube_id + ".json") > uploaded_on.getValue();

                        Log.d("FRETX API", "Getting Song From Server: " + entry.getString("title"));
                        getSongFromServer( fretx_id );
                    }
                }
                catch (Exception e) {
                    Log.d("FRETX API", String.format("Failed Checking Songs In Cache\r\n%s", e.toString()));
                }
            }
        });
    }
    //////////////////////////// INDEXING ///////////////////////////////////

    ///////////////////////////// SONGS /////////////////////////////////////

    public static void getSongFromServer(final String fretx_id) {

        String path = apiBase + String.format( "/songs/%s.json", fretx_id );

        async_client.get(path, new AsyncHttpResponseHandler() {

           @Override public void onSuccess(int status, Header[] headers, byte[] body) {
               AppCache.saveToCache(fretx_id + ".json", body);
           }

           @Override public void onFailure(int status, Header[] headers, byte[] error, Throwable e) {
               Log.d("FRETX API", e.toString());
           }

        });

    }

    private static void checkSongsInCache() {
        try {
            for(int i = 0; i < index.length(); i++) {

                JSONObject entry       = index.getJSONObject(i);
                String     youtube_id  = entry.getString("youtube_id");
	            String     fretx_id    = entry.getString("fretx_id");
                DateTime   uploaded_on = new DateTime(entry.getString("uploaded_on"));
                Boolean    is_latest   = AppCache.last_modified(fretx_id + ".json") > uploaded_on.getValue();

                if( AppCache.exists(fretx_id + ".json") && is_latest ) { continue; }

                Log.d("FRETX API", "Getting Song From Server: " + entry.getString("title"));
                getSongFromServer( fretx_id );
            }
        }

        catch (Exception e) {
            Log.d("FRETX API", String.format("Failed Checking Songs In Cache\r\n%s", e.toString()));
        }
    }

    ///////////////////////////// SONGS /////////////////////////////////////

    ///////////////////////////// GUI ACCESS /////////////////////////////////////

    public static int length() { return index.length(); }

    public static SongItem getSongItem(int i) {
        try {
            JSONObject song = index.getJSONObject(i);
            //Drawable image = Util.LoadImageFromWeb("http://img.youtube.com/vi/" + song.getString("youtube_id") + "/0.jpg");

            return new SongItem(song.getString("fretx_id"), song.getString("youtube_id"), song.getString("title"), song.getString("artist"),song.getString("song_title"),song.getString("uploaded_on"));
        }
        catch (Exception e) {
            Log.d("FRETX API", String.format("Failed Getting Song Item\r\n%s", e.toString()));
            return null;
        }
    }

    ///////////////////////////// GUI ACCESS /////////////////////////////////////

    ///////////////////////////// CALLBACKS /////////////////////////////////////

    private static ArrayList<Callback> callbacks = new ArrayList<>();

    public static void setListener(Callback cb) {
      callbacks.add(cb);
      if(ready) { cb.onReady(); }
      else      { cb.onBusy();  }
    }

    public interface Callback {
        void onBusy();
        void onReady();
    }

    public static AlertDialog createConnectionRetryDialog(final Context ctx){
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage(ctx.getString(R.string.no_internet_retry))
                .setTitle(ctx.getString(R.string.no_internet));
        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                initialize(ctx);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog dialog = builder.create();
        return dialog;
    }

    private static void fireBusy()  { ready = false; for(int i=0; i < callbacks.size(); i++) { callbacks.get(i).onBusy();  } }
    private static void fireReady() { ready = true;  for(int i=0; i < callbacks.size(); i++) { callbacks.get(i).onReady(); } }

    ///////////////////////////// CALLBACKS /////////////////////////////////////

}
