package com.axel_stein.domain.interactor.note;

import androidx.annotation.NonNull;

import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.repository.NoteRepository;

import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;

public class GetNoteInteractor {

    @NonNull
    private final NoteRepository mNoteRepository;

    public GetNoteInteractor(@NonNull NoteRepository noteRepository) {
        mNoteRepository = requireNonNull(noteRepository);
    }

    /**
     * @param id request
     * @throws IllegalStateException if id, notebook or title is empty
     */
    public Single<Note> execute(final String id, final boolean incrementViews) {
        return impl(id, incrementViews).subscribeOn(Schedulers.io());
    }

    private Single<Note> impl(final String id, final boolean incrementViews) {
        return Single.fromCallable(new Callable<Note>() {
            @Override
            public Note call() {
                return get(id, incrementViews);
            }
        });
    }

    public Note get(String id, boolean incrementViews) {
        Note note = mNoteRepository.get(id);
        if (note != null) {
            if (!note.isTrashed() && incrementViews) {
                note.incrementViews();
                mNoteRepository.updateViews(note, note.getViews());
            }
        } else {
            note = new Note();
        }
        return note;
    }

}
