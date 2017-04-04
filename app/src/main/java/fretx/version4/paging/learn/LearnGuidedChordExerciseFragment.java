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

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;

import fretx.version4.FretboardView;
import fretx.version4.R;
import fretx.version4.activities.MainActivity;
import fretx.version4.utils.MidiPlayer;
import rocks.fretx.audioprocessing.Chord;

public class LearnGuidedChordExerciseFragment extends Fragment{
	int nRepetitions;
	private LearnGuidedChordExerciseView chordExerciseView;
	private MidiPlayer midiPlayer;

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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mActivity = (MainActivity) getActivity();
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Guided Chord Exercise activated");
		mActivity.mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

		rootView = (FrameLayout) inflater.inflate(R.layout.learn_guided_chord_exercise_layout, container, false);
		chordExerciseView = (LearnGuidedChordExerciseView) rootView.findViewById(R.id.guidedChordExerciseView);
		chordExerciseView.setmActivity(mActivity);
		chordExerciseView.setRootView(rootView);
		fretboardView = (FretboardView) rootView.findViewById(R.id.fretboardView);
		chordExerciseView.setFretBoardView(fretboardView);

		return rootView;
	}

	@Override
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

		midiPlayer = new MidiPlayer();

		Button button = (Button) chordExerciseView.findViewById(R.id.playChordButton);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				midiPlayer.playChord(chordExerciseView.getChord());
			}
		});
	}

	@Override
	public void onResume(){
		super.onResume();
		midiPlayer.start();
		int[] config = midiPlayer.config();
		Log.d(this.getClass().getName(), "maxVoices: " + config[0]);
		Log.d(this.getClass().getName(), "numChannels: " + config[1]);
		Log.d(this.getClass().getName(), "sampleRate: " + config[2]);
		Log.d(this.getClass().getName(), "mixBufferSize: " + config[3]);
	}

	@Override
	public void onPause(){
		super.onPause();
		midiPlayer.stop();
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
		TextView finishedElapsedTimeText = (TextView) dialog.findViewById(R.id.finishedElapsedTimeText);
		finishedElapsedTimeText.setText("You finished this exercise in: " + String.format("%d:%02d", minutes, seconds));

		//Set onclicklisteners
		Button backToListButton = (Button) dialog.findViewById(R.id.finishedBackButton);
		Button nextExerciseButton = (Button) dialog.findViewById(R.id.finishedNextExerciseButton);

		final boolean lastExerciseInList;
		if (listPosition == listData.size() - 1) {
			nextExerciseButton.setText("FINISH");
			lastExerciseInList = true;
		} else lastExerciseInList = false;

		backToListButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mActivity.fragNavController.popFragment();
				dialog.dismiss();
			}
		});

		nextExerciseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (lastExerciseInList) {
					mActivity.fragNavController.popFragment();

					dialog.dismiss();
				} else {
					LearnGuidedChordExerciseFragment guidedChordExerciseFragment = new LearnGuidedChordExerciseFragment();
					guidedChordExerciseFragment.setExercise(listData.get(listPosition + 1));
					guidedChordExerciseFragment.setListData(listData, listPosition + 1);
					mActivity.fragNavController.replaceFragment(guidedChordExerciseFragment);
					dialog.dismiss();
				}
			}
		});

		dialog.show();
		Window window = dialog.getWindow();
		window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);


	}

}
