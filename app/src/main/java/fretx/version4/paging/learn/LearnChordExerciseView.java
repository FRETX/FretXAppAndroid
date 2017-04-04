package fretx.version4.paging.learn;

		import android.content.Context;
		import android.graphics.Canvas;
		import android.graphics.Paint;
		import android.os.Bundle;
		import android.os.CountDownTimer;
		import android.util.AttributeSet;
		import android.util.Log;
		import android.view.View;
		import android.widget.FrameLayout;
		import android.widget.RelativeLayout;
		import android.widget.TextView;
		import android.widget.Toast;

		import java.io.IOException;
		import java.util.ArrayList;
		import java.util.Arrays;
		import java.util.HashMap;

		import fretx.version4.BluetoothClass;
		import fretx.version4.FretboardView;
		import fretx.version4.R;
		import fretx.version4.Util;
		import fretx.version4.activities.MainActivity;
		import fretx.version4.paging.chords.ChordFragment;
		import rocks.fretx.audioprocessing.AudioAnalyzer;
		import rocks.fretx.audioprocessing.Chord;
		import rocks.fretx.audioprocessing.FingerPositions;
		import rocks.fretx.audioprocessing.MusicUtils;

/**
 * Created by Kickdrum on 29-Oct-16.
 */

public class LearnChordExerciseView extends RelativeLayout {

	protected fretx.version4.activities.MainActivity mActivity;
	protected FrameLayout rootView;
	protected FretboardView fretBoardView;

	protected boolean enableDrawing = true;

	private final double VOLUME_THRESHOLD = -9;
	private int width, height;

	protected ArrayList<Chord> chords = new ArrayList<Chord>(0);
	protected HashMap<String,FingerPositions> chordDb;

	boolean listening = false;
//	private boolean oneMistakeTolerance = true;
	CountDownTimer chordTimer;

	protected int chordsIndex = 0;

	private boolean backgroundMicWarningShown = false;

	private final long TIMER_TICK = 20;
	private final long ONSET_IGNORE_DURATION = 0; //in miliseconds
	private final long CHORD_LISTEN_DURATION = 500; //in miliseconds
	private final long TIMER_DURATION = ONSET_IGNORE_DURATION + CHORD_LISTEN_DURATION; //in miliseconds
	private final long CORRECTLY_PLAYED_DURATION = 160; //in milliseconds
	private long correctlyPlayedAccumulator = 0;

	private void startListening() {
		Log.d("startListening","starting");
		listening = true;
//		oneMistakeTolerance = true;
		chordTimer = new CountDownTimer(TIMER_DURATION, TIMER_TICK) {
			public void onTick(long millisUntilFinished) {
				if(mActivity == null)return;
				if(mActivity.audio == null) return;
				if(!mActivity.audio.isProcessing() || !mActivity.audio.isInitialized()) return;
				if(!mActivity.audio.isBufferAvailable()){
					Log.d("isBufferAvailable","false");
					if(!backgroundMicWarningShown){
						Toast.makeText(getContext(), getResources().getString(R.string.microphone_used_background), Toast.LENGTH_LONG).show();
						backgroundMicWarningShown = true;
					}
					return;
				}
				if(mActivity.audio.getVolume() < VOLUME_THRESHOLD) {
					this.cancel();
					listening = false;
					correctlyPlayedAccumulator = 0;
					Log.d("timer","prematurely canceled due to low volume");
				}
				if(millisUntilFinished > CHORD_LISTEN_DURATION){
					//ignore the onset
				} else {
					//listen and accumulate the correctly played durations
					//TODO: proper object comparison for Chord
					if(mActivity == null) return;
					if(mActivity.audio == null) return;
					if(!mActivity.audio.isProcessing()) return;
					Chord targetChord = chords.get(chordsIndex);
					Chord playedChord = mActivity.audio.getChord();
					Log.d("playedChord",playedChord.toString());
					if(playedChord != null){
						if (targetChord.toString().equals(playedChord.toString())) {
							correctlyPlayedAccumulator += TIMER_TICK;
							Log.d("correctlyPlayedAcc", Long.toString(correctlyPlayedAccumulator));
						} else{
//							if(oneMistakeTolerance){
//								oneMistakeTolerance = false;
//							} else {
								correctlyPlayedAccumulator = 0;
//							}

						}

					}
				}
				if(correctlyPlayedAccumulator >= CORRECTLY_PLAYED_DURATION){
					//terminate and advance
					this.cancel();
					advanceChord();
					correctlyPlayedAccumulator = 0;
					listening = false;
					Log.d("timer", "stopping timer and advancing chord");
				}
			}
			public void onFinish() {
				listening = false;
				Log.d("timer", "finished without hearing enough of correct chords");
			}
		};
		chordTimer.start();
	}

	protected void advanceChord() {
		chordsIndex++;
		if (chordsIndex == chords.size()) chordsIndex = 0;

		TextView textChord = (TextView) rootView.findViewById(R.id.textChord);
		textChord.setText(chords.get(chordsIndex).toString());
		fretBoardView.setFretboardPositions(chords.get(chordsIndex).getFingerPositions());

		byte[] bluetoothArray = MusicUtils.getBluetoothArrayFromChord(chords.get(chordsIndex).toString(),chordDb);
		BluetoothClass.sendToFretX(bluetoothArray);
	}

	public void setChords(ArrayList<Chord> c) {
		this.chords = c;
		mActivity.audio.setTargetChords(chords);
		chordsIndex = -1;
		advanceChord();

		TextView exerciseChordsText = (TextView) findViewById(R.id.exerciseChordsTextView);
		if(exerciseChordsText==null) return;
		String songChordsString = "";
		for (int i = 0; i < chords.size(); i++) {
			songChordsString = chords.get(i).toString() + " ";
		}
		exerciseChordsText.setText(songChordsString);

	}

	public void resetChords(){
		chordsIndex = 0;
	}


	public LearnChordExerciseView(Context context) {
		super(context);
		setWillNotDraw(false);
		chordDb = MusicUtils.parseChordDb();
		invalidate();

	}

	public LearnChordExerciseView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setWillNotDraw(false);
		chordDb = MusicUtils.parseChordDb();
		invalidate();
	}

	public LearnChordExerciseView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setWillNotDraw(false);
		chordDb = MusicUtils.parseChordDb();
		invalidate();
	}

	public void setmActivity(MainActivity mActivity) {
		this.mActivity = mActivity;
	}

	public void setRootView(FrameLayout rv) {
		this.rootView = rv;
	}

	public void setFretBoardView(FretboardView fv) {this.fretBoardView = fv;}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		width = w;
		height = h;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if(enableDrawing){
			if (mActivity == null) return;
			if (mActivity.audio == null) return;
			if (!mActivity.audio.isProcessing()) return;
			if (!listening) {
				startListening();
			}
			invalidate();
		}
	}

	public Chord getChord() {
		return chords.get(chordsIndex);
	}
}


