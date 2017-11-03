package com.axel_stein.noteapp.notes.edit.check_list;

import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.utils.ViewUtil;

import java.util.List;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

public class CheckListAdapter extends RecyclerView.Adapter<CheckListAdapter.ViewHolder> {
    private List<CheckItem> mItems;

    public CheckListAdapter() {
    }

    public void setItems(List<CheckItem> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    public List<CheckItem> getItems() {
        return mItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_check_list, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setItem(mItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private CheckBox mCheckBox;

        private ImageButton mDelete;

        private CheckItem mCheckItem;

        private ViewHolder(View itemView) {
            super(itemView);
            mCheckBox = itemView.findViewById(R.id.checkbox);
            mDelete = itemView.findViewById(R.id.button_delete);

            mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    mCheckItem.setChecked(b);
                    setItem(mCheckItem);
                }
            });
            mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    mItems.remove(pos);
                    notifyItemRemoved(pos);
                }
            });
        }

        void setItem(CheckItem item) {
            mCheckItem = item;

            mCheckBox.setChecked(item.isChecked());

            SpannableString s = new SpannableString(item.getTitle());
            if (item.isChecked()) {
                s.setSpan(new StrikethroughSpan(), 0, item.getTitle().length(), SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            mCheckBox.setText(s);

            ViewUtil.invisible(item.isChecked(), mDelete);
        }


    }

}
