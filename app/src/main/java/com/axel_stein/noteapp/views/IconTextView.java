package com.axel_stein.noteapp.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import com.axel_stein.noteapp.R;

public class IconTextView extends AppCompatTextView {

    private final ForegroundViewImpl mImpl = new ForegroundViewImpl(this);

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
        if (mImpl != null) {
            mImpl.init(context, attrs, defStyle);
        }

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

    public void update() {
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

    /**
     * Describes how the foreground is positioned.
     *
     * @return foreground gravity.
     * @see #setForegroundGravity(int)
     */
    public int getForegroundGravity() {
        if (mImpl != null) {
            return mImpl.getForegroundGravity();
        }
        return super.getForegroundGravity();
    }

    /**
     * Describes how the foreground is positioned. Defaults to START and TOP.
     *
     * @param foregroundGravity See {@link android.view.Gravity}
     * @see #getForegroundGravity()
     */
    public void setForegroundGravity(int foregroundGravity) {
        if (mImpl != null) {
            mImpl.setForegroundGravity(foregroundGravity);
        }
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable who) {
        return super.verifyDrawable(who) || (mImpl != null && mImpl.verifyDrawable(who));
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (mImpl != null) {
            mImpl.jumpDrawablesToCurrentState();
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mImpl != null) {
            mImpl.drawableStateChanged();
        }
    }

    /**
     * Returns the drawable used as the foreground of this FrameLayout. The
     * foreground drawable, if non-null, is always drawn on top of the children.
     *
     * @return A Drawable or null if no foreground was set.
     */
    public Drawable getForeground() {
        return mImpl.getForeground();
    }

    /**
     * Supply a Drawable that is to be rendered on top of all of the child
     * views in the frame layout. Any padding in the Drawable will be taken
     * into account by ensuring that the children are inset to be placed
     * inside of the padding area.
     *
     * @param drawable The Drawable to be drawn on top of the children.
     */
    public void setForeground(Drawable drawable) {
        super.setForeground(drawable);
        if (mImpl != null) {
            mImpl.setForeground(drawable);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mImpl != null) {
            mImpl.onLayout(changed);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mImpl != null) {
            mImpl.onSizeChanged();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mImpl != null) {
            mImpl.draw(canvas);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (mImpl != null) {
            mImpl.onTouchEvent(e);
        }
        return super.onTouchEvent(e);
    }

    public static class ForegroundViewImpl {
        private final View mTargetView;
        private final Rect mSelfBounds = new Rect();
        private final Rect mOverlayBounds = new Rect();
        protected boolean mForegroundInPadding = true;
        boolean mForegroundBoundsChanged = false;
        private Drawable mForeground;
        private int mForegroundGravity = Gravity.FILL;

        public ForegroundViewImpl(View targetView) {
            this.mTargetView = targetView;
        }

        public void init(Context context, AttributeSet attrs, int defStyle) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IconTextView, defStyle, 0);
            mForegroundGravity = a.getInt(R.styleable.IconTextView_foregroundGravity, mForegroundGravity);
            final Drawable d = a.getDrawable(R.styleable.IconTextView_foreground);
            if (d != null) {
                setForeground(d);
            }
            mForegroundInPadding = a.getBoolean(R.styleable.IconTextView_foregroundInsidePadding, true);
            a.recycle();
        }

        /**
         * Describes how the foreground is positioned.
         *
         * @return foreground gravity.
         * @see #setForegroundGravity(int)
         */
        public int getForegroundGravity() {
            return mForegroundGravity;
        }

        /**
         * Describes how the foreground is positioned. Defaults to START and TOP.
         *
         * @param foregroundGravity See {@link android.view.Gravity}
         * @see #getForegroundGravity()
         */
        public void setForegroundGravity(int foregroundGravity) {
            if (mForegroundGravity != foregroundGravity) {
                if ((foregroundGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) == 0) {
                    foregroundGravity |= Gravity.START;
                }
                if ((foregroundGravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
                    foregroundGravity |= Gravity.TOP;
                }
                mForegroundGravity = foregroundGravity;
                if (mForegroundGravity == Gravity.FILL && mForeground != null) {
                    Rect padding = new Rect();
                    mForeground.getPadding(padding);
                }
                mTargetView.requestLayout();
            }
        }

        public boolean verifyDrawable(Drawable who) {
            return who == mForeground;
        }

        public void jumpDrawablesToCurrentState() {
            if (mForeground != null) mForeground.jumpToCurrentState();
        }

        public void drawableStateChanged() {
            if (mForeground != null && mForeground.isStateful()) {
                mForeground.setState(mTargetView.getDrawableState());
            }
        }

        /**
         * Returns the drawable used as the foreground of this FrameLayout. The
         * foreground drawable, if non-null, is always drawn on top of the children.
         *
         * @return A Drawable or null if no foreground was set.
         */
        public Drawable getForeground() {
            return mForeground;
        }

        /**
         * Supply a Drawable that is to be rendered on top of all of the child
         * views in the frame layout. Any padding in the Drawable will be taken
         * into account by ensuring that the children are inset to be placed
         * inside of the padding area.
         *
         * @param drawable The Drawable to be drawn on top of the children.
         */
        public void setForeground(Drawable drawable) {
            if (mForeground != drawable) {
                if (mForeground != null) {
                    mForeground.setCallback(null);
                    mTargetView.unscheduleDrawable(mForeground);
                }
                mForeground = drawable;
                if (drawable != null) {
                    mTargetView.setWillNotDraw(false);
                    drawable.setCallback(mTargetView);
                    if (drawable.isStateful()) {
                        drawable.setState(mTargetView.getDrawableState());
                    }
                    if (mForegroundGravity == Gravity.FILL) {
                        Rect padding = new Rect();
                        drawable.getPadding(padding);
                    }
                } else {
                    mTargetView.setWillNotDraw(true);
                }
                mTargetView.requestLayout();
                mTargetView.invalidate();
            }
        }

        public void onLayout(boolean changed) {
            if (changed) {
                mForegroundBoundsChanged = true;
            }
        }

        public void onSizeChanged() {
            mForegroundBoundsChanged = true;
        }

        public void draw(Canvas canvas) {
            if (mForeground != null) {
                final Drawable foreground = mForeground;
                if (mForegroundBoundsChanged) {
                    mForegroundBoundsChanged = false;
                    final Rect selfBounds = mSelfBounds;
                    final Rect overlayBounds = mOverlayBounds;
                    final int w = mTargetView.getRight() - mTargetView.getLeft();
                    final int h = mTargetView.getBottom() - mTargetView.getTop();
                    if (mForegroundInPadding) {
                        selfBounds.set(0, 0, w, h);
                    } else {
                        selfBounds.set(mTargetView.getPaddingLeft(), mTargetView.getPaddingTop(),
                                w - mTargetView.getPaddingRight(), h - mTargetView.getPaddingBottom());
                    }
                    Gravity.apply(mForegroundGravity, foreground.getIntrinsicWidth(),
                            foreground.getIntrinsicHeight(), selfBounds, overlayBounds);
                    foreground.setBounds(overlayBounds);
                }
                foreground.draw(canvas);
            }
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public void onTouchEvent(MotionEvent e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    if (mForeground != null)
                        mForeground.setHotspot(e.getX(), e.getY());
                }
            }
        }
    }

}
