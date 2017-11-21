package fretx.version4.fretxapi.song;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.util.DateTime;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import cz.msebera.android.httpclient.Header;
import fretx.version4.R;
import fretx.version4.activities.BaseActivity;
import fretx.version4.fretxapi.AppCache;
import fretx.version4.fretxapi.Network;

import static fretx.version4.activities.BaseActivity.getActivity;

public class SongList {
    private static final String TAG = "KJKP6_API_SONGLIST";
    private static final String API_BASE = "http://player.fretx.rocks/api/v1/";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'hh:mm:ss.SSS'Z'";

    private static JSONArray index;
    private static AlertDialog dialog;
    private final static AsyncHttpClient async_client  = new AsyncHttpClient();
    private final static ArrayList<SongCallback> callbacks = new ArrayList<>();

    private static boolean requesting;

    /* = = = = = = = = = = = = = = = = = = = = = PUBLIC = = = = = = = = = = = = = = = = = = = = = */
    public static void initialize() {
        dialog = createConnectionRetryDialog(getActivity());
        async_client.setConnectTimeout(20000);
        getIndexFromServer();
    }

    //// TODO: 06/05/17 remove code duplicate
    public static void getIndexFromServer() {
        if (Network.isConnected()) {
            Log.d(TAG, "Network ok");
            async_client.setTimeout(5);
            async_client.get(API_BASE + "/songs/index.json", new JsonHttpResponseHandler() {
                @Override
                public void onStart() {
                    Log.d(TAG, "start retrieval of index from server");
                    requesting = true;
                    notifyListener();
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "finish retrieval of index from server");
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray o) {
                    Log.d(TAG, "success");
                    index = o;
                    saveIndexToCache();
                    checkSongsInCache();
                    requesting = false;
                    notifyListener();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.d(TAG, "failure");
                    if (getIndexFromCache()) {
                        checkSongsInCache();
                    }
                    requesting = false;
                    notifyListener();
                }

                @Override
                public void onRetry(int retryNo) {
                    Log.d(TAG, "retry");
                }

                @Override
                public void onCancel() {
                    Log.d(TAG, "cancel");
                }

                @Override
                public void onUserException(Throwable error) {
                    Log.d(TAG, error.toString());
                }
            });
        } else{
            Log.d(TAG, "Network ko");
            dialog.show();
        }
    }

    public static int length() {
        return index != null ? index.length() : 0;
    }

    public static ArrayList<SongItem> getSongItems() {
        final SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        final ArrayList<SongItem> list = new ArrayList<>();
        final int length = SongList.length();
        for(int i = 0; i < length; ++i) {
            final SongItem item = SongList.getSongItem(i, df);
            if (item != null && item.published) {
                list.add(item);
            }
        }
        return list;
    }

    private static SongItem getSongItem(int i, SimpleDateFormat df) {
        try {
            JSONObject song = index.getJSONObject(i);
            return new SongItem(song, df);
        } catch (Exception e) {
            Log.d(TAG, String.format("Failed getting song %s", e.toString()));
            return null;
        }
    }

    public static SongItem getRandomSongItem() {
        final Random randomGen = new Random();
        final int randomIndex = randomGen.nextInt(SongList.length());
        final SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        return getSongItem(randomIndex, df);
    }

    /* = = = = = = = = = = = = = = = = = = = = = PRIVATE = = = = = = = = = = = = = = = = = = = = */
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
//        Log.d(TAG,"dumping song index");
//        Log.d(TAG,index.toString());
        JSONArray tmpIndex = new JSONArray();
        JSONObject tmpObj = new JSONObject();
        boolean tmpPublished;
        Log.d(TAG,"pre-filter index length: " + Integer.toString(index.length()));
        for (int i = 0; i < index.length(); i++) {
            tmpPublished = false;
            try {
                tmpObj = index.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                tmpPublished = tmpObj.getBoolean("published");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(tmpPublished){
                tmpIndex.put(tmpObj);
            }
        }
        index = tmpIndex;
        Log.d(TAG,"post-filter index length: " + Integer.toString(index.length()));
        byte[] index_bytes = index.toString().getBytes();
        AppCache.saveToCache("index.json", index_bytes);
    }

    private static void getSongFromServer(final String fretx_id) {
        Log.d(TAG,"Trying to get song " + fretx_id);
        String path = API_BASE + String.format( "/songs/%s.json", fretx_id );

        async_client.get(path, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                AppCache.saveToCache(fretx_id + ".json", responseBody);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d(TAG,"getSongFromServer Fail");
                Log.d(TAG, error.toString());
                getSongFromServer(fretx_id);
            }
        });

    }

    private static void checkSongsInCache() {
        boolean toastShown = false;
        for(int i = 0; i < index.length(); i++) {
            try {
                final JSONObject entry = index.getJSONObject(i);
                final String fretx_id = entry.getString("fretx_id");
                final String updatedAt = entry.getString("updated_at");
                final DateTime uploadedAtDatetime = new DateTime(updatedAt);
                final boolean published = entry.getBoolean("published");
                if(published == false)
                    continue;
                //forced update
                if(AppCache.exists(fretx_id + ".json") || updatedAt == null){
                    Log.d(TAG,"forced update for " + fretx_id);
                    Log.d(TAG,"last_modified: " + Long.toString(AppCache.last_modified(fretx_id + ".json")));
                    Log.d(TAG,"uploaded_at: " + Long.toString(uploadedAtDatetime.getValue()));
                    if(!toastShown){
                        Toast.makeText(getActivity(), "Downloading new song content...", Toast.LENGTH_LONG).show();
                        toastShown = true;
                    }
                    getSongFromServer(fretx_id);
                }
                //time update
                else if (AppCache.last_modified(fretx_id + ".json") <= uploadedAtDatetime.getValue()) {
                    long timeDiff = AppCache.last_modified(fretx_id + ".json") - uploadedAtDatetime.getValue();
                    Log.d(TAG,"time update for " + fretx_id);
                    Log.d(TAG,"last_modified: " + Long.toString(AppCache.last_modified(fretx_id + ".json")));
                    Log.d(TAG,"uploaded_at: " + Long.toString(uploadedAtDatetime.getValue()));
                    Log.d(TAG,"time diff: " + Long.toString(timeDiff));
                    if(!toastShown){
                        Toast.makeText(getActivity(), "Downloading new song content...", Toast.LENGTH_LONG).show();
                        toastShown = true;
                    }
                    getSongFromServer(fretx_id);
                }
            } catch (Exception e) {
                Log.d(TAG, String.format("Failed Checking Song In Cache - %s", e.toString()));
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
                initialize();
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
        cb.onUpdate(requesting, index);
    }

    private static void notifyListener() {
        for (SongCallback callback: callbacks) {
            callback.onUpdate(requesting, index);
        }
    }
}
