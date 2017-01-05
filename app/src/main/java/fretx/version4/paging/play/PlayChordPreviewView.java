package fretx.version4.paging.play;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import fretx.version4.BluetoothClass;
import fretx.version4.R;
import fretx.version4.paging.learn.LearnChordExerciseView;
import rocks.fretx.audioprocessing.MusicUtils;

/**
 * Created by Kickdrum on 05-Jan-17.
 */

public class PlayChordPreviewView extends LearnChordExerciseView {
	int completedChords = -1;

	public PlayChordPreviewView(Context context) {
		super(context);
	}

	public PlayChordPreviewView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PlayChordPreviewView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}


	@Override
	protected void advanceChord() {
		chordsIndex++;
		completedChords++;
		if (chordsIndex == chords.size()) {
			finishExercise();
			return;
		}

		TextView textChord = (TextView) rootView.findViewById(R.id.textChord);
		textChord.setText(chords.get(chordsIndex).toString());
		fretBoardView.setFretboardPositions(chords.get(chordsIndex).getFingerPositions());

		TextView completedText = (TextView) rootView.findViewById(R.id.guidedChordExerciseCompletedText);
		completedText.setText(Integer.toString(completedChords) + "/" + Integer.toString(chords.size()));

		byte[] bluetoothArray = MusicUtils.getBluetoothArrayFromChord(chords.get(chordsIndex).toString(), chordDb);
		BluetoothClass.sendToFretX(bluetoothArray);
	}

	public void finishExercise(){
		chordsIndex = -1;
		completedChords = -1;
		advanceChord();
	}
}
