package com.axel_stein.noteapp.notes.edit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.axel_stein.domain.interactor.notebook.QueryNotebookInteractor;
import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.base.BaseFragment;
import com.axel_stein.noteapp.dialogs.ConfirmDialog;
import com.axel_stein.noteapp.dialogs.bottom_menu.BottomMenuDialog;
import com.axel_stein.noteapp.dialogs.label.CheckLabelsDialog;
import com.axel_stein.noteapp.dialogs.note.NoteInfoDialog;
import com.axel_stein.noteapp.dialogs.notebook.SelectNotebookDialog;
import com.axel_stein.noteapp.notes.edit.EditNoteContract.Presenter;
import com.axel_stein.noteapp.utils.DateFormatter;
import com.axel_stein.noteapp.utils.KeyboardUtil;
import com.axel_stein.noteapp.utils.MenuUtil;
import com.axel_stein.noteapp.utils.SimpleTextWatcher;
import com.axel_stein.noteapp.utils.ViewUtil;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.text.TextUtils.isEmpty;

public class EditNoteFragment extends BaseFragment implements EditNoteContract.View,
        ConfirmDialog.OnConfirmListener, SelectNotebookDialog.OnNotebookSelectedListener,
        CheckLabelsDialog.OnLabelCheckedListener, BottomMenuDialog.OnMenuItemClickListener {

    private static final String TAG_SAVE_NOTE = "TAG_SAVE_NOTE";

    private static final String TAG_DELETE_NOTE = "TAG_DELETE_NOTE";

    @BindView(R.id.edit_title)
    EditText mEditTitle;

    @BindView(R.id.edit_content)
    EditText mEditContent;

    @BindView(R.id.text_update)
    TextView mTextUpdate;

    @BindView(R.id.button_menu)
    ImageButton mButtonMenu;

    @Inject
    QueryNotebookInteractor mQueryNotebookInteractor;

    @Nullable
    private Presenter mPresenter;

    private Menu mMenu;

    private boolean mViewCreated;
    private boolean mTrash;
    private boolean mUpdate;
    private boolean mEditable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.getAppComponent().inject(this);

        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_edit_note, container, false);
        ButterKnife.bind(this, root);

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
            }
        });

        mViewCreated = true;

        if (mPresenter != null) {
            mPresenter.onCreateView(this);
        }

        return root;
    }

    @OnClick(R.id.button_menu)
    public void onButtonMenuClick() {
        new BottomMenuDialog.Builder()
                .setMenuRes(R.menu.bottom_menu_edit_note)
                .showMenuItem(R.id.menu_restore, mTrash)
                .showMenuItem(R.id.menu_delete, mUpdate)
                .showMenuItem(R.id.menu_move_to_trash, !mTrash && mUpdate)
                .showMenuItem(R.id.menu_select_notebook, !mTrash)
                .showMenuItem(R.id.menu_labels, !mTrash)
                .showMenuItem(R.id.menu_share, !mTrash)
                .showMenuItem(R.id.menu_info, false)
                .show(EditNoteFragment.this);
    }

    @Override
    public void onDestroyView() {
        mEditTitle = null;
        mEditContent = null;
        mViewCreated = false;
        mButtonMenu = null;
        mTextUpdate = null;
        mMenu = null;
        if (mPresenter != null) {
            mPresenter.onDestroyView();
        }
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.activity_edit_note, menu);

        mMenu = menu;
        MenuUtil.show(mMenu, mEditable, R.id.menu_fullscreen, R.id.menu_done);

        if (mPresenter != null) {
            mPresenter.addOnNoteChangedListener(new EditNoteContract.OnNoteChangedListener() {
                @Override
                public void onNoteChanged(boolean changed) {
                    if (mEditable) {
                        MenuUtil.show(mMenu, changed, R.id.menu_done);
                    }
                }
            });
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuUtil.tintMenuIconsAttr(getContext(), menu, R.attr.menuItemTintColor);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_done:
                if (mPresenter != null) {
                    mPresenter.save();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setEditable(boolean editable) {
        mEditable = editable;

        MenuUtil.show(mMenu, editable, R.id.menu_fullscreen, R.id.menu_done);

        ViewUtil.enable(editable, mButtonMenu);
        if (!mTrash) {
            ViewUtil.enable(editable, mEditTitle, mEditContent);
        }

        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
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

        mTrash = note.isTrash();
        mUpdate = note.getId() > 0;

        long date = System.currentTimeMillis();
        if (note.getId() > 0) {
            date = note.getUpdate();
        }
        mTextUpdate.setText(DateFormatter.formatDateTime(getContext(), date));

        ViewUtil.enable(!mTrash, mEditTitle, mEditContent);

        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void showDiscardChangesView() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setMessage(R.string.title_discard);
        dialog.setPositiveButtonText(R.string.action_keep_editing);
        dialog.setNegativeButtonText(R.string.action_discard);
        dialog.setTargetFragment(this, 0);
        dialog.show(getFragmentManager(), TAG_SAVE_NOTE);
    }

    @Override
    public void callFinish() {
        getActivity().finish();
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

        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.e("TAG", "share note: no activity found");
            showMessage(R.string.error_share);
        }
    }

    @Override
    public void showMessage(int msg) {
        if (mEditTitle != null) {
            Snackbar.make(mEditTitle, msg, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showSelectNotebookView(List<Notebook> notebooks, long selectedNotebook) {
        SelectNotebookDialog.launch(this, notebooks, selectedNotebook);
    }

    @Override
    public void showCheckLabelsView(List<Label> labels, List<Long> checkedLabels) {
        CheckLabelsDialog.launch(this, labels, checkedLabels);
    }

    @Override
    public void showNoteInfoView(Note note) {
        NoteInfoDialog.launch(getContext(), this, note);
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
    public void onConfirm(String tag) {
        switch (tag) {
            case TAG_DELETE_NOTE:
                if (mPresenter != null) {
                    mPresenter.delete();
                }
                break;
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
        mPresenter.addOnNoteChangedListener(new EditNoteContract.OnNoteChangedListener() {
            @Override
            public void onNoteChanged(boolean changed) {
                MenuUtil.show(mMenu, changed, R.id.menu_done);
            }
        });
        if (mViewCreated) {
            mPresenter.onCreateView(this);
        }
    }

    @Override
    public void onNotebookSelected(Notebook notebook) {
        if (mPresenter != null) {
            mPresenter.setNotebook(notebook);
        }
    }

    @Override
    public void onLabelChecked(List<Long> labels) {
        if (mPresenter != null) {
            mPresenter.setLabels(labels);
        }
    }

    @Override
    public void onMenuItemClick(BottomMenuDialog dialog, MenuItem item) {
        if (mPresenter != null) {
            switch (item.getItemId()) {
                case R.id.menu_select_notebook:
                    mPresenter.actionSelectNotebook();
                    break;

                case R.id.menu_labels:
                    mPresenter.actionCheckLabels();
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

                case R.id.menu_info:
                    mPresenter.actionInfo();
                    break;
            }
        }
        dialog.dismiss();
    }
}
