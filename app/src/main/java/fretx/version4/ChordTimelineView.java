package fretx.version4;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import fretx.version4.fretxapi.song.SongPunch;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 13/06/17 15:24.
 */

public class ChordTimelineView extends View {
    private static final String TAG = "KJKP6_TIMELINE_VIEW";

    private static final int COLOR_A = Color.MAGENTA;
    private static final int COLOR_B = Color.RED;
    private static final int COLOR_C = Color.YELLOW;
    private static final int COLOR_D = Color.GREEN;
    private static final int COLOR_E = Color.CYAN;
    private static final int COLOR_F = Color.BLUE;
    private static final int COLOR_G = Color.GRAY;
    private static final int COLOR_BACKGROUND = Color.WHITE;

    private ArrayList<SongPunch> punches;
    private int leftSpanMs;
    private int rightSpanMs;
    private long currentTimeMs;
    private int width = 1000;
    private int height = 200;
    private float ratio = 0;
    private float verticalBarX = 0;
    private Bitmap precomputedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    private Canvas precomputedCanvas = new Canvas(precomputedBitmap);
    private int precomputedStart;
    private final Paint paint = new Paint();
    private final Paint backgroundPainter = new Paint();

    public ChordTimelineView(Context context, AttributeSet attrs){
        super(context,attrs);
        backgroundPainter.setColor(COLOR_BACKGROUND);
        backgroundPainter.setStyle(Paint.Style.FILL);
        paintBackground();
    }

    //public methods
    public void setPunches(@NonNull ArrayList<SongPunch> sp){
        punches = sp;
        Log.v(TAG, "playing punches: " + punches.toString());
        precomputedStart = sp.size() == 0 ? 0 : sp.get(0).timeMs;
        Log.v(TAG, "precomputed start: " + precomputedStart);
        preCompute();
    }

    public void setSpan(int leftSpanMs, int rightSpanMs) {
        this.leftSpanMs = leftSpanMs;
        this.rightSpanMs = rightSpanMs;
        ratio = (float) width / (leftSpanMs + rightSpanMs);
        verticalBarX = width * leftSpanMs / (leftSpanMs + rightSpanMs);
    }

    public void update(long currentTimeMs) {
        this.currentTimeMs = currentTimeMs;
        Log.v(TAG, "update: " + currentTimeMs);
        invalidate();
    }

    //view heritage
    @Override
    protected void onDraw(Canvas canvas) {
        Log.v(TAG, "draw @ " + currentTimeMs);
        Log.v(TAG, "startSpan: " + (currentTimeMs - leftSpanMs));

        //draw moving blocks
        long deltaT = precomputedStart - (currentTimeMs - leftSpanMs);
        canvas.drawBitmap(precomputedBitmap, deltaT * ratio, 0, paint);
        Log.v(TAG, "deltaT: " + deltaT);

        //draw static vertical bar
        paint.setColor(Color.BLACK);
        canvas.drawLine(verticalBarX, 0, verticalBarX, height, paint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        Log.v(TAG, "resized: " + w + " x " + h);
        width = w;
        height = h;
        precomputedBitmap =  Bitmap.createBitmap(width * 5, height, Bitmap.Config.ARGB_8888);
        precomputedCanvas = new Canvas(precomputedBitmap);

        ratio = (float) width / (leftSpanMs + rightSpanMs);
        preCompute();
    }

    //drawing utils
    private void paintBackground() {
        precomputedCanvas.drawPaint(backgroundPainter);
    }

    private void setPainter(String root) {
        switch (root) {
            case "A":
                paint.setColor(COLOR_A);
                break;
            case "B":
                paint.setColor(COLOR_B);
                break;
            case "C":
                paint.setColor(COLOR_C);
                break;
            case "D":
                paint.setColor(COLOR_D);
                break;
            case "E":
                paint.setColor(COLOR_E);
                break;
            case "F":
                paint.setColor(COLOR_F);
                break;
            case "G":
                paint.setColor(COLOR_G);
                break;
            default:
                paint.setColor(COLOR_BACKGROUND);
                break;
        }
    }

    private void preCompute() {
        Log.v(TAG, "preCompute");

        if (punches.size() == 0)
            return;

        paintBackground();

        Log.v(TAG, "ratio: " + ratio);
        int x = 0;
        int index;
        for (index = 0; index < punches.size() - 1; ++index) {
            final SongPunch punch = punches.get(index);
            int width = (int)((punches.get(index + 1).timeMs - punch.timeMs) * ratio);
            Log.d(TAG, "x1: " + x + ", x2: " + (x + width));
            setPainter(punch.root);
            precomputedCanvas.drawRect(x, 0, x + width, height, paint);
            paint.setColor(Color.BLACK);
            paint.setTextSize(20);
            precomputedCanvas.drawText(punch.root + punch.type, x + 5, height / 2, paint);
            x += width;
        }
        final SongPunch punch = punches.get(punches.size() - 1);
        Log.d(TAG, "x1: " + x + ", x2: " + (x + 1000));
        setPainter(punch.root);
        precomputedCanvas.drawRect(x, 0, x + 3000, height, paint);
        paint.setColor(Color.BLACK);
        paint.setTextSize(height / 2);
        precomputedCanvas.drawText(punch.root + punch.type, x + 5, 3 * height / 4, paint);
    }
}