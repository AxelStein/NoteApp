package com.axel_stein.domain.interactor.note;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.repository.DriveSyncRepository;
import com.axel_stein.domain.repository.NoteLabelPairRepository;
import com.axel_stein.domain.repository.NoteRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.NoteValidator.isValid;

// todo test
public class RestoreNoteInteractor {

    @NonNull
    private NoteRepository mNoteRepository;

    @NonNull
    private NoteLabelPairRepository mNoteLabelPairRepository;

    @NonNull
    private DriveSyncRepository mDriveSyncRepository;

    public RestoreNoteInteractor(@NonNull NoteRepository n, @NonNull NoteLabelPairRepository p, @NonNull DriveSyncRepository d) {
        mNoteRepository = requireNonNull(n);
        mNoteLabelPairRepository = requireNonNull(p);
        mDriveSyncRepository = requireNonNull(d);
    }

    public Completable execute(@NonNull final Note note) {
        List<Note> notes = new ArrayList<>();
        notes.add(note);
        return execute(notes);
    }

    public Completable execute(@NonNull final List<Note> notes) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (!isValid(notes)) {
                    throw new IllegalArgumentException("notes is not valid");
                }
                mNoteRepository.restore(notes);
                for (Note note : notes) {
                    mNoteLabelPairRepository.restore(note);
                }
                mDriveSyncRepository.notifyNoteLabelPairsChanged(mNoteLabelPairRepository.query());
            }
        }).subscribeOn(Schedulers.io());
    }

}
