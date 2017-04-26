package fretx.version4.paging.learn.exercise;

import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import fretx.version4.FretboardView;
import fretx.version4.R;
import fretx.version4.activities.MainActivity;
import fretx.version4.paging.learn.guided.GuidedChordExercise;
import fretx.version4.utils.Audio;
import fretx.version4.utils.Bluetooth;
import fretx.version4.utils.Midi;
import fretx.version4.utils.TimeUpdater;
import rocks.fretx.audioprocessing.Chord;

public class LearnExerciseFragment extends Fragment implements Audio.AudioListener,
        LearnExerciseDialog.LearnGuidedChordExerciseListener {
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
    private int chordIndex;
	private ArrayList<Chord> exerciseChords;
    private final ArrayList<Chord> targetChords = new ArrayList<>();;

    private AlertDialog dialog;
    private TimeUpdater timeUpdater;

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
        Audio.getInstance().setAudioDetectorListener(this);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //stop listening
                playButton.setClickable(false);
                Audio.getInstance().stopListening();

                //check if music volume is up
                AudioManager audio = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
                if (audio.getStreamVolume(AudioManager.STREAM_MUSIC) < 5) {
                    Toast.makeText(getActivity(), "Volume is low", Toast.LENGTH_SHORT).show();
                }

                //play the chord
                Midi.getInstance().playChord(exerciseChords.get(chordIndex));

                //start listening after delay
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playButton.setClickable(true);
                        Audio.getInstance().startListening();
                    }
                }, 1500);
            }
        });

        //setup the first chord
        chordIndex = 0;
	}

	private void resumeAll() {
        if (exerciseChords.size() > 0 && chordIndex < exerciseChords.size()) {
            Audio.getInstance().setTargetChords(targetChords);

            setChord();
            timeUpdater.resumeTimer();
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
        Audio.getInstance().stopListening();
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d("DEBUG_YOLO", "onPause");
        pauseAll();
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
            LearnExerciseFragment guidedChordExerciseFragment = new LearnExerciseFragment();
            guidedChordExerciseFragment.setExercise(exerciseList, listIndex + 1);
            mActivity.fragNavController.replaceFragment(guidedChordExerciseFragment);
        }
    }

    public void onProgress() {
        double progress = Audio.getInstance().getProgress();
        //chord totally played
        if (progress >= 100) {
            chordProgress.setProgress(100);
            ++chordIndex;

            //end of the exercise
            if (chordIndex == exerciseChords.size()) {
                pauseAll();
                setPosition();
                LearnExerciseDialog dialog = LearnExerciseDialog.newInstance(this,
                        timeUpdater.getMinute(), timeUpdater.getSecond(),
                        exerciseList == null || listIndex == exerciseList.size() - 1);
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
    }

    public void onLowVolume() {
        thresholdImage.setImageResource(android.R.drawable.presence_audio_busy);
    }

    public void onHighVolume() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        thresholdImage.setImageResource(android.R.drawable.presence_audio_online);
    }

    public void onTimeout() {
        dialog = audioHelperDialog(getActivity());
        dialog.show();
    }

    //setup exercise flow & current exercise
    public void setExercise(List<GuidedChordExercise> exerciseList, int listIndex) {
        this.exerciseList = exerciseList;
        this.listIndex = listIndex;

        GuidedChordExercise exercise = exerciseList.get(listIndex);

        ArrayList<Chord> repeatedChords = new ArrayList<>();
        for (int i = 0; i < exercise.getRepetition(); i++) {
            repeatedChords.addAll(exercise.getChords());
        }
        this.setChords(repeatedChords);
    }

    //setup exercises chords form list of chords
    @SuppressWarnings("unchecked")
    public void setChords(ArrayList<Chord> chords) {
        exerciseChords = (ArrayList<Chord>) chords.clone();
        targetChords.clear();
        Set<Chord> hs = new LinkedHashSet<>();
        hs.addAll(chords);
        targetChords.addAll(hs);
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
        Audio.getInstance().setTargetChord(actualChord);
        Audio.getInstance().startListening();
        //setup the progress bar\
        chordProgress.setProgress(0);
        //update led
        Bluetooth.getInstance().setMatrix(actualChord);
    }

    //display chord position
    private void setPosition() {
        positionText.setText(chordIndex + "/" + exerciseChords.size());
    }

    //create a audio helper dialog
    private AlertDialog audioHelperDialog(Context context) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        //todo change text of dialog
        alertDialogBuilder.setTitle("Audio Detector")
                .setMessage("Common guys . . .")
                .setCancelable(false)
                .setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return alertDialogBuilder.create();
    }
}
