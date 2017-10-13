package com.axel_stein.domain.interactor.note;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Note;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.functions.Function;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;

public class EmptyTrashInteractor {

    @NonNull
    private QueryNoteInteractor mQueryInteractor;

    @NonNull
    private DeleteNoteInteractor mDeleteInteractor;

    public EmptyTrashInteractor(@NonNull QueryNoteInteractor queryInteractor, @NonNull DeleteNoteInteractor deleteInteractor) {
        mQueryInteractor = requireNonNull(queryInteractor);
        mDeleteInteractor = requireNonNull(deleteInteractor);
    }

    /**
     * Removes notes in trash
     */
    public Completable emptyTrash() {
        return mQueryInteractor.queryTrash()
                .flatMapCompletable(new Function<List<Note>, CompletableSource>() {
                    @Override
                    public CompletableSource apply(@io.reactivex.annotations.NonNull List<Note> notes) throws Exception {
                        return mDeleteInteractor.execute(notes);
                    }
                });
    }

}
