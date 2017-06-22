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
        float cx = 0;
        float cy = 0;

        public void set(float cx, float cy) {
            this.cx = cx;
            this.cy = cy;
        }
    }
    private final Hammer hammers[] = new Hammer[NB_HAMMERS];
    private float hammerClickRadius;
    private int selectedHammerIndex = -1;
    //listener
    private OnClickListener listener;


    private final Paint circlePainter = new Paint();

    public HeadStockView(Context context, AttributeSet attrs){
        super(context, attrs);

        headStockBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.classical_headstock);
        headStockImageIntrinsicHeight = headStockBitmap.getHeight();
        headStockImageIntrinsicWidth = headStockBitmap.getWidth();
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

        hammers[0].set(headStockImagePosX + headStockImageWidth * H1RX, headStockImagePosY + headStockImageHeight * H1RY);
        hammers[1].set(headStockImagePosX + headStockImageWidth * H2RX, headStockImagePosY + headStockImageHeight * H2RY);
        hammers[2].set(headStockImagePosX + headStockImageWidth * H3RX, headStockImagePosY + headStockImageHeight * H3RY);
        hammers[3].set(headStockImagePosX + headStockImageWidth * H4RX, headStockImagePosY + headStockImageHeight * H4RY);
        hammers[4].set(headStockImagePosX + headStockImageWidth * H5RX, headStockImagePosY + headStockImageHeight * H5RY);
        hammers[5].set(headStockImagePosX + headStockImageWidth * H6RX, headStockImagePosY + headStockImageHeight * H6RY);
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
