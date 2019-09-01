package com.axel_stein.noteapp.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.axel_stein.noteapp.R;

public class IconTextView extends AppCompatTextView {
    private int mIconTopTintColor;
    private int mIconLeftTintColor;
    private int mIconRightTintColor;

    private boolean mShowIcons;

    private boolean mShowIconTop;
    private boolean mShowIconLeft;
    private boolean mShowIconRight;

    private Drawable mIconTop;
    private Drawable mIconLeft;
    private Drawable mIconRight;

    public IconTextView(Context context) {
        this(context, null);
    }

    public IconTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IconTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IconTextView);

        mIconTopTintColor = a.getColor(R.styleable.IconTextView_iconTopTint, 0);
        mIconLeftTintColor = a.getColor(R.styleable.IconTextView_iconLeftTint, 0);
        mIconRightTintColor = a.getColor(R.styleable.IconTextView_iconRightTint, 0);

        mIconTop = a.getDrawable(R.styleable.IconTextView_iconTop);
        mIconLeft = a.getDrawable(R.styleable.IconTextView_iconLeft);
        mIconRight = a.getDrawable(R.styleable.IconTextView_iconRight);

        mShowIcons = a.getBoolean(R.styleable.IconTextView_showIcons, true);
        mShowIconTop = a.getBoolean(R.styleable.IconTextView_showIconTop, true);
        mShowIconLeft = a.getBoolean(R.styleable.IconTextView_showIconLeft, true);
        mShowIconRight = a.getBoolean(R.styleable.IconTextView_showIconRight, true);

        a.recycle();

        update();
    }

    public void showIcons(boolean showIcons) {
        mShowIcons = showIcons;
        update();
    }

    public void setIconTop(@DrawableRes int iconRes) {
        try {
            mIconTop = ContextCompat.getDrawable(getContext(), iconRes);
        } catch (Exception ignored) {
            mIconTop = null;
        }
        update();
    }

    public void setIconTop(Drawable icon) {
        mIconTop = icon;
        update();
    }

    public void setIconLeft(@DrawableRes int iconRes) {
        try {
            mIconLeft = ContextCompat.getDrawable(getContext(), iconRes);
        } catch (Exception ignored) {
            mIconLeft = null;
        }
        update();
    }

    public void setIconLeft(Drawable icon) {
        mIconLeft = icon;
        update();
    }

    public void setIconTopTintColor(@ColorInt int color) {
        mIconTopTintColor = color;
        update();
    }

    public void setIconTopTintColorRes(@ColorRes int color) {
        mIconTopTintColor = ContextCompat.getColor(getContext(), color);
        update();
    }

    public void setIconLeftTintColor(@ColorInt int color) {
        mIconLeftTintColor = color;
        update();
    }

    public void setIconLeftTintColorRes(@ColorRes int color) {
        mIconLeftTintColor = ContextCompat.getColor(getContext(), color);
        update();
    }

    public void showIconTop(boolean show) {
        mShowIconTop = show;
        update();
    }

    public void showIconLeft(boolean showIconLeft) {
        mShowIconLeft = showIconLeft;
        update();
    }

    public void setIconRight(@DrawableRes int iconRes) {
        try {
            mIconRight = ContextCompat.getDrawable(getContext(), iconRes);
        } catch (Exception ignored) {
            mIconRight = null;
        }
        update();
    }

    public void setIconRight(Drawable icon) {
        mIconRight = icon;
        update();
    }

    public void setIconRightTintColor(@ColorInt int color) {
        mIconRightTintColor = color;
        update();
    }

    public void setIconRightTintColorRes(@ColorRes int color) {
        mIconRightTintColor = ContextCompat.getColor(getContext(), color);
        update();
    }

    public void showIconRight(boolean showIconRight) {
        mShowIconRight = showIconRight;
        update();
    }

    private void update() {
        if (!mShowIcons) {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            return;
        }

        tintIcon(mIconTop, mIconTopTintColor);
        tintIcon(mIconLeft, mIconLeftTintColor);
        tintIcon(mIconRight, mIconRightTintColor);

        Drawable top = mShowIconTop ? mIconTop : null;
        Drawable left = mShowIconLeft ? mIconLeft : null;
        Drawable right = mShowIconRight ? mIconRight : null;

        setCompoundDrawablesWithIntrinsicBounds(left, top, right, null);
    }

    private void tintIcon(@Nullable Drawable icon, int color) {
        if (icon != null) {
            icon = icon.mutate();
            icon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

}
