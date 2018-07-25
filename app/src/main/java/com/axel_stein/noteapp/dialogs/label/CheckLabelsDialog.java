package com.axel_stein.noteapp.dialogs.label;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.LongSparseArray;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.axel_stein.domain.model.Label;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.utils.ResourceUtil;
import com.axel_stein.noteapp.views.IconTextView;

import java.util.ArrayList;
import java.util.List;

import static com.axel_stein.noteapp.utils.BooleanUtil.isTrue;
import static com.axel_stein.noteapp.utils.ObjectUtil.checkNotNull;

public class CheckLabelsDialog extends AppCompatDialogFragment {
    private String mTitle;
    private int mTitleRes;
    private String mPositiveButtonText;
    private int mPositiveButtonTextRes;
    private String mNegativeButtonText;
    private int mNegativeButtonTextRes;
    private List<Label> mItems;
    private boolean[] mCheckedPositions;
    private OnLabelCheckedListener mListener;
    private Adapter mAdapter;

    public static void launch(Fragment fragment, List<Label> labels, List<Long> checkedLabelIds) {
        CheckLabelsDialog dialog = new CheckLabelsDialog();
        dialog.setTitle(R.string.title_check_labels);
        dialog.setPositiveButtonText(R.string.action_ok);
        dialog.setNegativeButtonText(R.string.action_cancel);
        dialog.setLabels(labels, checkedLabelIds);
        dialog.setTargetFragment(fragment, 0);
        dialog.show(fragment.getFragmentManager(), null);
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setTitle(int titleRes) {
        mTitleRes = titleRes;
    }

    public void setPositiveButtonText(String positiveButtonText) {
        mPositiveButtonText = positiveButtonText;
    }

    public void setPositiveButtonText(int positiveButtonTextRes) {
        mPositiveButtonTextRes = positiveButtonTextRes;
    }

    public void setNegativeButtonText(String negativeButtonText) {
        mNegativeButtonText = negativeButtonText;
    }

    public void setNegativeButtonText(int negativeButtonTextRes) {
        mNegativeButtonTextRes = negativeButtonTextRes;
    }

    public void setLabels(List<Label> labels, List<Long> checkedLabelIds) {
        mItems = checkNotNull(labels);
        mCheckedPositions = new boolean[labels.size()];

        LongSparseArray<Boolean> map = new LongSparseArray<>();
        if (checkedLabelIds != null) {
            for (long id : checkedLabelIds) {
                map.put(id, true);
            }
        }

        for (int i = 0; i < labels.size(); i++) {
            Label label = labels.get(i);
            if (checkedLabelIds != null) {
                long id = label.getId();
                mCheckedPositions[i] = isTrue(map.get(id));
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDestroyView() {
        mAdapter = null;
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = getActivity();
        Fragment fragment = getTargetFragment();

        if (activity instanceof OnLabelCheckedListener) {
            mListener = (OnLabelCheckedListener) activity;
        } else if (fragment != null && fragment instanceof OnLabelCheckedListener) {
            mListener = (OnLabelCheckedListener) fragment;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getResourceText(mTitle, mTitleRes));
        builder.setPositiveButton(getResourceText(mPositiveButtonText, mPositiveButtonTextRes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (mListener != null && mAdapter != null) {
                    mListener.onLabelsChecked(mAdapter.getCheckedLabelIds());
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getResourceText(mNegativeButtonText, mNegativeButtonTextRes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setView(createView());
        return builder.create();
    }

    @SuppressLint("InflateParams")
    private View createView() {
        mAdapter = new Adapter();
        mAdapter.setItems(mItems, mCheckedPositions);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        RecyclerView view = (RecyclerView) inflater.inflate(R.layout.dialog_recycler_view, null);
        view.setAdapter(mAdapter);
        return view;
    }

    private String getResourceText(String s, int res) {
        return ResourceUtil.getString(getContext(), s, res);
    }

    public interface OnLabelCheckedListener {
        void onLabelsChecked(List<Long> labels);
    }

    private static class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private List<Label> mItems;
        private boolean[] mCheckedPositions;

        public void setItems(List<Label> items, boolean[] checkedPositions) {
            this.mItems = items;
            this.mCheckedPositions = checkedPositions;
            notifyDataSetChanged();
        }

        public List<Long> getCheckedLabelIds() {
            List<Long> result = new ArrayList<>();
            for (int i = 0; i < mCheckedPositions.length; i++) {
                if (mCheckedPositions[i]) {
                    result.add(mItems.get(i).getId());
                }
            }
            return result;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_dialog_label, parent, false);
            final ViewHolder holder = new ViewHolder(view);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getAdapterPosition();
                    if (pos >= 0 && pos < getItemCount()) {
                        mCheckedPositions[pos] = !mCheckedPositions[pos];
                        notifyItemChanged(pos);
                    }
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Label label = mItems.get(position);
            holder.mTitle.setText(label.getTitle());

            boolean checked = mCheckedPositions[position];
            int icon = checked ? R.drawable.ic_check_box_white_24dp : R.drawable.ic_check_box_outline_blank_white_24dp;

            holder.mChecked.setImageResource(icon);
            holder.mChecked.setSelected(checked);
        }

        @Override
        public int getItemCount() {
            return mItems == null ? 0 : mItems.size();
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private IconTextView mTitle;
        private ImageView mChecked;

        ViewHolder(View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.text_title);
            mChecked = itemView.findViewById(R.id.img_checked);
        }

    }
}
