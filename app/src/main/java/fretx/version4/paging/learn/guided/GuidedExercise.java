package fretx.version4.paging.learn.guided;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

import rocks.fretx.audioprocessing.Chord;

public class GuidedExercise implements Serializable{
	private String name = "";
	private String id = "";
	private final ArrayList<Chord> chords = new ArrayList<>();
	private int nRepetitions = 0;
	private final ArrayList<String> children = new ArrayList<>();
	private boolean locked = true;

	GuidedExercise(JSONObject chordExercise){
		try {
			this.name = chordExercise.getString("name");
			this.id = chordExercise.getString("id");
			this.nRepetitions = chordExercise.getInt("nRepetitions");
			final JSONArray tmpChordsArray;
			tmpChordsArray = chordExercise.getJSONArray("chords");
			for (int j = 0; j < tmpChordsArray.length(); j++) {
				final JSONObject chordJson;
				chordJson = tmpChordsArray.getJSONObject(j);
				this.chords.add(new Chord(chordJson.getString("root"), chordJson.getString("type")));
			}
			final JSONArray tmpChildArray;
			tmpChildArray = chordExercise.getJSONArray("children");
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

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}
}
