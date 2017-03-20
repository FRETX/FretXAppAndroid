package fretx.version4.paging.learn;

import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import fretx.version4.R;
import fretx.version4.activities.MainActivity;

public class LearnGuidedChordExerciseListAdapter extends ArrayAdapter<GuidedChordExercise> {

	MainActivity mActivity;
	int layoutResourceId;
	ArrayList<GuidedChordExercise> data = new ArrayList<GuidedChordExercise>();

	LearnGuidedChordExerciseListAdapter(MainActivity context , int layoutResourceId, ArrayList<GuidedChordExercise> data){
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.mActivity = context;
		this.data = data;
	}


	public View getView(final int position, View convertView, ViewGroup parent) {
		View row = convertView;

		RecordHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) mActivity).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new RecordHolder();

			holder.name = (TextView) row.findViewById(R.id.guidedChordExerciseName);
			holder.chords = (TextView) row.findViewById(R.id.guidedChordExerciseChords);

			row.setTag(holder);

		} else {
			holder = (RecordHolder) row.getTag();
		}

		final GuidedChordExercise item = data.get(position);

		holder.name.setText(item.name);
		String chordsString = "";
		for (int i = 0; i < item.chords.size(); i++) {
			chordsString += item.chords.get(i).toString() + " ";
		}
		holder.chords.setText(chordsString);

		//TODO: onClickListener

		row.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				LearnGuidedChordExerciseFragment guidedChordExerciseFragment = new LearnGuidedChordExerciseFragment();
				guidedChordExerciseFragment.setExercise(item);
				guidedChordExerciseFragment.setListData(data,position);
				mActivity.fragNavController.pushFragment(guidedChordExerciseFragment);
			}
		});

		return row;
	}

	static class RecordHolder{
		TextView name;
		TextView chords;
	}
}
