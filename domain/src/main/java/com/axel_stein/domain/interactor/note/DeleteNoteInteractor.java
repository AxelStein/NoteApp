package com.axel_stein.domain.interactor.note;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.repository.DriveSyncRepository;
import com.axel_stein.domain.repository.NoteLabelPairRepository;
import com.axel_stein.domain.repository.NoteRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.NoteValidator.isValid;

public class DeleteNoteInteractor {

    @NonNull
    private NoteRepository mRepository;

    @NonNull
    private NoteLabelPairRepository mNoteLabelPairRepository;

    @NonNull
    private DriveSyncRepository mDriveSyncRepository;

    public DeleteNoteInteractor(@NonNull NoteRepository r, @NonNull NoteLabelPairRepository p, @NonNull DriveSyncRepository d) {
        mRepository = requireNonNull(r);
        mNoteLabelPairRepository = requireNonNull(p);
        mDriveSyncRepository = requireNonNull(d);
    }

    /**
     * @param note to delete
     * @throws NullPointerException     if note is null
     * @throws IllegalArgumentException if id is 0
     */
    public Completable execute(@NonNull final Note note) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (!isValid(note)) {
                    throw new IllegalArgumentException("note is not valid");
                }
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
            public void run() throws Exception {
                if (!isValid(notes)) {
                    throw new IllegalArgumentException("notes is not valid");
                }
                for (Note note : notes) {
                    deleteImpl(note);
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    private void deleteImpl(Note note) {
        mRepository.delete(note);
        mNoteLabelPairRepository.delete(note);
        mDriveSyncRepository.noteDeleted(note);
    }

}
