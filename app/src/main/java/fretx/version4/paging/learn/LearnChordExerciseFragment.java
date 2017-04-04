package fretx.version4.paging.learn;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import fretx.version4.FretboardView;
import fretx.version4.activities.MainActivity;
import fretx.version4.R;
import fretx.version4.utils.MidiPlayer;
import rocks.fretx.audioprocessing.Chord;

public class LearnChordExerciseFragment extends Fragment {

    MainActivity mActivity;
    FrameLayout rootView = null;
	LearnChordExerciseView chordExerciseView;
	FretboardView fretboardView;
	ArrayList<Chord> exerciseChords;
	MidiPlayer midiPlayer;

    public LearnChordExerciseFragment(){}

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

	@Override
	public void onViewCreated(View v , Bundle savedInstanceState){
		// Instantiate the driver.
		midiPlayer = new MidiPlayer();

		chordExerciseView.setChords(exerciseChords);
		TextView exerciseChordsText = (TextView) v.findViewById(R.id.exerciseChordsTextView);
		if(exerciseChordsText==null) return;
		String songChordsString = "";
		for (int i = 0; i < exerciseChords.size(); i++) {
			songChordsString += exerciseChords.get(i).toString() + " ";
			Log.d("songChordString",songChordsString);
		}
		exerciseChordsText.setText(songChordsString);

        Button playChord = (Button) chordExerciseView.findViewById(R.id.playChordButton);
        playChord.setOnClickListener(new View.OnClickListener() {
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

	public void setChords(ArrayList<Chord> chords){
		this.exerciseChords = (ArrayList<Chord>) chords.clone();
		if(chordExerciseView == null) return;
		chordExerciseView.setChords(this.exerciseChords);
	}
}
