package fretx.version4;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 22/06/17 12:37.
 */

public class HeadStockView extends View{
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

    private int viewHeight, viewWidth;
    //headstock
    private final Bitmap headStockBitmap;
    private final int headStockImageIntrinsicHeight;
    private final int headStockImageIntrinsicWidth;
    private int headStockImagePosX;
    private int headStockImagePosY;
    private float headStockImageRatio = 1;
    private int headStockImageHeight;
    private int headStockImageWidth;
    private final Matrix headStockMatrix = new Matrix();
    //hammers
    private class Hammer {
        float rx = 0;
        float ry = 0;
        float cx = 0;
        float cy = 0;

        void set(float rx, float ry) {
            this.rx = cx;
            this.ry = cy;
        }

        void update(int x, int y) {
            cx = rx * x;
            cy = ry * y;
        }
    }
    private final Hammer hammers[] = new Hammer[NB_HAMMERS];
    private float hammerClickRadius;
    private int selectedHammerIndex = -1;
    //listener
    private OnClickListener listener;

    public HeadStockView(Context context, AttributeSet attrs){
        super(context, attrs);

        headStockBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.classical_headstock);
        headStockImageIntrinsicHeight = headStockBitmap.getHeight();
        headStockImageIntrinsicWidth = headStockBitmap.getWidth();

        hammers[0].set(H1RX, H1RY);
        hammers[1].set(H2RX, H2RY);
        hammers[2].set(H3RX, H3RY);
        hammers[3].set(H4RX, H4RY);
        hammers[4].set(H5RX, H5RY);
        hammers[5].set(H6RX, H6RY);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewHeight = h;
        viewWidth = w;

        final float ratioX = ((float) w) / headStockImageIntrinsicWidth;
        final float ratioY = ((float) h) / headStockImageIntrinsicHeight;
        if (ratioX < ratioY) {
            headStockImageRatio = ratioX;
        } else {
            headStockImageRatio = ratioY;
        }
        headStockImageWidth = (int) Math.floor(headStockImageIntrinsicWidth * headStockImageRatio);
        headStockImageHeight = (int) Math.floor(headStockImageIntrinsicHeight * headStockImageRatio);
        headStockImagePosX = (viewWidth - headStockImageWidth) / 2;
        headStockImagePosY = (viewHeight - headStockImageHeight) / 2;
        headStockMatrix.reset();
        headStockMatrix.postScale(headStockImageRatio, headStockImageRatio);
        headStockMatrix.postTranslate(headStockImagePosX, headStockImagePosY);

        for (Hammer hammer: hammers) {
            hammer.update(headStockImageWidth, headStockImageHeight);
        }
        hammerClickRadius = HAMMER_RADIUS * headStockImageHeight;
        hammerClickRadius *= hammerClickRadius;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            final float x = event.getX();
            final float y = event.getY();

            for (int index = 0; index < hammers.length; ++index) {
                final float dx = hammers[index].cx - x;
                final float dy = hammers[index].cy - y;
                if (dx * dx + dy * dy < hammerClickRadius) {
                    if (selectedHammerIndex != index) {
                        selectedHammerIndex = index;
                        if (listener != null) {
                            listener.notify();
                        }
                        break;
                    }
                }
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(headStockBitmap, headStockMatrix, null);
        //canvas.drawCircle(headStockImagePosX + headStockImageWidth * H6RX, headStockImagePosY + headStockImageHeight * H6RY, 0.05f * headStockImageHeight, circlePainter);
    }

    public void setOnClickListener(@NonNull OnClickListener listener) {
        this.listener = listener;
    }

    public int getSelectedHammerIndex() {
        return selectedHammerIndex;
    }
}
