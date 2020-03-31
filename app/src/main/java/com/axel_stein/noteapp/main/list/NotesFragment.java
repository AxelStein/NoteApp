package com.axel_stein.noteapp.main.list;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.ScrollableFragment;
import com.axel_stein.noteapp.dialogs.bottom_menu.BottomMenuDialog;
import com.axel_stein.noteapp.dialogs.note.DeleteNoteDialog;
import com.axel_stein.noteapp.dialogs.notebook.AddNotebookDialog;
import com.axel_stein.noteapp.dialogs.select_notebook.SelectNotebookDialog;
import com.axel_stein.noteapp.dialogs.select_notebook.SelectNotebookDialog.Builder;
import com.axel_stein.noteapp.main.edit2.EditNoteActivity2;
import com.axel_stein.noteapp.main.list.NotesContract.Presenter;
import com.axel_stein.noteapp.utils.DisplayUtil;
import com.axel_stein.noteapp.utils.MenuUtil;
import com.axel_stein.noteapp.utils.ViewUtil;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import static android.text.TextUtils.isEmpty;

public class NotesFragment extends Fragment implements NotesContract.View,
        SelectNotebookDialog.OnMenuItemClickListener,
        ScrollableFragment,
        BottomMenuDialog.OnMenuItemClickListener {

    private static final String TAG_SORT_NOTES = "com.axel_stein.noteapp.notes.list.TAG_SORT_NOTES";
    private static final String TAG_SELECT_NOTEBOOK = "com.axel_stein.noteapp.notes.list.TAG_SELECT_NOTEBOOK";

    private RecyclerView mRecyclerView;
    private TextView mEmptyView;

    @Nullable
    protected Presenter mPresenter;

    @Nullable
    private NoteAdapter mAdapter;

    private String mEmptyMsg;

    @Nullable
    private ActionMode mActionMode;

    private boolean mViewCreated;

    private int mPaddingTop;
    private int mPaddingBottom;

    private final NoteItemListener mItemListener = new NoteItemListener() {
        @Override
        public void onNoteClick(int pos, Note note) {
            if (mPresenter != null) {
                mPresenter.onNoteClick(pos, note);
            }
        }

        @Override
        public boolean onNoteLongClick(int pos, Note note) {
            return mPresenter != null && mPresenter.onNoteLongClick(pos, note);
        }
    };

    protected void setEmptyMsg(String emptyMsg) {
        mEmptyMsg = emptyMsg;
        updateEmptyView();
    }

    private void updateEmptyView() {
        ViewUtil.setText(mEmptyView, mEmptyMsg);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBusHelper.subscribe(this);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        EventBusHelper.unsubscribe(this);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notes, container, false);

        mRecyclerView = root.findViewById(R.id.recycler_view);
        mEmptyView = root.findViewById(R.id.empty_view);

        mAdapter = new NoteAdapter(mItemListener);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        updatePadding();
        updateEmptyView();

        ItemTouchHelper helper = new ItemTouchHelper(new SwipeActionCallback(getContext(), mAdapter, mPresenter));
        helper.attachToRecyclerView(mRecyclerView);

        mViewCreated = true;

        if (mPresenter != null) {
            setPresenter(mPresenter);
        }

        return root;
    }

    @SuppressWarnings("SameParameterValue")
    protected void setPaddingTop(int dp) {
        mPaddingTop = DisplayUtil.dpToPx(getContext(), dp);
        //updatePadding();
    }

    protected void setPaddingBottom(int dp) {
        mPaddingBottom = DisplayUtil.dpToPx(getContext(), dp);
        //updatePadding();
    }

    private void updatePadding() {
        if (mRecyclerView != null) {
            mRecyclerView.setPadding(0, mPaddingTop, 0, mPaddingBottom);
        }
    }

    @Override
    public void onDestroyView() {
        mAdapter = null;
        mRecyclerView = null;
        mEmptyView = null;
        mActionMode = null;
        mViewCreated = false;
        if (mPresenter != null) {
            mPresenter.onDestroyView();
        }
        super.onDestroyView();
    }

    @Override
    public void setNotes(List<Note> list) {
        if (mAdapter != null) {
            mAdapter.setNotes(list);
        }

        ViewUtil.setVisible(list != null && list.size() == 0, mEmptyView);

        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    @Override
    public void showError() {
        if (getContext() != null) {
            showSnackbarMessage(getString(R.string.error));
        }
    }

    @Override
    public void startCheckMode() {
        if (mActionMode != null) {
            return;
        }
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity == null) {
            return;
        }
        mActionMode = activity.startSupportActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                if (mPresenter != null) {
                    mode.getMenuInflater().inflate(mPresenter.getCheckModeMenu(), menu);
                    MenuUtil.tintMenuIconsAttr(getContext(), menu, R.attr.menuItemTintColor);
                    return true;
                }
                return false;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return mPresenter != null;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (mPresenter != null) {
                    mPresenter.onActionItemClicked(item.getItemId());
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                if (mPresenter != null) {
                    mPresenter.stopCheckMode();
                }
                mActionMode = null;
            }
        });
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemChecked(int pos, int checkCount) {
        if (mAdapter != null && mActionMode != null) {
            mActionMode.setTitle(String.valueOf(checkCount));
            MenuUtil.enable(mActionMode.getMenu(), checkCount != mAdapter.getItemCount(), R.id.menu_select_all);

            if (pos < 0) {
                mAdapter.notifyDataSetChanged();
            } else {
                mAdapter.notifyItemChanged(pos);
            }
        }
    }

    @Override
    public void stopCheckMode() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void showNote(Note note) {
        EditNoteActivity2.launch(getContext(), note);
    }

    @Override
    public void showSelectNotebookView(List<Notebook> notebooks) {
        Builder builder = new Builder();
        builder.setTitle(getString(R.string.title_select_notebook));
        builder.setAction(getString(R.string.action_new_notebook));

        List<Notebook> items = new ArrayList<>(notebooks);
        items.add(0, Notebook.from(Notebook.ID_INBOX, getString(R.string.action_inbox)));
        builder.setItems(items);

        builder.show(this, TAG_SELECT_NOTEBOOK);
    }

    @Override
    public void showConfirmDeleteDialog(List<Note> notes) {
        DeleteNoteDialog.launch(getActivity(), getFragmentManager(), notes);
    }

    @Override
    public void showSortDialog(int itemId) {
        BottomMenuDialog.Builder builder = new BottomMenuDialog.Builder();
        builder.setTitle(getString(R.string.action_sort));
        builder.setMenuRes(R.menu.sort_notes);
        builder.setCheckedItemId(itemId);
        builder.show(this, TAG_SORT_NOTES);
    }

    private void showSnackbarMessage(String msg) {
        if (getView() != null) {
            Snackbar.make(getView(), msg, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Subscribe
    public void onNotebookAdded(EventBusHelper.AddNotebook e) {
        if (mPresenter != null) {
            mPresenter.onNotebookSelected(e.getNotebook());
            mPresenter.stopCheckMode();
        }
    }

    @Nullable
    protected Presenter getPresenter() {
        return mPresenter;
    }

    public void setPresenter(@Nullable Presenter presenter) {
        if (mPresenter != null) {
            mPresenter.onDestroyView();
        }
        mPresenter = presenter;
        if (mViewCreated) {
            if (mAdapter != null) {
                mAdapter.setNotes(null);
                mAdapter.setPresenter(mPresenter);
            }
            if (mPresenter != null) {
                mPresenter.onCreateView(this);
            }
        }
    }

    @Override
    public void scrollToTop() {
        if (mRecyclerView != null) {
            mRecyclerView.scrollToPosition(0);
        }
    }

    @Override
    public void onMenuItemClick(BottomMenuDialog dialog, String tag, MenuItem item) {
        if (mPresenter != null) {
            mPresenter.onSortMenuItemClick(item);
        }
        dialog.dismiss();
    }

    @Override
    public void onMenuItemClick(SelectNotebookDialog dialog, String tag, Notebook notebook) {
        dialog.dismiss();
        if (mPresenter != null) {
            mPresenter.onNotebookSelected(notebook);
        }
    }

    @Override
    public void onActionClick(SelectNotebookDialog dialog) {
        dialog.dismiss();
        AddNotebookDialog.launch(this);
    }

    interface NoteItemListener {

        void onNoteClick(int pos, Note note);

        boolean onNoteLongClick(int pos, Note note);

    }

    static class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
        private List<Note> mNotes;
        private Presenter mPresenter;
        private final NoteItemListener mNoteItemListener;

        NoteAdapter(@NonNull NoteItemListener l) {
            mNoteItemListener = l;
        }

        void setPresenter(Presenter presenter) {
            mPresenter = presenter;
        }

        void setNotes(@Nullable List<Note> list) {
            this.mNotes = list;
            notifyDataSetChanged();
        }

        Note getItem(int position) {
            return mNotes.get(position);
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? 0 : 1;
        }

        @NonNull
        @Override
        public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            return new NoteViewHolder(inflater.inflate(R.layout.item_note, parent, false), mNoteItemListener);
        }

        @Override
        public void onBindViewHolder(NoteViewHolder holder, int position) {
            Note note = getItem(position);
            boolean checkList = note.isCheckList();

            holder.setNote(note);
            if (mPresenter != null) {
                holder.setChecked(mPresenter.hasChecked(), mPresenter.isChecked(note), checkList);
            } else {
                holder.setChecked(false, false, checkList);
            }
        }

        @Override
        public int getItemCount() {
            return mNotes == null ? 0 : mNotes.size();
        }

        class NoteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
            private final ImageView mIcon;
            private final ImageView mPin;
            private final ImageView mStar;
            private final TextView mTitle;
            private final TextView mContent;
            private final NoteItemListener mListener;

            private NoteViewHolder(final View itemView, NoteItemListener l) {
                super(itemView);
                mIcon = itemView.findViewById(R.id.img_icon);
                mPin = itemView.findViewById(R.id.img_pin);
                mStar = itemView.findViewById(R.id.img_starred);
                mTitle = itemView.findViewById(R.id.text_title);
                mContent = itemView.findViewById(R.id.text_content);
                mListener = l;

                mIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!onLongClick(v)) {
                            int pos = getAdapterPosition();
                            if (checkAdapterPosition()) {
                                mListener.onNoteClick(pos, getItem(pos));
                            }
                        }
                    }
                });
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
            }

            void setNote(Note note) {
                String content = note.getContent();

                ViewUtil.setVisible(!isEmpty(content), mContent);

                mTitle.setText(note.getTitle());
                mContent.setText(content);

                ViewUtil.setVisible(note.isPinned(), mPin);
                ViewUtil.setVisible(note.isStarred(), mStar);
            }

            void setChecked(boolean checkable, boolean checked, boolean checkList) {
                if (!checkable) {
                    mIcon.setImageResource(checkList ? R.drawable.ic_assignment_24dp : R.drawable.ic_description_24dp);
                } else if (checked) {
                    mIcon.setImageResource(R.drawable.ic_check_box_24dp);
                } else {
                    mIcon.setImageResource(R.drawable.ic_check_box_outline_blank_24dp);
                }
                mIcon.setSelected(checkable && checked);
            }

            @Override
            public void onClick(View view) {
                int pos = getAdapterPosition();
                if (checkAdapterPosition()) {
                    mListener.onNoteClick(pos, getItem(pos));
                }
            }

            @Override
            public boolean onLongClick(View view) {
                int pos = getAdapterPosition();
                return checkAdapterPosition() && mListener.onNoteLongClick(pos, getItem(pos));
            }

            private boolean checkAdapterPosition() {
                int pos = getAdapterPosition();
                return pos >= 0 && pos < getItemCount();
            }

        }

    }

}
