package com.axel_stein.noteapp.dialogs.notebook;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.utils.ResourceUtil;
import com.axel_stein.noteapp.utils.ViewUtil;
import com.axel_stein.noteapp.views.IconTextView;

import java.util.ArrayList;
import java.util.List;

import static com.axel_stein.domain.model.Notebook.ID_ADD;
import static com.axel_stein.domain.utils.TextUtil.notNullString;
import static com.axel_stein.noteapp.utils.ObjectUtil.checkNotNull;

@SuppressWarnings("SameParameterValue")
public class CheckNotebookDialog extends AppCompatDialogFragment {
    private String mTitle;
    private int mTitleRes;
    private String mNegativeButtonText;
    private int mNegativeButtonTextRes;
    private List<Notebook> mNotebooks;
    private String mSelectedNotebookId;
    private OnNotebookCheckedListener mListener;

    public static void launch(Fragment fragment, List<Notebook> notebooks, String selectedNotebook) {
        checkNotNull(fragment);
        checkNotNull(fragment.getContext());

        CheckNotebookDialog dialog = new CheckNotebookDialog();
        dialog.setTitle(R.string.title_select_notebook);
        dialog.setNegativeButtonText(R.string.action_cancel);
        dialog.setNotebooks(fragment.getContext(), notebooks, selectedNotebook);
        dialog.setTargetFragment(fragment, 0);
        if (fragment.getFragmentManager() != null) {
            dialog.show(fragment.getFragmentManager(), null);
        }
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    private void setTitle(int titleRes) {
        mTitleRes = titleRes;
    }

    public void setNegativeButtonText(String negativeButtonText) {
        mNegativeButtonText = negativeButtonText;
    }

    private void setNegativeButtonText(int negativeButtonTextRes) {
        mNegativeButtonTextRes = negativeButtonTextRes;
    }

    private void setNotebooks(Context context, List<Notebook> notebooks, String selectedNotebookId) {
        checkNotNull(notebooks);

        mSelectedNotebookId = selectedNotebookId;

        mNotebooks = new ArrayList<>(notebooks);
        mNotebooks.add(0, createInboxItem(context));
        mNotebooks.add(createAddItem(context));
    }

    private Notebook createInboxItem(Context context) {
        Notebook item = new Notebook();
        item.setId(Notebook.ID_INBOX);
        item.setTitle(context.getString(R.string.action_inbox));
        item.setIconRes(R.drawable.ic_inbox_24dp);
        return item;
    }

    private Notebook createAddItem(Context context) {
        Notebook item = new Notebook();
        item.setId(ID_ADD);
        item.setTitle(context.getString(R.string.action_add_notebook));
        item.setIconRes(R.drawable.ic_add_box_24dp);
        return item;
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
        } else if (fragment instanceof OnNotebookCheckedListener) {
            mListener = (OnNotebookCheckedListener) fragment;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        checkNotNull(getContext());

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

    @SuppressLint("InflateParams")
    private View createView() {
        Adapter adapter = new Adapter(new OnNotebookCheckedListener() {
            @Override
            public void onNotebookChecked(Notebook notebook) {
                String id = notNullString(notebook.getId());
                if (id.equals(ID_ADD)) {
                    dismiss();
                    AddNotebookDialog.launch(getActivity());
                    return;
                }
                if (id.equals(mSelectedNotebookId)) {
                    dismiss();
                    return;
                }
                if (id.equals(Notebook.ID_INBOX)) {
                    notebook = null;
                }
                mListener.onNotebookChecked(notebook);
                dismiss();
            }
        });
        adapter.setItems(mNotebooks, mSelectedNotebookId);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        RecyclerView view = (RecyclerView) inflater.inflate(R.layout.dialog_recycler_view, null);
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
        private String mSelectedNotebookId;
        private final OnNotebookCheckedListener mListener;

        Adapter(OnNotebookCheckedListener l) {
            mListener = l;
        }

        void setItems(List<Notebook> items, String selectedNotebookId) {
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

        @NonNull
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

            int icon = notebook.getIconRes();
            holder.mTitle.setIconLeft(icon != 0 ? icon : R.drawable.ic_book_24dp);

            if (notebook.getId().equals(ID_ADD)) {
                ViewUtil.hide(holder.mChecked);
            } else {
                ViewUtil.show(holder.mChecked);

                boolean selected = TextUtils.equals(mSelectedNotebookId, notebook.getId());
                holder.mChecked.setImageResource(selected ? R.drawable.ic_radio_button_checked_24dp : R.drawable.ic_radio_button_unchecked_24dp);
                holder.mChecked.setSelected(selected);
            }
        }

        @Override
        public int getItemCount() {
            return mItems == null ? 0 : mItems.size();
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private final IconTextView mTitle;
        private final ImageView mChecked;

        ViewHolder(View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.text_title);
            mChecked = itemView.findViewById(R.id.img_checked);
        }

    }

}
