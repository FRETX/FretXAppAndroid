package fretx.version4.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;

import fretx.version4.R;
import rocks.fretx.audioprocessing.FretboardPosition;

/**
 * Created by onurb_000 on 03/12/16.
 */

public class FretboardView extends View {
    private ArrayList<FretboardPosition> fretboardPositions;

	private float width, height, nStrings, nFrets, xPadding, yPaddingTop, yPaddingBottom, stringStep, fretStep;

    //drawables
	private final Drawable fretboardImage;
    private final Drawable redLed;
    private final Drawable blueLed;

	private final Rect imageBounds = new Rect();

	public FretboardView(Context context, AttributeSet attrs){
		super(context,attrs);

		nStrings = 6;
		nFrets = 4;
		nFrets++; //increment to include the nut, and the bottom-most line and keep the "fret" semantics understandable
		xPadding = 0.045f;
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
	}
}