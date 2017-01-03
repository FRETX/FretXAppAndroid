package fretx.version4.paging.learn;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import fretx.version4.BluetoothClass;
import fretx.version4.R;
import fretx.version4.Util;
import fretx.version4.activities.MainActivity;
import fretx.version4.fretxapi.AppCache;
import rocks.fretx.audioprocessing.Chord;
import rocks.fretx.audioprocessing.MusicUtils;

/**
 * Created by onurb_000 on 31/12/16.
 */

public class LearnFragmentGuidedChordExercise extends Fragment {

	MainActivity mActivity;
	FrameLayout rootView;
	ListView listView;

	public LearnFragmentGuidedChordExercise(){

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mActivity = (MainActivity) getActivity();
		rootView = (FrameLayout) inflater.inflate(R.layout.learn_guided_chord_list, container, false);
		return  rootView;
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
//		chordFingerings = MusicUtils.parseChordDb();
		BluetoothClass.sendToFretX(Util.str2array("{0}"));
	}

	@Override
	public void onViewCreated(View v, Bundle b){
		showTutorial();
		listView = (ListView) mActivity.findViewById(R.id.guidedChordExerciseList);
		initData();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mActivity == null || mActivity.audio == null) return;
	}


	private void showTutorial(){

	}

	private void initData(){
		ArrayList<GuidedChordExerciseHolder> listData = new ArrayList<>();
		//TODO: implement this in backend and use AppCache.getFromCache
//		String songJsonString = AppCache.getFromCache(songFile());
		File file = mActivity.getResources();
//		File file = new File( cacheDir, path);

		StringBuffer contents = new StringBuffer();
		BufferedReader reader = null;
		reader = new BufferedReader(new FileReader(file));
		String text = null;
		while ((text = reader.readLine()) != null) {
			contents.append(text).append(System.getProperty("line.separator"));
		}
		reader.close();
		//TODO: read the json
		//TODO: populate arraylist
		//TODO: set list adapter
		//TODO: write the list adapter, heh
	}

	static class GuidedChordExerciseHolder{
		String name;
		String id;
		ArrayList<Chord> chords;
		int nRepetitions;
	}



}
