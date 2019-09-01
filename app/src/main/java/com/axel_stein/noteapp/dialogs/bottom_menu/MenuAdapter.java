package com.axel_stein.noteapp.dialogs.bottom_menu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.views.IconTextView;

import java.util.List;

class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {
    private final LayoutInflater mInflater;
    private List<MenuItem> mItems;
    private OnItemClickListener mOnItemClickListener;

    MenuAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    void setOnItemClickListener(OnItemClickListener l) {
        mOnItemClickListener = l;
    }

    void setItems(List<MenuItem> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        MenuItem item = mItems.get(position);
        if (item.getItemId() == 0) {
            return -1;
        }
        return 0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final ViewHolder holder;
        if (viewType == -1) {
            holder = new DividerViewHolder(mInflater.inflate(R.layout.item_divider, parent, false));
        } else {
            holder = new ItemViewHolder(mInflater.inflate(R.layout.item_bottom_menu, parent, false));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = holder.getAdapterPosition();
                    MenuItem item = mItems.get(pos);
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(item);
                    }
                }
            });
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MenuItem item = mItems.get(position);
        if (getItemViewType(position) == 0) {
            ItemViewHolder itemVH = (ItemViewHolder) holder;
            itemVH.mTextView.setText(item.getTitle());

            if (item.isCheckable()) {
                int icon = item.isChecked() ? R.drawable.ic_done_white_24dp : 0;
                itemVH.mTextView.setIconRight(icon);
            } else {
                itemVH.mTextView.setIconRight(null);
            }

            itemVH.mTextView.setIconLeft(item.getIcon());
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
        void onItemClick(MenuItem item);
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

    static class DividerViewHolder extends ViewHolder {

        DividerViewHolder(View view) {
            super(view);
        }
    }

}
