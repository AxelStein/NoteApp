package com.axel_stein.domain.interactor.backup;

import androidx.annotation.NonNull;

import com.axel_stein.domain.json_wrapper.NoteWrapper;
import com.axel_stein.domain.json_wrapper.NotebookWrapper;
import com.axel_stein.domain.model.Backup;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.repository.NoteRepository;
import com.axel_stein.domain.repository.NotebookRepository;
import com.axel_stein.domain.repository.SettingsRepository;
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
    private SettingsRepository mSettingsRepository;

    public ImportBackupInteractor(@NonNull NoteRepository n,
                                  @NonNull NotebookRepository b,
                                  @NonNull SettingsRepository s) {
        mNoteRepository = requireNonNull(n);
        mNotebookRepository = requireNonNull(b);
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
                mNotebookRepository.deleteAll();

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

                String settings = backup.getJsonSettings();
                mSettingsRepository.importSettings(settings);
            }
        }).subscribeOn(Schedulers.io());
    }

}
