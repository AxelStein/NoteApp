package com.axel_stein.noteapp.main.edit;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.domain.interactor.note.DeleteNoteInteractor;
import com.axel_stein.domain.interactor.note.GetNoteInteractor;
import com.axel_stein.domain.interactor.note.InsertNoteInteractor;
import com.axel_stein.domain.interactor.note.SetNotebookNoteInteractor;
import com.axel_stein.domain.interactor.note.SetPinnedNoteInteractor;
import com.axel_stein.domain.interactor.note.SetStarredNoteInteractor;
import com.axel_stein.domain.interactor.note.SetTrashedNoteInteractor;
import com.axel_stein.domain.interactor.note.UpdateNoteInteractor;
import com.axel_stein.domain.interactor.notebook.GetNotebookInteractor;
import com.axel_stein.domain.interactor.notebook.QueryNotebookInteractor;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.base.BaseActivity;
import com.axel_stein.noteapp.dialogs.ConfirmDialog;
import com.axel_stein.noteapp.dialogs.notebook.AddNotebookDialog;
import com.axel_stein.noteapp.dialogs.select_notebook.SelectNotebookDialog;
import com.axel_stein.noteapp.utils.DateFormatter;
import com.axel_stein.noteapp.utils.MenuUtil;
import com.axel_stein.noteapp.utils.SimpleCompletableObserver;
import com.axel_stein.noteapp.utils.SimpleSingleObserver;
import com.axel_stein.noteapp.utils.SimpleTextWatcher;
import com.axel_stein.noteapp.utils.ViewUtil;
import com.axel_stein.noteapp.views.IconTextView;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.CompletableObserver;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static com.axel_stein.domain.utils.TextUtil.isEmpty;

