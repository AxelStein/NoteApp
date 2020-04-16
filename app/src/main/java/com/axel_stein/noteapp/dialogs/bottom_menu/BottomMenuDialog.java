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
    private static final String BUNDLE_TITLE = "BUNDLE_TITLE";
    private static final String BUNDLE_MENU_RES = "BUNDLE_MENU_RES";
    private static final String BUNDLE_CHECKED_ITEM_ID = "BUNDLE_CHECKED_ITEM_ID";
    private static final String BUNDLE_SPARSE_IDS = "BUNDLE_SPARSE_IDS";
    private static final String BUNDLE_SPARSE_VALUES = "BUNDLE_SPARSE_VALUES";

    private String mTitle;
    private int mMenuRes;
    private int mCheckedItemId;
    private OnMenuItemClickListener mListener;
    private SparseBooleanArray mItemsVisibility;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
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
        if (savedInstanceState != null) {
            mTitle = savedInstanceState.getString(BUNDLE_TITLE);
            mMenuRes = savedInstanceState.getInt(BUNDLE_MENU_RES);
            mCheckedItemId = savedInstanceState.getInt(BUNDLE_CHECKED_ITEM_ID);

            mItemsVisibility = new SparseBooleanArray();
            int[] ids = savedInstanceState.getIntArray(BUNDLE_SPARSE_IDS);
            boolean[] values = savedInstanceState.getBooleanArray(BUNDLE_SPARSE_VALUES);
            if (ids != null && values != null && ids.length == values.length) {
                for (int i = 0; i < ids.length; i++) {
                    mItemsVisibility.append(ids[i], values[i]);
                }
            }
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());

        View view = inflater.inflate(R.layout.dialog_bottom_menu, null);

        View viewPadding = view.findViewById(R.id.view_padding);
        View layoutTitle = view.findViewById(R.id.layout_title);
        ViewUtil.setVisible(!TextUtils.isEmpty(mTitle), layoutTitle);
        ViewUtil.setVisible(TextUtils.isEmpty(mTitle), viewPadding);

        TextView textTitle = view.findViewById(R.id.text_title);
        textTitle.setText(mTitle);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Menu menu = MenuUtil.inflateMenuFromResource(recyclerView, mMenuRes);
        MenuUtil.check(menu, mCheckedItemId, true);
        if (mItemsVisibility != null) {
            for (int i = 0; i < mItemsVisibility.size(); i++) {
                MenuUtil.show(menu, mItemsVisibility.valueAt(i), mItemsVisibility.keyAt(i));
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BUNDLE_MENU_RES, mMenuRes);
        outState.putString(BUNDLE_TITLE, mTitle);
        outState.putInt(BUNDLE_CHECKED_ITEM_ID, mCheckedItemId);

        if (mItemsVisibility != null) {
            int size = mItemsVisibility.size();
            int[] ids = new int[size];
            boolean[] values = new boolean[size];
            for (int i = 0; i < size; i++) {
                ids[i] = mItemsVisibility.keyAt(i);
                values[i] = mItemsVisibility.valueAt(i);
            }
            outState.putIntArray(BUNDLE_SPARSE_IDS, ids);
            outState.putBooleanArray(BUNDLE_SPARSE_VALUES, values);
        }
    }

    public interface OnMenuItemClickListener {
        void onMenuItemClick(BottomMenuDialog dialog, String tag, MenuItem item);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        private String mTitle;
        private int mMenuRes;
        private int mCheckedItemId;
        private SparseBooleanArray mItemsVisibility;

        public Builder() {
            mItemsVisibility = new SparseBooleanArray();
        }

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setMenuRes(int menuRes) {
            mMenuRes = menuRes;
            return this;
        }

        public void setCheckedItemId(int itemId) {
            this.mCheckedItemId = itemId;
        }

        public void show(Fragment fragment, String tag) {
            BottomMenuDialog dialog = new BottomMenuDialog();
            dialog.mTitle = mTitle;
            dialog.mMenuRes = mMenuRes;
            dialog.mCheckedItemId = mCheckedItemId;
            dialog.mItemsVisibility = mItemsVisibility;
            dialog.setTargetFragment(fragment, 0);
            assert fragment.getFragmentManager() != null;
            dialog.show(fragment.getFragmentManager(), tag);
        }

        public void show(FragmentManager manager, String tag) {
            BottomMenuDialog dialog = new BottomMenuDialog();
            dialog.mTitle = mTitle;
            dialog.mMenuRes = mMenuRes;
            dialog.mCheckedItemId = mCheckedItemId;
            dialog.mItemsVisibility = mItemsVisibility;
            dialog.show(manager, tag);
        }

        public void setMenuItemVisible(int itemId, boolean visible) {
            mItemsVisibility.append(itemId, visible);
        }
    }

}
