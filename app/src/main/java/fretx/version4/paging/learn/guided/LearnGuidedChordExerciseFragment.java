package fretx.version4.paging.learn.guided;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import fretx.version4.BluetoothClass;
import fretx.version4.ChordListener;
import fretx.version4.FretboardView;
import fretx.version4.R;
import fretx.version4.TimeUpdater;
import fretx.version4.activities.MainActivity;
import rocks.fretx.audioprocessing.Chord;
import rocks.fretx.audioprocessing.FingerPositions;
import rocks.fretx.audioprocessing.MusicUtils;

public class LearnGuidedChordExerciseFragment extends Fragment implements Observer{
	private MainActivity mActivity;

	//view
	private FretboardView fretboardView;
    private TextView chordsText;
    private TextView chordText;
	private TextView positionText;
    private TextView timeText;

	//chords
	int nRepetitions;
    int chordIndex;
	ArrayList<Chord> exerciseChords;
    private HashMap<String,FingerPositions> chordDb;

    //timeText
    TimeUpdater timeUpadater;

    //audio
    ChordListener chordListener;

    @Override
    public void update(Observable o, Object arg) {
        Log.d("AUDIO", "Observer triggered");
        ++chordIndex;
        setChord();
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //firebase log
		mActivity = (MainActivity) getActivity();
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Guided Chord Exercise activated");
		mActivity.mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        //retrieve chords database
        chordDb = MusicUtils.parseChordDb();

		//setup view
        FrameLayout rootView = (FrameLayout) inflater.inflate(R.layout.learn_guided_chord_exercise_layout, container, false);
		fretboardView = (FretboardView) rootView.findViewById(R.id.fretboardView);
        chordsText = (TextView) rootView.findViewById(R.id.exerciseChordsTextView);
		positionText = (TextView) rootView.findViewById(R.id.position);
        timeText = (TextView) rootView.findViewById(R.id.time);
        chordText = (TextView) rootView.findViewById(R.id.textChord);

		return rootView;
	}

    @Override
	public void onViewCreated(View v, Bundle savedInstanceState) {
        timeUpadater = new TimeUpdater(timeText);
        chordListener = new ChordListener(mActivity.audio);
        chordListener.addObserver(this);

		//display all chords at bottom
		String songChordsString = "";
		for (int i = 0; i < exerciseChords.size(); i++) {
			songChordsString += exerciseChords.get(i).toString() + " ";
			Log.d("songChordString", songChordsString);
		}
		chordsText.setText(songChordsString);

        //setup the first chord
        chordIndex = 0;
        if (exerciseChords.size() > 0)
            setChord();
	}

    @Override
    public void onResume() {
        super.onResume();
        timeUpadater.resumeTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        timeUpadater.pauseTimer();
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

    @SuppressWarnings("unchecked")
	public void setChords(ArrayList<Chord> chords) {
		this.exerciseChords = (ArrayList<Chord>) chords.clone();
	}

    private void setChord() {
        Chord actualChord = exerciseChords.get(chordIndex);

        //update chord title
        chordText.setText(actualChord.toString());
        //update finger positionText
        fretboardView.setFretboardPositions(actualChord.getFingerPositions());
		//update positionText
		positionText.setText(chordIndex + "/" + exerciseChords.size());
        //update chord listener
        chordListener.setTargetChord(actualChord);
        chordListener.listen();
        //update led
        byte[] bluetoothArray = MusicUtils.getBluetoothArrayFromChord(actualChord.toString(), chordDb);
        BluetoothClass.sendToFretX(bluetoothArray);
    }
}
