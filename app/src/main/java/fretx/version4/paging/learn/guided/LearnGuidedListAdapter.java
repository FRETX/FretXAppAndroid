package fretx.version4.paging.learn.guided;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import fretx.version4.R;
import rocks.fretx.audioprocessing.Chord;

class LearnGuidedListAdapter extends ArrayAdapter<GuidedExercise> {
	private int layoutResourceId;
	private ArrayList<GuidedExercise> data;
	private FragmentActivity context;

	LearnGuidedListAdapter(FragmentActivity context, int layoutResourceId, ArrayList<GuidedExercise> data){
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
	}

	@NonNull
	public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
		RecordHolder holder;

		if (convertView == null) {
			final LayoutInflater inflater = context.getLayoutInflater();
			convertView = inflater.inflate(layoutResourceId, parent, false);

			holder = new RecordHolder();

			holder.name = (TextView) convertView.findViewById(R.id.guidedChordExerciseName);
			holder.chords = (TextView) convertView.findViewById(R.id.guidedChordExerciseChords);
			holder.lock = (ImageView) convertView.findViewById(R.id.lock);

			convertView.setTag(holder);
		} else {
			holder = (RecordHolder) convertView.getTag();
		}

		final GuidedExercise item = data.get(position);

		holder.name.setText(item.getName());
		String chordsString = "";
		for (Chord chord: item.getSimpleChord()) {
			chordsString += chord.toString() + " ";
		}
		holder.chords.setText(chordsString);

		if (data.get(position).isLocked())
			holder.lock.setVisibility(View.VISIBLE);
		else
			holder.lock.setVisibility(View.INVISIBLE);

		return convertView;
	}

	private static class RecordHolder{
		TextView name;
		TextView chords;
		ImageView lock;
	}
}