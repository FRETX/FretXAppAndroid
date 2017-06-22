package fretx.version4;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 22/06/17 12:37.
 */

public class HeadStockView extends View{
    private int height, width;
    private final Drawable headStockImage;
    private final Rect imageBounds = new Rect();

    public HeadStockView(Context context, AttributeSet attrs){
        super(context, attrs);

        headStockImage = getContext().getResources().getDrawable(R.drawable.fretboard, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        height = h;
        width = w;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.getClipBounds(imageBounds);
        headStockImage.setBounds(imageBounds);
        headStockImage.draw(canvas);
    }

}
