package com.axel_stein.noteapp.dialogs.bottom_menu;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.views.IconTextView;

import java.util.List;

class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {
    private LayoutInflater mInflater;
    private List<MenuItem> mItems;
    private OnItemClickListener mOnItemClickListener;

    /*
    @Nullable
    private HashMap<Integer, String> mComments;
    */

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

    /*
    public void setComments(@Nullable HashMap<Integer, String> comments) {
        mComments = comments;
        notifyDataSetChanged();
    }
    */

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final ViewHolder holder = new ViewHolder(mInflater.inflate(R.layout.item_bottom_menu, parent, false));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getAdapterPosition();
                MenuItem item = mItems.get(pos);
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(item);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MenuItem item = mItems.get(position);
        holder.mTextView.setText(item.getTitle());
        /*
        int id = item.getItemId();
        if (mComments != null && mComments.containsKey(id)) {
            holder.mTextView.setText(String.format("%s  ‣  %s", item.getTitle(), mComments.get(id)));
        } else {
            holder.mTextView.setText(item.getTitle());
        }
        */
        holder.mTextView.setIconLeft(item.getIcon());
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

        private IconTextView mTextView;

        ViewHolder(View view) {
            super(view);
            mTextView = (IconTextView) view;
        }
    }

}
