package fretx.version4.paging.learn;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;

import fretx.version4.FretboardView;
import fretx.version4.activities.MainActivity;
import fretx.version4.R;
import rocks.fretx.audioprocessing.Chord;

public class LearnChordExerciseFragment extends Fragment {

    MainActivity mActivity;

    FrameLayout rootView = null;
	LearnChordExerciseView chordExerciseView;
	FretboardView fretboardView;
	ArrayList<Chord> exerciseChords;

    public LearnChordExerciseFragment(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = (MainActivity)getActivity();
        rootView = (FrameLayout) inflater.inflate(R.layout.learn_chord_exercise_layout, container, false);
	    chordExerciseView = (LearnChordExerciseView) rootView.findViewById(R.id.chordExerciseView);
	    chordExerciseView.setmActivity(mActivity);
	    chordExerciseView.setRootView(rootView);
	    fretboardView = (FretboardView) rootView.findViewById(R.id.fretboardView);
	    chordExerciseView.setFretBoardView(fretboardView);
        return rootView;
    }

	public void setChords(ArrayList<Chord> chords){
		this.exerciseChords = (ArrayList<Chord>) chords.clone();
		if(chordExerciseView == null) return;
		chordExerciseView.setChords(this.exerciseChords);
	}

	@Override
	public void onViewCreated(View v , Bundle savedInstanceState){
		chordExerciseView.setChords(exerciseChords);
		TextView exerciseChordsText = (TextView) v.findViewById(R.id.exerciseChordsTextView);
		if(exerciseChordsText==null) return;
		String songChordsString = "";
		for (int i = 0; i < exerciseChords.size(); i++) {
			songChordsString += exerciseChords.get(i).toString() + " ";
			Log.d("songChordString",songChordsString);
		}
		exerciseChordsText.setText(songChordsString);
	}
}
