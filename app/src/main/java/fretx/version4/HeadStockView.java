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
import android.view.MotionEvent;
import android.view.View;

import fretx.version4.utils.Preference;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 22/06/17 12:37.
 */

public class HeadStockView extends View{
    private static final String TAG = "KJKP6_HSV";
    private static final int TEXT_SIZE = 40;
    //headstock
    private HeadstockViewDescriptor descriptor;
    private final Bitmap headStockBitmap;
    private final int headStockImageIntrinsicHeight;
    private final int headStockImageIntrinsicWidth;
    private final Matrix headStockMatrix = new Matrix();
    private int selectedEarIndex;
    private boolean clickable = true;
    private Paint painter = new Paint();

    public interface OnEarSelectedListener {
        void onEarSelected(int selectedIndex);
    }
    private OnEarSelectedListener listener;

    public HeadStockView(Context context, AttributeSet attrs){
        super(context, attrs);

        if (Preference.getInstance().isElectricGuitar())
            descriptor = new HeadstockViewDescriptor(HeadstockViewDescriptor.Headstock.ELECTRIC);
        else
            descriptor = new HeadstockViewDescriptor(HeadstockViewDescriptor.Headstock.CLASSIC);
        headStockBitmap = BitmapFactory.decodeResource(context.getResources(), descriptor.ressourceId);
        headStockImageIntrinsicHeight = headStockBitmap.getHeight();
        headStockImageIntrinsicWidth = headStockBitmap.getWidth();

        painter.setTextSize(TEXT_SIZE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
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

        descriptor.update(headStockImageWidth, headStockImageHeight, headStockImagePosX, headStockImagePosY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (clickable && event.getAction() == MotionEvent.ACTION_DOWN) {
            final float x = event.getX();
            final float y = event.getY();

            for (int index = 0; index < descriptor.ears.length; ++index) {
                final float dx = descriptor.ears[index].ex - x;
                final float dy = descriptor.ears[index].ey - y;
                if (dx * dx + dy * dy < descriptor.earRadius * descriptor.earRadius) {
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
        for (int index = 0; index < descriptor.ears.length; ++index) {
            final HeadstockViewDescriptor.Ear ear = descriptor.ears[index];
            if (index == selectedEarIndex) {
                painter.setColor(Color.GREEN);
                canvas.drawRect(ear.sx, ear.sy, ear.sx + descriptor.stringWidth, descriptor.stringBottom, painter);
            } else {
                painter.setColor(Color.BLACK);
            }
            painter.setStrokeWidth(5);
            canvas.drawText(ear.name, ear.ex - TEXT_SIZE / 4, ear.ey + TEXT_SIZE / 4, painter);
        }
        //canvas.drawCircle(ear.ex, ear.ey, earClickRadius, painter);
    }

    public void setOnEarSelectedListener(@Nullable OnEarSelectedListener listener) {
        this.listener = listener;
    }

    public void setSelectedEar(int selectedEarIndex) {
        this.selectedEarIndex = selectedEarIndex;
        invalidate();
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }
}
