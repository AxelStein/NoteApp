package com.axel_stein.noteapp.notes.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.base.BaseFragment;
import com.axel_stein.noteapp.utils.SimpleTextWatcher;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ViewNoteFragment extends BaseFragment {

    @BindView(R.id.text_title)
    TextView mTextTitle;

    @BindView(R.id.text_content)
    TextView mTextContent;

    @BindView(R.id.text_update)
    TextView mTextUpdate;

    @Inject
    AppSettingsRepository mAppSettings;

    private boolean mViewCreated;
    private boolean mTrash;
    private boolean mUpdate;
    private boolean mEditable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //App.getAppComponent().inject(this);

        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_edit_note, container, false);
        ButterKnife.bind(this, root);

        mTextTitle.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                /*
                if (mPresenter != null) {
                    mPresenter.setTitle(s.toString());
                }
                */
            }
        });
        mTextContent.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                /*
                if (mPresenter != null) {
                    mPresenter.setContent(s.toString());
                }
                */
            }
        });

        int baseFontSize = mAppSettings.getBaseFontSize();
        mTextTitle.setTextSize(baseFontSize + 4);
        mTextContent.setTextSize(baseFontSize);

        mViewCreated = true;

        /*
        if (mPresenter != null) {
            mPresenter.onCreateView(this);
        }
        */

        return root;
    }

    @Override
    public void onDestroyView() {
        mTextTitle = null;
        mTextContent = null;
        mTextUpdate = null;
        mViewCreated = false;
        /*
        if (mPresenter != null) {
            mPresenter.onDestroyView();
        }
        */
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        /*
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
        */
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
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
        */
        return super.onOptionsItemSelected(item);
    }

    /*
    @Override
    public void setEditable(boolean editable) {
        mEditable = editable;

        MenuUtil.show(mMenu, editable, R.id.menu_fullscreen, R.id.menu_done, R.id.menu);

        if (!mTrash) {
            ViewUtil.enable(editable, mTextTitle, mEditContent);
        }

        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    public void setNote(Note note) {
        String title = note.getTitle();
        String content = note.getContent();

        int selectionTitle = mTextTitle.getSelectionStart();
        mTextTitle.setText(title);
        if (selectionTitle > 0) {
            mTextTitle.setSelection(selectionTitle);
        }

        SpannableStringBuilder builder = new SpannableStringBuilder();
        if (!isEmpty(content)) {
            int start = -1;

            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);
                if (c == '*') {
                    if (start == -1) {
                        start = i;
                    } else {
                        builder.setSpan(new StyleSpan(BOLD), start, i-1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        start = -1;
                    }
                } else {
                    builder.append(c);
                }
            }
        }

        int selectionContent = mEditContent.getSelectionStart();
        mEditContent.setText(builder);
        //mEditContent.setText(content);
        if (selectionContent > 0) {
            mEditContent.setSelection(selectionContent);
        }

        if (isEmpty(title) && isEmpty(content)) {
            KeyboardUtil.show(mTextTitle);
        }

        mTrash = note.isTrash();
        mUpdate = note.getId() > 0;

        long date = System.currentTimeMillis();
        if (note.getId() > 0) {
            date = note.getUpdate();
        }
        mTextUpdate.setText(DateFormatter.formatDateTime(getContext(), date));

        ViewUtil.enable(!mTrash, mTextTitle, mEditContent);

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
    public void showMessage(int msg) {
        if (mTextTitle != null) {
            Snackbar.make(mTextTitle, msg, Snackbar.LENGTH_SHORT).show();
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
    public EditNoteContract.Presenter getPresenter() {
        return mPresenter;
    }

    public void setPresenter(@NonNull EditNoteContract.Presenter presenter) {
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
    */

}
