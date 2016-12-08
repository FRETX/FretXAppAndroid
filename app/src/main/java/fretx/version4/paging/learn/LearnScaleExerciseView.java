package fretx.version4.paging.learn;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;

import fretx.version4.BluetoothClass;
import fretx.version4.FretboardPosition;
import fretx.version4.R;
import fretx.version4.Util;
import fretx.version4.activities.MainActivity;
import rocks.fretx.audioprocessing.MusicUtils;

/**
 * Created by onurb_000 on 03/12/16.
 */

public class LearnScaleExerciseView extends View {
	private Drawable fretboardImage;
	private ArrayList<FretboardPosition> notePositions;
	private rocks.fretx.audioprocessing.FretboardPosition tmpFp;
	private float x,y;
	//The image is 345x311
	private final float xOffset = 26f / 345f;
	private final float yOffset = 32f / 311f;
	private final float xStep = 60f / 345f;
	private final float yStep = 58f / 311f;

	private int[] notes;
	private int notesIndex = 0;
	private int currentNote = 0;

	private final double correctNoteThreshold = 0.25; //in semitones
	private Rect imageBounds = new Rect();
	private int[] strings = new int[6];

	private int color = getResources().getColor(R.color.primaryDark);
	private final Paint paint = new Paint();

	private MainActivity mActivity;

	public LearnScaleExerciseView(Context context) {
		super(context);
		setWillNotDraw(false);
		fretboardImage = context.getResources().getDrawable(R.drawable.fretboard);
		invalidate();

	}

	public LearnScaleExerciseView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setWillNotDraw(false);
		fretboardImage = context.getResources().getDrawable(R.drawable.fretboard);
		invalidate();
	}

	public LearnScaleExerciseView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setWillNotDraw(false);
		fretboardImage = context.getResources().getDrawable(R.drawable.fretboard);
		invalidate();
	}

	public void setmActivity(MainActivity ma){
		mActivity = ma;
	}

	public void setNotes(int[] nts){
		notes = nts;
		notesIndex = -1;
		advanceNote();

		invalidate();

		byte[] bluetoothArray = new byte[notes.length];
		for (int i = 0; i < notes.length; i++) {
			bluetoothArray[i] = MusicUtils.midiNoteToFretboardPosition(currentNote).getByteCode();
		}
//		bluetoothArray[0] = MusicUtils.midiNoteToFretboardPosition(currentNote).getByteCode();
		ConnectThread connectThread = new ConnectThread(bluetoothArray);
		connectThread.run();

		//TODO: flash whole scale
	}

	private void advanceNote() {
		notesIndex++;
		if(notesIndex == notes.length) notesIndex = 0;
		currentNote = notes[notesIndex];
		byte[] bluetoothArray = new byte[1];
		bluetoothArray[0] = MusicUtils.midiNoteToFretboardPosition(currentNote).getByteCode();
		ConnectThread connectThread = new ConnectThread(bluetoothArray);
		connectThread.run();
		//TODO: Send BT code here
	}


//	public void setNotePositions(ArrayList<FretboardPosition> np){
//		notePositions = np;
//		invalidate();
//	}

	protected void onDraw(Canvas canvas){

		if(mActivity==null) return;
		if(mActivity.audio==null) return;
		double pitch = mActivity.audio.getPitch();
		int currentNote = notes[notesIndex];

		if(pitch > -1){
			double pitchMidi = MusicUtils.hzToMidiNote(pitch);

			if (Math.abs(currentNote - pitchMidi) < correctNoteThreshold) {
				advanceNote();
			}
		}

		canvas.getClipBounds(imageBounds);
		fretboardImage.setBounds(imageBounds);
		fretboardImage.draw(canvas);

		for (int i = 0; i < notes.length; i++) {
			tmpFp = MusicUtils.midiNoteToFretboardPosition(notes[i]);
			x =  (float) imageBounds.width() * (xOffset + (6 - tmpFp.getString()) * xStep);
			y =  (float) imageBounds.height() * (yOffset + yStep * (tmpFp.getFret() - 0.5f));

			paint.setStyle(Paint.Style.FILL);
			paint.setColor(color);
			if(tmpFp.getFret() == 0){
				y += (yOffset*0.25)* (float) imageBounds.height();
				paint.setColor(getResources().getColor(R.color.blueLed));
			}
			canvas.drawCircle(x, y, imageBounds.width() * 0.03f, paint);
		}
		invalidate();
	}


	/////////////////////////////////BlueToothConnection/////////////////////////
	static private class ConnectThread extends Thread {
		byte[] array;

		public ConnectThread(byte[] tmp) {
			array = tmp;
		}

		public void run() {
			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				Util.startViaData(array);
			} catch (Exception connectException) {
				Log.i(BluetoothClass.tag, "connect failed");
				// Unable to connect; close the socket and get out
				try {
					BluetoothClass.mmSocket.close();
				} catch (IOException closeException) {
					Log.e(BluetoothClass.tag, "mmSocket.close");
				}
				return;
			}
			// Do work to manage the connection (in a separate thread)
			if (BluetoothClass.mHandler == null)
				Log.v("debug", "mHandler is null @ obtain message");
			else
				Log.v("debug", "mHandler is not null @ obtain message");
		}
	}



}
