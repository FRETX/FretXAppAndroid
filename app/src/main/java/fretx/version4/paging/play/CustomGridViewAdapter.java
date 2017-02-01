package fretx.version4.paging.play;

import android.app.Activity;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import fretx.version4.Config;
import fretx.version4.R;
import fretx.version4.activities.MainActivity;
import fretx.version4.fretxapi.SongItem;
import fretx.version4.fretxapi.SongPunch;
import fretx.version4.paging.learn.LearnFragmentChordExercise;
import rocks.fretx.audioprocessing.Chord;


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

		Picasso.with(context).load(item.imageURL()).placeholder(R.drawable.defaultthumb).into(holder.imageItem);
		//holder.imageItem.setImageDrawable(item.image);
		row.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				context.audio.enableChordDetector();
				context.audio.disableNoteDetector();
				context.audio.disablePitchDetector();
				if(context.previewEnabled){
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
							chords.add(new Chord(root, type));
						}

					if (chords.size() < 1) {
						Toast.makeText(context, "No chord data found for this song", Toast.LENGTH_SHORT).show();
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

					PlayFragmentChordPreview fragmentChordExercise = new PlayFragmentChordPreview();
					FragmentManager fragmentManager = context.getSupportFragmentManager();
					FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
					fragmentTransaction.add(R.id.play_container, fragmentChordExercise, "fragmentChordExercisePreviewMode");
					fragmentChordExercise.setChords(chords);
					fragmentChordExercise.setSongData(item);
					fragmentTransaction.addToBackStack("listViewToChordPreview");
					fragmentTransaction.commit();
					fragmentManager.executePendingTransactions();
				} else {

					boolean loadOfflinePlayer = false;
					if (Config.useOfflinePlayer) {
						String fileName = "fretx" + item.youtube_id.toLowerCase().replace("-", "_");
						int resourceIdentifier = getContext().getResources().getIdentifier(fileName, "raw", getContext().getPackageName());
						if(resourceIdentifier != 0){
							loadOfflinePlayer = true;
						}
					}
					if (loadOfflinePlayer) {
						PlayFragmentOfflinePlayer fragmentYoutubeFragment = new PlayFragmentOfflinePlayer();
						fragmentYoutubeFragment.setSong(item);
						FragmentManager fragmentManager = context.getSupportFragmentManager();
						FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
						fragmentTransaction.addToBackStack("playYoutubeList");
						fragmentTransaction.replace(R.id.play_container, fragmentYoutubeFragment, "PlayFragmentYoutubeFragment");
						fragmentTransaction.commit();
					} else {
						PlayFragmentYoutubeFragment fragmentYoutubeFragment = new PlayFragmentYoutubeFragment();
						fragmentYoutubeFragment.setSong(item);
						FragmentManager fragmentManager = context.getSupportFragmentManager();
						FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
						fragmentTransaction.addToBackStack("playYoutubeList");
						fragmentTransaction.replace(R.id.play_container, fragmentYoutubeFragment, "PlayFragmentYoutubeFragment");
						fragmentTransaction.commit();
					}
				}


			}
		});

//		ImageButton prePracticeButton = (ImageButton) row.findViewById(R.id.prePractice);
//		prePracticeButton.setOnClickListener(new View.OnClickListener(){
//			public void onClick(View v){
//				//Chord Preview Mode
////				v.setVisibility(View.INVISIBLE);
////				ViewGroup row = (ViewGroup) v.getParent();
////				for (int itemPos = 0; itemPos < row.getChildCount(); itemPos++) {
////					View view = row.getChildAt(itemPos);
////					if (view instanceof ProgressBar) {
////						ProgressBar progressBar = (ProgressBar) view; //Found it!
////						progressBar.setVisibility(View.VISIBLE);
////						break;
////					}
////				}
//
////				RelativeLayout parentView = (RelativeLayout) v.getParent();
////				MainActivity mActivity = (MainActivity) v.getContext();
////				ProgressBar progressBar = (ProgressBar) parentView.findViewById(R.id.chordPreviewProgressBar);
////				v.setVisibility(View.INVISIBLE);
////				progressBar.setVisibility(View.VISIBLE);
//			}
//		});

		return row;

	}

	static class RecordHolder {
		TextView txtPrimary;
		TextView txtSecondary;
		ImageView imageItem;


	}
}