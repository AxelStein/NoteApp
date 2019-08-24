package com.axel_stein.domain.interactor.label_helper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.NoteLabelPair;
import com.axel_stein.domain.repository.NoteLabelPairRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.validators.LabelValidator.isValidIds;
import static com.axel_stein.domain.utils.validators.NoteValidator.isValid;

public class SetLabelsInteractor {

    @NonNull
    private NoteLabelPairRepository mRepository;

    public SetLabelsInteractor(@NonNull NoteLabelPairRepository n) {
        mRepository = requireNonNull(n);
    }

    public Completable execute(@NonNull final Note note, @Nullable final List<String> labelIds) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (!isValid(note)) {
                    throw new IllegalArgumentException("note is not valid");
                }
                if (labelIds != null) {
                    if (!isValidIds(labelIds)) {
                        throw new IllegalArgumentException("labelIds is not valid");
                    }
                }
                setLabelsImpl(note, labelIds);
            }
        }).subscribeOn(Schedulers.io());
    }

    /**
     * @throws IllegalArgumentException if note`s id is 0 or label`s id is 0
     */
    public Completable execute(@NonNull final List<Note> notes, @Nullable final List<String> labelIds) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                if (!isValid(notes)) {
                    throw new IllegalArgumentException("notes is not valid");
                }

                if (labelIds != null) {
                    if (!isValidIds(labelIds)) {
                        throw new IllegalArgumentException("labelIds is not valid");
                    }
                }

                for (Note note : notes) {
                    setLabelsImpl(note, labelIds);
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    private void setLabelsImpl(Note note, final List<String> labels) {
        note.setLabels(labels);
        mRepository.delete(note);

        if (labels != null) {
            for (String l : labels) {
                mRepository.insert(new NoteLabelPair(note.getId(), l, note.isTrashed()));
            }
        }
    }

}
