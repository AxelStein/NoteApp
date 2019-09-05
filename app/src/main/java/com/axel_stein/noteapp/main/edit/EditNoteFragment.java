package com.axel_stein.noteapp.main.edit;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

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
import com.axel_stein.noteapp.utils.KeyboardUtil;
import com.axel_stein.noteapp.utils.MenuUtil;
import com.axel_stein.noteapp.utils.SimpleTextWatcher;
import com.axel_stein.noteapp.utils.ViewUtil;
import com.axel_stein.noteapp.views.IconTextView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static android.text.TextUtils.isEmpty;

public class EditNoteFragment extends Fragment implements EditNoteContract.View,
        ConfirmDialog.OnConfirmListener,
        SelectNotebookDialog.OnMenuItemClickListener {

    private static final String TAG_SAVE_NOTE = "TAG_SAVE_NOTE";
    private static final String TAG_DELETE_NOTE = "TAG_DELETE_NOTE";
    private static final String TAG_SELECT_NOTEBOOK = "TAG_SELECT_NOTEBOOK";

    private EditText mEditTitle;
    private EditText mEditContent;

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

        MenuUtil.show(mMenu, !mTrash, R.id.menu_pin_note, R.id.menu_star_note);
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
    public void setNotebookTitle(String title) {
        if (isEmpty(title)) {
            title = getString(R.string.action_inbox);
        }
        ViewUtil.show(!isEmpty(title), mNotebookView);
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

}
