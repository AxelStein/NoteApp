package com.axel_stein.noteapp.dialogs.bottom_menu;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.utils.MenuUtil;

public class BottomMenuDialog extends BottomSheetDialogFragment {
    private int mMenuRes;
    private SparseBooleanArray mItemsVisibility;
    private OnMenuItemClickListener mListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Fragment fragment = getTargetFragment();
        if (fragment != null) {
            setListeners(fragment);
        } else {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                setListeners(activity);
            }
        }
    }

    private void setListeners(Object o) {
        if (o instanceof OnMenuItemClickListener) {
            mListener = (OnMenuItemClickListener) o;
        }
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        mListener = null;
        super.onDestroyView();
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        RecyclerView view = (RecyclerView) inflater.inflate(R.layout.dialog_bottom_menu, null);

        Menu menu = MenuUtil.inflateMenuFromResource(view, mMenuRes);
        if (mItemsVisibility != null) {
            for (int i = 0; i < mItemsVisibility.size(); i++) {
                int id = mItemsVisibility.keyAt(i);
                MenuUtil.show(menu, mItemsVisibility.valueAt(i), id);
            }
        }

        MenuAdapter adapter = new MenuAdapter(getContext());
        adapter.setItems(MenuUtil.getVisibleMenuItems(menu));
        adapter.setOnItemClickListener(new MenuAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MenuItem item) {
                if (mListener != null) {
                    mListener.onMenuItemClick(BottomMenuDialog.this, item);
                }
            }
        });

        GridLayoutManager layoutManager = (GridLayoutManager) view.getLayoutManager();
        if (adapter.getItemCount() <= 4) {
            layoutManager.setSpanCount(1);
        }

        view.setAdapter(adapter);
        view.setHasFixedSize(true);

        BottomSheetDialog dialog = new BottomSheetDialog(getContext(), getTheme());
        dialog.setContentView(view);
        return dialog;
    }

    public interface OnMenuItemClickListener {
        void onMenuItemClick(BottomMenuDialog dialog, MenuItem item);
    }

    public static class Builder {
        private int mMenuRes;
        private SparseBooleanArray mItemsVisibility;

        public Builder setMenuRes(int menuRes) {
            mMenuRes = menuRes;
            return this;
        }

        public Builder showMenuItem(int itemId, boolean show) {
            if (mItemsVisibility == null) {
                mItemsVisibility = new SparseBooleanArray();
            }
            mItemsVisibility.put(itemId, show);
            return this;
        }

        public void show(Fragment fragment) {
            BottomMenuDialog dialog = new BottomMenuDialog();
            dialog.mMenuRes = mMenuRes;
            dialog.mItemsVisibility = mItemsVisibility;
            dialog.setTargetFragment(fragment, 0);
            dialog.show(fragment.getFragmentManager(), null);
        }
    }

}
