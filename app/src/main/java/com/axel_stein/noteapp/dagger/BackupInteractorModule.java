package com.axel_stein.noteapp.dagger;

import com.axel_stein.data.AppBackupFileRepository;
import com.axel_stein.data.label.SqlLabelRepository;
import com.axel_stein.data.note.SqlNoteRepository;
import com.axel_stein.data.note_label_pair.SqlNoteLabelPairRepository;
import com.axel_stein.data.notebook.SqlNotebookRepository;
import com.axel_stein.domain.interactor.backup.CreateBackupInteractor;
import com.axel_stein.domain.interactor.backup.ImportBackupInteractor;
import com.axel_stein.domain.interactor.backup.QueryBackupFileInteractor;
import com.axel_stein.domain.model.BackupFile;
import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.utils.DateFormatter;
import com.axel_stein.noteapp.utils.FileUtil;

import java.util.List;

import dagger.Module;
import dagger.Provides;
import io.reactivex.functions.Function;

@Module
public class BackupInteractorModule {

    @Provides
    QueryBackupFileInteractor query(AppBackupFileRepository repository) {
        return new QueryBackupFileInteractor(repository);
    }

    @Provides
    AppBackupFileRepository repository(App app) {
        return new AppBackupFileRepository(app);
    }

    @Provides
    CreateBackupInteractor exportBackup(SqlNoteRepository notes,
                                        SqlNotebookRepository notebooks,
                                        SqlLabelRepository labels,
                                        SqlNoteLabelPairRepository labelHelper) {
        return new CreateBackupInteractor(notes, notebooks, labels, labelHelper);
    }

    @Provides
    ImportBackupInteractor importBackup(SqlNoteRepository n,
                                        SqlNotebookRepository b,
                                        SqlLabelRepository l,
                                        SqlNoteLabelPairRepository p) {
        return new ImportBackupInteractor(n, b, l, p);
    }

    @Provides
    Function<List<BackupFile>, List<BackupFile>> fileInfoConverter(final App app) {
        return new Function<List<BackupFile>, List<BackupFile>>() {
            @Override
            public List<BackupFile> apply(List<BackupFile> backupFiles) throws Exception {
                for (BackupFile f : backupFiles) {
                    long m = f.getFile().lastModified();
                    long size = f.getFile().length();
                    f.setInfo(DateFormatter.formatDateTime(app, m) + " | " + FileUtil.humanReadableByteCount(size, true));
                }
                return backupFiles;
            }
        };
    }

}
