package com.axel_stein.noteapp.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.axel_stein.domain.interactor.note.EmptyTrashInteractor;
import com.axel_stein.domain.model.Note;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.dialogs.ConfirmDialog;
import com.axel_stein.noteapp.notes.list.NotesContract;
import com.axel_stein.noteapp.notes.list.NotesFragment;
import com.axel_stein.noteapp.notes.list.presenters.TrashNotesPresenter;
import com.axel_stein.noteapp.utils.MenuUtil;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class TrashFragment extends NotesFragment implements ConfirmDialog.OnConfirmListener {
    private static final String TAG_EMPTY_TRASH = "TAG_EMPTY_TRASH";

    @Inject
    EmptyTrashInteractor mEmptyTrashInteractor;

    private boolean mEmptyList = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent().inject(this);

        setHasOptionsMenu(true);

        setPresenter(new TrashNotesPresenter());
        setEmptyMsg(getString(R.string.empty_trash));
        setPaddingTop(8);
        setPaddingBottom(8);
    }

    @Override
    public void setNotes(List<Note> list) {
        super.setNotes(list);

        mEmptyList = list == null || list.size() == 0;

        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_trash, menu);
        MenuUtil.tintMenuIconsAttr(getContext(), menu, R.attr.menuItemTintColor);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuUtil.show(menu, !mEmptyList, R.id.menu_empty_trash);
        MenuUtil.show(menu, false, R.id.menu_sort);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_empty_trash:
                confirmEmptyTrashDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmEmptyTrashDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setTitle(R.string.title_empty_trash);
        dialog.setMessage(R.string.msg_empty_trash);
        dialog.setPositiveButtonText(R.string.action_empty_trash);
        dialog.setNegativeButtonText(R.string.action_cancel);
        dialog.setTargetFragment(this, 0);
        dialog.show(getFragmentManager(), TAG_EMPTY_TRASH);
    }

    @Override
    public void onConfirm(String tag) {
        if (tag != null) {
            switch (tag) {
                case TAG_EMPTY_TRASH:
                    emptyTrash();
                    break;
            }
        }
    }

    @Override
    public void onCancel(String tag) {

    }

    @SuppressLint("CheckResult")
    private void emptyTrash() {
        mEmptyTrashInteractor.emptyTrash()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() {
                        NotesContract.Presenter presenter = getPresenter();
                        if (presenter != null) {
                            presenter.forceUpdate();
                        }
                        EventBusHelper.showMessage(R.string.msg_trash_empty);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        EventBusHelper.showMessage(R.string.error);
                    }
                });
    }

}
