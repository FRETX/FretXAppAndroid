package fretx.version4.paging.learn.guided.list;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import fretx.version4.R;
import fretx.version4.activities.MainActivity;
import fretx.version4.paging.learn.guided.GuidedChordExercise;
import fretx.version4.paging.learn.guided.exercise.LearnGuidedChordExerciseFragment;
import rocks.fretx.audioprocessing.Chord;

class LearnGuidedChordExerciseListAdapter extends ArrayAdapter<GuidedChordExercise> {

	private MainActivity mActivity;
	private int layoutResourceId;
	private ArrayList<GuidedChordExercise> data = new ArrayList<>();

	LearnGuidedChordExerciseListAdapter(MainActivity context , int layoutResourceId, ArrayList<GuidedChordExercise> data){
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.mActivity = context;
		this.data = data;
	}

	@NonNull
	public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
		View row = convertView;
		RecordHolder holder;

		if (row == null) {
			LayoutInflater inflater = mActivity.getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new RecordHolder();

			holder.name = (TextView) row.findViewById(R.id.guidedChordExerciseName);
			holder.chords = (TextView) row.findViewById(R.id.guidedChordExerciseChords);

			row.setTag(holder);

		} else {
			holder = (RecordHolder) row.getTag();
		}

		final GuidedChordExercise item = data.get(position);

		holder.name.setText(item.getName());
		String chordsString = "";
		for (Chord chord: item.getChords()) {
			chordsString += chord.toString() + " ";
		}
		holder.chords.setText(chordsString);

		row.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				LearnGuidedChordExerciseFragment guidedChordExerciseFragment = new LearnGuidedChordExerciseFragment();
				guidedChordExerciseFragment.setExercise(item);
				mActivity.fragNavController.pushFragment(guidedChordExerciseFragment);
			}
		});

		return row;
	}

	private static class RecordHolder{
		TextView name;
		TextView chords;
	}
}
