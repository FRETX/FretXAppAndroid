package fretx.version4.fretxapi.song;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import com.google.api.client.util.DateTime;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.ResponseHandlerInterface;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import fretx.version4.Config;
import fretx.version4.R;
import fretx.version4.activities.BaseActivity;
import fretx.version4.fretxapi.AppCache;
import fretx.version4.fretxapi.Network;

public class SongList {
    private final static String TAG = "KJKP6_API_SONGLIST";

    private static String apiBase = Config.apiBase;
    private static JSONArray index = new JSONArray();
    private final static AsyncHttpClient async_client  = new AsyncHttpClient();
    private final static ArrayList<SongCallback> callbacks = new ArrayList<>();

    /* = = = = = = = = = = = = = = = = = = = = = PUBLIC = = = = = = = = = = = = = = = = = = = = = */
    public static void initialize(final Context ctx) {
        if (getIndexFromCache()) {
            Log.d(TAG, "Data already in cache");
            notifyListener();
        } else {
            Log.d(TAG, "Need to retrieve data");
            if (Network.isConnected()) {
                Log.d(TAG, "Network ok");
                getIndexFromServer();
            } else{
                Log.d(TAG, "Network ko");
                AlertDialog dialog = createConnectionRetryDialog(BaseActivity.getActivity());
                dialog.show();
            }
        }
    }

    public static void forceDownloadIndexFromServer() {
        if (Network.isConnected()) {
            Log.d(TAG, "Network ok");
            async_client.get(apiBase + "/songs/index.json", new JsonHttpResponseHandler() {
                @Override
                public void onStart() {
                    Log.d(TAG, "on start");
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray data) {
                    Log.d(TAG, "on success");
                    index = data;
                    saveIndexToCache();
                    checkSongsInCache();
                    notifyListener();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable e, JSONArray data) {
                    Log.d(TAG, "on failure");
                    notifyListener();
                }
            });
        } else {
            Log.d(TAG, "Network ko");
            AlertDialog dialog = createConnectionRetryDialog(BaseActivity.getActivity());
            dialog.show();
        }
    }

    public static int length() {
        return index.length();
    }

    public static SongItem getSongItem(int i) {
        try {
            JSONObject song = index.getJSONObject(i);
            return new SongItem(song);
        } catch (Exception e) {
            Log.d(TAG, String.format("Failed Getting Song Item\r\n%s", e.toString()));
            return null;
        }
    }

    //// TODO: 06/05/17 clean this handler, useless overides
    /* = = = = = = = = = = = = = = = = = = = = = PRIVATE = = = = = = = = = = = = = = = = = = = = */
    private static void getIndexFromServer() {
        async_client.setTimeout(5);
        async_client.get(apiBase + "/songs/index.json", new JsonHttpResponseHandler() {

            @Override
            public void onStart() {
                Log.d(TAG, "START");
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "FINISH");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray o) {
                Log.d(TAG, "SUCCESS");
                index = o;
                saveIndexToCache();
                Log.d(TAG, String.format("got index: %s", index ));
                checkSongsInCache();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d(TAG, "FAILURE");
                notifyListener();
            }

            @Override
            public void onRetry(int retryNo) {
                Log.d(TAG, "RETRY");
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "CANCEL");
            }

            @Override
            public void onUserException(Throwable error) {
                Log.d(TAG, error.toString());
            }
        });
    }

    private static boolean getIndexFromCache() {
        String data = AppCache.getFromCache("index.json");
        try {
            index = new JSONArray(data);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static void saveIndexToCache() {
        byte[] index_bytes = index.toString().getBytes();
        AppCache.saveToCache("index.json", index_bytes);
    }

    private static void getSongFromServer(final String fretx_id) {

        String path = apiBase + String.format( "/songs/%s.json", fretx_id );

        //// TODO: 06/05/17 clean this handler, useless overides
        async_client.get(path, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                Log.d(TAG, "START");
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "FNINISH");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d(TAG, "SUCCESS");
                AppCache.saveToCache(fretx_id + ".json", responseBody);
                notifyListener();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d(TAG, error.toString());
                notifyListener();
            }

            @Override
            public void onRetry(int retryNo) {
                Log.d(TAG, "RETRY");
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "CANCEL");
            }

            @Override
            public void onUserException(Throwable error) {
                Log.d(TAG, error.toString());
            }
        });

    }

    private static void checkSongsInCache() {
        for(int i = 0; i < index.length(); i++) {
            try {
                JSONObject entry       = index.getJSONObject(i);
                String     fretx_id    = entry.getString("fretx_id");

                if(entry.getString("uploaded_on") == null){
                    Log.e(TAG,"Uploaded date null for" + fretx_id);
                    getSongFromServer(fretx_id);
                    continue;
                }
                Log.d(TAG, entry.getString("uploaded_on"));
                DateTime   uploaded_on = new DateTime(entry.getString("uploaded_on"));
                boolean is_latest   = AppCache.last_modified(fretx_id + ".json") > uploaded_on.getValue();
                Log.d(TAG,"Parsed JSON for " + fretx_id);
                if(AppCache.exists(fretx_id + ".json") && is_latest ) {
                    continue;
                }
                Log.d(TAG, "Getting Song From Server: " + entry.getString("title"));
                getSongFromServer(fretx_id);
            } catch (Exception e) {
                Log.d(TAG, String.format("Failed Checking Song In Cache\r\n%s", e.toString()));
            }
        }
    }

    /* = = = = = = = = = = = = = = = = = = = = = DIALOG = = = = = = = = = = = = = = = = = = = = = */
    //// TODO: 05/05/17 move this code to the calling activity
    private static AlertDialog createConnectionRetryDialog(final Context ctx){
        final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
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
                notifyListener();
            }
        });
        return builder.create();
    }

    /* = = = = = = = = = = = = = = = = = = = = LISTENER = = = = = = = = = = = = = = = = = = = = = */
    //// TODO: 05/05/17 add remove listener method
    public static void setListener(SongCallback cb) {
        callbacks.add(cb);
    }

    private static void notifyListener() {
        for (SongCallback callback: callbacks) {
            callback.onUpdate();
        }
    }
}