public class EditNoteActivity extends BaseActivity implements SelectNotebookDialog.OnMenuItemClickListener,
        ConfirmDialog.OnConfirmListener {
    private static final String EXTRA_NOTE_ID = "com.axel_stein.noteapp.EXTRA_NOTE_ID";
    private static final String EXTRA_NOTEBOOK_ID = "com.axel_stein.noteapp.EXTRA_NOTEBOOK_ID";
    private static final String EXTRA_EDIT_TITLE_CURSOR = "EXTRA_EDIT_TITLE_CURSOR";
    private static final String EXTRA_EDIT_CONTENT_CURSOR = "EXTRA_EDIT_CONTENT_CURSOR";
    private static final String EXTRA_CHECK_LIST_JSON_SRC = "EXTRA_CHECK_LIST_JSON_SRC";
    private static final String TAG_DELETE_NOTE = "TAG_DELETE_NOTE";
    private static final String TAG_SELECT_NOTEBOOK = "TAG_SELECT_NOTEBOOK";
    private static final int INPUT_DELAY = 600;

    public static void launch(Context context, @Nullable String notebookId) {
        Intent intent = new Intent(context, EditNoteActivity.class);
        intent.putExtra(EXTRA_NOTEBOOK_ID, notebookId);
        context.startActivity(intent);
    }

    public static void launch(Context context, @NonNull Note note) {
        Intent intent = new Intent(context, EditNoteActivity.class);
        intent.putExtra(EXTRA_NOTE_ID, note.getId());
        context.startActivity(intent);
    }

    private Toolbar mToolbar;
    private IconTextView mNotebookView;
    private EditText mEditTitle;
    private EditText mEditContent;
    private View mFocusView;
    private TextView mTextModifiedDate;
    private TextView mTextViews;
    private RecyclerView mCheckRecyclerView;
    private CheckListAdapter mCheckListAdapter;
    private View mScrollView;
    private View mGlassView;
    private Note mNote;
    private Handler mHandler;
    private Runnable mEditTitleTask;
    private Runnable mEditContentTask;
    private TextWatcher mEditTitleTextWatcher;
    private TextWatcher mEditContentTextWatcher;
    private String mCheckListJsonSrc;

    @Inject
    GetNoteInteractor mGetNoteInteractor;

    @Inject
    InsertNoteInteractor mInsertNoteInteractor;

    @Inject
    UpdateNoteInteractor mUpdateNoteInteractor;

    @Inject
    AppSettingsRepository mAppSettings;

    @Inject
    GetNotebookInteractor mGetNotebookInteractor;

    @Inject
    QueryNotebookInteractor mQueryNotebookInteractor;

    @Inject
    SetStarredNoteInteractor mSetStarredNoteInteractor;

    @Inject
    SetPinnedNoteInteractor mSetPinnedNoteInteractor;

    @Inject
    SetTrashedNoteInteractor mSetTrashedNoteInteractor;

    @Inject
    SetNotebookNoteInteractor mSetNotebookNoteInteractor;

    @Inject
    DeleteNoteInteractor mDeleteNoteInteractor;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note_2);

        App.getAppComponent().inject(this);
        EventBusHelper.subscribe(this);

        mToolbar = findViewById(R.id.toolbar);
        mNotebookView = findViewById(R.id.notebook);
        mNotebookView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNotebookViewClick();
            }
        });
        mScrollView = findViewById(R.id.scroll_view);
        mGlassView = findViewById(R.id.glass_view);
        mFocusView = findViewById(R.id.focus_view);
        mCheckRecyclerView = findViewById(R.id.check_recycler_view);
        mEditTitle = findViewById(R.id.edit_title);
        mEditContent = findViewById(R.id.edit_content);
        mTextModifiedDate = findViewById(R.id.text_modified_date);
        mTextViews = findViewById(R.id.text_views);

        int baseFontSize = mAppSettings.getBaseFontSize();
        mEditTitle.setTextSize(baseFontSize + 4);
        mEditContent.setTextSize(baseFontSize);


        mHandler = new Handler(Looper.getMainLooper());
        mEditTitleTask = new Runnable() {
            @Override
            public void run() {
                updateNoteTitle(mEditTitle.getText().toString());
            }
        };
        mEditTitleTextWatcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.equals(s, mNote.getTitle())) {
                    mNote.setTitle(s.toString());

                    mHandler.removeCallbacks(mEditTitleTask);
                    mHandler.postDelayed(mEditTitleTask, INPUT_DELAY);
                }
            }
        };

        mEditContentTask = new Runnable() {
            @Override
            public void run() {
                updateNoteContent(mEditContent.getText().toString());
            }
        };
        mEditContentTextWatcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.equals(s, mNote.getContent())) {
                    mNote.setContent(s.toString());

                    mHandler.removeCallbacks(mEditContentTask);
                    mHandler.postDelayed(mEditContentTask, INPUT_DELAY);
                }
                mEditContent.setLineSpacing(0, 1);
                mEditContent.setLineSpacing(0, 1.5f);
            }
        };

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        // setEditViewsFocusable(mEditViewsFocusable);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            String noteId = intent.getStringExtra(EXTRA_NOTE_ID);
            String notebookId = intent.getStringExtra(EXTRA_NOTEBOOK_ID);

            if (isEmpty(noteId)) { // create note
                mNote = new Note();
                mNote.setNotebookId(notebookId);

                // handle send intent
                String action = intent.getAction();
                String type = intent.getType();
                if (Intent.ACTION_SEND.equals(action)) {
                    if ("text/plain".equals(type)) {
                        mNote.setTitle(intent.getStringExtra(Intent.EXTRA_SUBJECT));
                        mNote.setContent(intent.getStringExtra(Intent.EXTRA_TEXT));
                    }
                }

                mInsertNoteInteractor.execute(mNote)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CompletableObserver() {
                            @Override
                            public void onSubscribe(Disposable d) {
                            }

                            @Override
                            public void onComplete() {
                                setNoteLoaded(mNote);
                                editTitleRequestFocus();
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                                finish();
                            }
                        });
            } else {
                mGetNoteInteractor.execute(noteId, true)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<Note>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                            }

                            @Override
                            public void onSuccess(Note note) {
                                setNoteLoaded(note);
                                editTitleRequestFocus();
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                                finish();
                            }
                        });
            }
        } else { // restore
            String noteId = savedInstanceState.getString(EXTRA_NOTE_ID);
            mCheckListJsonSrc = savedInstanceState.getString(EXTRA_CHECK_LIST_JSON_SRC);
            mGetNoteInteractor.execute(noteId, false)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Note>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Note note) {
                            setNoteLoaded(note);
                            if (mEditTitle.hasFocus()) {
                                mEditTitle.setSelection(savedInstanceState.getInt(EXTRA_EDIT_TITLE_CURSOR));
                            }
                            if (mEditContent.hasFocus()) {
                                mEditContent.setSelection(savedInstanceState.getInt(EXTRA_EDIT_CONTENT_CURSOR));
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }
                    });
        }

        final View viewMain = findViewById(R.id.coordinator_edit);
        viewMain.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                viewMain.getWindowVisibleDisplayFrame(r);
                int screenHeight = viewMain.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;
                if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                    // keyboard is opened
                    if (!isKeyboardShowing) {
                        isKeyboardShowing = true;
                        onKeyboardVisibilityChanged(true);
                    }
                } else {
                    // keyboard is closed
                    if (isKeyboardShowing) {
                        isKeyboardShowing = false;
                        onKeyboardVisibilityChanged(false);
                    }
                }
            }
        });
    }

    private boolean isKeyboardShowing = false;
    private void onKeyboardVisibilityChanged(boolean opened) {
        if (!opened) {
            EventBusHelper.hideKeyboard();
        }
    }

    @Subscribe
    public void onHideKeyboard(EventBusHelper.HideKeyboard e) {
        if (!mFocusView.hasFocus()) {
            mFocusView.requestFocus();
        }
    }

    private void editTitleRequestFocus() {
        if (!mNote.isTrashed() && isEmptyNote()) {
            mEditTitle.requestFocus();
        }
    }

    @Override
    protected void onStop() {
        if (mNote.isCheckList()) {
            List<CheckItem> items = getCheckItems();
            mNote.setCheckListJson(CheckListHelper.toJson(items));
            if (!TextUtils.equals(mNote.getCheckListJson(), mCheckListJsonSrc)) {
                if (items.size() > 0) {
                    mNote.setTitle(items.get(0).getText());
                }
                mNote.setContent(CheckListHelper.toContentFromCheckList(items));

                mUpdateNoteInteractor.updateTitle(mNote.getId(), mNote.getTitle())
                        .andThen(mUpdateNoteInteractor.updateContent(mNote.getId(), mNote.getContent()))
                        .andThen(mUpdateNoteInteractor.updateCheckListJson(mNote.getId(), mNote.getCheckListJson()))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SimpleCompletableObserver() {
                            @Override
                            public void onComplete() {
                                EventBusHelper.updateNoteList();
                            }
                        });
            }
        }
        super.onStop();
    }

    private void updateNoteTitle(String title) {
        mUpdateNoteInteractor.updateTitle(mNote.getId(), title)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleCompletableObserver());
    }

    private void updateNoteContent(String content) {
        mUpdateNoteInteractor.updateContent(mNote.getId(), content)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleCompletableObserver());
    }

    /*
    private void updateNoteCheckListJson(String checkListJson) {
        mUpdateNoteInteractor.updateCheckListJson(mNote.getId(), checkListJson)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {}

                    @Override
                    public void onComplete() {}

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        showMessage(R.string.error);
                    }
                });
    }
    */

    @Override
    protected void onDestroy() {
        mEditTitle.removeTextChangedListener(mEditTitleTextWatcher);
        mEditContent.removeTextChangedListener(mEditContentTextWatcher);
        EventBusHelper.updateNoteList();
        EventBusHelper.unsubscribe(this);
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_NOTE_ID, mNote.getId());
        outState.putString(EXTRA_NOTEBOOK_ID, mNote.getNotebookId());
        outState.putInt(EXTRA_EDIT_TITLE_CURSOR, mEditTitle.getSelectionStart());
        outState.putInt(EXTRA_EDIT_CONTENT_CURSOR, mEditContent.getSelectionStart());
        outState.putString(EXTRA_CHECK_LIST_JSON_SRC, mCheckListJsonSrc);
    }

    private void setNoteLoaded(Note note) {
        mNote = note;

        fetchNotebookTitle(mNote.getNotebookId());

        if (mNote.isCheckList()) {
            mCheckListJsonSrc = mNote.getCheckListJson();
            ViewUtil.setVisible(mNote.isTrashed(), mGlassView);
            showCheckList(CheckListHelper.fromJson(mNote.getTitle(), mNote.getCheckListJson()));
        } else {
            mEditTitle.setText(mNote.getTitle());
            mEditContent.setText(mNote.getContent());

            mEditTitle.addTextChangedListener(mEditTitleTextWatcher);
            mEditContent.addTextChangedListener(mEditContentTextWatcher);
        }

        ViewUtil.enable(!mNote.isTrashed(), mEditTitle, mEditContent, mNotebookView,
                mCheckRecyclerView, mTextModifiedDate, mTextViews);

        mTextModifiedDate.setText(DateFormatter.formatDateTime(this, mNote.getModifiedDate().getMillis()));

        long views = mNote.getViews();
        ViewUtil.setVisible(views > 0, mTextViews);
        mTextViews.setText(String.valueOf(views));

        invalidateOptionsMenu();
    }

    /*
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
    */

    private void setNotebookTitle(String title) {
        if (TextUtils.isEmpty(title)) {
            title = getString(R.string.action_inbox);
        }
        ViewUtil.setVisible(!TextUtils.isEmpty(title), mNotebookView);
        ViewUtil.setText(mNotebookView, title);
    }

    private void fetchNotebookTitle(String notebookId) {
        if (TextUtils.isEmpty(notebookId)) {
            setNotebookTitle(null);
        } else {
            mGetNotebookInteractor.execute(notebookId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleSingleObserver<Notebook>() {
                        @Override
                        public void onSuccess(Notebook notebook) {
                            setNotebookTitle(notebook.getTitle());
                        }
                    });
        }
    }

    public void onNotebookViewClick() {
        mQueryNotebookInteractor.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleSingleObserver<List<Notebook>>() {
                    @Override
                    public void onSuccess(List<Notebook> notebooks) {
                        showSelectNotebookDialog(notebooks);
                    }
                });
    }

    private void showSelectNotebookDialog(List<Notebook> notebooks) {
        String notebookId = mNote.getNotebookId();
        if (isEmpty(notebookId)) {
            notebookId = Notebook.ID_INBOX;
        }
        SelectNotebookDialog.Builder builder = new SelectNotebookDialog.Builder();
        builder.setTitle(getString(R.string.title_select_notebook));
        builder.setAction(getString(R.string.action_new_notebook));

        List<Notebook> items = new ArrayList<>(notebooks);
        items.add(0, Notebook.from(Notebook.ID_INBOX, getString(R.string.action_inbox)));
        builder.setItems(items);

        builder.setSelectedNotebookId(notebookId);
        builder.show(getSupportFragmentManager(), TAG_SELECT_NOTEBOOK);
    }

    @Override
    public void onMenuItemClick(SelectNotebookDialog dialog, String tag, Notebook notebook) {
        dialog.dismiss();
        if (notebook == null) {
            notebook = new Notebook();
        }
        setNotebook(notebook);
    }

    private void setNotebook(final Notebook notebook) {
        mSetNotebookNoteInteractor.execute(mNote, notebook.getId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleCompletableObserver() {
                    @Override
                    public void onComplete() {
                        mNote.setNotebook(notebook);
                        setNotebookTitle(notebook.getTitle());
                        EventBusHelper.updateNoteList();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        showMessage(R.string.error);
                    }
                });
    }

    @Override
    public void onActionClick(SelectNotebookDialog dialog) {
        dialog.dismiss();
        AddNotebookDialog.launch(this);
    }

    @Subscribe
    public void onNotebookAdded(EventBusHelper.AddNotebook e) {
        setNotebook(e.getNotebook());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_edit_note, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mNote != null) {
            boolean trash = mNote.isTrashed();
            MenuUtil.show(menu, !trash, R.id.menu_pin_note, R.id.menu_star_note, R.id.menu_check_list,
                    R.id.menu_select_notebook, R.id.menu_share,
                    R.id.menu_move_to_trash, R.id.menu_duplicate);
            MenuUtil.show(menu, trash, R.id.menu_restore, R.id.menu_delete);

            MenuItem itemPinned = menu.findItem(R.id.menu_pin_note);
            if (itemPinned != null) {
                itemPinned.setIcon(mNote.isPinned() ? R.drawable.ic_bookmark_24dp : R.drawable.ic_bookmark_border_24dp);
                //MenuUtil.tintAttr(this, itemPinned, R.attr.menuItemTintColor);
            }

            MenuItem itemStarred = menu.findItem(R.id.menu_star_note);
            if (itemStarred != null) {
                itemStarred.setIcon(mNote.isStarred() ? R.drawable.ic_star_24dp : R.drawable.ic_star_border_24dp);
                //MenuUtil.tintAttr(this, itemStarred, R.attr.menuItemTintColor);
            }
            MenuUtil.tintMenuIconsAttr(this, menu, R.attr.menuItemTintColor);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_pin_note:
                actionPinNote();
                break;

            case R.id.menu_star_note:
                actionStarNote();
                break;

            case R.id.menu_move_to_trash:
                actionMoveToTrash();
                break;

            case R.id.menu_restore:
                actionRestore();
                break;

            case R.id.menu_delete:
                showConfirmDeleteNoteDialog();
                break;

            case R.id.menu_share:
                actionShare();
                break;

            case R.id.menu_duplicate:
                actionDuplicate();
                break;

            case R.id.menu_check_list:
                actionCheckList();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void actionStarNote() {
        final boolean val = !mNote.isStarred();
        mSetStarredNoteInteractor.execute(mNote, val)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        mNote.setStarred(val);
                        invalidateOptionsMenu();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        showMessage(R.string.error);
                    }
                });
    }

    private void actionPinNote() {
        final boolean val = !mNote.isPinned();
        mSetPinnedNoteInteractor.execute(mNote, val)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        mNote.setPinned(val);
                        invalidateOptionsMenu();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        showMessage(R.string.error);
                    }
                });
    }

    private void actionMoveToTrash() {
        // setEditableImpl(false);
        mSetTrashedNoteInteractor.execute(mNote, true)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        finish();

                        EventBusHelper.showMessage(R.string.msg_note_trashed, R.string.action_undo, new Runnable() {
                            @Override
                            public void run() {
                                actionRestore();
                            }
                        });
                        EventBusHelper.updateNoteList();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        showMessage(R.string.error);
                    }
                });
    }

    private void actionRestore() {
        // setEditableImpl(false);
        mSetTrashedNoteInteractor.execute(mNote, false)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        finish();
                        EventBusHelper.showMessage(R.string.msg_note_restored, R.string.action_undo, new Runnable() {
                            @Override
                            public void run() {
                                actionMoveToTrash();
                            }
                        });
                        EventBusHelper.updateNoteList();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        showMessage(R.string.error);
                    }
                });
    }

    private void actionShare() {
        if (isEmptyNote()) {
            showMessage(R.string.msg_note_empty);
            return;
        }

        String title = mNote.getTitle();
        String content = mNote.getContent();
        /*
        if (TextUtils.isEmpty(content)) {
            content = title;
        }
        */

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        intent.putExtra(Intent.EXTRA_TEXT, content);

        PackageManager pm = getPackageManager();
        if (pm != null) {
            if (intent.resolveActivity(pm) != null) {
                startActivity(intent);
            } else {
                Log.e("TAG", "share note: no activity found");
                showMessage(R.string.error_share);
            }
        }
    }

    private boolean isEmptyNote() {
        return mNote != null && TextUtils.isEmpty(mNote.getTitle()) && TextUtils.isEmpty(mNote.getContent());
    }

    private void actionDuplicate() {
        String copySuffix = getString(R.string.text_copy);

        Note duplicate = mNote.copy();
        duplicate.setId(null);

        String title = duplicate.getTitle();
        String content = duplicate.getContent();

        if (!TextUtils.isEmpty(title)) {
            duplicate.setTitle(String.format("%s (%s)", title, copySuffix));
        } else if (!TextUtils.isEmpty(content)) {
            duplicate.setContent(String.format("%s (%s)", content, copySuffix));
        }

        mInsertNoteInteractor.execute(duplicate)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleCompletableObserver() {
                    @Override
                    public void onComplete() {
                        showMessage(R.string.msg_note_duplicated);
                        EventBusHelper.updateNoteList();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        showMessage(R.string.error);
                    }
                });
    }

    public void showConfirmDeleteNoteDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setTitle(R.string.title_delete_note);
        dialog.setMessage(R.string.msg_delete_note);
        dialog.setPositiveButtonText(R.string.action_delete);
        dialog.setNegativeButtonText(R.string.action_cancel);
        dialog.show(getSupportFragmentManager(), TAG_DELETE_NOTE);
    }

    @Override
    public void onConfirm(String tag) {
        if (TAG_DELETE_NOTE.equals(tag)) {
            actionDelete();
        }
    }

    private void actionDelete() {
        mDeleteNoteInteractor.execute(mNote)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleCompletableObserver() {
                    @Override
                    public void onComplete() {
                        finish();
                        EventBusHelper.showMessage(R.string.msg_note_deleted);
                        EventBusHelper.updateNoteList();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        showMessage(R.string.error);
                    }
                });
    }

    @Override
    public void onCancel(String tag) {}

    private void actionCheckList() {
        if (mNote.isCheckList()) {
            mNote.setCheckList(false);
            mNote.setCheckListJson(null);

            List<CheckItem> items = getCheckItems();
            if (items.size() > 0) {
                mNote.setTitle(items.get(0).getText());
            }
            mNote.setContent(CheckListHelper.toContentFromCheckList(items));
            mEditTitle.setText(mNote.getTitle());
            mEditContent.setText(mNote.getContent());

            hideCheckList();
        } else {
            mNote.setCheckList(true);
            showCheckList(CheckListHelper.checkListFromContent(mNote.getTitle(), mNote.getContent()));
        }
        updateIsCheckList(mNote.isCheckList());
    }

    private void updateIsCheckList(boolean isCheckList) {
        mNote.setCheckList(isCheckList);
        mUpdateNoteInteractor.updateIsCheckList(mNote.getId(), isCheckList)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleCompletableObserver());
    }

    private void showCheckList(List<CheckItem> items) {
        ViewUtil.show(mFocusView, mCheckRecyclerView);
        ViewUtil.hide(mScrollView);

        mCheckListAdapter = new CheckListAdapter();
        mCheckListAdapter.setItems(items);

        String date = DateFormatter.formatDateTime(this, mNote.getModifiedDate().getMillis());
        mCheckListAdapter.addItem(new DataCheckItem(date, mNote.getViews()));

        mCheckRecyclerView.setAdapter(mCheckListAdapter);
        mCheckRecyclerView.setHasFixedSize(true);
        mCheckRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCheckListAdapter.setRecyclerView(mCheckRecyclerView);
        mCheckListAdapter.getItemTouchHelper().attachToRecyclerView(mCheckRecyclerView);
    }

    private void hideCheckList() {
        ViewUtil.hide(mFocusView, mCheckRecyclerView);
        ViewUtil.show(mScrollView);
        mCheckListAdapter = null;
    }

    @NonNull
    private List<CheckItem> getCheckItems() {
        if (mCheckListAdapter != null) {
            return mCheckListAdapter.getItems();
        }
        return new ArrayList<>();
    }

    private void showMessage(int msg) {
        Snackbar.make(mToolbar, msg, Snackbar.LENGTH_SHORT).show();
    }

}
