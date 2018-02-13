package com.axel_stein.domain.interactor.backup;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Backup;
import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.NoteLabelPair;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.repository.LabelRepository;
import com.axel_stein.domain.repository.NoteLabelPairRepository;
import com.axel_stein.domain.repository.NoteRepository;
import com.axel_stein.domain.repository.NotebookRepository;
import com.axel_stein.domain.utils.validators.LabelValidator;
import com.axel_stein.domain.utils.validators.NoteLabelPairValidator;
import com.axel_stein.domain.utils.validators.NoteValidator;
import com.axel_stein.domain.utils.validators.NotebookValidator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;

public class ImportBackupInteractor {

    @NonNull
    private NoteRepository mNoteRepository;

    @NonNull
    private NotebookRepository mNotebookRepository;

    @NonNull
    private LabelRepository mLabelRepository;

    @NonNull
    private NoteLabelPairRepository mNoteLabelPairRepository;

    public ImportBackupInteractor(@NonNull NoteRepository noteRepository,
                                  @NonNull NotebookRepository notebookRepository,
                                  @NonNull LabelRepository labelRepository,
                                  @NonNull NoteLabelPairRepository noteLabelPairRepository) {
        mNoteRepository = requireNonNull(noteRepository);
        mNotebookRepository = requireNonNull(notebookRepository);
        mLabelRepository = requireNonNull(labelRepository);
        mNoteLabelPairRepository = requireNonNull(noteLabelPairRepository);
    }

    public Completable execute(final String src) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                ObjectMapper mapper = new ObjectMapper();
                Backup backup = mapper.readValue(src, Backup.class);

                mNoteRepository.deleteAll();
                mNotebookRepository.deleteAll();
                mLabelRepository.deleteAll();
                mNoteLabelPairRepository.deleteAll();

                List<Note> notes = backup.getNotes();
                if (notes != null) {
                    for (Note note : notes) {
                        if (!NoteValidator.isValid(note)) {
                            System.out.println("Error: note is not valid = " + note);
                        } else {
                            mNoteRepository.insert(note);
                        }
                    }
                } else {
                    System.out.println("Error: notes not found");
                }

                List<Notebook> notebooks = backup.getNotebooks();
                if (notebooks != null) {
                    for (Notebook notebook : notebooks) {
                        if (!NotebookValidator.isValid(notebook)) {
                            System.out.println("Error: notebook is not valid = " + notebook);
                        } else {
                            mNotebookRepository.insert(notebook);
                        }
                    }
                } else {
                    System.out.println("Error: notebooks not found");
                }

                List<Label> labels = backup.getLabels();
                if (labels != null) {
                    for (Label label : labels) {
                        if (!LabelValidator.isValid(label)) {
                            System.out.println("Error: label is not valid = " + label);
                        } else {
                            mLabelRepository.insert(label);
                        }
                    }
                } else {
                    System.out.println("Error: labels not found");
                }

                List<NoteLabelPair> pairs = backup.getNoteLabelPairs();
                if (pairs != null) {
                    for (NoteLabelPair pair : pairs) {
                        if (!NoteLabelPairValidator.isValid(pair)) {
                            System.out.println("Error: pair is not valid = " + pair);
                        } else {
                            mNoteLabelPairRepository.insert(pair);
                        }
                    }
                } else {
                    System.out.println("Error: pairs not found");
                }
            }
        }).subscribeOn(Schedulers.io());
    }

}
