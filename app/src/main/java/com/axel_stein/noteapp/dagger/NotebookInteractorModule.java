package com.axel_stein.noteapp.dagger;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.data.note.SqlNoteRepository;
import com.axel_stein.data.notebook.SqlNotebookRepository;
import com.axel_stein.domain.interactor.note.DeleteNoteInteractor;
import com.axel_stein.domain.interactor.note.QueryNoteInteractor;
import com.axel_stein.domain.interactor.notebook.DeleteNotebookInteractor;
import com.axel_stein.domain.interactor.notebook.GetNotebookInteractor;
import com.axel_stein.domain.interactor.notebook.InsertNotebookInteractor;
import com.axel_stein.domain.interactor.notebook.QueryNotebookInteractor;
import com.axel_stein.domain.interactor.notebook.UpdateNotebookInteractor;
import com.axel_stein.domain.interactor.notebook.UpdateNotebookOrderInteractor;

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
    DeleteNotebookInteractor delete(SqlNotebookRepository repository,
                                    DeleteNoteInteractor deleteNotes,
                                    QueryNoteInteractor queryNotes) {
        return new DeleteNotebookInteractor(repository, deleteNotes, queryNotes);
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
    UpdateNotebookOrderInteractor updateOrder(SqlNotebookRepository repository, AppSettingsRepository settings) {
        return new UpdateNotebookOrderInteractor(repository, settings);
    }

}
