package fretx.version4;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

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

    private double leftMostPitchCts;
    private double rightMostPitchCts;
    private double centerPitchCts;

    private double ratioCtsPixel;
    private double currentPos = center;
    private long prevTime = -1;
    private double currentPitchInCents;

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
        ratioCtsPixel = width / (rightMostPitchCts - leftMostPitchCts);
    }

    public void setTargetPitch(double left, double right, double center) {
        if (left >= right || center <= left || center >= right) {
            Log.d(TAG, "setPitchs failed");
        } else {
            leftMostPitchCts = left;
            rightMostPitchCts = right;
            centerPitchCts = center;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0,0,width,height,backgroundPainter);
        barPainter.setColor(Color.WHITE);
        canvas.drawLine(width / 2, 0, width / 2 + 1, height, barPainter);

        drawPitchBar(canvas);
    }

    private void drawPitchBar(Canvas canvas) {

        final long deltaTime = System.currentTimeMillis() - prevTime;

        final double targetPos;
        if (currentPitchInCents < leftMostPitchCts) {
            Log.v(TAG, "left most");
            barPainter.setColor(Color.YELLOW);
            targetPos = 0;
        } else if (currentPitchInCents > rightMostPitchCts) {
            Log.v(TAG, "right most");
            barPainter.setColor(Color.RED);
            targetPos = width;
        } else {
            double difference = centerPitchCts - currentPitchInCents;
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
            canvas.drawRect(center, 0, (float) currentPos, height, barPainter);
        } else {
            canvas.drawRect((float) currentPos, 0, center, height, barPainter);
        }
    }



    public void setPitch(double currentPitchInCents) {
        if (prevTime == -1)
            prevTime = System.currentTimeMillis();

        this.currentPitchInCents = currentPitchInCents;
        invalidate();
    }
}
