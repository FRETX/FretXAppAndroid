package fretx.version4.paging.play;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;

import fretx.version4.Config;
import fretx.version4.FretboardView;
import fretx.version4.R;
import fretx.version4.activities.MainActivity;
import fretx.version4.fretxapi.SongItem;
import rocks.fretx.audioprocessing.Chord;

/**
 * Created by Kickdrum on 05-Jan-17.
 */

public class PlayFragmentChordPreview extends Fragment
{
	MainActivity mActivity;
	PlayChordPreviewView chordExerciseView;
	FrameLayout rootView = null;
	FretboardView fretboardView;
	ArrayList<Chord> exerciseChords;
	SongItem songItem;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mActivity = (MainActivity) getActivity();
		rootView = (FrameLayout) inflater.inflate(R.layout.play_chord_preview_layout, container, false);
		chordExerciseView = (PlayChordPreviewView) rootView.findViewById(R.id.playChordPreview);
		chordExerciseView.setmActivity(mActivity);
		chordExerciseView.setRootView(rootView);
		fretboardView = (FretboardView) rootView.findViewById(R.id.fretboardView);
		chordExerciseView.setFretBoardView(fretboardView);

		return rootView;
	}

	public void setSongData(SongItem item){
		songItem = item;
	}

	public void onViewCreated(View v, Bundle savedInstanceState) {
//		chordExerciseView.setFragment(this);
		chordExerciseView.setChords(exerciseChords);
		TextView exerciseChordsText = (TextView) v.findViewById(R.id.exerciseChordsTextView);
		if (exerciseChordsText == null) return;
		String songChordsString = "";
		for (int i = 0; i < exerciseChords.size(); i++) {
			songChordsString += exerciseChords.get(i).toString() + " ";
			Log.d("songChordString", songChordsString);
		}
		exerciseChordsText.setText(songChordsString);

		Button playButton = (Button) rootView.findViewById(R.id.previewStartSongButton);
		playButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				boolean loadOfflinePlayer = false;
				if (Config.useOfflinePlayer) {
					String fileName = "fretx" + songItem.youtube_id.toLowerCase().replace("-", "_");
					int resourceIdentifier = getContext().getResources().getIdentifier(fileName, "raw", getContext().getPackageName());
					if (resourceIdentifier != 0) {
						loadOfflinePlayer = true;
					}
				}
				if (loadOfflinePlayer) {
					PlayFragmentOfflinePlayer fragmentYoutubeFragment = new PlayFragmentOfflinePlayer();
					fragmentYoutubeFragment.setSong(songItem);
					FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
					FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
					fragmentTransaction.addToBackStack("playYoutubeList");
					fragmentTransaction.replace(R.id.play_container, fragmentYoutubeFragment, "PlayFragmentYoutubeFragment");
					fragmentTransaction.commit();
				} else {
					PlayFragmentYoutubeFragment fragmentYoutubeFragment = new PlayFragmentYoutubeFragment();
					fragmentYoutubeFragment.setSong(songItem);
					FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
					FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
					fragmentTransaction.addToBackStack("playYoutubeList");
					fragmentTransaction.replace(R.id.play_container, fragmentYoutubeFragment, "PlayFragmentYoutubeFragment");
					fragmentTransaction.commit();
				}
			}
		});
	}

	public void setChords(ArrayList<Chord> chords) {
		this.exerciseChords = (ArrayList<Chord>) chords.clone();
		if (chordExerciseView == null) return;
		chordExerciseView.setChords(this.exerciseChords);
	}

}
