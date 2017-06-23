package fretx.version4.paging.tuner;


/*

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import fretx.version4.R;
import fretx.version4.activities.MainActivity;
import fretx.version4.utils.audio.Audio;
import rocks.fretx.audioprocessing.AudioAnalyzer;
import rocks.fretx.audioprocessing.MusicUtils;

public class TunerView extends View {

	private MainActivity mActivity;
	private RelativeLayout rootView;

	private float centerPitch, currentPitch;
	private int width, height;
	private double lastAngle = Double.NaN;
	private int lastColor = getResources().getColor(R.color.primaryText);
	private final Paint paint = new Paint();
	protected double pitchRangeInCents = 200;
	private boolean initialDraw = true;
	private static double TUNING_THRESHOLD_CENTS = 5;

	//Animation vars
	private long currentTime, prevTime, deltaTime;
	private double acceleration = 5 , needleStopThreshold = 0.01, velocity = 0;
	private double currentAngle, targetAngle, deltaAngle;

	double gotum = -0;

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
		paint.setColor(getResources().getColor(R.color.primaryText));
		canvas.drawCircle(needleCenterX, needleCenterY, width * 0.05f, paint);

		currentTime = System.currentTimeMillis();

		if(initialDraw){
			prevTime = currentTime;
			targetAngle = -90;
			currentAngle = -90;
			initialDraw = false;
		}

		deltaTime = currentTime - prevTime;
		prevTime = currentTime;

		currentPitch = Audio.getInstance().getPitch();

		//Draw the note names and calculate pitch differences
		TextView textCurrentNote = (TextView) rootView.findViewById(R.id.textCurrentNote);
		double currentPitchInCents = 0;
		double centerPitchInCents = 0;
		double difference = Double.POSITIVE_INFINITY;

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

			String noteString;
			noteString = MusicUtils.midiNoteToName(centerMidiNote);
			noteString = noteString.substring(0, noteString.length() - 1); //remove the number
			textCurrentNote.setText(noteString);

			int prevNoteIndex = centerMidiNote - 1;
			int nextNoteIndex = centerMidiNote + 1;
			if (centerMidiNote == 0) prevNoteIndex = 0;
			TextView textPreviousNote = (TextView) rootView.findViewById(R.id.textPreviousNote);
			TextView textNextNote = (TextView) rootView.findViewById(R.id.textNextNote);

			noteString = MusicUtils.midiNoteToName(prevNoteIndex);
			noteString = noteString.substring(0, noteString.length() - 1); //remove the number
			textPreviousNote.setText(noteString);
			noteString = MusicUtils.midiNoteToName(nextNoteIndex);
			noteString = noteString.substring(0, noteString.length() - 1); //remove the number
			textNextNote.setText(noteString);
			currentPitchInCents = MusicUtils.hzToCent(currentPitch);
			centerPitchInCents = MusicUtils.hzToCent(centerPitch);
			difference = centerPitchInCents - currentPitchInCents;

			//Draw the needle base circle
			if (Math.abs(difference) < TUNING_THRESHOLD_CENTS) {
				lastColor = getResources().getColor(R.color.tunerCorrect);
			} else {
				lastColor = getResources().getColor(R.color.primaryText);
			}
			paint.setColor(lastColor);
			paint.setStyle(Paint.Style.FILL);
			canvas.drawCircle(needleCenterX, needleCenterY, width * 0.04f, paint);
		}


		if(currentPitch == -1){
			targetAngle = -90;
			lastColor = getResources().getColor(R.color.primaryText);
		} else {
			targetAngle =  (difference / pitchRangeInCents) * -90;
		}

		deltaAngle = targetAngle - currentAngle;
		velocity = acceleration * deltaAngle;
		currentAngle += ((double)deltaTime/1000) * velocity;

		if(currentAngle < - 90) {
			currentAngle = -90;
			velocity = 0;
		}
		if(currentAngle > 90 ) {
			currentAngle = 90;
			velocity = 0;
		}

		//Normalize the angle to [-pi,pi]
		currentAngle = Math.toRadians(currentAngle);
		currentAngle = Math.atan2(Math.sin(currentAngle), Math.cos(currentAngle));
		currentAngle = Math.toDegrees(currentAngle);



		//draw current angle
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(8.0f);
		paint.setColor(lastColor);
		canvas.drawLine(needleCenterX, needleCenterY,
				needleCenterX + (float) Math.sin(Math.toRadians(currentAngle)) * height * 0.5f,
				needleCenterY - (float) Math.cos(Math.abs(Math.toRadians(currentAngle))) * height * 0.5f,
				paint);
		invalidate();

	}
}
*/