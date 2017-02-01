package fretx.version4;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

import rocks.fretx.audioprocessing.Chord;
import rocks.fretx.audioprocessing.FingerPositions;
import rocks.fretx.audioprocessing.FretboardPosition;

/**
 * Created by onurb_000 on 03/12/16.
 */

public class FretboardView extends View {

	private ArrayList<FretboardPosition> fretboardPositions;

	private float width, height, nStrings, nFrets, xPadding, yPadding, stringStep, fretStep, rx, ry;
	private float xString, yFret, left, top, right, bottom;

	private Rect imageBounds = new Rect();
	private int string,fret,baseFret;
	private FretboardPosition fp = null;

	private int color = getResources().getColor(R.color.primary);
	private final Paint paint = new Paint();

	private Drawable fretboardImage, redLed, blueLed, currentLed;

	public FretboardView(Context context, AttributeSet attrs){
		super(context,attrs);
		initParameters();
	}

	public void setFretboardPositions(ArrayList<FretboardPosition> fp){
		fretboardPositions = fp;
		invalidate();
	}

	private void initParameters(){
		nStrings = 6;
		nFrets = 4;
		nFrets++; //increment to include the nut, and the bottom-most line and keep the "fret" semantics understandable
		xPadding = 0.045f;
		yPadding = 0.145f;
		rx = 0.054f;
		ry = 0.05f;
		fretboardImage = getContext().getResources().getDrawable(R.drawable.fretboard);
		redLed = getContext().getResources().getDrawable(R.drawable.fretboard_red_led);
		blueLed = getContext().getResources().getDrawable(R.drawable.fretboard_blue_led);
	}

	protected void onDraw(Canvas canvas){

		drawFretboard(canvas);

		if(fretboardPositions != null){
			//Draw the finger positions
			for (int i = 0; i < fretboardPositions.size(); i++) {
				fp = fretboardPositions.get(i);
				fret = fp.getFret();
				if(fret == -1) continue; //-1 means "don't play this string"
				string = fp.getString();
				if(baseFret > 0) { fret -= baseFret; }
				xString = (xPadding + (( (int) nStrings - string)*stringStep)) * width;
				yFret = (yPadding + ((fret-0.5f)*fretStep)) * height;

				float yString = (yPadding + (((int) nStrings - string) * stringStep)) * height;
				float xFret = (xPadding + ((fret - 0.5f) * fretStep)) * width;

				paint.setStyle(Paint.Style.FILL);
				paint.setColor(color);
				currentLed = redLed;
				if(fret == 0){
					yFret = (yPadding + ((fret-0.25f)*fretStep)) * height;

					xFret = (xPadding + ((fret - 0.15f) * fretStep)) * width;

					paint.setColor(getResources().getColor(R.color.blueLed));
					currentLed = blueLed;
				}
//				canvas.drawCircle(xString, yFret, width * 0.04f, paint);
//				canvas.drawCircle(xFret, yString, width * 0.04f, paint);
				currentLed.setBounds((int)(xFret-width*0.04),(int)(yString-width*0.05),(int)((width*0.08)+xFret),(int)((width*0.08)+yString));
				currentLed.draw(canvas);


			}

			//Draw the base fret indicator
			if(baseFret > 0) {
				paint.setColor(color);
				paint.setTextSize(xPadding * width * 1.2f);
				paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
				canvas.drawText(Integer.toString(baseFret), xPadding / 10 * width, yPadding * height, paint);
				paint.setStyle(Paint.Style.STROKE);
				paint.setStrokeWidth(yPadding * height / 4);
				paint.setColor(color);
//				canvas.drawLine(left, top, right, top, paint);
				canvas.drawLine(left, top, left, bottom, paint);
			}
		}
		invalidate();
	}

	private void drawFretboard(Canvas canvas){
		canvas.getClipBounds(imageBounds);
		fretboardImage.setBounds(imageBounds);
		fretboardImage.draw(canvas);
		//Draw the fretboard
		width = imageBounds.width();
		height = imageBounds.height();

		stringStep = (1 - (2 * yPadding)) / (nStrings - 1);
		fretStep = (1 - (2 * xPadding)) / (nFrets - 1);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(10);
		paint.setColor(getResources().getColor(R.color.primaryText));
		left = xPadding * width;
		top = yPadding * height;
		right = (1 - xPadding) * width;
		bottom = (1 - yPadding) * height;
//		//draw the outline
//		canvas.drawRoundRect(left, top, right, bottom, rx * width, ry * width, paint);
//		//draw the strings
//		for (int i = 1; i < nStrings - 1; i++) {
//			float yString = (yPadding + (i * stringStep)) * height;
//			canvas.drawLine(left, yString, right, yString, paint);
//		}
//		//draw the frets
//		for (int i = 1; i < nFrets - 1; i++) {
//			float xFret = (xPadding + (i * fretStep)) * width;
//			canvas.drawLine(xFret, top, xFret, bottom, paint);
//		}



//		stringStep = (1 - (2 * xPadding)) / (nStrings - 1);
//		fretStep = (1 - (2 * yPadding)) / (nFrets - 1);
//		paint.setStyle(Paint.Style.STROKE);
//		paint.setStrokeWidth(10);
//		paint.setColor(getResources().getColor(R.color.primaryText));
//		left = xPadding*width;
//		top = yPadding*height;
//		right = (1-xPadding)*width;
//		bottom = (1-yPadding)*height;
//		//draw the outline
//		canvas.drawRoundRect(left,top,right,bottom,rx*width,ry*width,paint);
//		//draw the strings
//		for (int i = 1; i < nStrings-1; i++) {
//			xString = (xPadding + (i*stringStep)) * width;
//			canvas.drawLine(xString,top,xString,bottom,paint);
//		}
//		//draw the frets
//		for (int i = 1; i < nFrets-1; i++) {
//			yFret = (yPadding + (i*fretStep)) * height;
//			canvas.drawLine(left,yFret,right,yFret,paint);
//		}
	}

}



