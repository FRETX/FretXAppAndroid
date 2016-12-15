package fretx.version4.paging.play;

import android.app.Activity;
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
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

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

			holder.txtTitle = (TextView) row.findViewById(R.id.item_text);
			holder.imageItem = (ImageView) row.findViewById(R.id.item_image);

			row.setTag(holder);

		} else {
			holder = (RecordHolder) row.getTag();
		}

		final SongItem item = data.get(position);

		holder.txtTitle.setText(item.artist + "\n" + item.song_title);

		Picasso.with(context).load(item.imageURL()).placeholder(R.drawable.defaultthumb).into(holder.imageItem);
		//holder.imageItem.setImageDrawable(item.image);
		row.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				PlayFragmentYoutubeFragment fragmentYoutubeFragment = new PlayFragmentYoutubeFragment();
				fragmentYoutubeFragment.setSong(item);
				FragmentManager fragmentManager = context.getSupportFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//				Bundle args = new Bundle();
//				args.putString("URL", item.songUrl);
//				args.putString("RAW", item.songTxt());
//				fragmentYoutubeFragment.setArguments(args);
				fragmentTransaction.replace(R.id.play_container, fragmentYoutubeFragment, "PlayFragmentYoutubeFragment");
				fragmentTransaction.commit();
			}
		});

		ImageButton prePracticeButton = (ImageButton) row.findViewById(R.id.prePractice);
		prePracticeButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				//Chord Preview Mode
				//TODO: ARRRRRGH this needs to be written so much better
				ArrayList<SongPunch> punches = item.punches();
				SongPunch tmpSp;
				String tmpKey;
				HashMap<String,SongPunch> uniqueChords = new HashMap<String, SongPunch>();
				for (int i = 0; i < punches.size(); i++) {
					tmpSp = punches.get(i);
					tmpKey = tmpSp.root + tmpSp.type;
					if(tmpKey.equals("No Chord")){ continue; }
					if(uniqueChords.containsKey(tmpKey)){ continue; }
					uniqueChords.put(tmpKey,tmpSp);
				}

				Set<String> keys = uniqueChords.keySet();
				Iterator it = keys.iterator();
				ArrayList<Chord> chords = new ArrayList<Chord>();
				String root,type;
				while(it.hasNext()){
					tmpSp = uniqueChords.get(it.next());
					root = tmpSp.root;
					type = tmpSp.type.toLowerCase();
					if(root.equals("") || type.equals("")) continue;
					Log.d("ViewAdapter","root " + root);
					Log.d("ViewAdapter","type " + type);
					if(type.equals("min")){
						type = "m";
						Log.d("ViewAdapter","new type " + type);
					}
					if(root == null || type == null) return;
					chords.add(new Chord(root,type));
				}

				LearnFragmentChordExercise fragmentChordExercise = new LearnFragmentChordExercise();
				FragmentManager fragmentManager = context.getSupportFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				fragmentTransaction.add(R.id.play_container, fragmentChordExercise, "fragmentChordExercisePreviewMode");
				fragmentChordExercise.setChords(chords);
				fragmentTransaction.addToBackStack("listViewToChordPreview");
				fragmentTransaction.commit();
				fragmentManager.executePendingTransactions();
			}
		});

		return row;

	}

	static class RecordHolder {
		TextView txtTitle;
		ImageView imageItem;

	}
}