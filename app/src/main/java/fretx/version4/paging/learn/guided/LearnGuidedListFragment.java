package fretx.version4.paging.learn.guided;

import android.content.Intent;
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
import fretx.version4.activities.ConnectivityActivity;
import fretx.version4.activities.ExerciseActivity;
import fretx.version4.activities.MainActivity;
import fretx.version4.fragment.exercise.YoutubeExercise;
import fretx.version4.utils.bluetooth.Bluetooth;
import fretx.version4.utils.bluetooth.BluetoothAnimator;

public class LearnGuidedListFragment extends Fragment {
	private static final String TAG = "KJKP6_GUIDED_LIST";
    private static final GuidedExerciseWrapper wrapper = new GuidedExerciseWrapper();

    private GridView gridView;
    private ArrayList<GuidedExercise> exercisesList = new ArrayList<>();
	private LearnGuidedListAdapter adapter;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new LearnGuidedListAdapter(getActivity(), R.layout.paging_learn_guided_list_item, exercisesList);
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
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (exercisesList.get(position).isLocked())
					return;
				final Intent intent = new Intent(getActivity(), ExerciseActivity.class);
                intent.putExtra("wrapper", wrapper);
                intent.putExtra("exerciseId", exercisesList.get(position).getId());
                startActivity(intent);
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		BluetoothAnimator.getInstance().stringFall();
	}

	private void initExercises(){

		adapter.notifyDataSetChanged();
	}

	private void initScores() {
		final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
		if (fUser != null) {
			final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(fUser.getUid()).child("score");
			mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					for (DataSnapshot snap : dataSnapshot.getChildren()) {
						final String exerciseId = snap.getKey();
						final String score = (String) dataSnapshot.child(exerciseId).child("score").getValue();
						if (score != null) {
							for (String childId: wrapper.getExercise(exerciseId).getChildren()) {
                                wrapper.getExercise(childId).setLocked(false);
							}
						}
						exercisesList.clear();
                        exercisesList.addAll(wrapper.getUnlockedExercises());
						adapter.notifyDataSetChanged();
					}
				}

				@Override
				public void onCancelled(DatabaseError databaseError) {
				}
			});
		}
	}
}