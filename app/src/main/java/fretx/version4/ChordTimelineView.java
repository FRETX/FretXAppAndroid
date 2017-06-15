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

    //view
    private int width = 1000;
    private int height = 200;
    private float ratio = 0;

    //blocks
    private ArrayList<SongPunch> punches;
    private int radius = height / 2;

    //vertical bar
    private int leftSpanMs;
    private int rightSpanMs;
    private float verticalBarX = 0;

    //painters
    private final Paint blockPainter = new Paint();
    private final Paint backgroundPainter = new Paint();
    private final Paint barPainter = new Paint();

    //colors
    private static final int COLOR_A = Color.MAGENTA;
    private static final int COLOR_B = Color.RED;
    private static final int COLOR_C = Color.YELLOW;
    private static final int COLOR_D = Color.GREEN;
    private static final int COLOR_E = Color.CYAN;
    private static final int COLOR_F = Color.BLUE;
    private static final int COLOR_G = Color.GRAY;
    private static final int COLOR_BACKGROUND = Color.WHITE;

    //precomputing
    private long currentTimeMs;
    private Bitmap precomputedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    private Canvas precomputedCanvas = new Canvas(precomputedBitmap);
    private int precomputedLengthMs = 0;
    private int precomputedStart;
    private int precomputedStop;


    public ChordTimelineView(Context context, AttributeSet attrs){
        super(context,attrs);
        backgroundPainter.setColor(COLOR_BACKGROUND);
        backgroundPainter.setStyle(Paint.Style.FILL);
        paintBackground();
        barPainter.setColor(Color.BLACK);
    }

    //public methods
    public void setPunches(@NonNull ArrayList<SongPunch> sp){
        punches = sp;
        //Log.v(TAG, "playing punches: " + punches.toString());
        precomputedStart = sp.size() == 0 ? 0 : sp.get(0).timeMs;
        //Log.v(TAG, "precomputed start: " + precomputedStart);
        precomputedStop = sp.size() == 0 ? 0 : sp.get(sp.size() - 1).timeMs;
        //Log.v(TAG, "precomputed stop: " + precomputedStop);
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
        //Log.v(TAG, "update: " + currentTimeMs);
        invalidate();
    }

    //view heritage
    @Override
    protected void onDraw(Canvas canvas) {
        //Log.v(TAG, "draw @ " + currentTimeMs);

        //draw moving blocks
        long deltaT = precomputedStart - (currentTimeMs - leftSpanMs);
        canvas.drawBitmap(precomputedBitmap, deltaT * ratio, 0, blockPainter);

        //draw static vertical bar
        canvas.drawLine(verticalBarX, 0, verticalBarX, height, barPainter);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        //Log.v(TAG, "resized: " + w + " x " + h);
        width = w;
        height = h;
        ratio = (float) width / (leftSpanMs + rightSpanMs);
        radius = height / 2;

        preCompute();
    }

    //drawing utils
    private void paintBackground() {
        precomputedCanvas.drawPaint(backgroundPainter);
    }

    private void setPainter(@NonNull String root) {
        switch (root.charAt(0)) {
            case 'A':
                blockPainter.setColor(COLOR_A);
                break;
            case 'B':
                blockPainter.setColor(COLOR_B);
                break;
            case 'C':
                blockPainter.setColor(COLOR_C);
                break;
            case 'D':
                blockPainter.setColor(COLOR_D);
                break;
            case 'E':
                blockPainter.setColor(COLOR_E);
                break;
            case 'F':
                blockPainter.setColor(COLOR_F);
                break;
            case 'G':
                blockPainter.setColor(COLOR_G);
                break;
            default:
                blockPainter.setColor(COLOR_BACKGROUND);
                break;
        }
    }

    private void preCompute() {
        //Log.v(TAG, "preCompute");

        if (punches.size() == 0)
            return;

        final int length = precomputedStop - precomputedStart;
        if (precomputedLengthMs < length) {
            precomputedLengthMs = length;
            //precomputedBitmap.recycle();
            precomputedBitmap = Bitmap.createBitmap(5 * width, height, Bitmap.Config.ARGB_8888);
            precomputedCanvas.setBitmap(precomputedBitmap);
        }

        paintBackground();

        int x = 0;
        int index;
        for (index = 0; index < punches.size() - 1; ++index) {
            final SongPunch punch = punches.get(index);
            int width = (int)((punches.get(index + 1).timeMs - punch.timeMs) * ratio);

            //draw block
            setPainter(punch.root == null || punch.root.length() == 0 ? "X" : punch.root);
            precomputedCanvas.drawCircle(x + radius, radius, radius, blockPainter);
            precomputedCanvas.drawCircle(x + width - radius, radius, radius, blockPainter);
            precomputedCanvas.drawRect(x + radius, 0, x + width - radius, height, blockPainter);

            //draw text
            blockPainter.setColor(Color.BLACK);
            blockPainter.setTextSize(radius / 2);
            precomputedCanvas.drawText(punch.root + punch.type, x + radius / 2, 2 * height / 3, blockPainter);
            x += width;
        }
    }
}