package fretx.version4;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.Nullable;
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
    //ears
    private static final int TEXT_SIZE = 20;
    private static final float EAR_RADIUS = 0.05f;
    private static final float E1RX = 0.09f;
    private static final float E1RY = 0.50f;
    private static final float E2RX = 0.09f;
    private static final float E2RY = 0.36f;
    private static final float E3RX = 0.09f;
    private static final float E3RY = 0.21f;
    private static final float E4RX = 0.88f;
    private static final float E4RY = 0.21f;
    private static final float E5RX = 0.88f;
    private static final float E5RY = 0.36f;
    private static final float E6RX = 0.88f;
    private static final float E6RY = 0.50f;
    //strings
    private static final float STRING_WIDTH = 0.02f;
    private static final float S1RX = 0.32f;
    private static final float S1RY = 0.77f;
    private static final float S2RX = 0.38f;
    private static final float S2RY = 0.77f;
    private static final float S3RX = 0.45f;
    private static final float S3RY = 0.77f;
    private static final float S4RX = 0.52f;
    private static final float S4RY = 0.77f;
    private static final float S5RX = 0.58f;
    private static final float S5RY = 0.77f;
    private static final float S6RX = 0.65f;
    private static final float S6RY = 0.77f;
    //headstock
    private final Bitmap headStockBitmap;
    private final int headStockImageIntrinsicHeight;
    private final int headStockImageIntrinsicWidth;
    private final Matrix headStockMatrix = new Matrix();
    private final Ear ears[] = new Ear[NB_HAMMERS];
    private float earClickRadius;
    private float stringWidth;
    private float stringBottom;
    private int selectedEarIndex;
    private Paint painter = new Paint(); //FOR DEBUG ONLY
    private class Ear {
        float erx = 0;
        float ery = 0;
        float srx = 0;
        float sry = 0;
        float ex = 0;
        float ey = 0;
        float sx = 0;
        float sy = 0;
        String name;

        Ear(float erx, float ery, float srx, float sry, String name) {
            this.erx = erx;
            this.ery = ery;
            this.srx = srx;
            this.sry = sry;
            this.name = name;
        }

        void update(int x, int y, int px, int py) {
            ex = px + erx * x;
            ey = py + ery * y;
            sx = px + srx * x;
            sy = py + sry * y;
        }
    }
    public interface OnEarSelectedListener {
        void onEarSelected(int selectedIndex);
    }
    private OnEarSelectedListener listener;

    public HeadStockView(Context context, AttributeSet attrs){
        super(context, attrs);

        headStockBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.classical_headstock);
        headStockImageIntrinsicHeight = headStockBitmap.getHeight();
        headStockImageIntrinsicWidth = headStockBitmap.getWidth();

        ears[0] = new Ear(E1RX, E1RY, S1RX, S1RY, "0");
        ears[1] = new Ear(E2RX, E2RY, S2RX, S2RY, "1");
        ears[2] = new Ear(E3RX, E3RY, S3RX, S3RY, "2");
        ears[3] = new Ear(E4RX, E4RY, S4RX, S4RY, "3");
        ears[4] = new Ear(E5RX, E5RY, S5RX, S5RY, "4");
        ears[5] = new Ear(E6RX, E6RY, S6RX, S6RY, "5");

        selectedEarIndex = 0;

        painter.setTextSize(TEXT_SIZE);
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

        for (Ear hammer: ears) {
            hammer.update(headStockImageWidth, headStockImageHeight, headStockImagePosX, headStockImagePosY);
        }
        earClickRadius = EAR_RADIUS * headStockImageHeight;
        stringWidth = STRING_WIDTH * headStockImageWidth;
        stringBottom = headStockImagePosY + headStockImageHeight;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            final float x = event.getX();
            final float y = event.getY();

            Log.d(TAG, "onTouchEvent: DOWN " + x+ ", " + y);
            for (int index = 0; index < ears.length; ++index) {
                final float dx = ears[index].ex - x;
                final float dy = ears[index].ey - y;
                if (dx * dx + dy * dy < earClickRadius * earClickRadius) {
                    if (selectedEarIndex != index) {
                        selectedEarIndex = index;
                        if (listener != null) {
                            listener.onEarSelected(selectedEarIndex);
                        }
                        invalidate();
                    }
                }
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(headStockBitmap, headStockMatrix, null);
        for (int index = 0; index < ears.length; ++index) {
            final Ear ear = ears[index];
            if (index == selectedEarIndex) {
                painter.setColor(Color.GREEN);
                canvas.drawRect(ear.sx, ear.sy, ear.sx + stringWidth, stringBottom, painter);
            } else {
                painter.setColor(Color.BLACK);

            }
            canvas.drawText(ear.name, ear.ex - TEXT_SIZE / 4, ear.ey + TEXT_SIZE / 4, painter);
        }
        //canvas.drawCircle(ear.ex, ear.ey, earClickRadius, painter);
    }

    public void setOnEarSelectedListener(@Nullable OnEarSelectedListener listener) {
        this.listener = listener;
    }
}
