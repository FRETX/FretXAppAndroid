package fretx.version4.paging.learn;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import fretx.version4.FretboardView;
import fretx.version4.activities.MainActivity;
import fretx.version4.R;
import rocks.fretx.audioprocessing.Chord;

public class LearnFragmentOne extends Fragment {

    MainActivity mActivity;

    RelativeLayout rootView = null;
	LearnChordExerciseView chordExerciseView;
	FretboardView fretboardView;

    public LearnFragmentOne(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = (MainActivity)getActivity();
        rootView = (RelativeLayout) inflater.inflate(R.layout.learn_chord_exercise_layout, container, false);
	    chordExerciseView = (LearnChordExerciseView) rootView.findViewById(R.id.chordExerciseView);
	    chordExerciseView.setmActivity(mActivity);
	    chordExerciseView.setRootView(rootView);
	    fretboardView = (FretboardView) rootView.findViewById(R.id.fretboardView);
	    chordExerciseView.setFretBoardView(fretboardView);

	    ArrayList<Chord> exerciseChords = new ArrayList<Chord>(0);
   	    String[] majorRoots = new String[]{"G","C","D"};
	    for (int i = 0; i < majorRoots.length; i++) {
		    exerciseChords.add(new Chord(majorRoots[i], "maj"));
	    }
//	    String[] majorRoots = new String[]{"G","D"};
//	    for (int i = 0; i < majorRoots.length; i++) {
//		    exerciseChords.add(new Chord(majorRoots[i], "maj"));
//	    }
//	    String[] minorRoots = new String[]{"A"};
//	    for (int i = 0; i < minorRoots.length; i++) {
//		    exerciseChords.add(new Chord(minorRoots[i], "m"));
//	    }
//	    exerciseChords.add(new Chord("C","maj"));

	    chordExerciseView.setChords(exerciseChords);

        return rootView;
    }
}
