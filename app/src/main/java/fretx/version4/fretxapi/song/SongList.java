package fretx.version4.fretxapi.song;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.api.client.util.DateTime;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import cz.msebera.android.httpclient.Header;
import fretx.version4.R;
import fretx.version4.activities.BaseActivity;
import fretx.version4.fretxapi.AppCache;
import fretx.version4.fretxapi.Network;

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
        dialog = createConnectionRetryDialog(BaseActivity.getActivity());
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
        byte[] index_bytes = index.toString().getBytes();
        AppCache.saveToCache("index.json", index_bytes);
    }

    private static void getSongFromServer(final String fretx_id) {

        String path = API_BASE + String.format( "/songs/%s.json", fretx_id );

        async_client.get(path, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                AppCache.saveToCache(fretx_id + ".json", responseBody);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d(TAG, error.toString());
            }
        });

    }

    private static void checkSongsInCache() {
        for(int i = 0; i < index.length(); i++) {
            try {
                final JSONObject entry = index.getJSONObject(i);
                final String fretx_id = entry.getString("fretx_id");
                final String updatedAt = entry.getString("updated_at");
                final DateTime uploadedAtDatetime = new DateTime(updatedAt);

                //forced update
                if(AppCache.exists(fretx_id + ".json") || updatedAt == null){
                    getSongFromServer(fretx_id);
                }
                //time update
                else if (AppCache.last_modified(fretx_id + ".json") <= uploadedAtDatetime.getValue()) {
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
