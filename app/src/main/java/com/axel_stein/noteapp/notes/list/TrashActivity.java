package com.axel_stein.noteapp.notes.list;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.axel_stein.domain.interactor.note.EmptyTrashInteractor;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.base.BaseActivity;
import com.axel_stein.noteapp.dialogs.ConfirmDialog;
import com.axel_stein.noteapp.notes.list.presenters.TrashNotesPresenter;
import com.axel_stein.noteapp.utils.MenuUtil;

import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class TrashActivity extends BaseActivity implements ConfirmDialog.OnConfirmListener {

    private static final String TAG_EMPTY_TRASH = "TAG_EMPTY_TRASH";
    private static final String TAG_FRAGMENT = "TAG_FRAGMENT";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Inject
    EmptyTrashInteractor mEmptyTrashInteractor;

    private NotesFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trash);

        App.getAppComponent().inject(this);
        ButterKnife.bind(this);
        EventBusHelper.subscribe(this);

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(TAG_FRAGMENT);
        if (fragment == null) {
            mFragment = new NotesFragment();
            mFragment.setPresenter(new TrashNotesPresenter());
            mFragment.setEmptyMsg(getString(R.string.empty_trash));
            setFragment(mFragment, TAG_FRAGMENT);
        } else {
            mFragment = (NotesFragment) fragment;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_trash, menu);
        MenuUtil.tintMenuIconsAttr(this, menu, R.attr.menuItemTintColor);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_empty_trash) {
            confirmEmptyTrashDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmEmptyTrashDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setTitle(R.string.title_empty_trash);
        dialog.setMessage(R.string.msg_empty_trash);
        dialog.setPositiveButtonText(R.string.action_empty_trash);
        dialog.setNegativeButtonText(R.string.action_cancel);
        dialog.show(getSupportFragmentManager(), TAG_EMPTY_TRASH);
    }

    private void emptyTrash() {
        mEmptyTrashInteractor.emptyTrash()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        EventBusHelper.showMessage(R.string.msg_trash_empty);
                        mFragment.setPresenter(new TrashNotesPresenter());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        EventBusHelper.showMessage(R.string.error);
                    }
                });
    }

    @Subscribe
    public void showMessage(EventBusHelper.Message e) {
        if (e.isRes()) {
            showMessage(e.getMsgRes());
        } else {
            showMessage(e.getMsg());
        }
    }

    private void showMessage(final String msg) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Snackbar.make(findViewById(R.id.coordinator_trash), msg, Snackbar.LENGTH_SHORT).show();
                } catch (Exception ignored) {
                }
            }
        }, 100);
    }

    private void showMessage(int msgRes) {
        showMessage(getString(msgRes));
    }

    @Override
    protected void onDestroy() {
        EventBusHelper.unsubscribe(this);
        super.onDestroy();
    }

    @Override
    public void onConfirm(String tag) {
        emptyTrash();
    }

    @Override
    public void onCancel(String tag) {

    }

}
