package fretx.version4.paging.learn.custom.exercise;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import fretx.version4.BluetoothClass;
import fretx.version4.ChordListener;
import fretx.version4.FretboardView;
import fretx.version4.R;
import fretx.version4.activities.MainActivity;
import fretx.version4.utils.MidiPlayer;
import rocks.fretx.audioprocessing.Chord;
import rocks.fretx.audioprocessing.FingerPositions;
import rocks.fretx.audioprocessing.MusicUtils;

public class LearnCustomExerciseFragment extends Fragment implements Observer {
    private MainActivity mActivity;

    //view
	private FretboardView fretboardView;
	private TextView chordsText;
    private TextView chordText;
    private TextView positionText;
    private Button playButton;

    //chords
    private HashMap<String,FingerPositions> chordDb;
    private ArrayList<Chord> exerciseChords;
    private int chordIndex;

    //audio
    ChordListener chordListener;
    MidiPlayer midiPlayer;

    @Override
    public void update(Observable o, Object arg) {
        ++chordIndex;

        //end of the exercise
        if (chordIndex == exerciseChords.size())
            chordIndex = 0;

        setChord();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = (MainActivity) getActivity();

        // Instantiate the driver.
        midiPlayer = new MidiPlayer();

        //retrieve chords database
        chordDb = MusicUtils.parseChordDb();

        //setup view
        LinearLayout rootView = (LinearLayout) inflater.inflate(R.layout.paging_learn_custom_exercise, container, false);
		fretboardView = (FretboardView) rootView.findViewById(R.id.fretboardView);
		chordsText = (TextView) rootView.findViewById(R.id.exerciseChordsTextView);
        positionText = (TextView) rootView.findViewById(R.id.position);
        chordText = (TextView) rootView.findViewById(R.id.textChord);
        Button playButton = (Button) rootView.findViewById(R.id.playChordButton);

        return rootView;
    }

	@Override
	public void onViewCreated(View v , Bundle savedInstanceState){
        chordListener = new ChordListener(mActivity.audio);
        chordListener.addObserver(this);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                midiPlayer.playChord(exerciseChords.get(chordIndex));
            }
        });

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
        //update positionText
        positionText.setText(chordIndex + "/" + exerciseChords.size());
        //update chord listener
        chordListener.success = false;
        chordListener.setTargetChord(actualChord);
        chordListener.startListening();
        //update led
        byte[] bluetoothArray = MusicUtils.getBluetoothArrayFromChord(actualChord.toString(), chordDb);
        BluetoothClass.sendToFretX(bluetoothArray);
    }
}
