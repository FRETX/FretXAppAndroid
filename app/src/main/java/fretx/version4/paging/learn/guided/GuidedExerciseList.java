package fretx.version4.paging.learn.guided;

import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import fretx.version4.R;

import static fretx.version4.activities.BaseActivity.getActivity;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 10/07/17 19:47.
 */

public class GuidedExerciseList implements Serializable{
    private static final String TAG = "KJKP6_EXERCISE_WRAPPER";
    private final HashMap<String, GuidedExercise> exercises = new HashMap<>();


    public GuidedExerciseList() {
        InputStream is = getActivity().getResources().openRawResource(R.raw.guided_chord_exercises_json);

        //read file
        final StringBuilder contents = new StringBuilder();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String text;
        try {
            while ((text = reader.readLine()) != null) {
                contents.append(text).append(System.getProperty("line.separator"));
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //parse file
        try {
            final JSONArray guidedExercises = new JSONArray(contents.toString());
            for (int i = 0; i < guidedExercises.length(); i++) {
                JSONObject exerciseJson = guidedExercises.getJSONObject(i);
                GuidedExercise exercise = new GuidedExercise(exerciseJson);
                exercises.put(exercise.getId(), exercise);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<GuidedExercise> getArray() {
        //build exercise list
        final ArrayList<GuidedExercise> array = new ArrayList<>();
        GuidedExercise exercise = this.exercises.get("root");
        if (exercise == null) {
            Log.d(TAG, "Cannot find root exercise");
        } else {
            array.clear();
            exercise.setLocked(false);
            final ArrayList<String> toAdd = new ArrayList<>();
            toAdd.add(exercise.getId());
            while (!toAdd.isEmpty()) {
                exercise = this.exercises.get(toAdd.get(0));
                array.add(exercise);
                toAdd.remove(0);
                toAdd.addAll(toAdd.size(), exercise.getChildren());
            }
        }
        return array;
    }

    public HashMap<String, GuidedExercise> getExercises() {
        return exercises;
    }

    public void setUnlocked(String exerciseId) {
        exercises.get(exerciseId).setLocked(false);
    }

    public GuidedExercise getExercise(String exerciseId) {
        return exercises.get(exerciseId);
    }
}
