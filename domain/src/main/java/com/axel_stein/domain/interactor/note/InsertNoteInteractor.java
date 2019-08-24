package com.axel_stein.domain.interactor.note;

import androidx.annotation.NonNull;

import com.axel_stein.domain.interactor.label_helper.SetLabelsInteractor;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.repository.NoteRepository;

import org.joda.time.DateTime;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.NoteValidator.validateBeforeInsert;

public class InsertNoteInteractor {

    @NonNull
    private NoteRepository mRepository;

    @NonNull
    private SetLabelsInteractor mSetLabelsInteractor;

    public InsertNoteInteractor(@NonNull NoteRepository r, @NonNull SetLabelsInteractor s) {
        mRepository = requireNonNull(r);
        mSetLabelsInteractor = requireNonNull(s);
    }

    /**
     * @param note to insert
     * @throws NullPointerException     if note is null
     * @throws IllegalArgumentException if notebook or note`s content is empty
     */
    public Completable execute(@NonNull final Note note) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (!validateBeforeInsert(note)) {
                    throw new IllegalArgumentException("note is not valid");
                }

                DateTime created = new DateTime();
                note.setCreatedDate(created);
                note.setModifiedDate(created);

                mRepository.insert(note);
            }
        })
        .andThen(mSetLabelsInteractor.execute(note, note.getLabels()))
        .subscribeOn(Schedulers.io());
    }

}
