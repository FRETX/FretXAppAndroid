package fretx.version4.paging.learn.guided;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import rocks.fretx.audioprocessing.Chord;

public class GuidedExercise {
	private String name;
	private String id;
	private ArrayList<Chord> chords;
	private int nRepetitions;
	private ArrayList<String> children;

	public GuidedExercise(){
		name = "";
		id = "";
		chords = new ArrayList<>();
		nRepetitions = 0;
        children = new ArrayList<>();
    }

	public GuidedExercise(JSONObject chordExercise){
		try {
			this.name = chordExercise.getString("name");
			this.id = chordExercise.getString("id");
			this.nRepetitions = chordExercise.getInt("nRepetitions");
			final JSONArray tmpChordsArray;
			tmpChordsArray = chordExercise.getJSONArray("chords");
			this.chords = new ArrayList<>();
			for (int j = 0; j < tmpChordsArray.length(); j++) {
				final JSONObject chordJson;
				chordJson = tmpChordsArray.getJSONObject(j);
				this.chords.add(new Chord(chordJson.getString("root"), chordJson.getString("type")));
			}
			final JSONArray tmpChildArray;
			tmpChildArray = chordExercise.getJSONArray("children");
			this.children = new ArrayList<>();
			for (int j = 0; j < tmpChildArray.length(); j++) {
				final JSONObject childJson;
				childJson = tmpChildArray.getJSONObject(j);
				this.children.add(childJson.getString("id"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public int getRepetition() {
		return nRepetitions;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public ArrayList<Chord> getChords() {
		return chords;
	}

	public ArrayList<String> getChildren() {
        return children;
    }
}
