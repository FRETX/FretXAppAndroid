package fretx.version4.paging.learn;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import fretx.version4.FretboardView;
import fretx.version4.activities.MainActivity;
import fretx.version4.R;
import rocks.fretx.audioprocessing.Chord;
//import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
//import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
//import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;
//import uk.co.deanwild.materialshowcaseview.shape.RectangleShape;
//import uk.co.deanwild.materialshowcaseview.shape.Shape;
//import uk.co.deanwild.materialshowcaseview.target.Target;

public class LearnFragmentChordExercise extends Fragment {

    MainActivity mActivity;

    FrameLayout rootView = null;
	LearnChordExerciseView chordExerciseView;
	FretboardView fretboardView;
	ArrayList<Chord> exerciseChords;

    public LearnFragmentChordExercise(){

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

//	    if(exerciseChords == null){
//		    exerciseChords = new ArrayList<Chord>(0);
//		    exerciseChords.add(new Chord("A","m"));
//		    exerciseChords.add(new Chord("C","maj"));
//		    exerciseChords.add(new Chord("G","maj"));
//	    }

//   	    String[] majorRoots = new String[]{"D","G","C"};
//	    for (int i = 0; i < majorRoots.length; i++) {
//		    exerciseChords.add(new Chord(majorRoots[i], "maj"));
//	    }
//	    String[] majorRoots = new String[]{"G","D"};
//	    for (int i = 0; i < majorRoots.length; i++) {
//		    exerciseChords.add(new Chord(majorRoots[i], "maj"));
//	    }
//	    String[] minorRoots = new String[]{"A"};
//	    for (int i = 0; i < minorRoots.length; i++) {
//		    exerciseChords.add(new Chord(minorRoots[i], "m"));
//	    }
//	    exerciseChords.add(new Chord("C","maj"));

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
