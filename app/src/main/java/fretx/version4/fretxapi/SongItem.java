package fretx.version4.fretxapi;

import java.util.Hashtable;

public class SongItem {
    public String songName;
    public String songUrl;
    public String songTxt;
    public String youtube_id;

    public SongItem(String name, String url, String text) {
        songName = name;
        songUrl  = url;
        songTxt  = text;
        youtube_id = url;
    }

    public String imageURL() {
        return "http://img.youtube.com/vi/" + songUrl + "/0.jpg";
    }

    public String songTxt()  { return AppCache.getFromCache(youtube_id + ".txt"); }
    public String songFile() { return youtube_id + ".txt"; }

}
