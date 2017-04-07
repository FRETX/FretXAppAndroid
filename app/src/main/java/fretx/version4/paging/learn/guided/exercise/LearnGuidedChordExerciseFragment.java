package fretx.version4.paging.learn.guided.exercise;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import fretx.version4.paging.learn.guided.GuidedChordExercise;
import fretx.version4.utils.MidiPlayer;
import rocks.fretx.audioprocessing.Chord;
import rocks.fretx.audioprocessing.FingerPositions;
import rocks.fretx.audioprocessing.MusicUtils;

public class LearnGuidedChordExerciseFragment extends Fragment implements Observer{
	private MainActivity mActivity;

	//view
	private FretboardView fretboardView;
    private TextView chordText;
	private TextView positionText;
    private TextView timeText;
    private Button playButton;

	//chords
	int nRepetitions;
    int chordIndex;
	ArrayList<Chord> exerciseChords;
    private HashMap<String,FingerPositions> chordDb;

    //timeText
    private TimeUpdater timeUpdater;

    //audio
    private ChordListener chordListener;
    private MidiPlayer midiPlayer;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //firebase log
		mActivity = (MainActivity) getActivity();
		Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "EXERCISE");
		bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Guided Chord");
		mActivity.mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        //retrieve chords database
        chordDb = MusicUtils.parseChordDb();

		//setup view
        FrameLayout rootView = (FrameLayout) inflater.inflate(R.layout.paging_learn_guided_exercise_layout, container, false);
		fretboardView = (FretboardView) rootView.findViewById(R.id.fretboardView);
		positionText = (TextView) rootView.findViewById(R.id.position);
        timeText = (TextView) rootView.findViewById(R.id.time);
        chordText = (TextView) rootView.findViewById(R.id.textChord);
        playButton = (Button) rootView.findViewById(R.id.playChordButton);

		return rootView;
	}

    @Override
	public void onViewCreated(View v, Bundle savedInstanceState) {
        timeUpdater = new TimeUpdater(timeText);
        chordListener = new ChordListener(mActivity.audio);
        chordListener.addObserver(this);
        midiPlayer = new MidiPlayer();

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                midiPlayer.playChord(exerciseChords.get(chordIndex));
            }
        });

        //setup the first chord
        chordIndex = 0;
	}

    @Override
    public void onResume() {
        super.onResume();
        timeUpdater.resumeTimer();
        midiPlayer.start();
        if (exerciseChords.size() > 0)
            setChord();
    }

    @Override
    public void onPause() {
        super.onPause();
        timeUpdater.pauseTimer();
        midiPlayer.stop();
        chordListener.stopListening();
    }

    @Override
    public void update(Observable o, Object arg) {
        ++chordIndex;

        //end of the exercise
        if (chordIndex == exerciseChords.size()) {
            LearnGuidedChordExerciseDialog dialog = LearnGuidedChordExerciseDialog.newInstance();
            dialog.show(getFragmentManager(), "dialog");
            chordIndex = 0;
        }

        setChord();
    }

    public void setExercise(GuidedChordExercise exercise){
		this.nRepetitions = exercise.getRepetition();
		ArrayList<Chord> repeatedChords = new ArrayList<>();
		for (int i = 0; i < exercise.getRepetition(); i++) {
			repeatedChords.addAll(exercise.getChords());
		}
		this.setChords(repeatedChords);
	}

    @SuppressWarnings("unchecked")
	public void setChords(ArrayList<Chord> chords) {
		this.exerciseChords = (ArrayList<Chord>) chords.clone();
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
