package com.axel_stein.noteapp.dialogs.note;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.axel_stein.domain.interactor.note.DeleteNoteInteractor;
import com.axel_stein.domain.model.Note;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static com.axel_stein.noteapp.utils.ObjectUtil.checkNotNull;

public class DeleteNoteDialog extends ConfirmDialog {
    private static final String BUNDLE_IDS = "BUNDLE_IDS";

    @Inject
    DeleteNoteInteractor mInteractor;

    private List<Note> mNotes;

    public static void launch(AppCompatActivity activity, Note note) {
        checkNotNull(activity);
        launch(activity, activity.getSupportFragmentManager(), note);
    }

    public static void launch(AppCompatActivity activity, List<Note> notes) {
        checkNotNull(activity);
        launch(activity, activity.getSupportFragmentManager(), notes);
    }

    public static void launch(Context context, Fragment fragment, Note note) {
        checkNotNull(fragment);

        DeleteNoteDialog dialog = createDialog(context, note);
        dialog.setTargetFragment(fragment, 0);
        assert fragment.getFragmentManager() != null;
        dialog.show(fragment.getFragmentManager(), null);
    }

    private static void launch(Context context, FragmentManager manager, Note note) {
        checkNotNull(manager);
        createDialog(context, note).show(manager, null);
    }

    public static void launch(Context context, FragmentManager manager, List<Note> notes) {
        checkNotNull(manager);
        createDialog(context, notes).show(manager, null);
    }

    private static DeleteNoteDialog createDialog(Context context, Note note) {
        checkNotNull(context);
        checkNotNull(note);
        return createDialog(context, Collections.singletonList(note));
    }

    private static DeleteNoteDialog createDialog(Context context, List<Note> notes) {
        checkNotNull(context);
        checkNotNull(notes);

        boolean one = notes.size() == 1;

        DeleteNoteDialog dialog = new DeleteNoteDialog();
        dialog.mNotes = notes;
        dialog.setTitle(R.string.title_delete_note);
        dialog.setMessage(one ? R.string.msg_delete_note : R.string.msg_delete_notes);
        dialog.setPositiveButtonText(R.string.action_delete);
        dialog.setNegativeButtonText(R.string.action_cancel);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent().inject(this);
        if (savedInstanceState != null) {
            mNotes = new ArrayList<>();
            ArrayList<String> ids = savedInstanceState.getStringArrayList(BUNDLE_IDS);
            if (ids != null) {
                for (String id : ids) {
                    mNotes.add(new Note(id));
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        ArrayList<String> ids = new ArrayList<>();
        for (Note n : mNotes) {
            ids.add(n.getId());
        }
        outState.putStringArrayList(BUNDLE_IDS, ids);
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onConfirm() {
        dismiss();
        mInteractor.execute(mNotes)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        int msg = mNotes.size() == 1 ? R.string.msg_note_deleted : R.string.msg_notes_deleted;
                        EventBusHelper.showMessage(msg);
                        EventBusHelper.updateNoteList();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        EventBusHelper.showMessage(R.string.error);
                    }
                });
    }

}
