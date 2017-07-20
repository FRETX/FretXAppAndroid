package fretx.version4.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import fretx.version4.utils.Preference;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 22/06/17 12:37.
 */

public class HeadStockView extends View{
    private static final String TAG = "KJKP6_HSV";
    private int TEXT_SIZE = 40;
    private HeadstockViewDescriptor descriptor;
    private final Bitmap headStockBitmapRight;
    private final Bitmap headStockBitmapLeft;
    private final int headStockImageIntrinsicHeight;
    private final int headStockImageIntrinsicWidth;
    private int viewWidth;
    private final Matrix headStockMatrix = new Matrix();
    private int selectedEarIndex;
    private boolean clickable = true;
    private Paint painter = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Typeface gothamFont = Typeface.createFromAsset(getContext().getAssets(),"fonts/GothamRoundedBook.ttf");
    private Typeface gothamFontBold = Typeface.create(gothamFont, Typeface.BOLD);

    public interface OnEarSelectedListener {
        void onEarSelected(int selectedIndex);
    }
    private OnEarSelectedListener listener;

    public HeadStockView(Context context, AttributeSet attrs){
        super(context, attrs);

        if (Preference.getInstance().isElectricGuitar()) {
            descriptor = new HeadstockViewDescriptor(HeadstockViewDescriptor.Headstock.ELECTRIC);
        } else {
            descriptor = new HeadstockViewDescriptor(HeadstockViewDescriptor.Headstock.CLASSIC);
        }
        headStockBitmapRight = BitmapFactory.decodeResource(context.getResources(), descriptor.ressourceId);
        headStockBitmapLeft = flip(headStockBitmapRight);
        headStockImageIntrinsicHeight = headStockBitmapRight.getHeight();
        headStockImageIntrinsicWidth = headStockBitmapRight.getWidth();
        viewWidth = headStockImageIntrinsicWidth;

        painter.setTextSize(TEXT_SIZE);
        painter.setTypeface(gothamFontBold);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        viewWidth = w;
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

        TEXT_SIZE = (int)((float)headStockImageWidth * 0.1f);
        painter.setTextSize(TEXT_SIZE);

        descriptor.update(headStockImageWidth, headStockImageHeight, headStockImagePosX, headStockImagePosY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (clickable && event.getAction() == MotionEvent.ACTION_DOWN) {
            final float x = event.getX();
            final float y = event.getY();

            if(Preference.getInstance().isLeftHanded()) {
                for (int index = 0; index < descriptor.ears.length; ++index) {
                    final float dx = viewWidth - descriptor.ears[index].exm - x;
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
            } else {
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
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(Preference.getInstance().isLeftHanded()) {
            canvas.drawBitmap(headStockBitmapLeft, headStockMatrix, null);
            for (int index = 0; index < descriptor.ears.length; ++index) {
                final HeadstockViewDescriptor.Ear ear = descriptor.ears[index];
                if (index == selectedEarIndex) {
                    painter.setColor(Color.GREEN);
                    canvas.drawRect(viewWidth - ear.sxm - descriptor.stringWidth, ear.sy, viewWidth - ear.sxm, descriptor.stringBottom, painter);
                } else {
                    painter.setColor(Color.WHITE);
                }
                painter.setStrokeWidth(5);
                canvas.drawText(ear.name, viewWidth - ear.exm - TEXT_SIZE / 4, ear.ey + TEXT_SIZE / 4, painter);
            }
        } else {
            canvas.drawBitmap(headStockBitmapRight, headStockMatrix, null);
            for (int index = 0; index < descriptor.ears.length; ++index) {
                final HeadstockViewDescriptor.Ear ear = descriptor.ears[index];
                if (index == selectedEarIndex) {
                    painter.setColor(Color.GREEN);
                    canvas.drawRect(ear.sx, ear.sy, ear.sx + descriptor.stringWidth, descriptor.stringBottom, painter);
                } else {
                    painter.setColor(Color.WHITE);
                }
                painter.setStrokeWidth(5);
                canvas.drawText(ear.name, ear.ex - TEXT_SIZE / 4, ear.ey + TEXT_SIZE / 4, painter);
            }
        }
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

    private Bitmap flip(Bitmap src)
    {
        Matrix m = new Matrix();
        m.preScale(-1, 1);
        final Bitmap dst = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, false);
        dst.setDensity(DisplayMetrics.DENSITY_DEFAULT);
        return dst;
    }
}
