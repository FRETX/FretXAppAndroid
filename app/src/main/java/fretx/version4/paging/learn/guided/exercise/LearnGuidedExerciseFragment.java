package fretx.version4.paging.learn.guided.exercise;

import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import fretx.version4.BluetoothClass;
import fretx.version4.utils.ChordListener;
import fretx.version4.FretboardView;
import fretx.version4.R;
import fretx.version4.utils.TimeUpdater;
import fretx.version4.activities.MainActivity;
import fretx.version4.paging.learn.guided.GuidedChordExercise;
import fretx.version4.utils.MidiPlayer;
import rocks.fretx.audioprocessing.Chord;
import rocks.fretx.audioprocessing.FingerPositions;
import rocks.fretx.audioprocessing.MusicUtils;

public class LearnGuidedExerciseFragment extends Fragment implements Observer,
        LearnGuidedExerciseDialog.LearnGuidedChordExerciseListener {
	private MainActivity mActivity;

	//view
	private FretboardView fretboardView;
    private TextView chordText;
    private TextView chordNextText;
	private TextView positionText;
    private TextView timeText;
    private Button playButton;
    private ProgressBar chordProgress;
    private ImageView thresholdImage;

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

    //exercises
    private List<GuidedChordExercise> exerciseList;
    private int listIndex;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("DEBUG_YOLO", "onCreateView");

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
        chordNextText = (TextView) rootView.findViewById(R.id.textNextChord);
        playButton = (Button) rootView.findViewById(R.id.playChordButton);
        chordProgress = (ProgressBar) rootView.findViewById(R.id.chord_progress);
        thresholdImage = (ImageView) rootView.findViewById(R.id.audio_thresold);

        return rootView;
	}

    @Override
	public void onViewCreated(View v, Bundle savedInstanceState) {
        Log.d("DEBUG_YOLO", "onViewCreated");

        timeUpdater = new TimeUpdater(timeText);
        chordListener = new ChordListener(mActivity.audio);
        chordListener.addObserver(this);
        midiPlayer = new MidiPlayer();
        midiPlayer.addObserver(this);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playButton.setClickable(false);
                chordListener.stopListening();
                midiPlayer.playChord(exerciseChords.get(chordIndex));
            }
        });

        //setup the first chord
        chordIndex = 0;
	}

	private void resumeAll() {
        if (exerciseChords.size() > 0 && chordIndex < exerciseChords.size()) {
            setChord();
            timeUpdater.resumeTimer();
            midiPlayer.start();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d("DEBUG_YOLO", "onResume");
        resumeAll();
    }

    private void pauseAll() {
        timeUpdater.pauseTimer();
        midiPlayer.stop();
        chordListener.stopListening();
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d("DEBUG_YOLO", "onPause");
        pauseAll();
    }

    //get actions of observables
    @Override
    public void update(Observable o, Object arg) {
        Log.d("DEBUG_YOLO", "callback chord listener");

        //advance to the next chord
        if (o instanceof ChordListener) {
            switch ((int) arg) {
                case ChordListener.STATUS_BELOW_THRESHOLD:
                    thresholdImage.setImageResource(android.R.drawable.presence_audio_busy);
                    break;
                case ChordListener.STATUS_UPSIDE_THRESHOLD:
                    thresholdImage.setImageResource(android.R.drawable.presence_audio_online);
                    break;
                case ChordListener.STATUS_PROGRESS_UPDATE:
                    double progress = chordListener.getProgress();
                    //chord totally played
                    if (progress >= 100) {
                        chordProgress.setProgress(100);
                        ++chordIndex;

                        //end of the exercise
                        if (chordIndex == exerciseChords.size()) {
                            pauseAll();
                            setPosition();
                            LearnGuidedExerciseDialog dialog = LearnGuidedExerciseDialog.newInstance(this,
                                    timeUpdater.getMinute(), timeUpdater.getSecond(), listIndex == exerciseList.size() - 1);
                            dialog.show(getFragmentManager(), "dialog");
                        }
                        //middle of an exercise
                        else {
                            setChord();
                        }
                    }
                    //chord in progress
                    else {
                        chordProgress.setProgress((int)progress);
                    }
                    break;
            }
        }
        //audio preview finished
        else if (o instanceof MidiPlayer) {
            Log.d("DEBUG_YOLO", "callback midiplayer");

            playButton.setClickable(true);
            chordListener.startListening();
        }
    }

    //retrieve result of the finished exercise dialog
    @Override
    public void onUpdate(boolean replay) {
        Log.d("DEBUG_YOLO", "onUpdate");

        //replay the actual exercise
        if (replay) {
            chordIndex = 0;
            timeUpdater.resetTimer();
            resumeAll();
        }
        //goes to the next exercise
        else {
            Toast.makeText(getActivity(), "DO NOT REPLAY!", Toast.LENGTH_SHORT).show();

            LearnGuidedExerciseFragment guidedChordExerciseFragment = new LearnGuidedExerciseFragment();
            guidedChordExerciseFragment.setExercise(exerciseList, listIndex + 1);
            mActivity.fragNavController.replaceFragment(guidedChordExerciseFragment);
        }
    }

    public void setExercise(List<GuidedChordExercise> exerciseList, int listIndex) {
        this.exerciseList = exerciseList;
        this.listIndex = listIndex;

        GuidedChordExercise exercise = exerciseList.get(listIndex);

        this.nRepetitions = exercise.getRepetition();
        ArrayList<Chord> repeatedChords = new ArrayList<>();
        for (int i = 0; i < exercise.getRepetition(); i++) {
            repeatedChords.addAll(exercise.getChords());
        }
        this.setChords(repeatedChords);
    }

    //setup exercises chords form list of chords
    @SuppressWarnings("unchecked")
    private void setChords(ArrayList<Chord> chords) {
        this.exerciseChords = (ArrayList<Chord>) chords.clone();
    }

    //setup everything according actual chord
    private void setChord() {
        Chord actualChord = exerciseChords.get(chordIndex);
        Log.d("DEBUG_YOLO", "setChord " + actualChord.toString());

        //update chord title
        chordText.setText(actualChord.toString());
        if (chordIndex + 1 < exerciseChords.size())
            chordNextText.setText(exerciseChords.get(chordIndex + 1).toString());
        else
            chordNextText.setText("");
        //update finger position
        fretboardView.setFretboardPositions(actualChord.getFingerPositions());
		//update positionText
		setPosition();
        //update chord listener
        chordListener.setTargetChord(actualChord);
        chordListener.startListening();
        //setup the progress bar\
        chordProgress.setProgress(0);
        //update led
        byte[] bluetoothArray = MusicUtils.getBluetoothArrayFromChord(actualChord.toString(), chordDb);
        BluetoothClass.sendToFretX(bluetoothArray);
    }

    //display chord position
    private void setPosition() {
        positionText.setText(chordIndex + "/" + exerciseChords.size());
    }
}
