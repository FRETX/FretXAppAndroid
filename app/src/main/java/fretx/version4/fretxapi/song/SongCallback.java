package fretx.version4.fretxapi.song;

import org.json.JSONArray;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 05/05/17 16:34.
 */

public interface SongCallback {
    void onUpdate(boolean requesting, JSONArray index);
}
