package fretx.version4.paging.learn;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.TextView;

import fretx.version4.BluetoothClass;
import fretx.version4.R;
import rocks.fretx.audioprocessing.MusicUtils;

/**
 * Created by Kickdrum on 04-Jan-17.
 */

public class LearnGuidedChordExerciseView extends LearnChordExerciseView {

	LearnGuidedChordExerciseFragment fragment;
	int completedChords = -1;
	Handler timerHandler = new Handler();
	Runnable timerRunnable;
	long startTime = 0;
	long elapsedTime = 0;


	public LearnGuidedChordExerciseView(Context context) {
		super(context);
	}

	public LearnGuidedChordExerciseView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LearnGuidedChordExerciseView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setFragment(LearnGuidedChordExerciseFragment fragment){
		this.fragment = fragment;
	}
	public void startTimer(){
		final TextView timerTextView = (TextView) rootView.findViewById(R.id.guidedChordExerciseTimeElapsedText);
		timerRunnable = new Runnable() {
			@Override
			public void run() {
				long millis = System.currentTimeMillis() - startTime;
				elapsedTime = millis;
				int seconds = (int) (millis / 1000);
				int minutes = seconds / 60;
				seconds = seconds % 60;
				timerTextView.setText(String.format("%d:%02d", minutes, seconds));
				timerHandler.postDelayed(this, 500);
			}
		};
		startTime = System.currentTimeMillis();
		timerHandler.postDelayed(timerRunnable, 0);
	}

	@Override
	protected void advanceChord() {
		chordsIndex++;
		completedChords++;


		TextView textChord = (TextView) rootView.findViewById(R.id.textChord);
		textChord.setText(chords.get(chordsIndex).toString());
		fretBoardView.setFretboardPositions(chords.get(chordsIndex).getFingerPositions());

		TextView completedText = (TextView) rootView.findViewById(R.id.guidedChordExerciseCompletedText);
		completedText.setText(Integer.toString(completedChords) + "/" + Integer.toString(chords.size()));

		if (chordsIndex == chords.size()) {
			finishExercise();
			return;
		}

		byte[] bluetoothArray = MusicUtils.getBluetoothArrayFromChord(chords.get(chordsIndex).toString(), chordDb);
		BluetoothClass.sendToFretX(bluetoothArray);
	}

	protected void finishExercise(){
		enableDrawing = false;
		fragment.finishExercise(elapsedTime);
	}


}
