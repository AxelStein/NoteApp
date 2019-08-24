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

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.ScrollableFragment;
import com.axel_stein.noteapp.dialogs.bottom_menu.BottomMenuDialog;
import com.axel_stein.noteapp.dialogs.label.CheckLabelsDialog;
import com.axel_stein.noteapp.dialogs.note.DeleteNoteDialog;
import com.axel_stein.noteapp.dialogs.notebook.CheckNotebookDialog;
import com.axel_stein.noteapp.main.edit.EditNoteActivity;
import com.axel_stein.noteapp.main.list.NotesContract.Presenter;
import com.axel_stein.noteapp.utils.DisplayUtil;
import com.axel_stein.noteapp.utils.MenuUtil;
import com.axel_stein.noteapp.utils.ViewUtil;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import static android.text.TextUtils.isEmpty;
import static androidx.recyclerview.widget.ItemTouchHelper.LEFT;
import static androidx.recyclerview.widget.ItemTouchHelper.RIGHT;

public class NotesFragment extends Fragment implements NotesContract.View,
        CheckNotebookDialog.OnNotebookCheckedListener,
        CheckLabelsDialog.OnLabelCheckedListener,
        ScrollableFragment,
        BottomMenuDialog.OnMenuItemClickListener {

    private static final String TAG_SORT_NOTES = "com.axel_stein.noteapp.notes.list.TAG_SORT_NOTES";

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

    private NoteItemListener mItemListener = new NoteItemListener() {
        @Override
        public void onNoteClick(int pos, Note note, View view) {
            if (mPresenter != null) {
                mPresenter.onNoteClick(pos, note, view);
            }
        }

        @Override
        public boolean onNoteLongClick(int pos, Note note) {
            return mPresenter != null && mPresenter.onNoteLongClick(pos, note);
        }
    };

    public void setEmptyMsg(String emptyMsg) {
        mEmptyMsg = emptyMsg;
        updateEmptyView();
    }

    private void updateEmptyView() {
        ViewUtil.setText(mEmptyView, mEmptyMsg);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
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

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if (mPresenter != null) {
                    int left = mPresenter.hasSwipeLeftAction() ? LEFT : 0;
                    int right = mPresenter.hasSwipeRightAction() ? RIGHT : 0;
                    return left | right;
                }
                return 0;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                if (mAdapter != null && mPresenter != null) {
                    switch (direction) {
                        case LEFT:
                            mPresenter.swipeLeft(mAdapter.getItem(pos));
                            break;

                        case RIGHT:
                            mPresenter.swipeRight(mAdapter.getItem(pos));
                            break;
                    }
                }
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }
        });
        helper.attachToRecyclerView(mRecyclerView);

        mViewCreated = true;

        if (mPresenter != null) {
            setPresenter(mPresenter);
        }

        return root;
    }

    public void setPaddingTop(int dp) {
        mPaddingTop = DisplayUtil.dpToPx(getContext(), dp);
        //updatePadding();
    }

    public void setPaddingBottom(int dp) {
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

        ViewUtil.show(list != null && list.size() == 0, mEmptyView);

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
    public void showNote(Note note, View view) {
        // todo EditNoteActivity.launch(getContext(), note);
        EditNoteActivity.launch(getActivity(), note, view);
    }

    @Override
    public void showSelectNotebookView(List<Notebook> notebooks) {
        CheckNotebookDialog.launch(this, notebooks, null);
    }

    @Override
    public void showCheckLabelsView(List<Label> labels) {
        CheckLabelsDialog.launch(this, labels, null);
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
        builder.addChecked(itemId);
        builder.show(this, TAG_SORT_NOTES);
    }

    private void showSnackbarMessage(String msg) {
        if (getView() != null) {
            Snackbar.make(getView(), msg, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNotebookChecked(Notebook notebook) {
        if (mPresenter != null) {
            mPresenter.onNotebookSelected(notebook);
        }
    }

    @Override
    public void onLabelsChecked(List<String> labels) {
        if (mPresenter != null) {
            mPresenter.onLabelsChecked(labels);
        }
    }

    @Nullable
    public Presenter getPresenter() {
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

    interface NoteItemListener {

        void onNoteClick(int pos, Note note, View view);

        boolean onNoteLongClick(int pos, Note note);

    }

    private static class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
        private List<Note> mNotes;
        private Presenter mPresenter;
        private NoteItemListener mNoteItemListener;

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
            holder.setNote(note);
            if (mPresenter != null) {
                holder.setChecked(mPresenter.hasChecked(), mPresenter.isChecked(note));
            } else {
                holder.setChecked(false, false);
            }
        }

        @Override
        public int getItemCount() {
            return mNotes == null ? 0 : mNotes.size();
        }

        class NoteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
            private ImageView mIcon;
            private ImageView mPin;
            private ImageView mStar;
            private TextView mTitle;
            private TextView mContent;
            private NoteItemListener mListener;

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
                                mListener.onNoteClick(pos, getItem(pos), itemView);
                            }
                        }
                    }
                });
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
            }

            public void setNote(Note note) {
                String content = note.getContent();

                ViewUtil.show(!isEmpty(content), mContent);

                mTitle.setText(note.getTitle());
                mContent.setText(content);

                ViewUtil.show(note.isPinned(), mPin);
                ViewUtil.show(note.isStarred(), mStar);
            }

            void setChecked(boolean checkable, boolean checked) {
                if (!checkable) {
                    mIcon.setImageResource(R.drawable.ic_description);
                } else if (checked) {
                    mIcon.setImageResource(R.drawable.ic_check_box_white_24dp);
                } else {
                    mIcon.setImageResource(R.drawable.ic_check_box_outline_blank_white_24dp);
                }
                mIcon.setSelected(checkable && checked);
            }

            @Override
            public void onClick(View view) {
                int pos = getAdapterPosition();
                if (checkAdapterPosition()) {
                    mListener.onNoteClick(pos, getItem(pos), view);
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
