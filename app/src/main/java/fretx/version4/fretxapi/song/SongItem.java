package fretx.version4.fretxapi.song;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fretx.version4.Util;
import fretx.version4.fretxapi.AppCache;

public class SongItem {
    public String fretx_id;
    public String youtube_id;
    public String title;
    public String artist;
    public String song_title;
    public String uploaded_on;
    public boolean published;

    public SongItem(JSONObject song) {
        try {
            this.fretx_id = song.getString("fretx_id");
            this.youtube_id = song.getString("youtube_id");
            this.title = song.getString("title");
            this.artist = song.getString("artist");
            this.song_title = song.getString("song_title");
            this.uploaded_on = song.getString("uploaded_on");
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
        String songJsonString = AppCache.getFromCache(songFile());
        JSONObject songJson = new JSONObject();
        String punchesJsonString = "";
        try{
            songJson = new JSONObject(songJsonString);
        } catch (JSONException e){
            Log.e("SongItem","Error reading song json into JSONObject - " + e);
        }
        ArrayList<SongPunch> punches = new ArrayList<>();
        try{
            punchesJsonString = songJson.getString("punches");
        } catch (JSONException e){
            Log.e("SongItem","Error reading punches array string into JSONArray - " + e);
        }
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

}
