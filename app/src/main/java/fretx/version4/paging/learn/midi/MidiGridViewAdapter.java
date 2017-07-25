package fretx.version4.paging.learn.midi;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import fretx.version4.R;
import fretx.version4.activities.MainActivity;

/**
 * 
 * @author manish.s
 *
 */
class MidiGridViewAdapter extends ArrayAdapter<File> {
    private static final String TAG = "KJKP6_MIDI_ADAPTER";
	private MainActivity mActivity;
	private int layoutResourceId;

	MidiGridViewAdapter(MainActivity context, int layoutResourceId,
                            ArrayList<File> data) {
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.mActivity = context;
	}

	@Override
	@NonNull
	public View getView(int position, View convertView, @NonNull ViewGroup parent) {
		View row = convertView;
		RecordHolder holder;

		if (row == null) {
			LayoutInflater inflater = mActivity.getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new RecordHolder();

			holder.txtPrimary = (TextView) row.findViewById(R.id.item_text);
			holder.delete = (ImageView) row.findViewById(R.id.delete);

			row.setTag(holder);
		} else {
			holder = (RecordHolder) row.getTag();
		}

		final File item = getItem(position);

		holder.txtPrimary.setText(item.getName());
		holder.delete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MidiGridViewAdapter.this.remove(item);
                if (item.exists()) {
                    if (item.delete()) {
                        Log.d(TAG, "file Deleted");
                    } else {
                        Log.d(TAG, "file not Deleted");
                    }
                }
			}
		});

		return row;
	}

	private static class RecordHolder {
		TextView txtPrimary;
		ImageView delete;
	}
}