package com.axel_stein.noteapp.notes.list;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.ScrollableFragment;
import com.axel_stein.noteapp.base.BaseFragment;
import com.axel_stein.noteapp.dialogs.label.CheckLabelsDialog;
import com.axel_stein.noteapp.dialogs.note.DeleteNoteDialog;
import com.axel_stein.noteapp.dialogs.notebook.CheckNotebookDialog;
import com.axel_stein.noteapp.notes.edit.EditNoteActivity;
import com.axel_stein.noteapp.notes.list.NotesContract.Presenter;
import com.axel_stein.noteapp.utils.MenuUtil;
import com.axel_stein.noteapp.utils.ViewUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.v7.widget.helper.ItemTouchHelper.LEFT;
import static android.support.v7.widget.helper.ItemTouchHelper.RIGHT;
import static android.text.TextUtils.isEmpty;

public class NotesFragment extends BaseFragment implements NotesContract.View,
        CheckNotebookDialog.OnNotebookCheckedListener,
        CheckLabelsDialog.OnLabelCheckedListener,
        ScrollableFragment {

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.empty_view)
    TextView mEmptyView;

    @Nullable
    private Presenter mPresenter;

    @Nullable
    private NoteAdapter mAdapter;

    private String mEmptyMsg;

    @Nullable
    private ActionMode mActionMode;

    private boolean mViewCreated;

    private boolean mShowBottomPadding = true;

    private NoteItemListener mItemListener = new NoteItemListener() {
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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notes, container, false);
        ButterKnife.bind(this, root);

        mAdapter = new NoteAdapter(mItemListener);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        updateBottomPadding();

        DividerItemDecoration divider = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(divider);

        updateEmptyView();

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, 0) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if (mPresenter != null) {
                    int left = mPresenter.hasSwipeLeftAction() ? LEFT : 0;
                    int right = mPresenter.hasSwipeRightAction() ? RIGHT : 0;
                    return left | right;
                }
                return 0;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
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

    public void showBottomPadding(boolean show) {
        mShowBottomPadding = show;
        updateBottomPadding();
    }

    private void updateBottomPadding() {
        if (mRecyclerView == null) {
            return;
        }

        Resources res = getResources();
        if (res == null) {
            return;
        }

        int p = res.getDimensionPixelOffset(R.dimen.note_list_bottom_padding);
        mRecyclerView.setPadding(0, 0, 0, mShowBottomPadding ? p : 0);
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
        if (mActionMode != null) {
            mActionMode.setTitle(String.valueOf(checkCount));
            if (mAdapter != null) {
                MenuUtil.enable(mActionMode.getMenu(), checkCount != mAdapter.getItemCount(), R.id.menu_select_all);
            }
        }
        if (mAdapter != null) {
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
        EditNoteActivity.launch(getContext(), note);
    }

    @Override
    public void showSelectNotebookView(List<Notebook> notebooks) {
        //SelectNotebookDialog.launch(this, notebooks, -1);
        CheckNotebookDialog.launch(this, notebooks, -1);
    }

    @Override
    public void showCheckLabelsView(List<Label> labels) {
        CheckLabelsDialog.launch(this, labels, null);
    }

    @Override
    public void showConfirmDeleteDialog(List<Note> notes) {
        DeleteNoteDialog.launch(getActivity(), getFragmentManager(), notes);
    }

    private void showSnackbarMessage(String msg) {
        if (getView() != null) {
            Snackbar.make(getView(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNotebookChecked(Notebook notebook) {
        if (mPresenter != null) {
            mPresenter.onNotebookSelected(notebook);
        }
    }

    @Override
    public void onLabelsChecked(List<Long> labels) {
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

    interface NoteItemListener {

        void onNoteClick(int pos, Note note);

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

        @Nullable
        List<Note> getNotes() {
            return mNotes;
        }

        void setNotes(@Nullable List<Note> list) {
            this.mNotes = list;
            notifyDataSetChanged();
        }

        Note getItem(int position) {
            return mNotes.get(position);
        }

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
            private TextView mNote;
            private TextView mContent;
            private NoteItemListener mListener;

            private NoteViewHolder(View itemView, NoteItemListener l) {
                super(itemView);
                mIcon = itemView.findViewById(R.id.img_icon);
                mPin = itemView.findViewById(R.id.img_pin);
                mNote = itemView.findViewById(R.id.text_note);
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

            public void setNote(Note note) {
                String content = note.getContent();

                ViewUtil.show(!isEmpty(content), mContent);

                mNote.setText(note.getTitle());
                mContent.setText(content);

                ViewUtil.show(note.isPinned(), mPin);
            }

            void setChecked(boolean checkable, boolean checked) {
                if (!checkable) {
                    mIcon.setImageResource(R.drawable.ic_description_white_24dp);
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
