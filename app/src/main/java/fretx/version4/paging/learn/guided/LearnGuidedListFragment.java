package fretx.version4.paging.learn.guided;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import fretx.version4.R;
import fretx.version4.activities.MainActivity;
import fretx.version4.utils.bluetooth.Bluetooth;
import fretx.version4.utils.bluetooth.BluetoothAnimator;

public class LearnGuidedListFragment extends Fragment {
    private static final String TAG = "KJKP6_GUIDED_LIST";

    private HashMap<String, GuidedExercise> exercises = new HashMap<>();
    private HashMap<String, Boolean> scores = new HashMap<>();
    private ArrayList<GuidedExercise> exercisesParsed = new ArrayList<>();

    private GridView gridView;
    private LearnGuidedListAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new LearnGuidedListAdapter(getActivity(), R.layout.paging_learn_guided_list_item, exercisesParsed);
        initExercises();
        initScores();
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final LinearLayout rootView = (LinearLayout) inflater.inflate(R.layout.paging_learn_guided_list, container, false);
		gridView = (GridView) rootView.findViewById(R.id.guidedChordExerciseList);
		return rootView;
	}

	@Override
	public void onViewCreated(View v, Bundle b){
		Bluetooth.getInstance().clearMatrix();
		initExercises();
		gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LearnGuidedExercise guidedChordExerciseFragment = new LearnGuidedExercise();
                guidedChordExerciseFragment.setExercise(exercisesParsed, position);
                ((MainActivity) getActivity()).fragNavController.pushFragment(guidedChordExerciseFragment);
            }
        });
	}

	@Override
	public void onResume() {
		super.onResume();
		BluetoothAnimator.getInstance().stringFall();
	}

	private void initExercises(){
		exercises.clear();
		//TODO: implement this in backend and use AppCache.getFromCache
		InputStream is = getActivity().getResources().openRawResource(R.raw.guided_chord_exercises_json);

		final StringBuilder contents = new StringBuilder();
		final BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		//read file
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

		//build exercise list
        GuidedExercise exercise = exercises.get("root");
        if (exercise == null) {
            Log.d(TAG, "Cannot find root exercise");
        } else {
            exercisesParsed.clear();
            final ArrayList<String> toAdd = new ArrayList<>();
            toAdd.add(exercise.getId());
            while (!toAdd.isEmpty()) {
                exercise = exercises.get(toAdd.get(0));
                exercisesParsed.add(exercise);
                toAdd.remove(0);

                final Boolean score = scores.get(exercise.getName());
                if (score != null) {
                    toAdd.addAll(toAdd.size(), exercise.getChildren());
                }
            }
        }

        adapter.notifyDataSetChanged();
	}

	private void initScores() {
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fUser != null) {
            scores.clear();
            final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(fUser.getUid()).child("score");
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        final String exerciseName = snap.getKey();
                        final String score = (String) dataSnapshot.child(exerciseName).child("score").getValue();
                        scores.put(exerciseName, score != null);
                        initExercises();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }
}
