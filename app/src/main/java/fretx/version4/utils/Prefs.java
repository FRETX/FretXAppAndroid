package fretx.version4.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 09/06/17 12:55.
 */

public class Prefs {
    private final static String TAG = "KJKP6_PREFS";
    public final static String LEFT_HANDED = "left";
    public final static String RIGHT_HANDED = "right";
    public final static String ACCOUSTIC_GUITAR = "acoustic";
    public final static String ELECTRIC_GUITAR = "electric";
    public final static String CLASSICAL_GUITAR = "classical";
    public final static String LEVEL_BEGINNER = "beginner";
    public final static String LEVEL_PLAYER = "player";
    public final static String NO_SONG_PREVIEW = "true";
    public final static String SONG_PREVIEW = "false";

    public String guitar;
    public String hand;
    public String level;
    public String tunerTutorial;
    public String previewTutorial;
    public String playTutorial;
    public String songPreview;

    public Prefs() {
        guitar = ACCOUSTIC_GUITAR;
        level = LEVEL_BEGINNER;
        hand = RIGHT_HANDED;
        tunerTutorial = "true";
        previewTutorial = "true";
        playTutorial = "true";
        songPreview = "true";
    }

    public Prefs(Prefs prefs) {
        this.guitar = prefs.guitar;
        this.hand = prefs.hand;
        this.level = prefs.level;
        this.tunerTutorial= prefs.tunerTutorial;
        this.previewTutorial = prefs.previewTutorial;
        this.playTutorial = prefs.playTutorial;
        this.songPreview = prefs.songPreview;
    }

    public String toJson() {
        final StringBuffer sb = new StringBuffer("{\"guitar\":\"");
        sb.append(guitar);
        sb.append("\",\"hand\":\"");
        sb.append(hand);
        sb.append("\",\"level\":\"");
        sb.append(level);
        sb.append("\",\"tunerTutorial\":\"");
        sb.append(tunerTutorial);
        sb.append("\",\"previewTutorial\":\"");
        sb.append(previewTutorial);
        sb.append("\",\"playTutorial\":\"");
        sb.append(playTutorial);
        sb.append("\",\"songPreview\":\"");
        sb.append(songPreview);
        sb.append("\"}");
        return sb.toString();
    }

    public static Prefs fromJson(String json) {
        try {
            final JSONObject jsonRoot = new JSONObject(json);
            final String hand = jsonRoot.getString("hand");
            final String guitar = jsonRoot.getString("guitar");
            final String level = jsonRoot.getString("level");
            final String tunerTutorial = jsonRoot.getString("tunerTutorial");
            final String previewTutorial = jsonRoot.getString("previewTutorial");
            final String playTutorial = jsonRoot.getString("playTutorial");
            final String songPreview = jsonRoot.getString("songPreview");
            final Builder builder = new Builder()
                    .setHand(hand)
                    .setGuitar(guitar)
                    .setLevel(level)
                    .setTunerTutorial(tunerTutorial)
                    .setPreviewTutorial(previewTutorial)
                    .setPlayTutorial(playTutorial)
                    .setSongPreview(songPreview);
            return builder.build();
        } catch (JSONException e) {
            Log.v(TAG, "parsing from json failed");
            e.printStackTrace();
            return null;
        }
    }

    public static class Builder {
        private Prefs prefs;

        public Builder() {
            prefs = Preference.getInstance().getPrefsCopy();
        }

        public Builder setGuitar(String guitar) {
            prefs.guitar = guitar;
            return this;
        }

        public Builder setHand(String hand) {
            prefs.hand = hand;
            return this;
        }

        public Builder setLevel(String level) {
            prefs.level= level;
            return this;
        }

        public Builder setTunerTutorial(String tunerTutorial) {
            prefs.tunerTutorial = tunerTutorial;
            return this;
        }

        public Builder setPreviewTutorial(String previewTutorial) {
            prefs.previewTutorial = previewTutorial;
            return this;
        }

        public Builder setPlayTutorial(String playTutorial) {
            prefs.playTutorial = playTutorial;
            return this;
        }

        public Builder setSongPreview(String songPreview) {
            prefs.songPreview = songPreview;
            return this;
        }

        public Prefs build() {
            return prefs;
        }
    }
}