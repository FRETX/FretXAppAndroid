package fretx.version4.paging.learn;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import fretx.version4.FretboardView;
import fretx.version4.activities.MainActivity;
import fretx.version4.R;
import rocks.fretx.audioprocessing.Chord;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;
import uk.co.deanwild.materialshowcaseview.shape.RectangleShape;
import uk.co.deanwild.materialshowcaseview.shape.Shape;
import uk.co.deanwild.materialshowcaseview.target.Target;

public class LearnFragmentOne extends Fragment {

    MainActivity mActivity;

    FrameLayout rootView = null;
	LearnChordExerciseView chordExerciseView;
	FretboardView fretboardView;

    public LearnFragmentOne(){

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

	    ArrayList<Chord> exerciseChords = new ArrayList<Chord>(0);
   	    String[] majorRoots = new String[]{"D","G","C"};
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

	@Override
	public void onViewCreated(View v , Bundle savedInstanceState){
		ShowcaseConfig config = new ShowcaseConfig();
		config.setDelay(50); // half second between each showcase view
		config.setMaskColor(getResources().getColor(R.color.showcaseOverlay));
		config.setShapePadding(5);
		MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(mActivity, "ChordExerciseId");

		sequence.setConfig(config);

		sequence.addSequenceItem((View) getActivity().findViewById(R.id.redLightsView),
				"Put your fingers on the red lights", "OKAY");

		sequence.addSequenceItem((View) getActivity().findViewById(R.id.blueLightsView),
				"Don't put any fingers on the strings with blue lights, but play them", "OKAY");

		sequence.addSequenceItem((View) getActivity().findViewById(R.id.noLightsView),
				"If you see no lights on a string, don't play that string", "GOT IT!");
		sequence.start();
	}
}
