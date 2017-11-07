package com.axel_stein.noteapp.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.design.widget.TextInputEditText;
import android.util.AttributeSet;

import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.utils.ColorUtil;

public class LinedEditText extends TextInputEditText {
    private Rect mRect;
    private Paint mPaint;

    public LinedEditText(Context context) {
        super(context);
        init();
    }

    public LinedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LinedEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setColor(ColorUtil.getColorAttr(getContext(), R.attr.editTextLineColor));
        //mPaint.setColor(ContextCompat.getColor(getContext(), R.color.edit_text_line_color));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //int count = getLineCount();

        int height = getHeight();
        int line_height = getLineHeight();

        int count = height / line_height;

        if (getLineCount() > count)
            count = getLineCount();//for long text with scrolling

        Rect r = mRect;
        Paint paint = mPaint;
        int baseline = getLineBounds(0, r);//first line

        for (int i = 0; i < count; i++) {
            canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
            baseline += getLineHeight();//next line
        }

        super.onDraw(canvas);
    }

}
