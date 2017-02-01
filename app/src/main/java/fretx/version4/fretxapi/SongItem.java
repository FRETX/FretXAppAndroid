package fretx.version4.fretxapi;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fretx.version4.Util;
import rocks.fretx.audioprocessing.Chord;

public class SongItem {
    public String youtube_id;
    public String title;
    public String artist;
    public String song_title;
    public String uploaded_on;

//    public JSONArray punches;


    public SongItem(String youtube_id, String title, String artist, String song_title, String uploaded_on) {
        this.youtube_id = youtube_id;
        this.title = title;
        this.artist = artist;
        this.song_title = song_title;
        this.uploaded_on = uploaded_on;
    }

    public String imageURL() {
        return "http://img.youtube.com/vi/" + youtube_id + "/0.jpg";
    }
    public String songFile() { return youtube_id + ".json"; }
    public ArrayList<SongPunch> punches(){
        String songJsonString = AppCache.getFromCache(songFile());
        JSONObject songJson = new JSONObject();
        String punchesJsonString = "";
        ArrayList<SongPunch> punches = new ArrayList<>();
        try{
            songJson = new JSONObject(songJsonString);
        } catch (JSONException e){
            Log.e("SongItem","Error reading song json into JSONObject - " + e);
        }
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
