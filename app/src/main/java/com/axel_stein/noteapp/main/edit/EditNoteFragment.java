package com.axel_stein.noteapp.main.edit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.dialogs.ConfirmDialog;
import com.axel_stein.noteapp.dialogs.notebook.AddNotebookDialog;
import com.axel_stein.noteapp.dialogs.select_notebook.SelectNotebookDialog;
import com.axel_stein.noteapp.dialogs.select_notebook.SelectNotebookDialog.Builder;
import com.axel_stein.noteapp.main.edit.EditNoteContract.Presenter;
import com.axel_stein.noteapp.main.edit.EditNotePresenter.CheckItem;
import com.axel_stein.noteapp.utils.KeyboardUtil;
import com.axel_stein.noteapp.utils.MenuUtil;
import com.axel_stein.noteapp.utils.SimpleTextWatcher;
import com.axel_stein.noteapp.utils.ViewUtil;
import com.axel_stein.noteapp.views.IconTextView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import static android.text.TextUtils.isEmpty;
import static android.view.Gravity.END;
import static android.view.Gravity.TOP;
import static androidx.recyclerview.widget.ItemTouchHelper.DOWN;
import static androidx.recyclerview.widget.ItemTouchHelper.UP;

public class EditNoteFragment extends Fragment implements EditNoteContract.View,
        ConfirmDialog.OnConfirmListener,
        SelectNotebookDialog.OnMenuItemClickListener {

    private static final String TAG_SAVE_NOTE = "TAG_SAVE_NOTE";
    private static final String TAG_DELETE_NOTE = "TAG_DELETE_NOTE";
    private static final String TAG_SELECT_NOTEBOOK = "TAG_SELECT_NOTEBOOK";

    private EditText mEditTitle;
    private EditText mEditContent;
    private View mFocusView;
    private RecyclerView mCheckRecyclerView;
    private CheckListAdapter mCheckListAdapter;
    private View mScrollView;

    @Nullable
    private IconTextView mNotebookView;

    @Inject
    AppSettingsRepository mAppSettings;

    @Nullable
    private Presenter mPresenter;

    private Menu mMenu;

    private boolean mViewCreated;
    private boolean mTrash;
    private boolean mUpdate;
    private boolean mEditable;
    private boolean mEditViewsFocusable = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent().inject(this);
        EventBusHelper.subscribe(this);

        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_edit_note, container, false);

        mScrollView = root.findViewById(R.id.scroll_view);

        mEditTitle = root.findViewById(R.id.edit_title);
        TextInputLayout mContentInputLayout = root.findViewById(R.id.text_input_content);
        mEditContent = root.findViewById(R.id.edit_content);

        mContentInputLayout.setCounterEnabled(false);

        mEditTitle.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (mPresenter != null) {
                    mPresenter.setTitle(s.toString());
                }
            }
        });
        mEditContent.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (mPresenter != null) {
                    mPresenter.setContent(s.toString());
                }
                mEditContent.setLineSpacing(0, 1);
                mEditContent.setLineSpacing(0, 1.5f);
            }
        });

        int baseFontSize = mAppSettings.getBaseFontSize();
        mEditTitle.setTextSize(baseFontSize + 4);
        mEditContent.setTextSize(baseFontSize);

        setEditViewsFocusable(mEditViewsFocusable);

        mFocusView = root.findViewById(R.id.focus_view);
        mCheckRecyclerView = root.findViewById(R.id.check_recycler_view);

        mViewCreated = true;

        if (mPresenter != null) {
            mPresenter.onCreateView(this);
        }

        return root;
    }

    private void setEditViewsFocusable(boolean focusable) {
        mEditViewsFocusable = focusable;

        setEditTextFocusable(mEditTitle, focusable);
        setEditTextFocusable(mEditContent, focusable);
    }

    private void setEditTextFocusable(EditText editText, boolean focusable) {
        editText.setFocusable(focusable);
        editText.setFocusableInTouchMode(focusable);
        editText.setClickable(focusable);
        editText.setLongClickable(focusable);
    }

    @Override
    public void onDestroyView() {
        mEditTitle = null;
        mEditContent = null;
        mNotebookView = null;
        mViewCreated = false;
        mMenu = null;
        mFocusView = null;
        mCheckRecyclerView = null;
        mScrollView = null;
        if (mPresenter != null) {
            mPresenter.onDestroyView();
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        EventBusHelper.unsubscribe(this);
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        mMenu = menu;
        MenuUtil.enable(mMenu, mEditable);

        MenuUtil.show(mMenu, !mTrash, R.id.menu_pin_note, R.id.menu_star_note, R.id.menu_check_list);
        MenuUtil.show(mMenu, !mTrash && mUpdate, R.id.menu_move_to_trash, R.id.menu_duplicate);
        MenuUtil.show(mMenu, !mTrash, R.id.menu_select_notebook, R.id.menu_share);
        MenuUtil.show(mMenu, mTrash, R.id.menu_restore);
        MenuUtil.show(mMenu, mTrash, R.id.menu_delete);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mPresenter != null) {
            setNotePinned(mPresenter.isPinned());
            setNoteStarred(mPresenter.isStarred());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mPresenter == null) {
            return super.onOptionsItemSelected(item);
        }

        switch (item.getItemId()) {
            case R.id.menu_pin_note:
                mPresenter.actionPinNote();
                break;

            case R.id.menu_star_note:
                mPresenter.actionStarNote();
                break;

            case R.id.menu_select_notebook:
                mPresenter.actionSelectNotebook();
                break;

            case R.id.menu_move_to_trash:
                mPresenter.actionMoveToTrash();
                break;

            case R.id.menu_restore:
                mPresenter.actionRestore();
                break;

            case R.id.menu_delete:
                mPresenter.actionDelete();
                break;

            case R.id.menu_share:
                mPresenter.actionShare();
                break;

            case R.id.menu_duplicate:
                mPresenter.actionDuplicate(getString(R.string.action_copy));
                break;

            case R.id.menu_check_list:
                mPresenter.actionCheckList();
                break;
        }

        return true;
    }

    @Override
    public void setEditable(boolean editable) {
        mEditable = editable;

        MenuUtil.enable(mMenu, editable);

        if (!mTrash) {
            ViewUtil.enable(editable, mEditTitle, mEditContent);
        }

        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    public void setNotePinned(boolean pinned) {
        if (mMenu != null) {
            MenuItem item = mMenu.findItem(R.id.menu_pin_note);
            if (item != null) {
                int icon = pinned ? R.drawable.ic_bookmark_24dp : R.drawable.ic_bookmark_border_24dp;
                item.setIcon(icon);
                MenuUtil.tintAttr(getContext(), item, R.attr.menuItemTintColor);
            }
        }
    }

    @Override
    public void setNote(Note note) {
        String title = note.getTitle();
        String content = note.getContent();

        int selectionTitle = mEditTitle.getSelectionStart();
        mEditTitle.setText(title);
        if (selectionTitle > 0) {
            mEditTitle.setSelection(selectionTitle);
        }

        int selectionContent = mEditContent.getSelectionStart();
        mEditContent.setText(content);
        if (selectionContent > 0) {
            mEditContent.setSelection(selectionContent);
        }

        if (isEmpty(title) && isEmpty(content)) {
            KeyboardUtil.show(mEditTitle);
        }

        mTrash = note.isTrashed();
        mUpdate = note.hasId();

        ViewUtil.enable(!mTrash, mEditTitle, mEditContent, mNotebookView);

        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    public void setNoteCheckList(Note note, List<CheckItem> items) {
        String title = note.getTitle();

        int selectionTitle = mEditTitle.getSelectionStart();
        mEditTitle.setText(title);
        if (selectionTitle > 0) {
            mEditTitle.setSelection(selectionTitle);
        }

        mTrash = note.isTrashed();
        mUpdate = note.hasId();

        ViewUtil.enable(!mTrash, mEditTitle, mNotebookView);

        showCheckList(items);

        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    public void setNotebookTitle(String title) {
        if (isEmpty(title)) {
            title = getString(R.string.action_inbox);
        }
        ViewUtil.setVisible(!isEmpty(title), mNotebookView);
        ViewUtil.setText(mNotebookView, title);
    }

    @Override
    public void clearFocus() {
        if (mEditTitle != null) {
            mEditTitle.clearFocus();
        }
        if (mEditContent != null) {
            mEditContent.clearFocus();
        }
    }

    @Override
    public void showDiscardChangesView() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setMessage(R.string.title_discard);
        dialog.setPositiveButtonText(R.string.action_keep_editing);
        dialog.setNegativeButtonText(R.string.action_discard);
        dialog.setTargetFragment(this, 0);
        if (getFragmentManager() != null) {
            dialog.show(getFragmentManager(), TAG_SAVE_NOTE);
        }
    }

    @Override
    public void callFinish() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    @Override
    public void showShareNoteView(Note note) {
        String title = note.getTitle();
        String content = note.getContent();

        if (isEmpty(content)) {
            content = title;
        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        intent.putExtra(Intent.EXTRA_TEXT, content);

        Context context = getContext();
        if (context != null) {
            PackageManager pm = context.getPackageManager();
            if (pm != null) {
                if (intent.resolveActivity(pm) != null) {
                    startActivity(intent);
                } else {
                    Log.e("TAG", "share note: no activity found");
                    showMessage(R.string.error_share);
                }
            }
        }
    }

    @Override
    public void showMessage(int msg) {
        if (mEditTitle != null) {
            Snackbar.make(mEditTitle, msg, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showSelectNotebookView(List<Notebook> notebooks, String selectedNotebookId) {
        Builder builder = new Builder();
        builder.setTitle(getString(R.string.title_select_notebook));
        builder.setAction(getString(R.string.action_new_notebook));

        List<Notebook> items = new ArrayList<>(notebooks);
        items.add(0, Notebook.from(Notebook.ID_INBOX, getString(R.string.action_inbox)));
        builder.setItems(items);

        builder.setSelectedNotebookId(selectedNotebookId);
        builder.show(this, TAG_SELECT_NOTEBOOK);
    }

    @Override
    public void showConfirmDeleteNoteView() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setTitle(R.string.title_delete_note);
        dialog.setMessage(R.string.msg_delete_note);
        dialog.setPositiveButtonText(R.string.action_delete);
        dialog.setNegativeButtonText(R.string.action_cancel);
        dialog.show(this, TAG_DELETE_NOTE);
    }

    @Override
    public void setNoteStarred(boolean starred) {
        if (mMenu != null) {
            MenuItem item = mMenu.findItem(R.id.menu_star_note);
            if (item != null) {
                item.setIcon(starred ? R.drawable.ic_star_24dp : R.drawable.ic_star_border_24dp);
            }
            MenuUtil.tintAttr(getContext(), item, R.attr.menuItemTintColor);
        }
    }

    @Override
    public void onConfirm(String tag) {
        if (TAG_DELETE_NOTE.equals(tag)) {
            if (mPresenter != null) {
                mPresenter.delete();
            }
        }
    }

    @Override
    public void onCancel(String tag) {
        if (mPresenter != null && TAG_SAVE_NOTE.equals(tag)) {
            mPresenter.confirmDiscardChanges();
        }
    }

    @Nullable
    public Presenter getPresenter() {
        return mPresenter;
    }

    public void setPresenter(@NonNull Presenter presenter) {
        mPresenter = presenter;
        if (mViewCreated) {
            mPresenter.onCreateView(this);
        }
    }

    void setNotebookView(IconTextView view) {
        mNotebookView = view;
        if (mNotebookView != null) {
            mNotebookView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mPresenter != null) {
                        mPresenter.actionSelectNotebook();
                    }
                }
            });
        }
    }

    @Subscribe
    public void onNotebookAdded(EventBusHelper.AddNotebook e) {
        if (mPresenter != null) {
            mPresenter.setNotebook(e.getNotebook());
        }
    }

    @Override
    public void onMenuItemClick(SelectNotebookDialog dialog, String tag, Notebook notebook) {
        dialog.dismiss();
        if (mPresenter != null) {
            mPresenter.setNotebook(notebook);
        }
    }

    @Override
    public void onActionClick(SelectNotebookDialog dialog) {
        dialog.dismiss();
        AddNotebookDialog.launch(this);
    }

    @Override
    public void showCheckList(List<CheckItem> items) {
        ViewUtil.show(mFocusView, mCheckRecyclerView);
        ViewUtil.hide(mScrollView);

        mCheckListAdapter = new CheckListAdapter();
        mCheckListAdapter.setItems(items);
        mCheckRecyclerView.setAdapter(mCheckListAdapter);
        mCheckRecyclerView.setHasFixedSize(true);
        mCheckRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mCheckListAdapter.setRecyclerView(mCheckRecyclerView);
        mCheckListAdapter.getItemTouchHelper().attachToRecyclerView(mCheckRecyclerView);
    }

    @Override
    public void hideCheckList() {
        ViewUtil.hide(mFocusView, mCheckRecyclerView);
        ViewUtil.show(mScrollView);
        mCheckListAdapter = null;
    }

    @Override
    public List<CheckItem> getCheckItems() {
        if (mCheckListAdapter != null) {
            return mCheckListAdapter.getItems();
        }
        return null;
    }

    @Override
    public void setTitle(String title) {
        if (mEditTitle != null) {
            mEditTitle.setText(title);
        }
    }

    @Override
    public void setContent(String content) {
        if (mEditContent != null) {
            mEditContent.setText(content);
        }
    }

    @Subscribe
    public void onHideKeyboard(EventBusHelper.HideKeyboard e) {
        if (!mFocusView.hasFocus()) {
            mFocusView.requestFocus();
        }
    }

    public class CheckListAdapter extends RecyclerView.Adapter<CheckListAdapter.ViewHolder> {
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

            if (getItemCount() == 1 && mPresenter != null) {
                mPresenter.actionCheckList();
            }
        }

        private void checkItem(int pos, boolean checked) {
            CheckItem item = getItemAt(pos);
            item.setChecked(checked);
            notifyItemChanged(pos);

            int to = checked ? getItemCount()-1 : 1;
            mItems.remove(pos);
            mItems.add(to, item);
            notifyItemMoved(pos, to);
        }

        @Override
        public int getItemViewType(int position) {
            if (getItemAt(position).isCheckable()) {
                return 0;
            }
            return -1;
        }

        @SuppressLint("ClickableViewAccessibility")
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == -1) {
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
                                switch (menuItem.getItemId()) {
                                    case R.id.menu_remove_checked_items:
                                        removeCheckedItems();
                                        break;
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

            if (getItemViewType(p) == -1) {
                TitleViewHolder titleViewHolder = (TitleViewHolder) holder;
                titleViewHolder.mEditTextListener.setViewHolder(holder);
                titleViewHolder.mEditText.setText(item.getText());
            } else {
                CheckViewHolder checkViewHolder = (CheckViewHolder) holder;
                boolean checked = item.isChecked();

                checkViewHolder.mEditTextListener.setViewHolder(holder);
                checkViewHolder.mEditText.setText(item.getText());
                checkViewHolder.mEditText.setEnabled(!checked);

                if (mAddItemFlag && position == mAddItemPos) {
                    mAddItemFlag = false;
                    mAddItemPos = -1;
                    checkViewHolder.mEditText.requestFocus();
                }

                boolean hasFocus = checkViewHolder.mEditText.hasFocus();
                checkViewHolder.mCheckBox.setChecked(checked);
                if (checked) {
                    ViewUtil.show(checkViewHolder.mClear);
                    ViewUtil.hide(checkViewHolder.mDragHandle);
                } else {
                    ViewUtil.setVisible(hasFocus, checkViewHolder.mClear);
                    ViewUtil.setVisible(!hasFocus, checkViewHolder.mDragHandle);
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

}
