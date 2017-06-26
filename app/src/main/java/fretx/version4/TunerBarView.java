package fretx.version4;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
    private static final double TUNING_THRESHOLD_CENTS = 5;
    private static final double ACCELERATION = 5;

    private final Paint barPainter = new Paint();
    private final Paint backgroundPainter = new Paint();

    private int width = 1000;
    private int height = 200;
    private int center = width / 2;

    private int tuningIndex;
    private double centerPitchsCts[] = new double[6];
    private double leftMostPitchCts;
    private double rightMostPitchCts;
    private double ratioCtsPixel;
    private double currentPos = center;
    private long prevTime = -1;

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
        canvas.drawLine(width / 2, 0, width / 2 + 1, height, barPainter);

        drawPitchBar(canvas);
        invalidate();
    }

    private void drawPitchBar(Canvas canvas) {
        if (prevTime == -1)
            prevTime = System.currentTimeMillis();
        final long deltaTime = System.currentTimeMillis() - prevTime;

        double currentPitch = Audio.getInstance().getPitch();
        if (currentPitch != -1) {
            //final double currentPitchInCents = 7600;
            final double currentPitchInCents = MusicUtils.hzToCent(currentPitch);
            Log.v(TAG, "current pitch cts: " + currentPitchInCents);

            final double targetPos;
            if (currentPitchInCents < leftMostPitchCts) {
                Log.v(TAG, "left most");
                targetPos = 0;
            } else if (currentPitchInCents > rightMostPitchCts) {
                Log.v(TAG, "right most");
                targetPos = width;
            } else {
                double difference = centerPitchsCts[tuningIndex] - currentPitchInCents;
                if (Math.abs(difference) < TUNING_THRESHOLD_CENTS) {
                    barPainter.setColor(Color.GREEN);
                } else {
                    barPainter.setColor(Color.WHITE);
                }
                targetPos = (currentPitchInCents - leftMostPitchCts) * ratioCtsPixel;
            }

            final double deltaPos = targetPos - currentPos;
            final double velocity = ACCELERATION * deltaPos;
            currentPos += ((double) deltaTime / 1000) * velocity;

            if (currentPos > width / 2) {
                if (currentPos > width) {
                    currentPos = width;
                }
                canvas.drawRect(center, 0, (float) currentPos, height, barPainter);
            } else {
                if (currentPos < 0) {
                    currentPos = 0;
                }
                canvas.drawRect((float) currentPos, 0, center, height, barPainter);
            }

        } else {
            Log.v(TAG, "get picth failed");
        }
    }
}
