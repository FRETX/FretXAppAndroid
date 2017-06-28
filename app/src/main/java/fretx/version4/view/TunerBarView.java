package fretx.version4.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import fretx.version4.R;
import rocks.fretx.audioprocessing.MusicUtils;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 23/06/17 10:11.
 */

public class TunerBarView extends View {
    private static final String TAG = "KJKP6_TBV";
    private static final double TUNING_THRESHOLD_CENTS = 6;
    private static final double ACCELERATION = 7;

    private final Paint barPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint backgroundPainter = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int width = 1000;
    private int height = 200;
    private int center = width / 2;
    private int barMarginVertical = 5;
    private int greenTickRadius = 90;

    private double centerPitchCts;
    private double centerPitchInHz;
    private double leftMostPitchHz;
    private double rightMostPitchHz;

    private double ratioHzPixel;
    private double currentPos;
    private long prevTime = -1;
    private double currentPitchInCents = -1;
    private double currentPitchInHz = -1;

    private Drawable greenTick = getResources().getDrawable(R.drawable.green_tick);


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
        barMarginVertical = (int)((float)height * 0.07f);
        greenTickRadius = height/2;
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
        drawPitchBar(canvas);
        barPainter.setColor(Color.WHITE);
        barPainter.setStrokeWidth( (float)width*0.01f );
        canvas.drawLine(width / 2, 0, width / 2 + 1, height, barPainter);
    }

    private void drawPitchBar(Canvas canvas) {
        final long time = System.currentTimeMillis();
        final long deltaTime = time - prevTime;
        prevTime = time;

        double targetPos;

        if(currentPitchInHz == -1){
            targetPos = center;
            if(currentPos < center){
                barPainter.setColor(Color.parseColor("#FF6600"));
            } else {
                barPainter.setColor(Color.RED);
            }
        } else {
            targetPos = (currentPitchInHz-centerPitchInHz)*ratioHzPixel + center;
            if(Math.abs(currentPitchInCents-centerPitchCts) < TUNING_THRESHOLD_CENTS){
                barPainter.setColor(Color.GREEN);
            } else                 if(targetPos < center){
                barPainter.setColor(Color.parseColor("#FF6600"));
            } else if (targetPos > center ){
                barPainter.setColor(Color.RED);
            }





        }



//        if (currentPitchInCents < 0) {
//            targetPos = center;
//            if (currentPos > center)
//                barPainter.setColor(Color.RED);
//            else
//                barPainter.setColor(Color.parseColor("#FF6600"));
//        } else if (currentPitchInHz <= leftMostPitchHz) {
//            barPainter.setColor(Color.parseColor("#FF6600"));
//            targetPos = 0;
//        } else if (currentPitchInHz >= rightMostPitchHz) {
//            barPainter.setColor(Color.RED);
//            targetPos = width;
//        } else {
//            double difference = centerPitchCts - currentPitchInCents;
//            if (Math.abs(difference) < TUNING_THRESHOLD_CENTS) {
//                barPainter.setColor(Color.GREEN);
//            } else if (centerPitchCts < centerPitchCts){
//                barPainter.setColor(Color.parseColor("#FF6600"));
//            } else {
//                barPainter.setColor(Color.RED);
//            }
//            targetPos = (currentPitchInHz - leftMostPitchHz) * ratioHzPixel;
//        }

        if(targetPos > width) targetPos = width;
        if(targetPos < 0) targetPos = 0;

        final double deltaPos = targetPos - currentPos;
        final double velocity = ACCELERATION * deltaPos;
        currentPos += ((double) deltaTime / 1000) * velocity;

        if (currentPos > width)
            currentPos = width;
        else if (currentPos < 0)
            currentPos = 0;

        if (currentPos > center) {
            canvas.drawRect(center, barMarginVertical, (float) currentPos, height-barMarginVertical, barPainter);
        } else {
            canvas.drawRect((float) currentPos, barMarginVertical, center, height-barMarginVertical, barPainter);
        }

        if(currentPitchInHz != -1 && Math.abs(currentPitchInCents-centerPitchCts) < TUNING_THRESHOLD_CENTS){
            greenTick.setBounds(center-greenTickRadius, height/2-greenTickRadius, center+greenTickRadius, height/2+greenTickRadius);
            greenTick.draw(canvas);
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
