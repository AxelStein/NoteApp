package com.axel_stein.noteapp.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

public class BottomView extends LinearLayout {

    private int mSelectedItemId;

    private OnNavigationItemSelectedListener mItemSelectedListener;

    private OnNavigationItemReselectedListener mItemReselectedListener;

    public BottomView(Context context) {
        super(context);
    }

    public BottomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BottomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setSelectedItemId(int selectedItemId) {
        mSelectedItemId = selectedItemId;
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

    private void updateViewImpl() {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getId() == mSelectedItemId) {

            }
        }
    }

    public interface OnNavigationItemSelectedListener {

        /**
         * Called when an item in the bottom navigation menu is selected.
         *
         * @param item The selected item
         *
         * @return true to display the item as the selected item and false if the item should not
         *         be selected. Consider setting non-selectable items as disabled preemptively to
         *         make them appear non-interactive.
         */
        boolean onNavigationItemSelected(@NonNull MenuItem item);
    }

    /**
     * Listener for handling reselection events on bottom navigation items.
     */
    public interface OnNavigationItemReselectedListener {

        /**
         * Called when the currently selected item in the bottom navigation menu is selected again.
         *
         * @param item The selected item
         */
        void onNavigationItemReselected(@NonNull MenuItem item);
    }

}
