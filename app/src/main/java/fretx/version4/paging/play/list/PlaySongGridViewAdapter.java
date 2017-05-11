package fretx.version4.paging.play.list;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import fretx.version4.Config;
import fretx.version4.R;
import fretx.version4.activities.MainActivity;
import fretx.version4.fretxapi.song.SongItem;
import fretx.version4.fretxapi.song.SongPunch;
import fretx.version4.paging.play.preview.PlayPreview;
import fretx.version4.paging.play.player.PlayOfflinePlayerFragment;
import fretx.version4.paging.play.player.PlayYoutubeFragment;
import rocks.fretx.audioprocessing.Chord;

/**
 * 
 * @author manish.s
 *
 */
class PlaySongGridViewAdapter extends ArrayAdapter<SongItem> {
	private MainActivity mActivity;
	private int layoutResourceId;
	private ArrayList<SongItem> data = new ArrayList<>();

	PlaySongGridViewAdapter(MainActivity context, int layoutResourceId,
	                               ArrayList<SongItem> data) {
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
			holder.txtSecondary = (TextView) row.findViewById(R.id.item_text_secondary);
			holder.imageItem = (ImageView) row.findViewById(R.id.item_image);

			row.setTag(holder);
		} else {
			holder = (RecordHolder) row.getTag();
		}

		final SongItem item = data.get(position);

		holder.txtSecondary.setText(item.artist);
		holder.txtPrimary.setText(item.song_title);

		Picasso.with(mActivity).load(item.imageURL()).placeholder(R.drawable.defaultthumb).into(holder.imageItem);
		return row;
	}

	private static class RecordHolder {
		TextView txtPrimary;
		TextView txtSecondary;
		ImageView imageItem;
	}
}