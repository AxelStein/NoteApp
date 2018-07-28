package com.axel_stein.domain.interactor.note;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.repository.DriveSyncRepository;
import com.axel_stein.domain.repository.NoteLabelPairRepository;
import com.axel_stein.domain.repository.NoteRepository;

import org.joda.time.DateTime;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.NoteValidator.isValid;

public class SetTrashedNoteInteractor {

    @NonNull
    private NoteRepository mNoteRepository;

    @NonNull
    private NoteLabelPairRepository mNoteLabelPairRepository;

    @NonNull
    private DriveSyncRepository mDriveSyncRepository;

    public SetTrashedNoteInteractor(@NonNull NoteRepository n, @NonNull NoteLabelPairRepository l, @NonNull DriveSyncRepository d) {
        mNoteRepository = requireNonNull(n);
        mNoteLabelPairRepository = requireNonNull(l);
        mDriveSyncRepository = requireNonNull(d);
    }

    public Completable execute(@NonNull final Note note, final boolean trashed) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                setTrashedImpl(note, trashed);

                mDriveSyncRepository.noteTrashed(note, trashed);
                mDriveSyncRepository.notifyNoteLabelPairsChanged(mNoteLabelPairRepository.query());
            }
        }).subscribeOn(Schedulers.io());
    }

    public Completable execute(@NonNull final List<Note> notes, final boolean trashed) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                if (!isValid(notes)) {
                    throw new IllegalArgumentException("notes is not valid");
                }

                for (Note note : notes) {
                    setTrashedImpl(note, trashed);
                }

                mDriveSyncRepository.notesTrashed(notes, trashed);
                mDriveSyncRepository.notifyNoteLabelPairsChanged(mNoteLabelPairRepository.query());
            }
        }).subscribeOn(Schedulers.io());
    }

    private void setTrashedImpl(Note note, boolean trashed) {
        if (!isValid(note)) {
            throw new IllegalArgumentException();
        }

        note.setTrashed(trashed);
        note.setTrashedDate(trashed ? new DateTime() : null);

        mNoteRepository.setTrashed(note, trashed);
        mNoteLabelPairRepository.setTrashed(note, trashed);
    }

}
