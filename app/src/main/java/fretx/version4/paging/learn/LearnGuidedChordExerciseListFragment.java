package fretx.version4.paging.learn;

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

/**
 * Created by onurb_000 on 31/12/16.
 */

public class LearnGuidedChordExerciseListFragment extends Fragment {

	MainActivity mActivity;
	LinearLayout rootView;
	GridView listView;
	ArrayList<GuidedChordExercise> listData = new ArrayList<>();

	public LearnGuidedChordExerciseListFragment(){

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mActivity = (MainActivity) getActivity();
		rootView = (LinearLayout) inflater.inflate(R.layout.learn_guided_chord_list, container, false);
		return  rootView;
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
//		chordFingerings = MusicUtils.parseChordDb();
		BluetoothClass.sendToFretX(Util.str2array("{0}"));
	}

	@Override
	public void onViewCreated(View v, Bundle b){
		showTutorial();
		listView = (GridView) mActivity.findViewById(R.id.guidedChordExerciseList);
		initData();
		listView.setAdapter(new LearnGuidedChordExerciseListAdapter(mActivity, R.layout.guided_chord_exercise_list_item, listData));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mActivity == null || mActivity.audio == null) return;
	}


	private void showTutorial(){

	}

	private void initData(){
		listData.clear();
		//TODO: implement this in backend and use AppCache.getFromCache
//		String songJsonString = AppCache.getFromCache(songFile());
		InputStream is = mActivity.getResources().openRawResource(R.raw.guided_chord_exercises_json);
//		File file = new File( cacheDir, path);

		StringBuffer contents = new StringBuffer();
		BufferedReader reader = null;
		reader = new BufferedReader(new InputStreamReader(is));
		String text = null;
		try {
			while ((text = reader.readLine()) != null) {
				contents.append(text).append(System.getProperty("line.separator"));
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

//		JSONObject jsonObject = null;
//		try {
//			jsonObject = new JSONObject(contents.toString());
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
		JSONArray guidedExercises = null;
		try {
			guidedExercises = new JSONArray(contents.toString());
			GuidedChordExercise tmpRecord = new GuidedChordExercise();
			JSONObject chordExercise;//, chordJson;
//			JSONArray tmpChordsArray;
			for (int i = 0; i < guidedExercises.length(); i++) {
				chordExercise = guidedExercises.getJSONObject(i);
				tmpRecord = new GuidedChordExercise(chordExercise);
				listData.add(tmpRecord);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}



}
