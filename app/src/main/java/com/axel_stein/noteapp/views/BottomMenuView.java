package com.axel_stein.noteapp.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.R;

import javax.inject.Inject;

public class BottomMenuView extends LinearLayout {

    @Inject
    AppSettingsRepository mAppSettings;

    private int mSelectedItemId;

    private ColorStateList mItemIconTint;

    private ColorStateList mItemTextColor;

    private OnNavigationItemSelectedListener mItemSelectedListener;

    private OnNavigationItemReselectedListener mItemReselectedListener;

    public BottomMenuView(Context context) {
        this(context, null);
    }

    public BottomMenuView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomMenuView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        App.getAppComponent().inject(this);

        boolean nightMode = mAppSettings.nightMode();
        mItemIconTint = ContextCompat.getColorStateList(context,
                nightMode ? R.color.bottom_navigation_icon_dark : R.color.bottom_navigation_icon_light);
        mItemTextColor = ContextCompat.getColorStateList(context,
                nightMode ? R.color.bottom_navigation_text_dark : R.color.bottom_navigation_text_light);
    }

    static class SavedState extends BaseSavedState {
        int selectedItemId;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            selectedItemId = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(selectedItemId);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public Parcelable onSaveInstanceState() {
        // Force our ancestor class to save its state
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.selectedItemId = mSelectedItemId;

        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        mSelectedItemId = ss.selectedItemId;
        updateViewImpl();
    }

    public void setSelectedItemId(int selectedItemId) {
        mSelectedItemId = selectedItemId;
        updateViewImpl();

        if (mItemSelectedListener != null) {
            mItemSelectedListener.onNavigationItemSelected(selectedItemId);
        }
    }

    public int getSelectedItemId() {
        return mSelectedItemId;
    }

    public void setItemSelectedListener(OnNavigationItemSelectedListener l) {
        mItemSelectedListener = l;
    }

    public void setItemReselectedListener(OnNavigationItemReselectedListener l) {
        mItemReselectedListener = l;
    }

    public void setItemIconTintList(@Nullable ColorStateList tint) {
        mItemIconTint = tint;
        updateViewImpl();
    }

    public void setItemTextColor(@Nullable ColorStateList textColor) {
        mItemTextColor = textColor;
        updateViewImpl();
    }

    private void init() {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            child.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = v.getId();
                    if (id == mSelectedItemId && mItemReselectedListener != null) {
                        mItemReselectedListener.onNavigationItemReselected(mSelectedItemId);
                    } else {
                        setSelectedItemId(id);
                    }
                }
            });
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void updateViewImpl() {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            IconTextView child = (IconTextView) getChildAt(i);
            boolean checked = child.getId() == mSelectedItemId;
            updateItemIconImpl(child, checked);
            updateItemTextColorImpl(child, checked);
        }
    }

    private void updateItemIconImpl(IconTextView view, boolean checked) {
        int[] state = new int[]{checked ? android.R.attr.state_checked : -android.R.attr.state_checked};
        view.setIconTopTintColor(mItemIconTint.getColorForState(state, 0));
    }

    private void updateItemTextColorImpl(IconTextView view, boolean checked) {
        int[] state = new int[]{checked ? android.R.attr.state_checked : -android.R.attr.state_checked};
        view.setTextColor(mItemTextColor.getColorForState(state, 0));
    }

    public interface OnNavigationItemSelectedListener {

        void onNavigationItemSelected(int itemId);

    }

    public interface OnNavigationItemReselectedListener {

        void onNavigationItemReselected(int itemId);

    }

}
