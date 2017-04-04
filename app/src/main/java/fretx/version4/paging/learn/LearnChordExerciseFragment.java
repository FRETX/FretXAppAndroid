package fretx.version4.paging.learn;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import fretx.version4.BluetoothClass;
import fretx.version4.FretboardView;
import fretx.version4.R;
import rocks.fretx.audioprocessing.Chord;
import rocks.fretx.audioprocessing.FingerPositions;
import rocks.fretx.audioprocessing.MusicUtils;

public class LearnChordExerciseFragment extends Fragment {
    //view
	private FretboardView fretboardView;
	private TextView chordsText;
    private TextView chordText;

    //chords
    private HashMap<String,FingerPositions> chordDb;
    private ArrayList<Chord> exerciseChords;
    private int chordIndex;

    public LearnChordExerciseFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //retrieve chords database
        chordDb = MusicUtils.parseChordDb();

        //setup view
        FrameLayout rootView = (FrameLayout) inflater.inflate(R.layout.learn_chord_exercise_layout, container, false);
		fretboardView = (FretboardView) rootView.findViewById(R.id.fretboardView);
		chordsText = (TextView) rootView.findViewById(R.id.exerciseChordsTextView);
        chordText = (TextView) rootView.findViewById(R.id.textChord);

        return rootView;
    }

	@Override
	public void onViewCreated(View v , Bundle savedInstanceState){
        //display all chords at bottom
        String songChordsString = "";
		for (Chord chord: exerciseChords) {
			songChordsString += chord.toString() + " ";
		}
		chordsText.setText(songChordsString);

        //setup the first chord
        chordIndex = 0;
        if (exerciseChords.size() > 0)
            setChord();
	}

    @SuppressWarnings("unchecked")
	public void setChords(@NonNull ArrayList<Chord> chords){
		exerciseChords = (ArrayList<Chord>) chords.clone();
	}

    private void setChord() {
        Chord actualChord = exerciseChords.get(chordIndex);

        //update chord title
        chordText.setText(actualChord.toString());
        //update finger position
        fretboardView.setFretboardPositions(actualChord.getFingerPositions());
        //update led
        byte[] bluetoothArray = MusicUtils.getBluetoothArrayFromChord(actualChord.toString(), chordDb);
        BluetoothClass.sendToFretX(bluetoothArray);
    }
}
