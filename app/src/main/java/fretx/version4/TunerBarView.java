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
    private static final int PITCH_RANGE_CTS = 200;
    private static final int BAR_WIDTH = 10;
    private static final double TUNING_THRESHOLD_CENTS = 5;

    private final Paint barPainter = new Paint();
    private final Paint backgroundPainter = new Paint();
    private int tuningIndex = -1;
    private double centerPitchsCts[] = new double[6];
    private double leftMostPitchCts;
    private double rightMostPitchCts;
    private double ratioCtsPixel;

    private int width = 1000;
    private int height = 200;

    public TunerBarView(Context context, AttributeSet attrs){
        super(context, attrs);
        backgroundPainter.setColor(Color.DKGRAY);

        final int tuningMidiNote[] = MusicUtils.getTuningMidiNotes(MusicUtils.TuningName.STANDARD);
        for (int index = 0; index < tuningMidiNote.length; ++index) {
            final double hz = MusicUtils.midiNoteToHz(tuningMidiNote[index]);
            centerPitchsCts[index] = MusicUtils.hzToCent(hz);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(TAG, "onSizeChanged: " + w + ", " + h);
        width = w;
        height = h;
        ratioCtsPixel = width / PITCH_RANGE_CTS;
    }

    public void setTuningIndex(int index) {
        tuningIndex = index;
        Log.v(TAG, "target pitch cts: " + centerPitchsCts[tuningIndex]);
        leftMostPitchCts = centerPitchsCts[index] - PITCH_RANGE_CTS;
        rightMostPitchCts = centerPitchsCts[index] + PITCH_RANGE_CTS;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0,0,width,height,backgroundPainter);
        barPainter.setColor(Color.WHITE);
        canvas.drawLine(width / 2, 0, width / 2 + 1, height, barPainter);

        if (tuningIndex == -1) {
            Log.v(TAG, "tuner not set");
        } else {
            drawPitchBar(canvas);
        }
        invalidate();
    }

    private void drawPitchBar(Canvas canvas) {
        double currentPitch = Audio.getInstance().getPitch();
        if (currentPitch != -1) {
            final double currentPitchInCents = MusicUtils.hzToCent(currentPitch);
            Log.v(TAG, "current pitch cts: " + currentPitchInCents);
            if (currentPitchInCents < leftMostPitchCts) {
                barPainter.setColor(Color.RED);
                canvas.drawLine(0, 0, BAR_WIDTH, height, barPainter);
            } else if (currentPitchInCents > rightMostPitchCts) {
                barPainter.setColor(Color.RED);
                canvas.drawLine(width - BAR_WIDTH, 0, width, height, barPainter);
            } else {
                double difference = centerPitchsCts[tuningIndex] - currentPitchInCents;
                difference = (difference < 0) ? -difference : difference;
                if (difference < TUNING_THRESHOLD_CENTS) {
                    barPainter.setColor(Color.WHITE);
                } else {
                    barPainter.setColor(Color.GREEN);
                }
                final double pos = (currentPitchInCents - leftMostPitchCts) * ratioCtsPixel;
                canvas.drawLine((float) (pos - BAR_WIDTH / 2), 0, (float) (pos - BAR_WIDTH / 2), height, barPainter);
            }
        } else {
            Log.v(TAG, "get picth failed:");
        }
    }
}
