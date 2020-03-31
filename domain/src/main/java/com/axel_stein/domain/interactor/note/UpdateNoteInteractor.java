package com.axel_stein.domain.interactor.note;

import androidx.annotation.NonNull;

import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.repository.NoteRepository;

import org.joda.time.DateTime;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.TextUtil.isEmpty;
import static com.axel_stein.domain.utils.validators.NoteValidator.validateBeforeUpdate;

public class UpdateNoteInteractor {

    @NonNull
    private final NoteRepository mRepository;

    public UpdateNoteInteractor(@NonNull NoteRepository r) {
        mRepository = requireNonNull(r);
    }

    /**
     * @param note to update
     * @throws NullPointerException     if note is null
     * @throws IllegalArgumentException if id, notebook or note`s content is empty
     */
    public Completable execute(@NonNull final Note note) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                if (!validateBeforeUpdate(note)) {
                    throw new IllegalArgumentException("note is note valid");
                }
                note.setModifiedDate(new DateTime());
                mRepository.update(note);
            }
        })
        .subscribeOn(Schedulers.io());
    }

    public Completable updateTitle(final String noteId, final String title) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                if (isEmpty(noteId)) {
                    throw new IllegalArgumentException("noteId is null");
                }
                mRepository.updateModifiedDate(noteId, new DateTime());
                mRepository.updateTitle(noteId, title);
            }
        }).subscribeOn(Schedulers.io());
    }

    public Completable updateContent(final String noteId, final String content) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                if (isEmpty(noteId)) {
                    throw new IllegalArgumentException("noteId is null");
                }
                mRepository.updateModifiedDate(noteId, new DateTime());
                mRepository.updateContent(noteId, content);
            }
        }).subscribeOn(Schedulers.io());
    }

    public Completable updateIsCheckList(final String noteId, final boolean isCheckList) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                if (isEmpty(noteId)) {
                    throw new IllegalArgumentException("noteId is null");
                }
                mRepository.updateIsCheckList(noteId, isCheckList);
            }
        }).subscribeOn(Schedulers.io());
    }

    public Completable updateCheckListJson(final String noteId, final String checkListJson) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() {
                if (isEmpty(noteId)) {
                    throw new IllegalArgumentException("noteId is null");
                }
                mRepository.updateCheckListJson(noteId, checkListJson);
            }
        }).subscribeOn(Schedulers.io());
    }
}
