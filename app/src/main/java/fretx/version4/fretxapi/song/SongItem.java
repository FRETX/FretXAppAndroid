package fretx.version4.fretxapi.song;

import android.util.Log;

import com.google.api.client.util.DateTime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import fretx.version4.utils.Util;
import fretx.version4.fretxapi.AppCache;
import rocks.fretx.audioprocessing.Chord;

public class SongItem {
    private final String TAG = "KJKP6_SONGITEM";
    public String fretx_id;
    public String youtube_id;
    public String title;
    public String artist;
    public String song_title;
    public String uploaded_on;
    public Date updated_at;
    public boolean published;

    public SongItem(JSONObject song, SimpleDateFormat dateFormat) {
        try {
            fretx_id = song.getString("fretx_id");
            youtube_id = song.getString("youtube_id");
            title = song.getString("title");
            artist = song.getString("artist");
            song_title = song.getString("song_title");
            uploaded_on = song.getString("uploaded_on");
            try {
                updated_at = dateFormat.parse(song.getString("updated_at"));
            } catch (ParseException e) {
                e.printStackTrace();
                updated_at = null;
            }
            this.published = song.getString("published").equals("true");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String imageURL() {
        return "http://img.youtube.com/vi/" + youtube_id + "/0.jpg";
    }

    public String songFile() {
        return fretx_id + ".json";
    }

    public ArrayList<SongPunch> punches() {
        final JSONObject songJson;
        try{
            songJson = new JSONObject(AppCache.getFromCache(songFile()));
        } catch (JSONException e){
            Log.e("SongItem","Error reading song json into JSONObject - " + e);
            return null;
        }
        String punchesJsonString;
        try{
            punchesJsonString = songJson.getString("punches");
        } catch (JSONException e){
            Log.e("SongItem","Error reading punches array string into JSONArray - " + e);
            return null;
        }
        final ArrayList<SongPunch> punches = new ArrayList<>();
        try{
            JSONArray punchesJson = new JSONArray(punchesJsonString);
            JSONObject punchJson, chordJson;
            SongPunch punch;
            for (int i = 0; i < punchesJson.length(); i++) {
                punchJson = punchesJson.getJSONObject(i);
                chordJson = punchJson.getJSONObject("chord");
                punch = new SongPunch(punchJson.getInt("time_ms"), chordJson.getString("root"), chordJson.getString("quality"), Util.str2array(chordJson.getString("fingering")));
                punches.add(punch);
            }
        } catch(JSONException e){
            Log.e("SongItem",e.toString());
        }
        return punches;
    }

    public ArrayList<Chord> getChords() {
        final ArrayList<SongPunch> punches = punches();
        final ArrayList<Chord> chords = new ArrayList<>();

        if (punches == null)
            return chords;
        for (int i = 0; i < punches.size(); i++) {
            final SongPunch tmpSp = punches.get(i);
            final String root = tmpSp.root;
            String type = tmpSp.type.toLowerCase();

            if((root + type).equals("No Chord"))
                continue;
            if (root.equals("") || type.equals(""))
                continue;
            if (type.equals("min")) {
                type = "m";
                Log.d(TAG, "new type " + type);
            }
            try {
                chords.add(new Chord(root, type));
            } catch(Exception e){
                Log.d(TAG, e.toString());
            }
        }
        return chords;
    }
}
