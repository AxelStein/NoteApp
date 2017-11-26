package com.axel_stein.noteapp.notes.edit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.text.Editable;
import android.text.Layout;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.axel_stein.data.AppSettingsRepository;
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
import com.axel_stein.noteapp.dialogs.notebook.SelectNotebookDialog;
import com.axel_stein.noteapp.notes.edit.EditNoteContract.Presenter;
import com.axel_stein.noteapp.utils.ColorUtil;
import com.axel_stein.noteapp.utils.DateFormatter;
import com.axel_stein.noteapp.utils.KeyboardUtil;
import com.axel_stein.noteapp.utils.MenuUtil;
import com.axel_stein.noteapp.utils.SimpleTextWatcher;
import com.axel_stein.noteapp.utils.ViewUtil;
import com.axel_stein.noteapp.views.SearchPanel;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static android.text.TextUtils.isEmpty;

public class EditNoteFragment extends BaseFragment implements EditNoteContract.View,
        ConfirmDialog.OnConfirmListener,
        SelectNotebookDialog.OnNotebookSelectedListener,
        CheckLabelsDialog.OnLabelCheckedListener,
        BottomMenuDialog.OnMenuItemClickListener {

    private static final String TAG_SAVE_NOTE = "TAG_SAVE_NOTE";

    private static final String TAG_DELETE_NOTE = "TAG_DELETE_NOTE";

    @BindView(R.id.scroll_view)
    NestedScrollView mScrollView;

    @BindView(R.id.edit_title)
    EditText mEditTitle;

    @BindView(R.id.edit_content)
    EditText mEditContent;

    @BindView(R.id.text_update)
    TextView mTextUpdate;

    @Nullable
    SearchPanel mSearchPanel;

    @Inject
    QueryNotebookInteractor mQueryNotebookInteractor;

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
    private boolean mMenuItemEnabled = true;
    private List<Integer> mIndexes;
    private int mPreviousIndex;

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

    public void setSearchPanel(@Nullable SearchPanel searchPanel) {
        mSearchPanel = searchPanel;
        if (mSearchPanel != null) {
            mSearchPanel.setCallback(new SearchPanel.Callback() {
                @Override
                public void onQueryTextChange(String q) {
                    if (!mViewCreated) {
                        return;
                    }

                    String content = mEditContent.getText().toString();
                    mEditContent.setText(content);

                    if (isEmpty(q)) {
                        if (mSearchPanel != null) {
                            mSearchPanel.setQueryResultCount(0);
                        }
                        mIndexes = null;
                        mPreviousIndex = -1;
                        return;
                    }

                    Pattern pattern = Pattern.compile(q, Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(content);

                    int color = ColorUtil.getColorAttr(getContext(), R.attr.searchSelectorColor);
                    int resultCount = 0;

                    mIndexes = new ArrayList<>();
                    mPreviousIndex = -1;

                    while (matcher.find()) {
                        resultCount++;

                        int start = matcher.start();
                        mIndexes.add(start);

                        setSpan(color, start, matcher.end());
                    }

                    if (mSearchPanel != null) {
                        mSearchPanel.setQueryResultCount(resultCount);
                    }
                }

                @Override
                public void onCursorChange(int cursor) {
                    if (mIndexes != null && mViewCreated) {
                        --cursor;

                        final int index = mIndexes.get(cursor);
                        int queryLength = mSearchPanel.getQuery().length();
                        int end = index + queryLength;
                        int currentColor = ColorUtil.getColorAttr(getContext(), R.attr.searchSelectorCurrentColor);
                        setSpan(currentColor, index, end);

                        if (mPreviousIndex != -1 && mPreviousIndex != index) {
                            int color = ColorUtil.getColorAttr(getContext(), R.attr.searchSelectorColor);
                            setSpan(color, mPreviousIndex, mPreviousIndex + queryLength);
                        }
                        mPreviousIndex = index;

                        Layout layout = mEditContent.getLayout();
                        if (layout == null) {
                            mEditContent.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mEditContent != null) {
                                        Layout layout = mEditContent.getLayout();
                                        if (layout != null) {
                                            int line = layout.getLineForOffset(index);
                                            int y = layout.getLineBottom(line);
                                            mScrollView.scrollTo(0, y);
                                        }
                                    }
                                }
                            });
                        } else {
                            int line = layout.getLineForOffset(index);
                            int y = layout.getLineBottom(line);
                            mScrollView.scrollTo(0, y);
                        }
                    }
                }

                @Override
                public void onShow() {
                    setEditViewsFocusable(false);
                    enableMenuItem(false);
                }

                @Override
                public void onClose() {
                    setEditViewsFocusable(true);
                    enableMenuItem(true);

                    mIndexes = null;
                    mPreviousIndex = -1;
                    if (mViewCreated) {
                        mEditContent.setText(mEditContent.getText().toString());
                    }
                }
            });
        }
    }

    private void setEditViewsFocusable(boolean focusable) {
        mEditViewsFocusable = focusable;

        if (mViewCreated) {
            mEditTitle.setFocusable(focusable);
            mEditTitle.setFocusableInTouchMode(focusable);

            mEditContent.setFocusable(focusable);
            mEditContent.setFocusableInTouchMode(focusable);
        }
    }

    private void enableMenuItem(boolean enabled) {
        mMenuItemEnabled = enabled;
        MenuUtil.enable(mMenu, enabled, R.id.menu);
    }

    private void setSpan(int color, int start, int end) {
        BackgroundColorSpan span = new BackgroundColorSpan(color);
        mEditContent.getText().setSpan(span, start, end, SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public void onButtonMenuClick() {
        new BottomMenuDialog.Builder()
                .setMenuRes(R.menu.bottom_menu_edit_note)
                .showMenuItem(R.id.menu_restore, mTrash)
                .showMenuItem(R.id.menu_delete, mUpdate)
                .showMenuItem(R.id.menu_move_to_trash, !mTrash && mUpdate)
                .showMenuItem(R.id.menu_select_notebook, !mTrash)
                .showMenuItem(R.id.menu_labels, !mTrash)
                .showMenuItem(R.id.menu_share, !mTrash)
                .showMenuItem(R.id.menu_search, !mTrash)
                .showMenuItem(R.id.menu_duplicate, !mTrash && mUpdate)
                .show(EditNoteFragment.this);
    }

    @Override
    public void onDestroyView() {
        mSearchPanel = null;
        mEditTitle = null;
        mEditContent = null;
        mTextUpdate = null;
        mViewCreated = false;
        mMenu = null;
        mScrollView = null;
        if (mPresenter != null) {
            mPresenter.onDestroyView();
        }
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        mMenu = menu;
        MenuUtil.show(mMenu, mEditable, R.id.menu_fullscreen, R.id.menu_done);
        enableMenuItem(mMenuItemEnabled);

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_done:
                if (mPresenter != null) {
                    mPresenter.save();
                }
                break;

            case R.id.menu:
                onButtonMenuClick();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setEditable(boolean editable) {
        mEditable = editable;

        MenuUtil.show(mMenu, editable, R.id.menu_fullscreen, R.id.menu_done, R.id.menu);

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

                case R.id.menu_search:
                    if (mSearchPanel != null) {
                        mSearchPanel.show();
                    }
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
                    mPresenter.actionDuplicate(getString(R.string.copy));
                    break;
            }
        }
        dialog.dismiss();
    }
}
