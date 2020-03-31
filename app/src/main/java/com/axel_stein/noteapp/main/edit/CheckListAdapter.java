package com.axel_stein.noteapp.main.edit;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.utils.KeyboardUtil;
import com.axel_stein.noteapp.utils.ViewUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.view.Gravity.END;
import static android.view.Gravity.TOP;
import static androidx.recyclerview.widget.ItemTouchHelper.DOWN;
import static androidx.recyclerview.widget.ItemTouchHelper.UP;

public class CheckListAdapter extends RecyclerView.Adapter<CheckListAdapter.ViewHolder> {
    private static final int ITEM_TITLE = 0;
    private static final int ITEM_LIST = 1;

    private List<CheckItem> mItems;
    private ItemTouchHelper mItemTouchHelper;
    private boolean mAddItemFlag;
    private int mAddItemPos = -1;
    private RecyclerView mRecyclerView;

    CheckListAdapter() {
        mItems = new ArrayList<>();
        mItems.add(new CheckItem());
    }

    void setItems(List<CheckItem> items) {
        mItems = items == null ? new ArrayList<CheckItem>() : items;
        notifyDataSetChanged();
    }

    void addItem(CheckItem item) {
        if (mItems == null) {
            mItems = new ArrayList<>();
        }
        mItems.add(item);
        notifyDataSetChanged();
    }

    List<CheckItem> getItems() {
        return mItems;
    }

    void setRecyclerView(RecyclerView view) {
        this.mRecyclerView = view;
    }

    private void addItemAt(int pos, String text) {
        if (getItemCount() > 200) { // fixme
            return;
        }

        CheckItem item = new CheckItem();
        item.setText(text);
        mItems.add(pos, item);

        mAddItemFlag = true;
        mAddItemPos = pos;

        notifyItemInserted(pos);

        if (mRecyclerView != null) {
            mRecyclerView.scrollToPosition(pos);
        }
    }

    ItemTouchHelper getItemTouchHelper() {
        mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView rw, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int pos1 = viewHolder.getAdapterPosition();
                int pos2 = target.getAdapterPosition();

                CheckItem n1 = getItemAt(pos1);
                CheckItem n2 = getItemAt(pos2);

                if (n1.isCheckable() && n2.isCheckable()) {
                    if (!n1.isChecked() && !n2.isChecked()) {
                        swapItems(pos1, pos2);
                        return true;
                    }
                }

                return false;
            }

