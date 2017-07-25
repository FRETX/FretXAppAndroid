package fretx.version4.paging.learn.midi;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.fretx.midi.MidiFile;
import com.fretx.midi.event.MidiEvent;
import com.fretx.midi.event.NoteOff;
import com.fretx.midi.event.NoteOn;
import com.fretx.midi.event.meta.Tempo;
import com.fretx.midi.util.MidiEventListener;
import com.fretx.midi.util.MidiProcessor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import fretx.version4.R;
import fretx.version4.fragment.FretboardFragment;
import fretx.version4.utils.bluetooth.Bluetooth;
import rocks.fretx.audioprocessing.FretboardPosition;
import rocks.fretx.audioprocessing.MusicUtils;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 21/07/17 14:00.
 */

public class MidiExercise extends Fragment {
    private static final String TAG = "KJKP6_MIDI_EXERCISE";
    private final FretboardFragment fretboardFragment = new FretboardFragment();
    private final SparseArray<FretboardPosition> notes = new SparseArray<>();
    private final ArrayList<FretboardPosition> positions = new ArrayList<>();
    private final Handler handler = new Handler();
    private SeekBar seekbar;
    private MidiProcessor processor;
    private MidiFile midiFile;
    private Button playPause;
    private String filename;
    private long totalTicks;
    private long currentTick;
    private boolean touched;

    public static MidiExercise newInstance(File mdf) {
        final MidiExercise fragment = new MidiExercise();
        try {
            fragment.filename = mdf.getName();
            fragment.midiFile = new MidiFile(mdf);
            fragment.totalTicks = fragment.midiFile.getLengthInTicks();
            Log.d(TAG, "length: " + fragment.totalTicks);
        } catch (IOException e) {
            fragment.midiFile = null;
            e.printStackTrace();
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentTick = 0;

        // Create a new MidiProcessor:
        processor = new MidiProcessor(midiFile);

        // Register for the events you're interested in:
        EventPrinter ep = new EventPrinter("Individual Listener");
        processor.registerEventListener(ep, Tempo.class);
        processor.registerEventListener(ep, NoteOn.class);
        processor.registerEventListener(ep, NoteOff.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final LinearLayout rootView = (LinearLayout) inflater.inflate(R.layout.paging_learn_midi_exercise, container, false);

        final android.support.v4.app.FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fretboard_fragment_container, fretboardFragment);
        fragmentTransaction.commit();
        //fretboardFragment.strum();

        TextView name = (TextView) rootView.findViewById(R.id.name);
        name.setText(filename);

        seekbar = (SeekBar) rootView.findViewById(R.id.seekbar);
        seekbar.setMax((int)totalTicks);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                notes.clear();
                processor.seekTo(seekBar.getProgress());
            }
        });

        playPause = (Button) rootView.findViewById(R.id.playpause);
        playPause.setText("play");
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (processor.isRunning()) {
                    processor.stop();
                } else {
                    processor.start();
                }
            }
        });
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        processor.stop();
    }

    private class EventPrinter implements MidiEventListener {
        private String mLabel;

        public EventPrinter(String label) {
            mLabel = label;
        }

        @Override
        public void onStart(boolean fromBeginning) {
            handler.post(updatePauseButton);
            if (fromBeginning) {
                Log.d(TAG, mLabel + " Started!");
            } else {
                Log.d(TAG, mLabel + " resumed");
            }
        }

        @Override
        public void onEvent(MidiEvent event, long ms) {
            if (event instanceof NoteOn) {
                NoteOn noteOn = (NoteOn) event;

                seekbar.setProgress((int)event.getTick());
                if (noteOn.getVelocity() == 0) {
                    if (notes.get(noteOn.getNoteValue()) != null) {
                        touched = true;
                        notes.remove(noteOn.getNoteValue());
                    }
                } else {
                    if (notes.get(noteOn.getNoteValue()) == null) {
                        final FretboardPosition pos = MusicUtils.midiNoteToFretboardPosition(noteOn.getNoteValue());
                        touched = true;
                        notes.put(noteOn.getNoteValue(), pos);
                    }
                }
                handler.post(updateBluetooth);
            } else if (event instanceof NoteOff) {
                NoteOff noteOff = (NoteOff) event;
                if (notes.get(noteOff.getNoteValue()) != null) {
                    touched = true;
                    notes.remove(noteOff.getNoteValue());
                }
                handler.post(updateBluetooth);
            }
        }

        @Override
        public void onStop(boolean finished) {
            handler.post(updatePlayButton);
            if (finished) {
                Log.d(TAG, mLabel + " Finished!");
            } else {
                Log.d(TAG, mLabel + " paused");
            }
        }
    }

    private Runnable updateBluetooth = new Runnable() {
        @Override
        public void run() {
            SparseArray<FretboardPosition> notesClone = notes.clone();
            byte fingerings[] = new byte[notesClone.size()];
            positions.clear();
            touched = false;
            for(int i = 0; !touched && i < notesClone.size(); i++) {
                int key = notesClone.keyAt(i);
                fingerings[i] = notesClone.get(key).getByteCode();
                positions.add(notesClone.get(key));
            }
            fretboardFragment.setFingerings(positions);
            Bluetooth.getInstance().setMatrix(fingerings);
        }
    };

    private Runnable updatePlayButton = new Runnable() {
        @Override
        public void run() {
            playPause.setText("play");
        }
    };

    private Runnable updatePauseButton = new Runnable() {
        @Override
        public void run() {
            playPause.setText("pause");
        }
    };
}
