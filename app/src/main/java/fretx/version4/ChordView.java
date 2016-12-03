package fretx.version4;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import rocks.fretx.audioprocessing.FingerPositions;

/**
 * Created by onurb_000 on 03/12/16.
 */

public class ChordView extends View {
	private Drawable fretboardImage;
	private FingerPositions fingerPositions;

	//The image is 345x311
	private final float xOffset = 26f / 345f;
	private final float yOffset = 32f / 311f;
	private final float xStep = 60f / 345f;
	private final float yStep = 58f / 311f;

	private Rect imageBounds = new Rect();
	private int[] strings = new int[6];

	private int color = getResources().getColor(R.color.red_dark);
	private final Paint paint = new Paint();

	public ChordView(Context context, AttributeSet attrs){
		super(context,attrs);
		fretboardImage = context.getResources().getDrawable(R.drawable.fretboard);
	}

	public void setFingerPositions(FingerPositions fp){
		//TODO: add basefret indicator for chords not based on 0
		fingerPositions = fp;
		invalidate();
	}

	protected void onDraw(Canvas canvas){
		canvas.getClipBounds(imageBounds);
		fretboardImage.setBounds(imageBounds);
		fretboardImage.draw(canvas);


		strings[0] = fingerPositions.string6;
		strings[1] = fingerPositions.string5;
		strings[2] = fingerPositions.string4;
		strings[3] = fingerPositions.string3;
		strings[4] = fingerPositions.string2;
		strings[5] = fingerPositions.string1;

		for (int i = 0; i < strings.length; i++) {
			int fret = strings[i];
			if(fingerPositions.baseFret > 0) { fret -= fingerPositions.baseFret; }
			float x =  (float) imageBounds.width() * (xOffset + i * xStep);
			float y =  (float) imageBounds.height() * (yOffset + yStep * (fret - 0.5f));
//			Log.d("x", Float.toString(x));
//			Log.d("y", Float.toString(y));

			paint.setStyle(Paint.Style.FILL);
			paint.setColor(color);

			if(fret == 0){
				y += (yOffset*0.25)* (float) imageBounds.height();
				paint.setColor(getResources().getColor(R.color.blue2));
			}
//			paint.setStrokeWidth(1.0f);


			canvas.drawCircle(x, y, imageBounds.width() * 0.03f, paint);

		}

		if(fingerPositions.baseFret > 0){
			paint.setColor(color);
			paint.setTextSize(xOffset*1.2f*imageBounds.width());
			paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
			canvas.drawText( Integer.toString(fingerPositions.baseFret), xOffset/10*imageBounds.width(), yOffset*imageBounds.height(), paint);

			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(yOffset*imageBounds.height()/4);
			paint.setColor(color);
			canvas.drawLine(xOffset*0.8f*imageBounds.width(),yOffset*0.75f*imageBounds.height(),(1-xOffset*0.8f)*imageBounds.width(),yOffset*0.75f*imageBounds.height(),paint);
		}


//		canvas.drawCircle(imageBounds.centerX(), imageBounds.centerY(), imageBounds.width() * 0.05f, paint);
//		Log.d("chordView",imageBounds.toString());
	}
}
