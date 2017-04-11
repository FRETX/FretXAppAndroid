package fretx.version4.paging.learn.guided;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import fretx.version4.BluetoothClass;
import fretx.version4.R;
import fretx.version4.Util;
import fretx.version4.activities.MainActivity;

public class LearnGuidedListFragment extends Fragment {
	MainActivity mActivity;
	LinearLayout rootView;
	GridView gridView;
	ArrayList<GuidedChordExercise> exercises = new ArrayList<>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mActivity = (MainActivity) getActivity();
		rootView = (LinearLayout) inflater.inflate(R.layout.paging_learn_guided_list, container, false);
		return  rootView;
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		BluetoothClass.sendToFretX(Util.str2array("{0}"));
	}

	@Override
	public void onViewCreated(View v, Bundle b){
		gridView = (GridView) mActivity.findViewById(R.id.guidedChordExerciseList);
		initExercises();
		gridView.setAdapter(new LearnGuidedListAdapter(mActivity, R.layout.paging_learn_guided_list_item, exercises));
	}

	private void initExercises(){
		exercises.clear();
		//TODO: implement this in backend and use AppCache.getFromCache
		InputStream is = mActivity.getResources().openRawResource(R.raw.guided_chord_exercises_json);

		StringBuilder contents = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String text;
		try {
			while ((text = reader.readLine()) != null) {
				contents.append(text).append(System.getProperty("line.separator"));
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			JSONArray guidedExercises = new JSONArray(contents.toString());
			for (int i = 0; i < guidedExercises.length(); i++) {
				JSONObject exerciseJson = guidedExercises.getJSONObject(i);
				GuidedChordExercise exercise = new GuidedChordExercise(exerciseJson);
				exercises.add(exercise);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}



}
