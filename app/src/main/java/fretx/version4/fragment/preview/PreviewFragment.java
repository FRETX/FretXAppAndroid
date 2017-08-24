package fretx.version4.fragment.preview;


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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;

import fretx.version4.R;
import fretx.version4.fragment.FretboardFragment;
import fretx.version4.utils.TimeUpdater;
import fretx.version4.utils.audio.Audio;
import fretx.version4.utils.audio.Midi;
import fretx.version4.utils.audio.SoundPoolPlayer;
import fretx.version4.utils.bluetooth.Bluetooth;
import rocks.fretx.audioprocessing.Chord;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 09/05/17 10:22.
 */

public class PreviewFragment extends Fragment implements Audio.AudioListener {
    private final static String TAG = "KJKP6_PREVIEW";
    private final static int SUCCESS_DELAY_MS = 500;
    private PreviewListener listener;
    private final Handler handler = new Handler();
    private SoundPoolPlayer sound;
    private boolean midiAutoPlay = true;

    //view
    private TextView chordText;
    private TextView chordNextText;
    private ImageView playButton;
    private ImageView thresholdImage;
    private ProgressBar exerciseProgress;
    private ImageView greenTick;
    private Button nextChordButton;
    private Button playSongButton;

    //childFragment
    private final FretboardFragment fretboardFragment = new FretboardFragment();

    //chords
    private int chordIndex;
    private final ArrayList<Chord> exerciseChords = new ArrayList<>();
    private final ArrayList<Chord> targetChords = new ArrayList<>();
    private final ArrayList<Chord> majorChords = new ArrayList<>();

