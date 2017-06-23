package fretx.version4;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import fretx.version4.utils.audio.Audio;
import rocks.fretx.audioprocessing.MusicUtils;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 23/06/17 10:11.
 */

public class TunerBarView extends View {

    private static final String TAG = "KJKP6_TBV";
    private static final int HALF_PITCH_RANGE_CTS = 100;
    private static final int BAR_WIDTH = 10;
    private static final double TUNING_THRESHOLD_CENTS = 5;
    private static final int BAR_MARGIN = 5;

    private final Paint barPainter = new Paint();
    private final Paint backgroundPainter = new Paint();
    private int tuningIndex = -1;
    private double centerPitchsCts[] = new double[6];
    private double leftMostPitchCts;
    private double rightMostPitchCts;
    private double ratioCtsPixel;

    private int width = 1000;
    private int height = 200;
    private int center = width / 2;
    private int verticalCenter = height / 2;

    private Drawable greenTick = getResources().getDrawable(R.drawable.green_tick);
    private int greenTickRadius = Math.round( ((float) height * 0.95f) / 2f );

    public TunerBarView(Context context, AttributeSet attrs){
        super(context, attrs);
        backgroundPainter.setColor(Color.DKGRAY);

        final int tuningMidiNote[] = MusicUtils.getTuningMidiNotes(MusicUtils.TuningName.STANDARD);
        for (int index = 0; index < tuningMidiNote.length; ++index) {
            final double hz = MusicUtils.midiNoteToHz(tuningMidiNote[index]);
            centerPitchsCts[index] = MusicUtils.hzToCent(hz);
        }
        setTuningIndex(0);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(TAG, "onSizeChanged: " + w + ", " + h);
        width = w;
        height = h;
        center = Math.round( (float) width / 2f );
        verticalCenter = Math.round( (float) height / 2f );
        ratioCtsPixel = width / HALF_PITCH_RANGE_CTS;
    }

    public void setTuningIndex(int index) {
        tuningIndex = index;
        Log.v(TAG, "target pitch cts: " + centerPitchsCts[tuningIndex]);
        leftMostPitchCts = centerPitchsCts[index] - HALF_PITCH_RANGE_CTS;
        rightMostPitchCts = centerPitchsCts[index] + HALF_PITCH_RANGE_CTS;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0,0,width,height,backgroundPainter);
        barPainter.setColor(Color.WHITE);
        barPainter.setStrokeWidth(3);
        canvas.drawLine(width / 2, 0, width / 2 + 1, height, barPainter);

        drawPitchBar(canvas);
        invalidate();
    }

    private void drawPitchBar(Canvas canvas) {
        double currentPitch = Audio.getInstance().getPitch();
        if (currentPitch != -1) {
//            final double currentPitchInCents = 7600;//MusicUtils.hzToCent(currentPitch);
            final double currentPitchInCents = MusicUtils.hzToCent(currentPitch);
            Log.v(TAG, "current pitch: " + currentPitch);
            if (currentPitchInCents < leftMostPitchCts) {
                Log.v(TAG, "left most");
                barPainter.setColor(Color.RED);
                canvas.drawRect(0, BAR_MARGIN, center, height-BAR_MARGIN, barPainter);
            } else if (currentPitchInCents > rightMostPitchCts) {
                Log.v(TAG, "right most");
                barPainter.setColor(Color.RED);
                canvas.drawRect(center, BAR_MARGIN, width, height-BAR_MARGIN, barPainter);
            } else {
                double difference = centerPitchsCts[tuningIndex] - currentPitchInCents;
                if (Math.abs(difference) < TUNING_THRESHOLD_CENTS) {
                    barPainter.setColor(Color.GREEN);
                    //Draw the green tick over center
                    greenTick.setBounds(center-greenTickRadius, verticalCenter-greenTickRadius, center+greenTickRadius, verticalCenter+greenTickRadius);
                    greenTick.draw(canvas);

                } else {
                    barPainter.setColor(Color.WHITE);
                }
                final double pos = center - (difference / HALF_PITCH_RANGE_CTS * center); //Think "HALF_PITCH_RANGE_PIXELS" instead of center
                if (pos > center )
                    canvas.drawRect(center, BAR_MARGIN, (float) pos, height-BAR_MARGIN, barPainter);
                else
                    canvas.drawRect((float) pos, BAR_MARGIN, center, height-BAR_MARGIN, barPainter);
            }
        } else {
            Log.v(TAG, "get pitch failed");
        }
    }
}
