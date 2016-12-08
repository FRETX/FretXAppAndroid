package fretx.version4.paging.learn;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
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
//	private Drawable fretboardImage;
	private ArrayList<FretboardPosition> notePositions;
	private rocks.fretx.audioprocessing.FretboardPosition tmpFp;
	private float x,y;
	//The image is 345x311
	private float width, height, nStrings, nFrets, xPadding, yPadding, stringStep, fretStep, rx, ry;
	private float xString, yFret, left, top, right, bottom;

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
		initParameters();
//		fretboardImage = context.getResources().getDrawable(R.drawable.fretboard);
		invalidate();

	}

	public LearnScaleExerciseView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setWillNotDraw(false);
		initParameters();
//		fretboardImage = context.getResources().getDrawable(R.drawable.fretboard);
		invalidate();
	}

	public LearnScaleExerciseView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setWillNotDraw(false);
		initParameters();
//		fretboardImage = context.getResources().getDrawable(R.drawable.fretboard);
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

	private void initParameters(){
		nStrings = 6;
		nFrets = 4;
		nFrets++; //increment to include the nut, and the bottom-most line and keep the "fret" semantics understandable
		xPadding = 0.1f;
		yPadding = 0.1f;
		rx = 0.054f;
		ry = 0.05f;
	}

	protected void onDraw(Canvas canvas){

//		if(mActivity==null) return;
//		if(mActivity.audio==null) return;
		//for some reason this way of handling null objects doesn't work, reverting back to nested ifs for now, until I figure out proper release of activities and/or make the bluetooth into a non-activity popup, maybe a fragment
		double pitch = -1;
		if(mActivity == null) return;
		if(mActivity.audio == null) return;
		if(!mActivity.audio.isProcessing()) return;
		pitch = mActivity.audio.getPitch();
		int currentNote = notes[notesIndex];

		if(pitch > -1){
			double pitchMidi = MusicUtils.hzToMidiNote(pitch);

			if (Math.abs(currentNote - pitchMidi) < correctNoteThreshold) {
				advanceNote();
			}
		}

		canvas.getClipBounds(imageBounds);
		width = imageBounds.width();
		height = imageBounds.height();
		stringStep = (1 - (2 * xPadding)) / (nStrings - 1);
		fretStep = (1 - (2 * yPadding)) / (nFrets - 1);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(10);
		paint.setColor(getResources().getColor(R.color.primaryText));
		left = xPadding*width;
		top = yPadding*height;
		right = (1-xPadding)*width;
		bottom = (1-yPadding)*height;
		canvas.drawRoundRect(left,top,right,bottom,rx*width,ry*width,paint);
		for (int i = 1; i < nStrings-1; i++) {
			xString = (xPadding + (i*stringStep)) * width;
			canvas.drawLine(xString,top,xString,bottom,paint);
		}
		for (int i = 1; i < nFrets-1; i++) {
			yFret = (yPadding + (i*fretStep)) * height;
			canvas.drawLine(left,yFret,right,yFret,paint);
		}

		for (int i = 0; i < notes.length; i++) {
			tmpFp = MusicUtils.midiNoteToFretboardPosition(notes[i]);
		}

		for (int i = 0; i < notes.length; i++) {
			tmpFp = MusicUtils.midiNoteToFretboardPosition(notes[i]);
			xString = (xPadding + (tmpFp.getString()*stringStep)) * width;
			yFret = (yPadding + ((tmpFp.getFret()-0.5f)*fretStep)) * height;
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(color);
			if(tmpFp.getFret() == 0){
				yFret = (yPadding + ((tmpFp.getFret()-0.25f)*fretStep)) * height;
				paint.setColor(getResources().getColor(R.color.blueLed));
			}
			canvas.drawCircle(xString, yFret, width * 0.03f, paint);
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