    private AlertDialog dialog;
    private boolean finished;
    private TimeUpdater timeUpdater;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        majorChords.add(new Chord("A", "maj"));
        majorChords.add(new Chord("B", "maj"));
        majorChords.add(new Chord("C", "maj"));
        majorChords.add(new Chord("D", "maj"));
        majorChords.add(new Chord("E", "maj"));
        majorChords.add(new Chord("F", "maj"));
        majorChords.add(new Chord("G", "maj"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //setup view
        LinearLayout rootView = (LinearLayout) inflater.inflate(R.layout.fragment_preview, container, false);
        chordText = (TextView) rootView.findViewById(R.id.textChord);
        chordNextText = (TextView) rootView.findViewById(R.id.textNextChord);
        playButton = (ImageView) rootView.findViewById(R.id.playChordButton);
        thresholdImage = (ImageView) rootView.findViewById(R.id.audio_thresold);
        exerciseProgress = (SeekBar) rootView.findViewById(R.id.seekbar);
        greenTick = (ImageView) rootView.findViewById(R.id.green_tick);
        nextChordButton = (Button) rootView.findViewById(R.id.next_chord_button);
        playSongButton = (Button) rootView.findViewById(R.id.play_song_button);

        final android.support.v4.app.FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fretboard_fragment_container, fretboardFragment);
        fragmentTransaction.commit();

        exerciseProgress.setMax(exerciseChords.size());

        dialog = audioHelperDialog(getActivity());

        return rootView;
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        timeUpdater = new TimeUpdater(null);
        Audio.getInstance().setAudioDetectorListener(this);

        //avoid interaction with seekbar
        exerciseProgress.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        nextChordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextChord();
            }
        });

        playSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPlaySong();
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (midiAutoPlay) {
                    playButton.setImageResource(R.drawable.speaker_off);
                    midiAutoPlay = false;
                } else {
                    midiAutoPlay = true;
                    playButton.setImageResource(R.drawable.speaker_on);
                    //playMidi();
                }
            }
        });

        //setup the first chord
        chordIndex = 0;
    }

    /*

     */

    @Override
    public void onResume() {
        super.onResume();
        sound = new SoundPoolPlayer(getActivity());

        if (exerciseChords.size() > 0 && chordIndex < exerciseChords.size()) {
            Audio.getInstance().setTargetChords(targetChords);
            setChord();
            timeUpdater.resumeTimer();
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        timeUpdater.pauseTimer();
        Audio.getInstance().stopListening();

        //in case success actions were playing
        handler.removeCallbacksAndMessages(null);
        greenTick.setVisibility(View.INVISIBLE);
        sound.release();

        //if the last chord has been played, display dialog
        if (exerciseChords.size() != 0 && chordIndex == exerciseChords.size() && !finished) {
            Bluetooth.getInstance().clearMatrix();
            finished = true;
            listener.onFinish(timeUpdater.getMinute(), timeUpdater.getSecond());
        }

        super.onPause();
    }

    private final Runnable hideSuccess = new Runnable() {
        @Override
        public void run() {
            greenTick.setVisibility(View.INVISIBLE);

            //end of the exercise
            if (chordIndex == exerciseChords.size()) {
                timeUpdater.pauseTimer();
                finished = true;
                listener.onFinish(timeUpdater.getMinute(), timeUpdater.getSecond());
            }
            //middle of the exercise
            else {
                setChord();
            }
        }
    };

    @Override
    public void onProgress() {
        double progress = Audio.getInstance().getProgress();
        //chord totally played
        if (progress >= 100) {
            ++chordIndex;

            Audio.getInstance().stopListening();
            greenTick.setVisibility(View.VISIBLE);
            Bluetooth.getInstance().lightMatrix();
            sound.playShortResource(R.raw.chime_bell_ding);

            handler.postDelayed(hideSuccess, SUCCESS_DELAY_MS);
        }
    }

    @Override
    public void onLowVolume() {
        thresholdImage.setImageResource(android.R.drawable.presence_audio_busy);
    }

    @Override
    public void onHighVolume() {
        //auto dismiss dialog when volume goes up
        //if (dialog != null) {
        // dialog.dismiss();
        // dialog = null;
        //}
        thresholdImage.setImageResource(android.R.drawable.presence_audio_online);
    }

    @Override
    public void onTimeout() {
        dialog.show();
    }

    public void setTargetChords(ArrayList<Chord> chords) {
        targetChords.clear();
        targetChords.addAll(new HashSet<>(chords));
        for (Chord majorChord: majorChords) {
            final String chordRoot = majorChord.getRoot();
            boolean rootExist = false;
            for (Chord e: chords) {
                if ( e.getRoot().equals(chordRoot) ||
                        ((e.getRoot().equals("A")) && chordRoot.equals("F")) || //temporary heuristic
                        ((e.getRoot().equals("F")) && chordRoot.equals("A"))
                        ) {
                    rootExist = true;
                    break;
                }
            }
            if (!rootExist)
                targetChords.add(majorChord);
        }
    }

    //setup exercises chords form list of chords
    public void setChords(ArrayList<Chord> chords) {
        exerciseChords.addAll(chords);
    }

    public void setChords(ArrayList<Chord> chords, int rep) {
        for (int i = 0; i < rep; i++) {
            exerciseChords.addAll(chords);
        }
    }

    //setup everything according actual chord
    private void setChord() {
        if(chordIndex >= exerciseChords.size()) return;
        Chord actualChord = exerciseChords.get(chordIndex);
        Log.d(TAG, "setChord " + actualChord.toString());

        //update chord title
        chordText.setText(actualChord.toString());
        if (chordIndex + 1 < exerciseChords.size())
            chordNextText.setText(exerciseChords.get(chordIndex + 1).toString());
        else
            chordNextText.setText("");
        //play midi
        if (midiAutoPlay)
            playMidi();
        //update finger position
        fretboardFragment.setChord(actualChord);
        fretboardFragment.strum();
        //update chord listener
        Audio.getInstance().setTargetChord(actualChord);
        Audio.getInstance().startListening();
        //setup the progress bar
        exerciseProgress.setProgress(chordIndex);
        //update led
        Bluetooth.getInstance().setMatrix(actualChord);
    }

    //create a audio helper dialog
    private AlertDialog audioHelperDialog(Context context) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        //todo change text of dialog
        alertDialogBuilder.setTitle("Audio Detector")
                .setMessage("Low sound detected. Please try bringing your guitar closer or playing louder.")
                .setCancelable(false)
                .setPositiveButton("Ok",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        return alertDialogBuilder.create();
    }

    public void setListener(PreviewListener previewListener) {
        listener = previewListener;
    }

    public void reset() {
        chordIndex = 0;

        //reset timer
        timeUpdater.resetTimer();
        timeUpdater.resumeTimer();

        finished = false;

        setChord();
    }

    public void nextChord() {
        if (exerciseChords.size() > 0 && chordIndex < exerciseChords.size()) {
            ++chordIndex;
            setChord();
        }
        if (chordIndex == exerciseChords.size()) {
            timeUpdater.pauseTimer();
            Audio.getInstance().stopListening();
            finished = true;
            listener.onFinish(timeUpdater.getMinute(), timeUpdater.getSecond());
        }
    }

    private void playMidi() {
        if (chordIndex < 0 || chordIndex == exerciseChords.size())
            return;

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
        //Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                playButton.setClickable(true);
                Audio.getInstance().startListening();
            }
        }, 1500);
    }
}
