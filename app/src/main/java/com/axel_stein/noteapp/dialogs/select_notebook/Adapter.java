package com.axel_stein.noteapp.dialogs.select_notebook;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.views.IconTextView;

import java.util.List;

import static com.axel_stein.domain.utils.TextUtil.notEmpty;

class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    private final LayoutInflater mInflater;
    private List<Notebook> mItems;
    private String mSelectedNotebookId;
    private OnItemClickListener mOnItemClickListener;

    Adapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    void setOnItemClickListener(OnItemClickListener l) {
        mOnItemClickListener = l;
    }

    void setItems(List<Notebook> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    void setSelectedNotebookId(String id) {
        mSelectedNotebookId = id;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final ViewHolder holder = new ItemViewHolder(mInflater.inflate(R.layout.item_select_notebook, parent, false));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = holder.getAdapterPosition();
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(mItems.get(pos));
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notebook notebook = mItems.get(position);
        ItemViewHolder itemVH = (ItemViewHolder) holder;
        itemVH.mTextView.setText(notebook.getTitle());

        if (notEmpty(mSelectedNotebookId)) {
            int icon = R.drawable.ic_radio_button_unchecked_24dp;
            if (TextUtils.equals(notebook.getId(), mSelectedNotebookId)) {
                icon = R.drawable.ic_radio_button_checked_24dp;
            }
            itemVH.mTextView.setIconLeft(icon);
        }
    }

    @Override
    public int getItemCount() {
        if (mItems == null) {
            return 0;
        }
        return mItems.size();
    }

    interface OnItemClickListener {
        void onItemClick(Notebook notebook);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(View view) {
            super(view);

        }
    }

    static class ItemViewHolder extends ViewHolder {
        private final IconTextView mTextView;

        ItemViewHolder(View view) {
            super(view);
            mTextView = (IconTextView) view;
        }
    }

}
