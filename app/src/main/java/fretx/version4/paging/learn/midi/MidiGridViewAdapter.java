package fretx.version4.paging.learn.midi;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import fretx.version4.R;
import fretx.version4.activities.MainActivity;
import fretx.version4.fretxapi.song.SongItem;

/**
 * 
 * @author manish.s
 *
 */
class MidiGridViewAdapter extends ArrayAdapter<File> {
	private MainActivity mActivity;
	private int layoutResourceId;
	private ArrayList<File> data = new ArrayList<>();

	MidiGridViewAdapter(MainActivity context, int layoutResourceId,
                            ArrayList<File> data) {
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.mActivity = context;
		this.data = data;
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

			row.setTag(holder);
		} else {
			holder = (RecordHolder) row.getTag();
		}

		final File item = data.get(position);

		holder.txtPrimary.setText(item.getName());

		return row;
	}

	private static class RecordHolder {
		TextView txtPrimary;
	}
}