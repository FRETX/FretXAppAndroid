package fretx.version4.paging.learn.guided;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import rocks.fretx.audioprocessing.Chord;

class GuidedChordExercise {
	String name;
	String id;
	ArrayList<Chord> chords;
	int nRepetitions;

	GuidedChordExercise(){
		name = "";
		id = "";
		chords = new ArrayList<>();
		nRepetitions = 0;
	}

	GuidedChordExercise(JSONObject chordExercise){
		JSONObject chordJson;
		JSONArray tmpChordsArray;
		try {
			this.name = chordExercise.getString("name");
			this.id = chordExercise.getString("id");
			this.nRepetitions = chordExercise.getInt("nRepetitions");
			tmpChordsArray = chordExercise.getJSONArray("chords");
			this.chords = new ArrayList<>();
			for (int j = 0; j < tmpChordsArray.length(); j++) {
				chordJson = tmpChordsArray.getJSONObject(j);
				Log.d("adding chord", chordJson.getString("root") + chordJson.getString("type"));
				this.chords.add(new Chord(chordJson.getString("root"), chordJson.getString("type")));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
