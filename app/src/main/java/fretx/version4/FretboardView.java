package fretx.version4;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;

import rocks.fretx.audioprocessing.FretboardPosition;

/**
 * Created by onurb_000 on 03/12/16.
 */

public class FretboardView extends View {

	private ArrayList<FretboardPosition> fretboardPositions;

	private float width, height, nStrings, nFrets, xPadding, yPadding, yPaddingTop, yPaddingBottom, stringStep, fretStep;
	private float left, top, bottom;

	private Rect imageBounds = new Rect();
	private int baseFret;

    //painter
	private final int color;
	private final Paint paint;

    //drawables
	private final Drawable fretboardImage;
    private final Drawable redLed;
    private final Drawable blueLed;

	public FretboardView(Context context, AttributeSet attrs){
		super(context,attrs);

		paint = new Paint();

		final TypedValue typedValue = new TypedValue();
		context.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
		color = typedValue.data;

		nStrings = 6;
		nFrets = 4;
		nFrets++; //increment to include the nut, and the bottom-most line and keep the "fret" semantics understandable
		xPadding = 0.045f;
		yPadding = 0.15f;
		yPaddingTop = 0.137f;
		yPaddingBottom = 0.1159f;

		fretboardImage = getContext().getResources().getDrawable(R.drawable.fretboard, null);
		redLed = getContext().getResources().getDrawable(R.drawable.fretboard_red_led, null);
		blueLed = getContext().getResources().getDrawable(R.drawable.fretboard_blue_led, null);
	}

	public void setFretboardPositions(ArrayList<FretboardPosition> fp){
		fretboardPositions = fp;
		invalidate();
	}

	protected void onDraw(Canvas canvas){

		drawFretboard(canvas);

		if(fretboardPositions != null){
			//Draw the finger positions
			for (int i = 0; i < fretboardPositions.size(); i++) {
				FretboardPosition fp = fretboardPositions.get(i);
				int fret = fp.getFret();
				if(fret == -1) continue; //-1 means "don't play this string"
				int string = fp.getString();
				if(baseFret > 0) { fret -= baseFret; }

				float yString = (1f - (yPaddingTop + (((int) nStrings - string) * stringStep))) * height;
				float xFret = (xPadding + ((fret - 0.5f) * fretStep)) * width;

				Drawable currentLed = redLed;
				if(fret == 0){
					xFret = (xPadding + ((fret - 0.1f) * fretStep)) * width;
					currentLed = blueLed;
				}
				currentLed.setBounds((int)(xFret-width*0.04),(int)(yString-width*0.04),(int)((width*0.08)+xFret),(int)((width*0.08)+yString));
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
				canvas.drawLine(left, top, left, bottom, paint);
			}
		}
	}

	private void drawFretboard(Canvas canvas){
		canvas.getClipBounds(imageBounds);
		fretboardImage.setBounds(imageBounds);
		fretboardImage.draw(canvas);
		//Draw the fretboard
		width = imageBounds.width();
		height = imageBounds.height();

		stringStep = (1 - ((yPaddingTop + yPaddingBottom))) / (nStrings - 1);


		fretStep = (1 - (2 * xPadding)) / (nFrets - 1);
		left = xPadding * width;
		top = yPaddingTop * height;
		bottom = (1 - yPadding) * height;
	}
}