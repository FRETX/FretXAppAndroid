package fretx.version4.paging.play;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import fretx.version4.R;
import fretx.version4.activities.MainActivity;
import fretx.version4.fretxapi.SongItem;


/**
 * 
 * @author manish.s
 *
 */
public class CustomGridViewAdapter extends ArrayAdapter<SongItem> {
	MainActivity context;
	int layoutResourceId;
	ArrayList<SongItem> data = new ArrayList<SongItem>();

	public CustomGridViewAdapter(MainActivity context, int layoutResourceId,
								 ArrayList<SongItem> data) {
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		RecordHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new RecordHolder();

			holder.txtTitle = (TextView) row.findViewById(R.id.item_text);
			holder.imageItem = (ImageView) row.findViewById(R.id.item_image);

			row.setTag(holder);

		} else {
			holder = (RecordHolder) row.getTag();
		}

		final SongItem item = data.get(position);
		holder.txtTitle.setText(item.songName.replaceFirst("-","\n"));
		Picasso.with(context).load(item.imageURL()).placeholder(R.drawable.defaultthumb).into(holder.imageItem);
		//holder.imageItem.setImageDrawable(item.image);
		row.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				PlayFragmentYoutubeFragment fragmentYoutubeFragment = new PlayFragmentYoutubeFragment();
				FragmentManager fragmentManager = context.getSupportFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				Bundle args = new Bundle();
				args.putString("URL", item.songUrl);
				args.putString("RAW", item.songTxt());
				fragmentYoutubeFragment.setArguments(args);
				fragmentTransaction.replace(R.id.play_container, fragmentYoutubeFragment);
				fragmentTransaction.commit();
			}
		});
		return row;

	}

	static class RecordHolder {
		TextView txtTitle;
		ImageView imageItem;

	}
}