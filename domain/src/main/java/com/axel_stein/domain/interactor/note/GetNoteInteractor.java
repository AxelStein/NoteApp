package com.axel_stein.domain.interactor.note;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.repository.NoteLabelPairRepository;
import com.axel_stein.domain.repository.NoteRepository;

import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.NoteValidator.isValid;

public class GetNoteInteractor {

    @NonNull
    private NoteRepository mNoteRepository;

    @NonNull
    private NoteLabelPairRepository mNoteLabelPairRepository;

    public GetNoteInteractor(@NonNull NoteRepository noteRepository,
                             @NonNull NoteLabelPairRepository helperRepository) {
        mNoteRepository = requireNonNull(noteRepository);
        mNoteLabelPairRepository = requireNonNull(helperRepository);
    }

    /**
     * @param id request
     * @throws IllegalStateException if id, notebook or title is empty
     */
    public Single<Note> execute(final long id) {
        return Single.fromCallable(new Callable<Note>() {
            @Override
            public Note call() throws Exception {
                Note note = mNoteRepository.get(id);
                if (note != null) {
                    if (!isValid(note)) {
                        throw new IllegalStateException("note is not valid");
                    }
                    if (!note.isTrash()) {
                        note.incrementRelevance();
                        mNoteRepository.update(note);
                    }
                    note.setLabels(mNoteLabelPairRepository.queryLabelsOfNote(note));
                } else {
                    note = new Note();
                }
                return note;
            }
        }).subscribeOn(Schedulers.io());
    }

}
