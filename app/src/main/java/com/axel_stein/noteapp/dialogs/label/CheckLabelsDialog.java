package com.axel_stein.noteapp.dialogs.label;

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

import com.axel_stein.domain.model.Label;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.utils.ResourceUtil;

import java.util.ArrayList;
import java.util.List;

import static com.axel_stein.noteapp.utils.ObjectUtil.checkNotNull;
import static com.axel_stein.noteapp.utils.BooleanUtil.isTrue;

public class CheckLabelsDialog extends AppCompatDialogFragment {

    private String mTitle;
    private int mTitleRes;
    private String mPositiveButtonText;
    private int mPositiveButtonTextRes;
    private String mNegativeButtonText;
    private int mNegativeButtonTextRes;
    private List<Label> mLabels;
    private String[] mLabelTitles;
    private boolean[] mCheckedPositions;
    private OnLabelCheckedListener mListener;

    public static void launch(Fragment fragment, List<Label> labels, List<Long> checkedLabels) {
        CheckLabelsDialog dialog = new CheckLabelsDialog();
        dialog.setTitle(R.string.title_check_labels);
        dialog.setPositiveButtonText(R.string.action_ok);
        dialog.setNegativeButtonText(R.string.action_cancel);
        dialog.setLabels(labels, checkedLabels);
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

    public void setLabels(List<Label> labels, List<Long> checkedLabels) {
        mLabels = checkNotNull(labels);

        mLabelTitles = new String[labels.size()];
        mCheckedPositions = new boolean[labels.size()];

        LongSparseArray<Boolean> map = new LongSparseArray<>();
        if (checkedLabels != null) {
            for (long l : checkedLabels) {
                map.put(l, true);
            }
        }

        for (int i = 0; i < labels.size(); i++) {
            Label label = labels.get(i);
            mLabelTitles[i] = label.getTitle();
            if (checkedLabels != null) {
                mCheckedPositions[i] = isTrue(map.get(label.getId()));
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
            public void onClick(DialogInterface dialog, int which) {
                if (mListener != null) {
                    List<Long> result = new ArrayList<>();

                    for (int i = 0; i < mCheckedPositions.length; i++) {
                        if (mCheckedPositions[i]) {
                            result.add(mLabels.get(i).getId());
                        }
                    }

                    mListener.onLabelChecked(result);
                }
            }
        });
        builder.setNegativeButton(getResourceText(mNegativeButtonText, mNegativeButtonTextRes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        if (mLabelTitles != null) {
            builder.setMultiChoiceItems(mLabelTitles, mCheckedPositions, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                    mCheckedPositions[i] = b;
                }
            });
        }

        return builder.create();
    }

    private String getResourceText(String s, int res) {
        return ResourceUtil.getString(getContext(), s, res);
    }

    public interface OnLabelCheckedListener {
        void onLabelChecked(List<Long> labels);
    }

}
