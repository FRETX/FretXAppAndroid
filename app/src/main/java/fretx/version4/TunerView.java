package fretx.version4;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import fretx.version4.activities.MainActivity;
import rocks.fretx.audioprocessing.AudioAnalyzer;
import rocks.fretx.audioprocessing.MusicUtils;


public class TunerView extends View {

	private MainActivity mActivity;
	private RelativeLayout rootView;

	private float centerPitch, currentPitch;
	private int width, height;
	private double lastAngle = Double.NaN;
	private int lastColor = getResources().getColor(R.color.black);
	private final Paint paint = new Paint();
	protected double pitchRangeInCents = 200;

	public TunerView(Context context) {
		super(context);
	}

	public TunerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TunerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setmActivity(MainActivity mActivity) {
		this.mActivity = mActivity;
	}

	public void setRootView(RelativeLayout rv) {
		this.rootView = rv;
	}

	public void setCenterPitch(float centerPitch) {
		this.centerPitch = centerPitch;
		invalidate();
	}

	public void setCurrentPitch(float currentPitch) {
		this.currentPitch = currentPitch;
		invalidate();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		width = w;
		height = h;
	}

	@Override
	protected void onDraw(Canvas canvas) {

		float halfWidth = width / 2;
		float needleCenterX = halfWidth;
		float needleCenterY = (float) height * 0.7f;

		paint.setStrokeWidth(10.0f);
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(getResources().getColor(R.color.black));
		canvas.drawCircle(needleCenterX, needleCenterY, width * 0.05f, paint);
//	    canvas.drawLine(halfWidth, 0, halfWidth, height, paint);

		TextView textCurrentNote = (TextView) rootView.findViewById(R.id.textCurrentNote);

		if(mActivity.audio.isInitialized() && mActivity.audio.isProcessing()){
			currentPitch = mActivity.audio.getPitch();
		} else {
			currentPitch = -1;
		}

		double angleOfIndicator = Double.NaN;
		angleOfIndicator = lastAngle;
		if (currentPitch > -1) {
			int[] tuningMidi = MusicUtils.getTuningMidiNotes(MusicUtils.TuningName.STANDARD);
			double[] tuning = new double[tuningMidi.length];
			for (int i = 0; i < tuningMidi.length; i++) {
				tuning[i] = MusicUtils.midiNoteToHz(tuningMidi[i]);
			}

			double[] differences = tuning.clone();
			for (int i = 0; i < differences.length; i++) {
				differences[i] -= currentPitch;
				differences[i] = Math.abs(differences[i]);
			}

			int minIndex = AudioAnalyzer.findMinIndex(differences);
			centerPitch = (float) tuning[minIndex];
			int centerMidiNote = tuningMidi[minIndex];

			//TODO: add "note name without number" method to MusicUtils
			String noteString;
			noteString = MusicUtils.midiNoteToName(centerMidiNote);
			noteString = noteString.substring(0,noteString.length()-1); //remove the number
			textCurrentNote.setText(noteString);

			int prevNoteIndex = centerMidiNote - 1;
			int nextNoteIndex = centerMidiNote + 1;
			if (centerMidiNote == 0) prevNoteIndex = 0;
			TextView textPreviousNote = (TextView) rootView.findViewById(R.id.textPreviousNote);
			TextView textNextNote = (TextView) rootView.findViewById(R.id.textNextNote);

			noteString = MusicUtils.midiNoteToName(prevNoteIndex);
			noteString = noteString.substring(0,noteString.length()-1); //remove the number
			textPreviousNote.setText(noteString);
			noteString = MusicUtils.midiNoteToName(nextNoteIndex);
			noteString = noteString.substring(0,noteString.length()-1); //remove the number
			textNextNote.setText(noteString);

			double currentPitchInCents = MusicUtils.hzToCent(currentPitch);
			double centerPitchInCents = MusicUtils.hzToCent(centerPitch);
			double difference = centerPitchInCents - currentPitchInCents;

			//10 cents is the "just noticeable difference" for a lot of humans
			if (Math.abs(difference) < 10) {
				lastColor = getResources().getColor(R.color.green);
			} else {
				lastColor = getResources().getColor(R.color.black);
			}

			paint.setColor(lastColor);
			paint.setStyle(Paint.Style.FILL);
			canvas.drawCircle(needleCenterX, needleCenterY, width * 0.04f, paint);

			//Draw the line between an interval of one semitone lower and one semitone higher than center pitch
			if (currentPitchInCents > centerPitchInCents + pitchRangeInCents) {
				//Draw a straight line to the right
				angleOfIndicator = 90;
			} else if (currentPitchInCents < centerPitchInCents - pitchRangeInCents) {
				//Draw a straight line to the left
				angleOfIndicator = -90;
			} else {
				angleOfIndicator = (difference / pitchRangeInCents) * 90;
			}


			//arbitrary mapping for better display
			angleOfIndicator = (Math.exp((90 - Math.abs(angleOfIndicator)) / -30) - 0.0498) * 90 / 85.52 * 90 * Math.signum(angleOfIndicator);

			//convert to radians from degrees
			angleOfIndicator = Math.toRadians(angleOfIndicator);
			//reverse direction to match the left-to-right increasing frequency
			angleOfIndicator *= -1;

			angleOfIndicator = Math.toDegrees(angleOfIndicator);
			double maxAngle = 20;
			if (angleOfIndicator < -maxAngle) angleOfIndicator = -maxAngle;
			if (angleOfIndicator > maxAngle) angleOfIndicator = maxAngle;

//		    Log.d("angle", Double.toString(angleOfIndicator));

			angleOfIndicator = Math.toRadians(angleOfIndicator);
			lastAngle = angleOfIndicator;
		}

		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(8.0f);
		paint.setColor(lastColor);
		canvas.drawLine(needleCenterX, needleCenterY,
				halfWidth + (float) Math.sin(angleOfIndicator) * height * 0.9f,
				height - (float) Math.cos(Math.abs(angleOfIndicator)) * height * 0.9f, paint);
		invalidate();
	}
}