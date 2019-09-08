package com.axel_stein.noteapp.dialogs.select_notebook;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
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

import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.utils.ViewUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import static com.axel_stein.domain.utils.TextUtil.notEmpty;

public class SelectNotebookDialog extends BottomSheetDialogFragment {
    private String mTitle;
    private String mAction;
    private List<Notebook> mItems;
    private String mSelectedNotebookId;
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

    @SuppressWarnings("ConstantConditions")
    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        View view = inflater.inflate(R.layout.dialog_select_notebook, null);

        View layoutTitle = view.findViewById(R.id.layout_title);
        ViewUtil.setVisible(!TextUtils.isEmpty(mTitle), layoutTitle);

        TextView textTitle = view.findViewById(R.id.text_title);
        textTitle.setText(mTitle);

        if (notEmpty(mAction)) {
            TextView btnAction = view.findViewById(R.id.btn_action);
            btnAction.setText(mAction);
            btnAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onActionClick(SelectNotebookDialog.this);
                    }
                }
            });
            ViewUtil.show(btnAction);
        }

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Adapter adapter = new Adapter(getContext());
        adapter.setItems(mItems);
        adapter.setSelectedNotebookId(mSelectedNotebookId);
        adapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(Notebook notebook) {
                if (mListener != null) {
                    mListener.onMenuItemClick(SelectNotebookDialog.this, getTag(), notebook);
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
        void onMenuItemClick(SelectNotebookDialog dialog, String tag, Notebook notebook);
        void onActionClick(SelectNotebookDialog dialog);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        private String mTitle;
        private String mAction;
        private String mSelectedNotebookId;
        private List<Notebook> mItems;

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public void setAction(String action) {
            this.mAction = action;
        }

        public Builder addItem(Notebook item) {
            if (mItems == null) {
                mItems = new ArrayList<>();
            }
            mItems.add(item);
            return this;
        }

        public Builder setItems(List<Notebook> items) {
            mItems = items;
            return this;
        }

        public void setSelectedNotebookId(String id) {
            this.mSelectedNotebookId = id;
        }

        public void show(Fragment fragment, String tag) {
            SelectNotebookDialog dialog = setupDialog();
            dialog.setTargetFragment(fragment, 0);
            assert fragment.getFragmentManager() != null;
            dialog.show(fragment.getFragmentManager(), tag);
        }

        public void show(FragmentManager manager, String tag) {
            setupDialog().show(manager, tag);
        }

        private SelectNotebookDialog setupDialog() {
            SelectNotebookDialog dialog = new SelectNotebookDialog();
            dialog.mAction = mAction;
            dialog.mTitle = mTitle;
            dialog.mSelectedNotebookId = mSelectedNotebookId;
            dialog.mItems = mItems;
            return dialog;
        }

    }
}