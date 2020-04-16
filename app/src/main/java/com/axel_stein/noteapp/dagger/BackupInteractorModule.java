package com.axel_stein.noteapp.dagger;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.data.note.SqlNoteRepository;
import com.axel_stein.data.notebook.SqlNotebookRepository;
import com.axel_stein.data.reminder.SqlReminderRepository;
import com.axel_stein.domain.interactor.backup.CreateBackupInteractor;
import com.axel_stein.domain.interactor.backup.ImportBackupInteractor;

import dagger.Module;
import dagger.Provides;

@Module
public class BackupInteractorModule {

    @Provides
    CreateBackupInteractor exportBackup(SqlNoteRepository n,
                                        SqlNotebookRepository b,
                                        AppSettingsRepository s,
                                        SqlReminderRepository r) {
        return new CreateBackupInteractor(n, b, s, r);
    }

    @Provides
    ImportBackupInteractor importBackup(SqlNoteRepository n,
                                        SqlNotebookRepository b,
                                        AppSettingsRepository s,
                                        SqlReminderRepository r) {
        return new ImportBackupInteractor(n, b, s, r);
    }

}
