package com.axel_stein.domain.interactor.note;

import androidx.annotation.NonNull;

import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.repository.NoteRepository;
import com.axel_stein.domain.repository.ReminderRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.TextUtil.isEmpty;

public class DeleteNoteInteractor {

    @NonNull
    private final NoteRepository mRepository;

    @NonNull
    private final ReminderRepository mReminderRepository;

    public DeleteNoteInteractor(@NonNull NoteRepository n, @NonNull ReminderRepository r) {
        mRepository = requireNonNull(n);
        mReminderRepository = requireNonNull(r);
    }

    /**
     * @param note to delete
     * @throws NullPointerException     if note is null
     * @throws IllegalArgumentException if id is 0
     */
    public Completable execute(@NonNull final Note note) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                deleteImpl(note);
            }
        }).subscribeOn(Schedulers.io());
    }

    /**
     * @throws NullPointerException     if notes is null
     * @throws IllegalArgumentException if id is 0
     */
    public Completable execute(@NonNull final List<Note> notes) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                for (Note note : notes) {
                    deleteImpl(note);
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    private void deleteImpl(Note note) {
        if (note == null || isEmpty(note.getId())) {
            throw new IllegalArgumentException("note is not valid");
        }
        if (note.hasReminder()) {
            mReminderRepository.delete(note.getReminderId());
        }
        mRepository.delete(note);
    }

}
