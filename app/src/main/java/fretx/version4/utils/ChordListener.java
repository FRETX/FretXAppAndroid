package fretx.version4.utils;

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
    public static final int STATUS_BELOW_THRESHOLD = 0;
    public static final int STATUS_UPSIDE_THRESHOLD = 1;
    public static final int STATUS_PROGRESS_UPDATE = 2;
    public static final int STATUS_TIMEOUT = 3;

    //audio
    private AudioProcessing audio;
    private Chord targetChord;
    private double correctlyPlayedAccumulator;
    private boolean upsideThreshold;
    private int timeoutCounter;
    private boolean timeoutNotified;

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
    static private final int TIMEOUT_THRESHOLD = 20;

    public ChordListener(@NonNull AudioProcessing audio) {
        this.audio = audio;
        this.upsideThreshold = false;
        this.timeoutNotified = false;

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
        timeoutCounter = 0;
        chordTimer.cancel();
        chordTimer.start();
        Log.d(TAG, "starting the countdownTimer");
    }

    public void stopListening() {
        chordTimer.cancel();
    }

    public double getProgress() {
        return correctlyPlayedAccumulator / CORRECTLY_PLAYED_DURATION * 100;
    }

    //todo replace with a handler to avoid restart of countdown timer
    private CountDownTimer chordTimer = new CountDownTimer(TIMER_DURATION, TIMER_TICK) {
        public void onTick(long millisUntilFinished) {

            //todo remove this 2 checks - should not happen
            if (!audio.isInitialized()) {
                //Log.d("USELESSSTUF", "not initialized");
                return;
            }
            if (!audio.isProcessing()) {
                //Log.d("USELESSSTUF", "not processing");
                return;
            }

            if (!audio.isBufferAvailable()) {
                return;
            }

            //nothing heard
            if (audio.getVolume() < VOLUME_THRESHOLD) {
                if (upsideThreshold) {
                    upsideThreshold = false;
                    correctlyPlayedAccumulator = 0;
                    setChanged();
                    notifyObservers(STATUS_BELOW_THRESHOLD);
                }
                //Log.d(TAG, "prematurely canceled due to low volume");
            }
            //chord heard
            else {
                if (!upsideThreshold) {
                    upsideThreshold = true;
                    setChanged();
                    notifyObservers(STATUS_UPSIDE_THRESHOLD);
                }

                //update progress
                if (millisUntilFinished <= CHORD_LISTEN_DURATION) {
                    Chord playedChord = audio.getChord();
                    //Log.d(TAG, "played:" + playedChord.toString());

                    //if (targetChord.toString().equals(playedChord.toString())) {
                    correctlyPlayedAccumulator += TIMER_TICK;
                    //Log.d(TAG, "correctly played acc -> " + correctlyPlayedAccumulator);
                    //} else {
                    //    correctlyPlayedAccumulator = 0;
                    //    //Log.d(TAG, "not correctly played acc");
                    //}
                    setChanged();
                    notifyObservers(STATUS_PROGRESS_UPDATE);
                }

                //stop the count down timer
                if (correctlyPlayedAccumulator >= CORRECTLY_PLAYED_DURATION) {
                    //Log.d(TAG, "- - - - - chord detected - - - - -");
                    this.cancel();
                }
            }
        }

        public void onFinish() {
            //Log.d(TAG, "finished without hearing enough of correct chords");
            correctlyPlayedAccumulator = 0;
            setChanged();
            notifyObservers(STATUS_PROGRESS_UPDATE);
            timeoutCounter += 1;
            if (!timeoutNotified && timeoutCounter >= TIMEOUT_THRESHOLD) {
                setChanged();
                notifyObservers(STATUS_TIMEOUT);
                timeoutNotified = true;
            }
            chordTimer.start();
        }
    };
}