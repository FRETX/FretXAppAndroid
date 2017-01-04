package fretx.version4.paging.learn;


import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;

import fretx.version4.FretboardView;
import fretx.version4.R;
import fretx.version4.activities.MainActivity;
import rocks.fretx.audioprocessing.Chord;

public class LearnFragmentGuidedChordExercise extends Fragment{
	int nRepetitions;
	LearnGuidedChordExerciseView chordExerciseView;

	MainActivity mActivity;

	FrameLayout rootView = null;
	FretboardView fretboardView;
	ArrayList<Chord> exerciseChords;

	ArrayList<GuidedChordExercise> listData;
	int listPosition;

	public void setListData(ArrayList<GuidedChordExercise> listData, int listPosition) {
		this.listData = listData;
		this.listPosition = listPosition;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mActivity = (MainActivity) getActivity();
		rootView = (FrameLayout) inflater.inflate(R.layout.learn_guided_chord_exercise_layout, container, false);
		chordExerciseView = (LearnGuidedChordExerciseView) rootView.findViewById(R.id.guidedChordExerciseView);
		chordExerciseView.setmActivity(mActivity);
		chordExerciseView.setRootView(rootView);
		fretboardView = (FretboardView) rootView.findViewById(R.id.fretboardView);
		chordExerciseView.setFretBoardView(fretboardView);

		return rootView;
	}

	public void onViewCreated(View v, Bundle savedInstanceState) {
		chordExerciseView.setFragment(this);
		chordExerciseView.setChords(exerciseChords);
		TextView exerciseChordsText = (TextView) v.findViewById(R.id.exerciseChordsTextView);
		if (exerciseChordsText == null) return;
		String songChordsString = "";
		for (int i = 0; i < exerciseChords.size(); i++) {
			songChordsString += exerciseChords.get(i).toString() + " ";
			Log.d("songChordString", songChordsString);
		}
		exerciseChordsText.setText(songChordsString);
		chordExerciseView.startTimer();
	}

	public void setExercise(GuidedChordExercise exercise){
		this.nRepetitions = exercise.nRepetitions;
		ArrayList<Chord> repeatedChords = new ArrayList<>();
		for (int i = 0; i < exercise.nRepetitions; i++) {
			repeatedChords.addAll(exercise.chords);
		}
		this.setChords(repeatedChords);
		Log.d("nRepetitions",Integer.toString(nRepetitions));
	}

	public void setChords(ArrayList<Chord> chords) {
		this.exerciseChords = (ArrayList<Chord>) chords.clone();
		if (chordExerciseView == null) return;
		chordExerciseView.setChords(this.exerciseChords);
	}


	public void finishExercise(long elapsedTime){


		final Dialog dialog = new Dialog(mActivity);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setCancelable(false);
		dialog.setContentView(R.layout.guided_exercise_finished_layout);

		int seconds = (int) (elapsedTime / 1000);
		int minutes = seconds / 60;
		seconds = seconds % 60;
		TextView finishedElapsedTimeText = (TextView) mActivity.findViewById(R.id.finishedElapsedTimeText);
		finishedElapsedTimeText.setText("You finished this exercise in: "+ String.format("%d:%02d", minutes, seconds));

		//Set onclicklisteners
		Button backToListButton = (Button) mActivity.findViewById(R.id.finishedBackButton);
		Button nextExerciseButton = (Button) mActivity.findViewById(R.id.finishedNextExerciseButton);

		final boolean lastExerciseInList;
		if(listPosition == listData.size()-1){
			nextExerciseButton.setText("FINISH");
			lastExerciseInList = true;
		} else lastExerciseInList = false;

		backToListButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				fragmentTransaction.replace(R.id.learn_container, new LearnFragmentGuidedChordExerciseList());
//				fragmentTransaction.addToBackStack("guidedChordExercise");
				fragmentTransaction.commit();
			}
		});

		nextExerciseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(lastExerciseInList){
					FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
					FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
					fragmentTransaction.replace(R.id.learn_container, new LearnFragmentGuidedChordExerciseList());
//				fragmentTransaction.addToBackStack("guidedChordExercise");
					fragmentTransaction.commit();
				} else {
					LearnFragmentGuidedChordExercise guidedChordExerciseFragment = new LearnFragmentGuidedChordExercise();
					guidedChordExerciseFragment.setExercise(listData.get(listPosition + 1));
					guidedChordExerciseFragment.setListData(listData, listPosition + 1);
					FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
					FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
					fragmentTransaction.replace(R.id.learn_container, guidedChordExerciseFragment, "PlayFragmentYoutubeFragment");
					fragmentTransaction.commit();
				}
			}
		});

		dialog.show();
		Window window = dialog.getWindow();
		window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	}

}
