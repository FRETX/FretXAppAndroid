package fretx.version4;

import android.os.CountDownTimer;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import android.os.Handler;

import rocks.fretx.audioprocessing.AudioProcessing;
import rocks.fretx.audioprocessing.Chord;

/**
 * Created by Kickdrum on 21-Feb-17.
 */

public class ChordListener {
	CountDownTimer chordTimer;

	private long correctlyPlayedAccumulator = 0;
	boolean listening = false;
	protected ArrayList<Chord> chords = new ArrayList<Chord>(0);
	private final double VOLUME_THRESHOLD = -9;
	private long timerTick, onsetIgnoreDuration, chordListenDuration, timerDuration, correctlyPlayedDuration;
	private int handlerInterval;

	private Handler mHandler = new Handler(Looper.getMainLooper());
	private AudioProcessing audio;
	private Runnable listener;

	private Observable detectedChord;

	private final int HANDLER_INTERVAL = 25; //in miliseconds
	private final long TIMER_TICK = 20;
	private final long ONSET_IGNORE_DURATION = 0; //in miliseconds
	private final long CHORD_LISTEN_DURATION = 500; //in miliseconds
	private final long TIMER_DURATION = ONSET_IGNORE_DURATION + CHORD_LISTEN_DURATION; //in miliseconds
	private final long CORRECTLY_PLAYED_DURATION = 160; //in milliseconds


	public ChordListener(AudioProcessing audio){

		this.audio = audio;

		timerTick = TIMER_TICK;
		onsetIgnoreDuration = ONSET_IGNORE_DURATION;
		chordListenDuration = CHORD_LISTEN_DURATION;
		timerDuration = TIMER_DURATION;
		correctlyPlayedDuration = CORRECTLY_PLAYED_DURATION;
		handlerInterval = HANDLER_INTERVAL;

		listener = new Runnable() {
			@Override
			public void run() {
				try{
					//TODO: listen to chord changes
				} finally {
					mHandler.postDelayed(listener,handlerInterval);
				}
			}
		};
	}

	public void startListening() {
		listener.run();
	}

	public void stopListening(){
		mHandler.removeCallbacks(listener);
	}

	public void addObserver(Observer o){
		detectedChord.addObserver(o);
	}



}



