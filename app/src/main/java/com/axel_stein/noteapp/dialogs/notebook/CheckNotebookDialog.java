package com.axel_stein.noteapp.dialogs.notebook;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.utils.ResourceUtil;
import com.axel_stein.noteapp.views.IconTextView;

import java.util.ArrayList;
import java.util.List;

import static com.axel_stein.noteapp.utils.ObjectUtil.checkNotNull;

public class CheckNotebookDialog extends AppCompatDialogFragment {
    private String mTitle;
    private int mTitleRes;
    private String mNegativeButtonText;
    private int mNegativeButtonTextRes;
    private List<Notebook> mNotebooks;
    private long mSelectedNotebookId;
    private OnNotebookCheckedListener mListener;

    public static void launch(Fragment fragment, List<Notebook> notebooks, long selectedNotebook) {
        CheckNotebookDialog dialog = new CheckNotebookDialog();
        dialog.setTitle(R.string.title_select_notebook);
        dialog.setNegativeButtonText(R.string.action_cancel);
        dialog.setNotebooks(fragment.getContext(), notebooks, selectedNotebook);
        dialog.setTargetFragment(fragment, 0);
        dialog.show(fragment.getFragmentManager(), null);
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setTitle(int titleRes) {
        mTitleRes = titleRes;
    }

    public void setNegativeButtonText(String negativeButtonText) {
        mNegativeButtonText = negativeButtonText;
    }

    public void setNegativeButtonText(int negativeButtonTextRes) {
        mNegativeButtonTextRes = negativeButtonTextRes;
    }

    public void setNotebooks(Context context, List<Notebook> notebooks, long selectedNotebookId) {
        checkNotNull(notebooks);

        mNotebooks = new ArrayList<>(notebooks);
        mNotebooks.add(0, Notebook.from(context.getString(R.string.action_inbox)));

        this.mSelectedNotebookId = selectedNotebookId;
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

        if (activity instanceof OnNotebookCheckedListener) {
            mListener = (OnNotebookCheckedListener) activity;
        } else if (fragment != null && fragment instanceof OnNotebookCheckedListener) {
            mListener = (OnNotebookCheckedListener) fragment;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getResourceText(mTitle, mTitleRes));
        builder.setNegativeButton(getResourceText(mNegativeButtonText, mNegativeButtonTextRes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setView(createView());
        return builder.create();
    }

    private View createView() {
        Adapter adapter = new Adapter(new OnNotebookCheckedListener() {
            @Override
            public void onNotebookChecked(Notebook notebook) {
                if (notebook.getId() == 0) { // this is Inbox
                    notebook = null;
                }
                mListener.onNotebookChecked(notebook);
                dismiss();
            }
        });
        adapter.setItems(mNotebooks, mSelectedNotebookId);

        RecyclerView view = new RecyclerView(getContext());
        view.setLayoutManager(new LinearLayoutManager(getContext()));
        view.setAdapter(adapter);
        return view;
    }

    private String getResourceText(String s, int res) {
        return ResourceUtil.getString(getContext(), s, res);
    }

    public interface OnNotebookCheckedListener {
        void onNotebookChecked(Notebook notebook);
    }

    private static class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private List<Notebook> mItems;
        private long mSelectedNotebookId;
        private OnNotebookCheckedListener mListener;

        Adapter(OnNotebookCheckedListener l) {
            mListener = l;
        }

        public void setItems(List<Notebook> items, long selectedNotebookId) {
            this.mItems = items;
            this.mSelectedNotebookId = selectedNotebookId;
            notifyDataSetChanged();
        }

        private Notebook getItem(int pos) {
            if (mItems != null && pos >= 0 && pos < mItems.size()) {
                return mItems.get(pos);
            }
            return null;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_dialog_notebook, parent, false);
            final ViewHolder holder = new ViewHolder(view);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getAdapterPosition();
                    Notebook notebook = getItem(pos);
                    if (notebook != null && mListener != null) {
                        mListener.onNotebookChecked(notebook);
                    }
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Notebook notebook = mItems.get(position);
            holder.mTitle.setText(notebook.getTitle());
            holder.mTitle.setIconLeft(notebook.getId() == 0 ? R.drawable.ic_inbox_white_24dp : R.drawable.ic_book_white_24dp);

            boolean selected = mSelectedNotebookId == notebook.getId();
            int icon = selected ? R.drawable.ic_radio_button_checked_white_24dp : R.drawable.ic_radio_button_unchecked_white_24dp;

            holder.mChecked.setImageResource(icon);
            holder.mChecked.setSelected(selected);
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
