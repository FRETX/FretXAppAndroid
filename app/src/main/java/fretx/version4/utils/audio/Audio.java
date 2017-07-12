package fretx.version4.utils.audio;

import android.os.CountDownTimer;
import android.util.Log;

import java.util.ArrayList;

import rocks.fretx.audioprocessing.AudioProcessing;
import rocks.fretx.audioprocessing.Chord;

/**
 * FretXapp for FretX
 * Created by pandor on 14/04/17 14:20.
 */

public class Audio {
    private final String TAG = "KJKP6_AUDIO_UTIL";

    //audio settings
    static private final int FS = 16000;
    static private final double BUFFER_SIZE_S = 0.1;
    static private final double BUFFER_SIZE_TUNER_S = 0.05;

    public enum modeOptimization {
        TUNER,
        CHORD
    }

    private modeOptimization mode = modeOptimization.CHORD;

    static private final long TIMER_TICK = 10;
    static private final long ONSET_IGNORE_DURATION_MS = 0;
    static private final long CHORD_LISTEN_DURATION_MS = 500;
    static private final long TIMER_DURATION_MS = ONSET_IGNORE_DURATION_MS + CHORD_LISTEN_DURATION_MS;
    static private final long CORRECTLY_PLAYED_DURATION_MS = 160;
    static private final double VOLUME_THRESHOLD = -9;
    static private final int TIMEOUT_MS = 10000;

    //audio
    private boolean enabled;
    private AudioProcessing audio;
    private Chord targetChord;
    private double correctlyPlayedAccumulator;
    private boolean upsideThreshold;
    private int timeoutCounter;
    private boolean timeoutNotified;

    //listener
    private AudioListener listener;

    /* = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = */
    private static class Holder {
        private static final Audio instance = new Audio();
    }

    private Audio() {
    }

    public static Audio getInstance() {
        return Holder.instance;
    }

    /* = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = */

    public void init() {
        Log.d(TAG, "init");
        audio = new AudioProcessing();
        enabled = true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public modeOptimization getMode(){
        return mode;
    }

    public void setMode(modeOptimization newMode) {
        if (!enabled)
            return;
        mode = newMode;
        audio.stop();
        start();
    }

    public void start() {
        if (!enabled)
            return;
        Log.d(TAG, "start");
        if (!audio.isInitialized())
            try {
                if(mode == modeOptimization.CHORD){
                    audio.initialize(FS, BUFFER_SIZE_S);
                } else if(mode == modeOptimization.TUNER){
                    audio.initialize(FS, BUFFER_SIZE_TUNER_S);
                }
            } catch (IllegalArgumentException e) {
                enabled = false;
                return;
            }
        if (!audio.isProcessing())
            audio.start();
        timeoutCounter = 0;
        timeoutNotified = false;
    }

    public void stop() {
        if (!enabled)
            return;
        Log.d(TAG, "stop");
        if (audio.isProcessing() ) {
            audio.stop();
        }
    }



    public void setTargetChord(Chord chord) {
        targetChord = chord;
    }

    public void setTargetChords(ArrayList<Chord> chords) {
        if (!enabled)
            return;
        audio.setTargetChords(chords);
        Log.d(TAG,audio.getTargetChords().toString());
    }

    public void startListening() {
        if (!enabled)
            return;
        Log.d(TAG, "start listening");
        correctlyPlayedAccumulator = 0;
        timeoutCounter = 0;
        chordTimer.cancel();
        chordTimer.start();
    }

    public void stopListening() {
        Log.d(TAG, "stop listening");
        chordTimer.cancel();
    }

    public double getProgress() {
        return correctlyPlayedAccumulator / CORRECTLY_PLAYED_DURATION_MS * 100;
    }

    public float getPitch() {
        return enabled ? audio.getPitch() : -1;
    }

    //// TODO: 06/05/17 replace with a handler to avoid restart of countdown timer
    private CountDownTimer chordTimer = new CountDownTimer(TIMER_DURATION_MS, TIMER_TICK) {
        public void onTick(long millisUntilFinished) {
            //// TODO: 06/05/17 remove this 2 or 3 checks - should not happen
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
                correctlyPlayedAccumulator = 0;
                listener.onProgress();
                if (upsideThreshold) {
                    upsideThreshold = false;
                    Log.d(TAG, "LOW");
                    listener.onLowVolume();
                }
                //Log.d(TAG, "prematurely canceled due to low volume");
            }
            //chord heard
            else {
                if (!upsideThreshold) {
                    upsideThreshold = true;
                    Log.d(TAG, "UP");
                    listener.onHighVolume();
                }
                //update progress

                Chord playedChord = audio.getChord();
                if (playedChord == null) {
                    Log.d(TAG, "played chord is null");
                    return;
                }

// Log.d(TAG, "played:" + playedChord.toString());
//                Log.d(TAG, "played:" + Double.toString(audio.getChordSimilarity()));
//                Log.d(TAG, "possible:" + audio.getTargetChords().toString());

                if (targetChord.toString().equals(playedChord.toString())) {
                    // && audio.getChordSimilarity() > 0.5
                    correctlyPlayedAccumulator += TIMER_TICK;
                    Log.d(TAG, "correctly played acc -> " + correctlyPlayedAccumulator);
                } else {
                    correctlyPlayedAccumulator = 0;
                    //Log.d(TAG, "not correctly played acc");
                }

                listener.onProgress();

                //stop the count down timer
                if (correctlyPlayedAccumulator >= CORRECTLY_PLAYED_DURATION_MS) {
                    Log.d(TAG, "chord detected");
                    this.cancel();
                }
            }
        }

        public void onFinish() {
            correctlyPlayedAccumulator = 0;
            listener.onProgress();
            timeoutCounter += CHORD_LISTEN_DURATION_MS;
            if (!timeoutNotified && timeoutCounter >= TIMEOUT_MS) {
                listener.onTimeout();
                timeoutNotified = true;
            }
            chordTimer.start();
        }
    };

    /* = = = = = = = = = = = = = = = = = = = = = = =  = = = = = = = = = = = = = = = = = = = = = = */
    public void setAudioDetectorListener(AudioListener listener) {
        this.listener = listener;
    }

    public interface AudioListener {
        void onProgress();
        void onLowVolume();
        void onHighVolume();
        void onTimeout();
    }
}
