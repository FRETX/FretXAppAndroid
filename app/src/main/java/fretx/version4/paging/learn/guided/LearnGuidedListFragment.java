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

import fretx.version4.R;
import fretx.version4.activities.ExerciseActivity;
import fretx.version4.utils.bluetooth.Bluetooth;
import fretx.version4.utils.bluetooth.BluetoothAnimator;

public class LearnGuidedListFragment extends Fragment {
	private static final String TAG = "KJKP6_GUIDED_LIST";
    private static final GuidedExerciseList exercisesList = new GuidedExerciseList();

    private GridView gridView;
	private LearnGuidedListAdapter adapter;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new LearnGuidedListAdapter(getActivity(), R.layout.paging_learn_guided_list_item, exercisesList.getArray());
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
                final GuidedExercise exercise = exercisesList.getArray().get(position);
				if (!exercise.isLocked()) {
                    final Intent intent = new Intent(getActivity(), ExerciseActivity.class);
                    intent.putExtra("exerciseList", exercisesList);
                    intent.putExtra("exerciseId", exercise.getId());
                    startActivity(intent);
                }
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		BluetoothAnimator.getInstance().stringFall();
		initScores();
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
							for (String childId: exercisesList.getExercise(exerciseId).getChildren()) {
                                exercisesList.setUnlocked(childId);
                                Log.d(TAG, "unlock exercise: " + childId);
							}
						}
					}
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "update unlocked exercises");
                }

				@Override
				public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "canceled");
				}
			});
		}
	}
}