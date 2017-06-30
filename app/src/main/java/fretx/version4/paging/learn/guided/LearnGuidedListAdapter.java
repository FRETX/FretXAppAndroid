package fretx.version4.paging.learn.guided;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import fretx.version4.R;
import rocks.fretx.audioprocessing.Chord;

class LearnGuidedListAdapter extends ArrayAdapter<GuidedExercise> {
	private int layoutResourceId;
	private ArrayList<GuidedExercise> data = new ArrayList<>();
	private FragmentActivity context;

	LearnGuidedListAdapter(FragmentActivity context , int layoutResourceId, ArrayList<GuidedExercise> data){
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
	}

	@NonNull
	public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
		View row = convertView;
		RecordHolder holder;

		if (row == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new RecordHolder();

			holder.name = (TextView) row.findViewById(R.id.guidedChordExerciseName);
			holder.chords = (TextView) row.findViewById(R.id.guidedChordExerciseChords);

			row.setTag(holder);

		} else {
			holder = (RecordHolder) row.getTag();
		}

		final GuidedExercise item = data.get(position);

		holder.name.setText(item.getName());
		String chordsString = "";
		for (Chord chord: item.getChords()) {
			chordsString += chord.toString() + " ";
		}
		holder.chords.setText(chordsString);

		return row;
	}

	private static class RecordHolder{
		TextView name;
		TextView chords;
	}
}
