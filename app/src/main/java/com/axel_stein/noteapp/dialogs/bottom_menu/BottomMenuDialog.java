package com.axel_stein.noteapp.dialogs.bottom_menu;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.utils.MenuUtil;
import com.axel_stein.noteapp.utils.ViewUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomMenuDialog extends BottomSheetDialogFragment {
    private String mTitle;
    private int mMenuRes;
    private Object mData;
    private SparseBooleanArray mCheckedItems;
    private SparseBooleanArray mItemsVisibility;
    private OnMenuItemClickListener mListener;

    public Object getData() {
        return mData;
    }

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

    @SuppressWarnings("ConstantConditions")
    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        View view = inflater.inflate(R.layout.dialog_bottom_menu, null);

        View layoutTitle = view.findViewById(R.id.layout_title);
        ViewUtil.show(!TextUtils.isEmpty(mTitle), layoutTitle);

        TextView textTitle = view.findViewById(R.id.text_title);
        textTitle.setText(mTitle);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Menu menu = MenuUtil.inflateMenuFromResource(recyclerView, mMenuRes);
        if (mItemsVisibility != null) {
            for (int i = 0; i < mItemsVisibility.size(); i++) {
                int id = mItemsVisibility.keyAt(i);
                boolean visible = mItemsVisibility.valueAt(i);
                MenuUtil.show(menu, visible, id);
            }
        }

        if (mCheckedItems != null) {
            for (int i = 0; i < mCheckedItems.size(); i++) {
                int id = mCheckedItems.keyAt(i);
                boolean checked = mCheckedItems.valueAt(i);
                MenuUtil.check(menu, id, checked);
            }
        }

        MenuAdapter adapter = new MenuAdapter(getContext());
        adapter.setItems(MenuUtil.getVisibleMenuItems(menu));
        adapter.setOnItemClickListener(new MenuAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MenuItem item) {
                if (mListener != null) {
                    mListener.onMenuItemClick(BottomMenuDialog.this, getTag(), item);
                }
            }
        });

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager grid = (GridLayoutManager) layoutManager;
            if (adapter.getItemCount() <= 4) {
                grid.setSpanCount(1);
            }
        }

        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);

        BottomSheetDialog dialog = new BottomSheetDialog(getContext(), getTheme());
        dialog.setContentView(view);
        return dialog;
    }

    public interface OnMenuItemClickListener {
        void onMenuItemClick(BottomMenuDialog dialog, String tag, MenuItem item);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        private String mTitle;
        private int mMenuRes;
        private Object mData;
        private SparseBooleanArray mCheckedItems;
        private SparseBooleanArray mItemsVisibility;

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setMenuRes(int menuRes) {
            mMenuRes = menuRes;
            return this;
        }

        public Builder setData(Object data) {
            mData = data;
            return this;
        }

        public Builder showMenuItem(int itemId, boolean show) {
            if (mItemsVisibility == null) {
                mItemsVisibility = new SparseBooleanArray();
            }
            mItemsVisibility.put(itemId, show);
            return this;
        }

        public Builder addChecked(int itemId) {
            return addChecked(itemId, true);
        }

        @SuppressWarnings("SameParameterValue")
        Builder addChecked(int itemId, boolean checked) {
            if (mCheckedItems == null) {
                mCheckedItems = new SparseBooleanArray();
            }
            mCheckedItems.put(itemId, checked);
            return this;
        }

        public void show(Fragment fragment, String tag) {
            BottomMenuDialog dialog = new BottomMenuDialog();
            dialog.mTitle = mTitle;
            dialog.mMenuRes = mMenuRes;
            dialog.mData = mData;
            dialog.mCheckedItems = mCheckedItems;
            dialog.mItemsVisibility = mItemsVisibility;
            dialog.setTargetFragment(fragment, 0);
            assert fragment.getFragmentManager() != null;
            dialog.show(fragment.getFragmentManager(), tag);
        }

        public void show(FragmentManager manager, String tag) {
            BottomMenuDialog dialog = new BottomMenuDialog();
            dialog.mTitle = mTitle;
            dialog.mMenuRes = mMenuRes;
            dialog.mData = mData;
            dialog.mCheckedItems = mCheckedItems;
            dialog.mItemsVisibility = mItemsVisibility;
            dialog.show(manager, tag);
        }

    }

}
