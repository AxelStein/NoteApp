package com.axel_stein.noteapp.dagger;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.data.note.SqlNoteRepository;
import com.axel_stein.data.note_label_pair.SqlNoteLabelPairRepository;
import com.axel_stein.data.notebook.SqlNotebookRepository;
import com.axel_stein.domain.interactor.label_helper.SetLabelsInteractor;
import com.axel_stein.domain.interactor.note.DeleteNoteInteractor;
import com.axel_stein.domain.interactor.note.EmptyTrashInteractor;
import com.axel_stein.domain.interactor.note.GetNoteInteractor;
import com.axel_stein.domain.interactor.note.InsertNoteInteractor;
import com.axel_stein.domain.interactor.note.QueryNoteInteractor;
import com.axel_stein.domain.interactor.note.RestoreNoteInteractor;
import com.axel_stein.domain.interactor.note.SetNotebookInteractor;
import com.axel_stein.domain.interactor.note.TrashNoteInteractor;
import com.axel_stein.domain.interactor.note.UpdateNoteInteractor;

import dagger.Module;
import dagger.Provides;

@Module
class NoteInteractorModule {

    @Provides
    DeleteNoteInteractor delete(SqlNoteRepository repository, SqlNoteLabelPairRepository helperRepository) {
        return new DeleteNoteInteractor(repository, helperRepository);
    }

    @Provides
    EmptyTrashInteractor emptyTrash(QueryNoteInteractor query, DeleteNoteInteractor delete) {
        return new EmptyTrashInteractor(query, delete);
    }

    @Provides
    GetNoteInteractor get(SqlNoteRepository repository,
                          SqlNoteLabelPairRepository pairRepository,
                          SqlNotebookRepository notebookRepository) {
        return new GetNoteInteractor(repository, pairRepository, notebookRepository);
    }

    @Provides
    InsertNoteInteractor insert(SqlNoteRepository repository, SetLabelsInteractor setLabelsInteractor) {
        return new InsertNoteInteractor(repository, setLabelsInteractor);
    }

    @Provides
    QueryNoteInteractor query(SqlNoteRepository repository,
                              AppSettingsRepository settingsRepository,
                              SqlNoteLabelPairRepository helperRepository) {
        return new QueryNoteInteractor(repository, settingsRepository, helperRepository);
    }

    @Provides
    RestoreNoteInteractor restore(SqlNoteRepository repository, SqlNoteLabelPairRepository noteLabelPairRepository) {
        return new RestoreNoteInteractor(repository, noteLabelPairRepository);
    }

    @Provides
    SetLabelsInteractor setLabels(SqlNoteLabelPairRepository repository) {
        return new SetLabelsInteractor(repository);
    }

    @Provides
    SetNotebookInteractor setNotebook(SqlNoteRepository repository) {
        return new SetNotebookInteractor(repository);
    }

    @Provides
    TrashNoteInteractor trash(SqlNoteRepository repository, SqlNoteLabelPairRepository noteLabelPairRepository) {
        return new TrashNoteInteractor(repository, noteLabelPairRepository);
    }

    @Provides
    UpdateNoteInteractor update(SqlNoteRepository repository, SetLabelsInteractor setLabelsInteractor) {
        return new UpdateNoteInteractor(repository, setLabelsInteractor);
    }

}
