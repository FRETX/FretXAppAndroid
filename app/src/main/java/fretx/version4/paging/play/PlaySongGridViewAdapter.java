package fretx.version4.paging.play;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import fretx.version4.fretxapi.SongItem;
import fretx.version4.fretxapi.SongPunch;
import rocks.fretx.audioprocessing.Chord;

/**
 * 
 * @author manish.s
 *
 */
class PlaySongGridViewAdapter extends ArrayAdapter<SongItem> {
	private MainActivity mActivity;
	private int layoutResourceId;
	private ArrayList<SongItem> data = new ArrayList<SongItem>();

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
		RecordHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) mActivity).getLayoutInflater();
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

//		holder.txtTitle.setText(item.artist + "\n" + item.song_title);
		holder.txtSecondary.setText(item.artist);
		holder.txtPrimary.setText(item.song_title);

		Picasso.with(mActivity).load(item.imageURL()).placeholder(R.drawable.defaultthumb).into(holder.imageItem);
		//holder.imageItem.setImageDrawable(item.image);
		row.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mActivity.audio.enableChordDetector();
				mActivity.audio.disableNoteDetector();
				mActivity.audio.disablePitchDetector();
				if(mActivity.previewEnabled){
					Bundle bundle = new Bundle();
					bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Preview: " + item.fretx_id);
					bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, item.song_title);
					mActivity.mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
					//Launch exercise with sequence of chords
					ArrayList<SongPunch> punches = item.punches();
					SongPunch tmpSp;
					String tmpKey;
					ArrayList<Chord> chords = new ArrayList<Chord>();
					String root, type;
						for (int i = 0; i < punches.size(); i++) {
							tmpSp = punches.get(i);
							root = tmpSp.root;
							type = tmpSp.type.toLowerCase();
							if( (root + type).equals("No Chord") ) continue;
							if (root.equals("") || type.equals("")) continue;
							if (type.equals("min")) {
								type = "m";
								Log.d("ViewAdapter", "new type " + type);
							}
							if (root == null || type == null) return;

							try {
								chords.add(new Chord(root, type));
							} catch(Exception e){
								Log.e(root + type,e.toString());
							}

						}

					if (chords.size() < 1) {
						Toast.makeText(mActivity, "No chord data found for this song", Toast.LENGTH_SHORT).show();
						return;
					}

					//Find Unique chords
//					ArrayList<SongPunch> punches = item.punches();
//					SongPunch tmpSp;
//					String tmpKey;
//					HashMap<String, SongPunch> uniqueChords = new HashMap<String, SongPunch>();
//					for (int i = 0; i < punches.size(); i++) {
//						tmpSp = punches.get(i);
//						tmpKey = tmpSp.root + tmpSp.type;
//						if (tmpKey.equals("No Chord")) {
//							continue;
//						}
//						if (uniqueChords.containsKey(tmpKey)) {
//							continue;
//						}
//						uniqueChords.put(tmpKey, tmpSp);
//					}
//
//					Set<String> keys = uniqueChords.keySet();
//					Iterator it = keys.iterator();
//					ArrayList<Chord> chords = new ArrayList<Chord>();
//					String root, type;
//					while (it.hasNext()) {
//						tmpSp = uniqueChords.get(it.next());
//						root = tmpSp.root;
//						type = tmpSp.type.toLowerCase();
//						if (root.equals("") || type.equals("")) continue;
//						Log.d("ViewAdapter", "root " + root);
//						Log.d("ViewAdapter", "type " + type);
//						if (type.equals("min")) {
//							type = "m";
//							Log.d("ViewAdapter", "new type " + type);
//						}
//						if (root == null || type == null) return;
//						chords.add(new Chord(root, type));
//					}

					PlayChordPreviewFragment fragmentChordExercise = new PlayChordPreviewFragment();
					fragmentChordExercise.setChords(chords);
					fragmentChordExercise.setSongData(item);
					mActivity.fragNavController.pushFragment(fragmentChordExercise);

				} else {
					Bundle bundle = new Bundle();
					bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Song: " + item.fretx_id);
					bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, item.song_title);
					mActivity.mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

					boolean loadOfflinePlayer = false;
					if (Config.useOfflinePlayer) {
						String fileName = "fretx" + item.youtube_id.toLowerCase().replace("-", "_");
						int resourceIdentifier = getContext().getResources().getIdentifier(fileName, "raw", getContext().getPackageName());
						if(resourceIdentifier != 0){
							loadOfflinePlayer = true;
						}
					}
					if (loadOfflinePlayer) {
						PlayOfflinePlayerFragment offlinePlayerFragment = new PlayOfflinePlayerFragment();
						offlinePlayerFragment.setSong(item);
						mActivity.fragNavController.pushFragment(offlinePlayerFragment);
					} else {
						PlayYoutubeFragment youtubeFragment = new PlayYoutubeFragment();
						youtubeFragment.setSong(item);
						mActivity.fragNavController.pushFragment(youtubeFragment);
					}
				}


			}
		});
		return row;
	}

	private static class RecordHolder {
		TextView txtPrimary;
		TextView txtSecondary;
		ImageView imageItem;
	}
}