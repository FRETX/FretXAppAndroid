package fretx.version4;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
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
    private int halfHeight = height / 2;
    private float ratio = 0;

    //blocks
    private ArrayList<SongPunch> punches;
    private int radius = height / 2;
    private final static int STROKE_WIDTH = 10;

    //vertical bar
    private int leftSpanMs;
    private int rightSpanMs;
    private float verticalBarX = 0;
    private int verticalBarWidth = 10;

    //painters
    private final Paint blockFillPainter = new Paint();
    private final Paint blockStrokePainter = new Paint();
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
    private static final int COLOR_BACKGROUND = Color.DKGRAY;
    private static final int COLOR_STROKE = Color.WHITE;

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
        blockStrokePainter.setStrokeWidth(STROKE_WIDTH);
        blockStrokePainter.setColor(COLOR_STROKE);
        blockStrokePainter.setStyle(Paint.Style.STROKE);
        blockFillPainter.setStyle(Paint.Style.FILL);
        barPainter.setColor(Color.WHITE);
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

        canvas.drawPaint(backgroundPainter);

        //draw moving blocks
        long deltaT = precomputedStart - (currentTimeMs - leftSpanMs);
        canvas.drawBitmap(precomputedBitmap, deltaT * ratio, 0, blockFillPainter);

        //draw static vertical bar
        canvas.drawRect(verticalBarX, 0, verticalBarX + verticalBarWidth, height, barPainter);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        //Log.v(TAG, "resized: " + w + " x " + h);
        width = w;
        height = h;
        halfHeight = height / 2;
        radius = halfHeight - STROKE_WIDTH;
        ratio = (float) width / (leftSpanMs + rightSpanMs);

        preCompute();
    }

    //drawing utils
    private void paintPrecomputedBackground() {
        precomputedCanvas.drawPaint(backgroundPainter);
    }

    private void setPainter(@NonNull String root) {
        switch (root.charAt(0)) {
            case 'A':
                blockFillPainter.setColor(COLOR_A);
                break;
            case 'B':
                blockFillPainter.setColor(COLOR_B);
                break;
            case 'C':
                blockFillPainter.setColor(COLOR_C);
                break;
            case 'D':
                blockFillPainter.setColor(COLOR_D);
                break;
            case 'E':
                blockFillPainter.setColor(COLOR_E);
                break;
            case 'F':
                blockFillPainter.setColor(COLOR_F);
                break;
            case 'G':
                blockFillPainter.setColor(COLOR_G);
                break;
            default:
                blockFillPainter.setColor(COLOR_BACKGROUND);
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

        paintPrecomputedBackground();

        int x = 0;
        int index;
        for (index = 0; index < punches.size() - 1; ++index) {
            final SongPunch punch = punches.get(index);
            int width = (int)((punches.get(index + 1).timeMs - punch.timeMs) * ratio);

            setPainter(punch.root == null || punch.root.length() == 0 ? "X" : punch.root);
            if (blockFillPainter.getColor() != COLOR_BACKGROUND) {
                //draw stroke
                precomputedCanvas.drawCircle(x + radius + STROKE_WIDTH, halfHeight, radius, blockStrokePainter);
                precomputedCanvas.drawCircle(x + width - radius - STROKE_WIDTH, halfHeight, radius, blockStrokePainter);
                precomputedCanvas.drawRect(x + radius + STROKE_WIDTH, STROKE_WIDTH, x + width - radius - STROKE_WIDTH, height - STROKE_WIDTH, blockStrokePainter);
                blockFillPainter.setStrokeWidth(0);

                //draw the fill
                precomputedCanvas.drawCircle(x + radius + STROKE_WIDTH, halfHeight, radius, blockFillPainter);
                precomputedCanvas.drawCircle(x + width - radius - STROKE_WIDTH, halfHeight, radius, blockFillPainter);
                precomputedCanvas.drawRect(x + radius + STROKE_WIDTH, STROKE_WIDTH, x + width - radius - STROKE_WIDTH, height - STROKE_WIDTH, blockFillPainter);

                //draw text
                blockFillPainter.setColor(Color.WHITE);
                blockFillPainter.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                blockFillPainter.setTextSize(radius / 2);
                precomputedCanvas.drawText(punch.root + punch.type, x + radius / 2, 5 * height / 8, blockFillPainter);
            }

            x += width;
        }
    }
}