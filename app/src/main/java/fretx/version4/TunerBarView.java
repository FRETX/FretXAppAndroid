package fretx.version4;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import rocks.fretx.audioprocessing.MusicUtils;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 23/06/17 10:11.
 */

public class TunerBarView extends View {
    private static final String TAG = "KJKP6_TBV";
    private static final double TUNING_THRESHOLD_CENTS = 5;
    private static final double ACCELERATION = 5;

    private final Paint barPainter = new Paint();
    private final Paint backgroundPainter = new Paint();

    private int width = 1000;
    private int height = 200;
    private int center = width / 2;

    private double centerPitchCts;
    private double centerPitchInHz;
    private double leftMostPitchHz;
    private double rightMostPitchHz;

    private double ratioHzPixel;
    private double currentPos;
    private long prevTime = -1;
    private double currentPitchInCents = -1;
    private double currentPitchInHz = -1;

    public TunerBarView(Context context, AttributeSet attrs){
        super(context, attrs);
        backgroundPainter.setColor(Color.DKGRAY);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(TAG, "onSizeChanged: " + w + ", " + h);
        width = w;
        height = h;
        center = Math.round( (float) width / 2f );
        currentPos = center;
        ratioHzPixel = width / (rightMostPitchHz - leftMostPitchHz);

    }

    public void setTargetPitch(double leftMostCts, double centerCts, double rightMostCts) {
        if (leftMostCts >= rightMostCts || centerCts <= leftMostCts || center >= rightMostCts) {
            Log.d(TAG, "setPitchs failed");
        } else {
            leftMostPitchHz = MusicUtils.centToHz(leftMostCts);
            rightMostPitchHz = MusicUtils.centToHz(rightMostCts);
            centerPitchInHz = MusicUtils.centToHz(centerCts);
            centerPitchCts = centerCts;
            Log.d(TAG, "==== SET TUNER BAR TARGET PITCH ====");
            Log.d(TAG, "left: " + leftMostPitchHz);
            Log.d(TAG, "center: " + centerPitchInHz);
            Log.d(TAG, "right: " + rightMostPitchHz);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0,0,width,height,backgroundPainter);
        barPainter.setColor(Color.WHITE);
        barPainter.setStrokeWidth(3);
        canvas.drawLine(width / 2, 0, width / 2 + 1, height, barPainter);
        drawPitchBar(canvas);
    }

    private void drawPitchBar(Canvas canvas) {
        final long time = System.currentTimeMillis();
        final long deltaTime = time - prevTime;
        prevTime = time;

        final double targetPos;
        if (currentPitchInCents < 0) {
            targetPos = center;
            if (currentPos > center)
                barPainter.setColor(Color.RED);
            else
                barPainter.setColor(Color.parseColor("#FF6600"));
        } else if (currentPitchInHz <= leftMostPitchHz) {
            barPainter.setColor(Color.parseColor("#FF6600"));
            targetPos = 0;
        } else if (currentPitchInHz >= rightMostPitchHz) {
            barPainter.setColor(Color.RED);
            targetPos = width;
        } else {
            double difference = centerPitchCts - currentPitchInCents;
            if (Math.abs(difference) < TUNING_THRESHOLD_CENTS) {
                barPainter.setColor(Color.GREEN);
            } else if (centerPitchCts < centerPitchCts){
                barPainter.setColor(Color.parseColor("#FF6600"));
            } else {
                barPainter.setColor(Color.RED);
            }
            targetPos = (currentPitchInHz - leftMostPitchHz) * ratioHzPixel;
        }

        final double deltaPos = targetPos - currentPos;
        final double velocity = ACCELERATION * deltaPos;
        currentPos += ((double) deltaTime / 1000) * velocity;

        if (currentPos > width)
            currentPos = width;
        else if (currentPos < 0)
            currentPos = 0;

        if (currentPos > center) {
            canvas.drawRect(center, 0, (float) currentPos, height, barPainter);
        } else {
            canvas.drawRect((float) currentPos, 0, center, height, barPainter);

        }
    }

    public void setPitch(double currentPitchInCents, double currentPitchInHz) {
        if (prevTime < 0)
            prevTime = System.currentTimeMillis();

        this.currentPitchInCents = currentPitchInCents;
        this.currentPitchInHz = currentPitchInHz;

        invalidate();
    }
}