package fretx.version4.paging.learn.guided;

import android.content.Intent;
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

import fretx.version4.R;
import fretx.version4.activities.MainActivity;
import fretx.version4.utils.bluetooth.BluetoothAnimator;

public class LearnGuidedListFragment extends Fragment {
	MainActivity mActivity;
	LinearLayout rootView;
	GridView gridView;
	ArrayList<GuidedExercise> exercises = new ArrayList<>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mActivity = (MainActivity) getActivity();
		rootView = (LinearLayout) inflater.inflate(R.layout.paging_learn_guided_list, container, false);
		return  rootView;
	}

	@Override
	public void onViewCreated(View v, Bundle b){
		gridView = (GridView) mActivity.findViewById(R.id.guidedChordExerciseList);
		initExercises();
<<<<<<< Updated upstream
		gridView.setAdapter(new LearnGuidedListAdapter(mActivity, R.layout.paging_learn_guided_list_item, exercises));
=======
		gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*
                if (exercisesList.get(position).isLocked())
                    return;
                final LearnGuidedExercise guidedChordExerciseFragment = new LearnGuidedExercise();
                guidedChordExerciseFragment.setExercise(exercises, exercisesList.get(position).getId());
                ((MainActivity) getActivity()).fragNavController.pushFragment(guidedChordExerciseFragment);
                 */
                final Intent intent = new Intent(getActivity(), MainActivity.class);
                getActivity().startActivityForResult(intent, 1);
            }
        });
>>>>>>> Stashed changes
	}

	@Override
	public void onResume() {
		super.onResume();
		BluetoothAnimator.getInstance().stringFall();
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
				GuidedExercise exercise = new GuidedExercise(exerciseJson);
				exercises.add(exercise);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}



}
