package fretx.version4;

import android.os.CountDownTimer;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Observable;

import rocks.fretx.audioprocessing.AudioProcessing;
import rocks.fretx.audioprocessing.Chord;

/**
 * Created by Kickdrum on 21-Feb-17.
 */

public class ChordListener extends Observable {
    private final String TAG = "AUDIO";

    //observable
    public boolean success;

    //audio
    private AudioProcessing audio;
    private Chord targetChord;
    private double correctlyPlayedAccumulator;

    //audio settings
    private long timerTick;
    private long onsetIgnoreDuration;
    private long chordListenDuration;
    private long timerDuration;
    private long correctlyPlayedDuration;

    static private final long TIMER_TICK = 20;
    static private final long ONSET_IGNORE_DURATION = 0; //in miliseconds
    static private final long CHORD_LISTEN_DURATION = 500; //in miliseconds
    static private final long TIMER_DURATION = ONSET_IGNORE_DURATION + CHORD_LISTEN_DURATION; //in miliseconds
    static private final long CORRECTLY_PLAYED_DURATION = 160; //in milliseconds

    static private final double VOLUME_THRESHOLD = -9;

    private CountDownTimer chordTimer = new CountDownTimer(TIMER_DURATION, TIMER_TICK) {
        public void onTick(long millisUntilFinished) {
            if (!audio.isInitialized()) {
                Log.d(TAG, "not initialized");
                return;
            }

            if (!audio.isProcessing()) {
                Log.d(TAG, "not processing");
                return;
            }

            if (!audio.isBufferAvailable()) {
                Log.d(TAG, "isBufferAvailable = false");
                return;
            }

            if (audio.getVolume() < VOLUME_THRESHOLD) {
                correctlyPlayedAccumulator = 0;
                Log.d(TAG, "prematurely canceled due to low volume");
                return;
            }

            if (millisUntilFinished <= CHORD_LISTEN_DURATION) {
                Chord playedChord = audio.getChord();
                Log.d(TAG, "played:" + playedChord.toString());

                //if (targetChord.toString().equals(playedChord.toString())) {
                correctlyPlayedAccumulator += TIMER_TICK;
                //    Log.d(TAG, "correctly played acc:" + correctlyPlayedAccumulator);
                //} else {
                //    correctlyPlayedAccumulator = 0;
                //}
            }

            if (correctlyPlayedAccumulator >= CORRECTLY_PLAYED_DURATION) {
                Log.d(TAG, "- - - - - chord detected - - - - -");
                this.cancel();
                success = true;
                setChanged();
                notifyObservers();
            }
        }

        public void onFinish() {
            Log.d(TAG, "finished without hearing enough of correct chords");
            correctlyPlayedAccumulator = 0;
            chordTimer.start();
        }
    };

    public ChordListener(@NonNull AudioProcessing audio) {
        this.audio = audio;

        timerTick = TIMER_TICK;
        onsetIgnoreDuration = ONSET_IGNORE_DURATION;
        chordListenDuration = CHORD_LISTEN_DURATION;
        timerDuration = TIMER_DURATION;
        correctlyPlayedDuration = CORRECTLY_PLAYED_DURATION;
    }

    public void setTargetChord(Chord chord) {
        targetChord = chord;
    }

    public void startListening() {
        correctlyPlayedAccumulator = 0;
        chordTimer.cancel();
        chordTimer.start();
        Log.d(TAG, "starting the countdownTimer");
    }

    public void stopListening() {
        chordTimer.cancel();
    }
}