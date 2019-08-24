package com.axel_stein.noteapp.dialogs.main_menu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.utils.ColorUtil;
import com.axel_stein.noteapp.views.IconTextView;

import java.util.List;

class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    private final int ITEM_PRIMARY = 0;
    private final int ITEM_DIVIDER = -1;

    private LayoutInflater mInflater;
    private List<Item> mItems;
    private OnItemClickListener mOnItemClickListener;
    private String mSelectedItemId;
    private int mSelectedColor;

    Adapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mSelectedColor = ColorUtil.getColorAttr(context, R.attr.selectedItemBackgroundColor);
    }

    void setOnItemClickListener(OnItemClickListener l) {
        mOnItemClickListener = l;
    }

    void setItems(List<Item> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    void setSelectedItemId(String itemId) {
        this.mSelectedItemId = itemId;
    }

    private PrimaryItem getItem(int pos) {
        return mItems != null && pos >= 0 && pos < mItems.size() ? (PrimaryItem) mItems.get(pos) : null;
    }

    @Override
    public int getItemViewType(int position) {
        Item item = mItems.get(position);
        if (item instanceof PrimaryItem) {
            return ITEM_PRIMARY;
        }
        return ITEM_DIVIDER;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final ViewHolder holder;
        if (viewType == ITEM_PRIMARY) {
            holder = new PrimaryItemVH(mInflater.inflate(R.layout.item_primary, parent, false), this, mOnItemClickListener);
        } else {
            holder = new DividerItemVH(mInflater.inflate(R.layout.item_divider, parent, false));
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int type = getItemViewType(position);
        if (type == ITEM_PRIMARY) {
            PrimaryItemVH pvh = (PrimaryItemVH) holder;
            PrimaryItem item = (PrimaryItem) mItems.get(position);

            if (item.hasTitleRes()) {
                pvh.mTextView.setText(item.getTitleRes());
            } else {
                pvh.mTextView.setText(item.getTitle());
            }
            pvh.mTextView.setIconLeft(item.getIcon());

            boolean selected = item.isSelectable() && item.getId().equalsIgnoreCase(mSelectedItemId);
            //pvh.mTextView.setChecked(selected);
            pvh.itemView.setBackgroundColor(selected ? mSelectedColor : 0);
        }
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

    interface OnItemClickListener {
        void onItemClick(PrimaryItem item);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(View view) {
            super(view);
        }
    }

    static class PrimaryItemVH extends ViewHolder {
        IconTextView mTextView;

        PrimaryItemVH(View view, final Adapter adapter, final OnItemClickListener l) {
            super(view);
            mTextView = view.findViewById(R.id.text_view);
            mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    l.onItemClick(adapter.getItem(pos));
                }
            });
        }
    }

    static class DividerItemVH extends ViewHolder {

        DividerItemVH(View view) {
            super(view);
        }
    }

}