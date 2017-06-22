package fretx.version4;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 22/06/17 12:37.
 */

public class HeadStockView extends View{
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
    private final Bitmap headStockBitmap;
    private final int headStockImageIntrinsicHeight;
    private final int headStockImageIntrinsicWidth;
    private int headStockImagePosX;
    private int headStockImagePosY;
    private float headStockImageRatio = 1;
    private int headStockImageHeight;
    private int headStockImageWidth;
    private final Matrix headStockMatrix = new Matrix();

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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(headStockBitmap, headStockMatrix, null);
        //canvas.drawCircle(headStockImagePosX + headStockImageWidth * H6RX, headStockImagePosY + headStockImageHeight * H6RY, 0.05f * headStockImageHeight, circlePainter);
    }

}