//package fretx.version4;
//
//import android.content.Context;
//import android.graphics.Canvas;
//import android.graphics.Paint;
//import android.graphics.Rect;
//import android.graphics.Typeface;
//import android.util.AttributeSet;
//import android.view.View;
//
//import rocks.fretx.audioprocessing.FingerPositions;
//
///**
// * Created by onurb_000 on 03/12/16.
// */
//
//public class FretboardView extends View {
//	private FingerPositions fingerPositions;
//
//	private float width, height, nStrings, nFrets, xPadding, yPadding, stringStep, fretStep, rx, ry;
//	private float xString, yFret, left, top, right, bottom;
//
//	private Rect imageBounds = new Rect();
//	private int[] strings = new int[6];
//
//	private int color = getResources().getColor(R.color.primary);
//	private final Paint paint = new Paint();
//
//
//	private void initParameters(){
//		nStrings = 6;
//		nFrets = 4;
//		nFrets++; //increment to include the nut, and the bottom-most line and keep the "fret" semantics understandable
//		xPadding = 0.1f;
//		yPadding = 0.1f;
//		rx = 0.054f;
//		ry = 0.05f;
//	}
//
//	public FretboardView(Context context, AttributeSet attrs, int defstyle){
//		super(context,attrs,defstyle);
//		initParameters();
//	}
//
//	public FretboardView(Context context, AttributeSet attrs){
//		super(context,attrs);
//		initParameters();
//	}
//	public FretboardView(Context context){
//		super(context);
//		initParameters();
//	}
//
//	public void setFingerPositions(FingerPositions fp){
//		//TODO: add basefret indicator for chords not based on 0
//		fingerPositions = fp;
//		invalidate();
//	}
//
//	protected void onDraw(Canvas canvas){
//		canvas.getClipBounds(imageBounds);
//
//		width = imageBounds.width();
//		height = imageBounds.height();
//		stringStep = (1 - (2 * xPadding)) / (nStrings - 1);
//		fretStep = (1 - (2 * yPadding)) / (nFrets - 1);
//		paint.setStyle(Paint.Style.STROKE);
//		paint.setStrokeWidth(10);
//		paint.setColor(getResources().getColor(R.color.primaryText));
//		left = xPadding*width;
//		top = yPadding*height;
//		right = (1-xPadding)*width;
//		bottom = (1-yPadding)*height;
//
//		canvas.drawRoundRect(left,top,right,bottom,rx*width,ry*width,paint);
//
//		for (int i = 1; i < nStrings-1; i++) {
//			xString = (xPadding + (i*stringStep)) * width;
//			canvas.drawLine(xString,top,xString,bottom,paint);
//		}
//		for (int i = 1; i < nFrets-1; i++) {
//			yFret = (yPadding + (i*fretStep)) * height;
//			canvas.drawLine(left,yFret,right,yFret,paint);
//		}
//
//			if(fingerPositions != null){
//				strings[0] = fingerPositions.string6;
//				strings[1] = fingerPositions.string5;
//				strings[2] = fingerPositions.string4;
//				strings[3] = fingerPositions.string3;
//				strings[4] = fingerPositions.string2;
//				strings[5] = fingerPositions.string1;
//
//
//				for (int i = 0; i < strings.length; i++) {
//					int fret = strings[i];
//					if(fingerPositions.baseFret > 0) { fret -= fingerPositions.baseFret; }
//					xString = (xPadding + (i*stringStep)) * width;
//					yFret = (yPadding + ((fret-0.5f)*fretStep)) * height;
//
//					paint.setStyle(Paint.Style.FILL);
//					paint.setColor(color);
//
//					if(fret == 0){
//						yFret = (yPadding + ((fret-0.25f)*fretStep)) * height;
//						paint.setColor(getResources().getColor(R.color.blueLed));
//					}
//
//					canvas.drawCircle(xString, yFret, width * 0.03f, paint);
//				}
//				if(fingerPositions.baseFret > 0) {
//					paint.setColor(color);
//					paint.setTextSize(xPadding * width * 1.2f);
//					paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
//					canvas.drawText(Integer.toString(fingerPositions.baseFret), xPadding / 10 * width, yPadding * height, paint);
//					paint.setStyle(Paint.Style.STROKE);
//					paint.setStrokeWidth(yPadding * height / 4);
//					paint.setColor(color);
//					canvas.drawLine(left, top, right, top, paint);
//				}
//			}
//		invalidate();
//		}
//
//
//}
//
