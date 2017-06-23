package fretx.version4;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 22/06/17 12:37.
 */

public class HeadStockView extends View{
    private static final String TAG = "KJKP6_HSV";
    private static final int NB_HAMMERS = 6;
    private static final float HAMMER_RADIUS = 0.05f;
    private static final float H1RX = 0.08f;
    private static final float H1RY = 0.28f;
    private static final float H2RX = 0.08f;
    private static final float H2RY = 0.39f;
    private static final float H3RX = 0.08f;
    private static final float H3RY = 0.50f;
    private static final float H4RX = 0.92f;
    private static final float H4RY = 0.28f;
    private static final float H5RX = 0.92f;
    private static final float H5RY = 0.39f;
    private static final float H6RX = 0.92f;
    private static final float H6RY = 0.50f;

    //headstock
    private final Bitmap headStockBitmap;
    private final int headStockImageIntrinsicHeight;
    private final int headStockImageIntrinsicWidth;
    private final Matrix headStockMatrix = new Matrix();
    //hammers
    private final Hammer hammers[] = new Hammer[NB_HAMMERS];
    private float hammerClickRadius;
    private int selectedHammerIndex = -1;
    private Paint circlePainter = new Paint(); //FOR DEBUG ONLY
    private class Hammer {
        float rx = 0;
        float ry = 0;
        float cx = 0;
        float cy = 0;

        Hammer(float rx, float ry) {
            this.rx = rx;
            this.ry = ry;
        }

        void update(int x, int y, int px, int py) {
            cx = px + rx * x;
            cy = py + ry * y;
        }
    }

    public HeadStockView(Context context, AttributeSet attrs){
        super(context, attrs);

        headStockBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.classical_headstock);
        headStockImageIntrinsicHeight = headStockBitmap.getHeight();
        headStockImageIntrinsicWidth = headStockBitmap.getWidth();

        hammers[0] = new Hammer(H1RX, H1RY);
        hammers[1] = new Hammer(H2RX, H2RY);
        hammers[2] = new Hammer(H3RX, H3RY);
        hammers[3] = new Hammer(H4RX, H4RY);
        hammers[4] = new Hammer(H5RX, H5RY);
        hammers[5] = new Hammer(H6RX, H6RY);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(TAG, "onSizeChanged: " + w + ", " + h);
        super.onSizeChanged(w, h, oldw, oldh);

        final float ratioX = ((float) w) / headStockImageIntrinsicWidth;
        final float ratioY = ((float) h) / headStockImageIntrinsicHeight;
        float headStockImageRatio;
        if (ratioX < ratioY) {
            headStockImageRatio = ratioX;
        } else {
            headStockImageRatio = ratioY;
        }
        final int headStockImageWidth = (int) Math.floor(headStockImageIntrinsicWidth * headStockImageRatio);
        final int headStockImageHeight = (int) Math.floor(headStockImageIntrinsicHeight * headStockImageRatio);
        final int headStockImagePosX = (w - headStockImageWidth) / 2;
        final int headStockImagePosY = (h - headStockImageHeight) / 2;
        headStockMatrix.reset();
        headStockMatrix.postScale(headStockImageRatio, headStockImageRatio);
        headStockMatrix.postTranslate(headStockImagePosX, headStockImagePosY);

        for (Hammer hammer: hammers) {
            hammer.update(headStockImageWidth, headStockImageHeight, headStockImagePosX, headStockImagePosY);
        }
        hammerClickRadius = HAMMER_RADIUS * headStockImageHeight;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            final float x = event.getX();
            final float y = event.getY();

            Log.d(TAG, "onTouchEvent: DOWN " + x+ ", " + y);
            for (int index = 0; index < hammers.length; ++index) {
                final float dx = hammers[index].cx - x;
                final float dy = hammers[index].cy - y;
                if (dx * dx + dy * dy < hammerClickRadius * hammerClickRadius) {
                    selectedHammerIndex = index;
                }
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(headStockBitmap, headStockMatrix, null);
        //FOR DEBUG ONLY
        for (Hammer hammer: hammers) {
            canvas.drawCircle(hammer.cx, hammer.cy, hammerClickRadius, circlePainter);
        }
    }

    public int getSelectedHammerIndex() {
        return selectedHammerIndex;
    }
}
