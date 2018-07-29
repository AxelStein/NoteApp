package com.axel_stein.noteapp.dagger;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.data.note.SqlNoteRepository;
import com.axel_stein.data.notebook.SqlNotebookRepository;
import com.axel_stein.domain.interactor.notebook.DeleteNotebookInteractor;
import com.axel_stein.domain.interactor.notebook.GetNotebookInteractor;
import com.axel_stein.domain.interactor.notebook.InsertNotebookInteractor;
import com.axel_stein.domain.interactor.notebook.QueryNotebookInteractor;
import com.axel_stein.domain.interactor.notebook.UpdateColorNotebookInteractor;
import com.axel_stein.domain.interactor.notebook.UpdateNotebookInteractor;
import com.axel_stein.domain.interactor.notebook.UpdateOrderNotebookInteractor;
import com.axel_stein.domain.interactor.notebook.UpdateViewsNotebookInteractor;
import com.axel_stein.noteapp.google_drive.GoogleDriveInteractor;

import dagger.Module;
import dagger.Provides;

@Module
class NotebookInteractorModule {

    @Provides
    InsertNotebookInteractor insert(SqlNotebookRepository repository, GoogleDriveInteractor d) {
        return new InsertNotebookInteractor(repository, d);
    }

    @Provides
    UpdateNotebookInteractor update(SqlNotebookRepository repository, GoogleDriveInteractor d) {
        return new UpdateNotebookInteractor(repository, d);
    }

    @Provides
    DeleteNotebookInteractor delete(SqlNoteRepository n, SqlNotebookRepository b, GoogleDriveInteractor d) {
        return new DeleteNotebookInteractor(n, b, d);
    }

    @Provides
    GetNotebookInteractor get(SqlNotebookRepository repository) {
        return new GetNotebookInteractor(repository);
    }

    @Provides
    QueryNotebookInteractor query(SqlNotebookRepository repository,
                                  SqlNoteRepository noteRepository,
                                  AppSettingsRepository appSettingsRepository) {
        return new QueryNotebookInteractor(repository, noteRepository, appSettingsRepository);
    }

    @Provides
    UpdateOrderNotebookInteractor updateOrder(SqlNotebookRepository n, AppSettingsRepository s, GoogleDriveInteractor d) {
        return new UpdateOrderNotebookInteractor(n, s, d);
    }

    @Provides
    UpdateColorNotebookInteractor color(SqlNotebookRepository n, GoogleDriveInteractor d) {
        return new UpdateColorNotebookInteractor(n, d);
    }

    @Provides
    UpdateViewsNotebookInteractor views(SqlNotebookRepository n, GoogleDriveInteractor d) {
        return new UpdateViewsNotebookInteractor(n, d);
    }

}
