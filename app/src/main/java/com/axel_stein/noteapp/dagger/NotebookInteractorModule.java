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

import dagger.Module;
import dagger.Provides;

@Module
class NotebookInteractorModule {

    @Provides
    InsertNotebookInteractor insert(SqlNotebookRepository repository) {
        return new InsertNotebookInteractor(repository);
    }

    @Provides
    UpdateNotebookInteractor update(SqlNotebookRepository repository) {
        return new UpdateNotebookInteractor(repository);
    }

    @Provides
    DeleteNotebookInteractor delete(SqlNoteRepository n, SqlNotebookRepository b) {
        return new DeleteNotebookInteractor(n, b);
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
    UpdateOrderNotebookInteractor updateOrder(SqlNotebookRepository n, AppSettingsRepository s) {
        return new UpdateOrderNotebookInteractor(n, s);
    }

    @Provides
    UpdateColorNotebookInteractor color(SqlNotebookRepository n) {
        return new UpdateColorNotebookInteractor(n);
    }

    @Provides
    UpdateViewsNotebookInteractor views(SqlNotebookRepository n) {
        return new UpdateViewsNotebookInteractor(n);
    }

}
