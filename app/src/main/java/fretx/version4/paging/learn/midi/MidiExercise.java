package fretx.version4.paging.learn.midi;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.pdrogfer.mididroid.MidiFile;
import com.pdrogfer.mididroid.event.MidiEvent;
import com.pdrogfer.mididroid.event.NoteOff;
import com.pdrogfer.mididroid.event.NoteOn;
import com.pdrogfer.mididroid.event.meta.Tempo;
import com.pdrogfer.mididroid.util.MidiEventListener;
import com.pdrogfer.mididroid.util.MidiProcessor;

import java.io.File;
import java.io.IOException;

import fretx.version4.R;
import fretx.version4.utils.bluetooth.Bluetooth;
import rocks.fretx.audioprocessing.FretboardPosition;
import rocks.fretx.audioprocessing.MusicUtils;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 21/07/17 14:00.
 */

public class MidiExercise extends Fragment {
    private static final String TAG = "KJKP6_MIDI_EXERCISE";
    private MidiProcessor processor;
    private MidiFile midiFile;
    private Button playPause;
    private final SparseArray<Byte> notes = new SparseArray<>();

    public static MidiExercise newInstance(File mdf) {
        final MidiExercise fragment = new MidiExercise();
        try {
            fragment.midiFile = new MidiFile(mdf);
        } catch (IOException e) {
            fragment.midiFile = null;
            e.printStackTrace();
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        playPause = (Button) rootView.findViewById(R.id.playpause);
        playPause.setText("play");
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (processor.isRunning()) {
                    playPause.setText("play");
                    processor.stop();
                } else {
                    playPause.setText("pause");
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
        playPause.setText("play");
    }

    private class EventPrinter implements MidiEventListener {
        private String mLabel;

        public EventPrinter(String label) {
            mLabel = label;
        }

        @Override
        public void onStart(boolean fromBeginning) {
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
                if (noteOn.getVelocity() == 0) {
                    if (notes.get(noteOn.getNoteValue()) != null) {
                        notes.remove(noteOn.getNoteValue());
                    }
                } else {
                    if (notes.get(noteOn.getNoteValue()) == null) {
                        FretboardPosition pos = MusicUtils.midiNoteToFretboardPosition(noteOn.getNoteValue());
                        notes.put(noteOn.getNoteValue(), pos.getByteCode());
                    }
                }
                updateBluetooth();
            } else if (event instanceof NoteOff) {
                NoteOff noteOff = (NoteOff) event;
                if (notes.get(noteOff.getNoteValue()) != null) {
                    notes.remove(noteOff.getNoteValue());
                }
                updateBluetooth();
            }
        }

        @Override
        public void onStop(boolean finished) {
            if (finished) {
                Log.d(TAG, mLabel + " Finished!");
            } else {
                Log.d(TAG, mLabel + " paused");
            }
        }
    }

    private void updateBluetooth() {
        byte fingerings[] = new byte[notes.size()];
        for(int i = 0; i < notes.size(); i++) {
            int key = notes.keyAt(i);
            fingerings[i] = notes.get(key);
        }
        Bluetooth.getInstance().setMatrix(fingerings);
    }
}
