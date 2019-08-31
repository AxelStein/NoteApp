package com.axel_stein.noteapp.main.list;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.main.list.NotesContract.Presenter;
import com.axel_stein.noteapp.main.list.NotesFragment.NoteAdapter;
import com.axel_stein.noteapp.utils.DisplayUtil;

import static androidx.recyclerview.widget.ItemTouchHelper.LEFT;
import static androidx.recyclerview.widget.ItemTouchHelper.RIGHT;

public class SwipeActionCallback extends ItemTouchHelper.SimpleCallback {
    private Context mContext;
    private NoteAdapter mAdapter;
    private Presenter mPresenter;

    private Drawable mIconLeft;
    private ColorDrawable mBackgroundLeft;

    private Drawable mIconRight;
    private ColorDrawable mBackgroundRight;

    private Drawable icon;
    private Drawable background;

    private final int ICON_MARGIN;
    private final int ICON_MARGIN_BORDER;

    public SwipeActionCallback(Context context, NoteAdapter adapter, Presenter presenter) {
        super(0,0);
        mContext = context;
        mAdapter = adapter;
        mPresenter = presenter;
        ICON_MARGIN = DisplayUtil.dpToPx(context, 16);
        ICON_MARGIN_BORDER = DisplayUtil.dpToPx(context, 40);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        if (mPresenter != null) {
            int leftAction = 0;
            if (mPresenter.hasSwipeLeftAction()) {
                leftAction = LEFT;
                mIconLeft = getSwipeActionIcon(mPresenter.getSwipeLeftAction());
                mBackgroundLeft = getSwipeActionBackground(mPresenter.getSwipeLeftAction());
            }

            int rightAction = 0;
            if (mPresenter.hasSwipeRightAction()) {
                rightAction = RIGHT;
                mIconRight = getSwipeActionIcon(mPresenter.getSwipeRightAction());
                mBackgroundRight = getSwipeActionBackground(mPresenter.getSwipeRightAction());
            }
            return leftAction | rightAction;
        }
        return 0;
    }

    private Drawable getSwipeActionIcon(int action) {
        switch (action) {
            case AppSettingsRepository.SWIPE_ACTION_TRASH_RESTORE:
                int ic = mPresenter.isTrash() ? R.drawable.ic_restore_white_24dp : R.drawable.ic_delete_white_24dp;
                return ContextCompat.getDrawable(mContext, ic);

            case AppSettingsRepository.SWIPE_ACTION_PIN:
                return ContextCompat.getDrawable(mContext, R.drawable.ic_bookmark_white_24dp);

                case AppSettingsRepository.SWIPE_ACTION_STAR:
                    return ContextCompat.getDrawable(mContext, R.drawable.ic_star_white_24dp);
        }
        return null;
    }

    private ColorDrawable getSwipeActionBackground(int action) {
        switch (action) {
            case AppSettingsRepository.SWIPE_ACTION_TRASH_RESTORE:
                return new ColorDrawable(ContextCompat.getColor(mContext, R.color.swipe_action_trash));

            case AppSettingsRepository.SWIPE_ACTION_PIN:
                return new ColorDrawable(ContextCompat.getColor(mContext, R.color.swipe_action_pin));

            case AppSettingsRepository.SWIPE_ACTION_STAR:
                return new ColorDrawable(ContextCompat.getColor(mContext, R.color.swipe_action_star));
        }
        return null;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int pos = viewHolder.getAdapterPosition();
        if (mPresenter != null) {
            switch (direction) {
                case LEFT:
                    mPresenter.swipeLeft(mAdapter.getItem(pos));
                    break;

                case RIGHT:
                    mPresenter.swipeRight(mAdapter.getItem(pos));
                    break;
            }
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        View itemView = viewHolder.itemView;

        if (dX > 0) { // Swiping to the right
            icon = mIconRight;
            background = mBackgroundRight;

            background.setBounds(itemView.getLeft(), itemView.getTop(),
                    itemView.getLeft() + ((int) dX),
                    itemView.getBottom());
            background.draw(c);
        } else if (dX < 0) { // Swiping to the left
            icon = mIconLeft;
            background = mBackgroundLeft;

            background.setBounds(itemView.getRight() + ((int) dX), itemView.getTop(), itemView.getRight(), itemView.getBottom());
            background.draw(c);
        } else if (background != null) { // view is unSwiped
            background.setBounds(0, 0, 0, 0);
        } else {
            return;
        }
        background.draw(c);

        // setup icon
        int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + icon.getIntrinsicHeight();

        if (dX > ICON_MARGIN_BORDER) { // Swiping to the right
            int iconLeft = itemView.getLeft() + ICON_MARGIN;
            int iconRight = iconLeft + icon.getIntrinsicWidth();
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
        } else if (dX < -ICON_MARGIN_BORDER) { // Swiping to the left
            int iconLeft = itemView.getRight() - ICON_MARGIN - icon.getIntrinsicWidth();
            int iconRight = itemView.getRight() - ICON_MARGIN;
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
        } else { // view is unSwiped
            icon.setBounds(0, 0, 0, 0);
            background.setBounds(0, 0, 0, 0);
        }
        icon.draw(c);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

}