            @Override
            public int getDragDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int pos = viewHolder.getAdapterPosition();
                int count = getItemCount();
                if (pos >= 0 && pos < count) {
                    CheckItem item = getItemAt(pos);
                    if (item.isChecked()) {
                        return 0;
                    }
                    return UP | DOWN;
                }
                return 0;
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                //logList();
                    /*
                    if (mOrderChangedListener != null) {
                        mOrderChangedListener.onNotebookOrderChanged(mItems);
                    }
                    */
            }
        });
        return mItemTouchHelper;
    }

    private void swapItems(int from, int to) {
        if (mItems != null && from >= 0 && to >= 0) {
            Collections.swap(mItems, from, to);
            notifyItemMoved(from, to);
        }
    }

    private CheckItem getItemAt(int pos) {
        return mItems.get(pos);
    }

    private void removeCheckedItems() {
        for (int i = mItems.size()-1; i >= 0; i--) {
            CheckItem item = getItemAt(i);
            if (item.isChecked()) {
                removeItemAt(i);
            }
        }
    }

    private void removeItemAt(int pos) {
        mItems.remove(pos);
        notifyItemRemoved(pos);
        /*
        todo
        if (getItemCount() == 1 && mPresenter != null) {
            mPresenter.actionCheckList();
        }
        */
    }

    private void checkItem(int pos, boolean checked) {
        CheckItem item = getItemAt(pos);
        item.setChecked(checked);
        notifyItemChanged(pos);

        int to = checked ? getItemCount()-1 : 1; // todo
        mItems.remove(pos);
        mItems.add(to, item);
        notifyItemMoved(pos, to);
    }

    @Override
    public int getItemViewType(int position) {
        CheckItem item = getItemAt(position);
        if (item.isCheckable()) {
            return ITEM_LIST;
        }
        return ITEM_TITLE;
    }

    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_TITLE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_check_list_title, parent, false);
            TitleViewHolder tvh = new TitleViewHolder(v, new EditTextListener());
            tvh.mOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu menu = new PopupMenu(v.getContext(), v, TOP | END, 0, R.style.PopupMenu);
                    menu.inflate(R.menu.check_list);
                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            if (menuItem.getItemId() == R.id.menu_remove_checked_items) {
                                removeCheckedItems();
                            }
                            return true;
                        }
                    });
                    menu.show();
                }
            });
            return tvh;
        }

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_check_list, parent, false);
        final CheckViewHolder vh = new CheckViewHolder(v, new EditTextListener());
        if (mItemTouchHelper != null) {
            vh.mDragHandle.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        mItemTouchHelper.startDrag(vh);
                    }
                    return false;
                }
            });
        }
        vh.mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = vh.mCheckBox.isChecked();
                if (checked && vh.mEditText.hasFocus()) {
                    KeyboardUtil.hide(vh.mEditText);
                }
                checkItem(vh.getAdapterPosition(), checked);
            }
        });
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int p) {
        int position = holder.getAdapterPosition();
        CheckItem item = mItems.get(position);

        switch (getItemViewType(p)) {
            case ITEM_TITLE: {
                TitleViewHolder vh = (TitleViewHolder) holder;
                vh.mEditTextListener.setViewHolder(holder);
                vh.mEditText.setText(item.getText());
                break;
            }

            case ITEM_LIST: {
                CheckViewHolder vh = (CheckViewHolder) holder;
                boolean checked = item.isChecked();

                vh.mEditTextListener.setViewHolder(holder);
                vh.mEditText.setText(item.getText());
                vh.mEditText.setEnabled(!checked);

                if (mAddItemFlag && position == mAddItemPos) {
                    mAddItemFlag = false;
                    mAddItemPos = -1;
                    vh.mEditText.requestFocus();
                }

                boolean hasFocus = vh.mEditText.hasFocus();
                vh.mCheckBox.setChecked(checked);
                if (checked) {
                    ViewUtil.show(vh.mClear);
                    ViewUtil.hide(vh.mDragHandle);
                } else {
                    ViewUtil.setVisible(hasFocus, vh.mClear);
                    ViewUtil.setVisible(!hasFocus, vh.mDragHandle);
                }
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class TitleViewHolder extends ViewHolder {
        EditText mEditText;
        EditTextListener mEditTextListener;
        ImageButton mOptions;

        TitleViewHolder(@NonNull View itemView, EditTextListener listener) {
            super(itemView);
            mEditText = itemView.findViewById(R.id.edit_title);
            mEditTextListener = listener;
            mEditText.addTextChangedListener(mEditTextListener);
            mOptions = itemView.findViewById(R.id.button_more_options);
        }
    }

    class CheckViewHolder extends ViewHolder {
        ImageView mDragHandle;
        CheckBox mCheckBox;
        EditText mEditText;
        EditTextListener mEditTextListener;
        ImageButton mClear;

        @SuppressLint("ClickableViewAccessibility")
        CheckViewHolder(View v, EditTextListener listener) {
            super(v);
            mDragHandle = v.findViewById(R.id.drag_handle);
            mCheckBox = v.findViewById(R.id.checkbox);

            mClear = v.findViewById(R.id.button_clear);
            mClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeItemAt(getAdapterPosition());
                }
            });
            mEditTextListener = listener;

            mEditText = v.findViewById(R.id.edit_text);
            mEditText.addTextChangedListener(mEditTextListener);
            mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (mCheckBox.isChecked()) {
                        ViewUtil.show(mClear);
                        ViewUtil.hide(mDragHandle);
                    } else {
                        ViewUtil.setVisible(hasFocus, mClear);
                        ViewUtil.setVisible(!hasFocus, mDragHandle);
                    }
                    if (hasFocus) {
                        mEditText.setSelection(mEditText.getEditableText().length());
                    }
                        /*
                        if (!hasFocus) {
                            KeyboardUtil.hide(view);
                        }
                        */
                }
            });
            mEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            mEditText.setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                    if (actionId == 5) {
                        int cursor = mEditText.getSelectionStart();
                        int pos = getAdapterPosition();
                        int nextPos = pos + 1;
                        if (cursor < mEditText.getText().length()) {
                            String text = mEditText.getText().toString();

                            getItemAt(pos).setText(text.substring(0, cursor));
                            notifyItemChanged(pos);

                            addItemAt(nextPos, text.substring(cursor));
                        } else {
                            addItemAt(nextPos, null);
                        }
                    }
                    return true;
                }
            });
            mEditText.clearFocus();
                /*
                InputFilter filter = new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        if (end == 0 || dstart < dend) {
                            if (dstart == dend && dstart == 0 && !mAddItemFlag) {
                                removeItemAt(getAdapterPosition());
                            }
                        }

                        return source;
                    }
                };
                mEditText.setFilters(new InputFilter[] { filter });
                */
        }
    }

    private class EditTextListener implements TextWatcher {
        private ViewHolder viewHolder;

        void setViewHolder(ViewHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            CheckItem item = getItemAt(viewHolder.getAdapterPosition());
            item.setText(charSequence.toString());
        }

        @Override
        public void afterTextChanged(Editable editable) {}

    }
}
