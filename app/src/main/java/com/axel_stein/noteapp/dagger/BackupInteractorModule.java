package com.axel_stein.noteapp.dagger;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.data.note.SqlNoteRepository;
import com.axel_stein.data.notebook.SqlNotebookRepository;
import com.axel_stein.domain.interactor.backup.CreateBackupInteractor;
import com.axel_stein.domain.interactor.backup.ImportBackupInteractor;

import dagger.Module;
import dagger.Provides;

@Module
public class BackupInteractorModule {

    @Provides
    CreateBackupInteractor exportBackup(SqlNoteRepository notes,
                                        SqlNotebookRepository notebooks,
                                        AppSettingsRepository settingsRepository) {
        return new CreateBackupInteractor(notes, notebooks, settingsRepository);
    }

    @Provides
    ImportBackupInteractor importBackup(SqlNoteRepository n,
                                        SqlNotebookRepository b,
                                        AppSettingsRepository s) {
        return new ImportBackupInteractor(n, b, s);
    }

}
