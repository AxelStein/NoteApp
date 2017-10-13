package com.axel_stein.noteapp.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.AttributeSet;

import com.axel_stein.noteapp.R;

public class CustomCheckedTextView extends AppCompatCheckBox {
    private Drawable mCheckMark;
    private boolean mCheckable = true;

    private int mDefaultPadding;
    private int mDefaultPaddingHalf;

    public CustomCheckedTextView(Context context) {
        this(context, null);
    }

    public CustomCheckedTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomCheckedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mDefaultPadding = getResources().getDimensionPixelOffset(R.dimen.default_item_padding);
        mDefaultPaddingHalf = mDefaultPadding / 2;

        /*
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomCheckedTextView);
            ColorStateList list = a.getColorStateList(R.styleable.CustomCheckedTextView_drawableLeftTint);

            Drawable[] drawables = getCompoundDrawables();
            for (Drawable drawable : drawables) {
                if (drawable != null && list != null) {
                    //drawable.mutate().setTintList(list);
                    drawable.mutate().setColorFilter(list.getDefaultColor(), PorterDuff.Mode.SRC_ATOP);
                    //DrawableCompat.setTintList(drawable, list);
                }
            }

            a.recycle();
        }
        */
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCheckMark = getCompoundDrawables()[0];
        //DrawableCompat.setTintList();
    }

    public void setCheckable(boolean checkable) {
        if (mCheckable != checkable) {
            mCheckable = checkable;

            setCompoundDrawablesWithIntrinsicBounds(checkable ? mCheckMark : null, null, null, null);
        }

        setPadding(checkable ? mDefaultPaddingHalf : mDefaultPadding, 0, mDefaultPadding, 0);
    }

}
