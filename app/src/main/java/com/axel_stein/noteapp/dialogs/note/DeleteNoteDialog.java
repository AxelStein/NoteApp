package com.axel_stein.noteapp.dialogs.note;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.axel_stein.domain.interactor.note.DeleteNoteInteractor;
import com.axel_stein.domain.model.Note;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.EventBusHelper;
import com.axel_stein.noteapp.R;
import com.axel_stein.noteapp.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

import static com.axel_stein.noteapp.utils.ObjectUtil.checkNotNull;

public class DeleteNoteDialog extends ConfirmDialog {

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
        dialog.show(fragment.getFragmentManager(), null);
    }

    public static void launch(Context context, FragmentManager manager, Note note) {
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

        List<Note> list = new ArrayList<>();
        list.add(note);
        return createDialog(context, list);
    }

    private static DeleteNoteDialog createDialog(Context context, List<Note> notes) {
        checkNotNull(context);
        checkNotNull(notes);

        boolean one = notes.size() > 0 && notes.size() == 1;

        DeleteNoteDialog dialog = new DeleteNoteDialog();
        dialog.mNotes = notes;
        dialog.setTitle(one ? R.string.title_delete_note : R.string.title_delete_notes);
        dialog.setMessage(one ? R.string.msg_delete_note : R.string.msg_delete_notes);
        dialog.setPositiveButtonText(R.string.action_delete);
        dialog.setNegativeButtonText(R.string.action_cancel);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent().inject(this);
    }

    @Override
    protected void onConfirm() {
        dismiss();
        mInteractor.execute(mNotes)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        int msg = mNotes.size() == 1 ? R.string.msg_note_deleted : R.string.msg_notes_deleted;
                        EventBusHelper.showMessage(msg);
                        EventBusHelper.updateNoteList();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        EventBusHelper.showMessage(R.string.error);
                    }
                });
    }

}
