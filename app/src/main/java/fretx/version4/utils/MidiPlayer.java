package fretx.version4.utils;

import android.os.Handler;
import android.util.Log;

import org.billthefarmer.mididriver.GeneralMidiConstants;
import org.billthefarmer.mididriver.MidiDriver;

import java.util.Arrays;

import rocks.fretx.audioprocessing.Chord;

/**
 * Created by pandor on 4/4/17.
 */

public class MidiPlayer extends MidiDriver implements MidiDriver.OnMidiStartListener {
    private int noteDelay = 30;
    private int sustainDelay = 500;
    private Handler handler = new Handler();
    private int notesIndex;

    @Override
    public void onMidiStart() {
        Log.d(this.getClass().getName(), "onMidiStart()");

        byte[] event = new byte[2];
        event[0] = (byte) 0xC0; //"Program Change" event for channel 1
        event[1] = GeneralMidiConstants.ACOUSTIC_GUITAR_NYLON; //set instrument
        write(event);
    }

    public MidiPlayer() {
        super();
        setOnMidiStartListener(this);
    }

    public MidiPlayer(int noteDelay, int sustainDelay) {
        super();
        setOnMidiStartListener(this);
        this.noteDelay = noteDelay;
        this.sustainDelay = sustainDelay;
    }

    private void playNote(int note){
        byte[] event = new byte[3];
        event[0] = (byte) (0x90 | 0x00);  // 0x90 = note On, 0x00 = channel 1
        event[1] =  Byte.parseByte(Integer.toString(note));
        event[2] = (byte) 0x7F;  // 0x7F = the maximum velocity (127)
        write(event);

        Log.d("playing note",Integer.toString(note));
    }

    private void stopNote(int note) {
        byte[] event = new byte[3];
        event[0] = (byte) (0x80 | 0x00);  // 0x80 = note Off, 0x00 = channel 1
        event[1] = Byte.parseByte(Integer.toString(note));
        event[2] = (byte) 0x00;  // 0x00 = the minimum velocity (0)
        write(event);
        Log.d("stopping note", Integer.toString(note));
    }

    public void playChord(Chord chord) {
        final int[] notes = chord.getMidiNotes();
        notesIndex = 0;

        Log.d("notes", Arrays.toString(notes));

        final Runnable turnOffAllNotes = new Runnable() {
            @Override
            public void run() {
                for (int note: notes) {
                    stopNote(note);
                }
            }
        };

        Runnable playNoteSequence = new Runnable() {
            @Override
            public void run() {
                if(notesIndex < notes.length) {
                    playNote(notes[notesIndex++]);
                    handler.postDelayed(this, noteDelay);
                } else {
                    handler.postDelayed(turnOffAllNotes, sustainDelay);
                }
            }
        };

        handler.post(playNoteSequence);
    }
}