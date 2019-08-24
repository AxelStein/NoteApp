package com.axel_stein.domain.interactor.backup;

import androidx.annotation.NonNull;

import com.axel_stein.domain.json_wrapper.LabelWrapper;
import com.axel_stein.domain.json_wrapper.NoteWrapper;
import com.axel_stein.domain.json_wrapper.NotebookWrapper;
import com.axel_stein.domain.model.Backup;
import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.NoteLabelPair;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.repository.LabelRepository;
import com.axel_stein.domain.repository.NoteLabelPairRepository;
import com.axel_stein.domain.repository.NoteRepository;
import com.axel_stein.domain.repository.NotebookRepository;
import com.axel_stein.domain.repository.SettingsRepository;
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

    @NonNull
    private SettingsRepository mSettingsRepository;

    public ImportBackupInteractor(@NonNull NoteRepository n,
                                  @NonNull NotebookRepository b,
                                  @NonNull LabelRepository l,
                                  @NonNull NoteLabelPairRepository p,
                                  @NonNull SettingsRepository s) {
        mNoteRepository = requireNonNull(n);
        mNotebookRepository = requireNonNull(b);
        mLabelRepository = requireNonNull(l);
        mNoteLabelPairRepository = requireNonNull(p);
        mSettingsRepository = requireNonNull(s);
    }

    public Completable execute(final String src) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                ObjectMapper mapper = new ObjectMapper();
                Backup backup = mapper.readValue(src, Backup.class);

                System.out.println("Import backup, version " + backup.getVersion());

                mNoteRepository.deleteAll();
                mNotebookRepository.delete();
                mLabelRepository.delete();
                mNoteLabelPairRepository.deleteAll();

                List<NoteWrapper> notes = backup.getNotes();
                if (notes != null) {
                    for (NoteWrapper wrapper : notes) {
                        Note note = wrapper.toNote();
                        if (!NoteValidator.isValid(note)) {
                            System.out.println("Error: note is not valid = " + note);
                        } else {
                            mNoteRepository.insert(note);
                        }
                    }
                } else {
                    System.out.println("Error: notes not found");
                }

                List<NotebookWrapper> notebooks = backup.getNotebooks();
                if (notebooks != null) {
                    for (NotebookWrapper wrapper : notebooks) {
                        Notebook notebook = wrapper.toNotebook();
                        if (!NotebookValidator.isValid(notebook)) {
                            System.out.println("Error: notebook is not valid = " + notebook);
                        } else {
                            mNotebookRepository.insert(notebook);
                        }
                    }
                } else {
                    System.out.println("Error: notebooks not found");
                }

                List<LabelWrapper> labels = backup.getLabels();
                if (labels != null) {
                    for (LabelWrapper wrapper : labels) {
                        Label label = wrapper.toLabel();
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

                String settings = backup.getJsonSettings();
                mSettingsRepository.importSettings(settings);
            }
        }).subscribeOn(Schedulers.io());
    }

}
