package com.axel_stein.noteapp.dagger;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.data.note.SqlNoteRepository;
import com.axel_stein.data.note_label_pair.SqlNoteLabelPairRepository;
import com.axel_stein.domain.interactor.label_helper.SetLabelsInteractor;
import com.axel_stein.domain.interactor.note.DeleteNoteInteractor;
import com.axel_stein.domain.interactor.note.EmptyTrashInteractor;
import com.axel_stein.domain.interactor.note.GetNoteInteractor;
import com.axel_stein.domain.interactor.note.InsertNoteInteractor;
import com.axel_stein.domain.interactor.note.PinNoteInteractor;
import com.axel_stein.domain.interactor.note.QueryNoteInteractor;
import com.axel_stein.domain.interactor.note.RestoreNoteInteractor;
import com.axel_stein.domain.interactor.note.SetNotebookInteractor;
import com.axel_stein.domain.interactor.note.TrashNoteInteractor;
import com.axel_stein.domain.interactor.note.UnpinNoteInteractor;
import com.axel_stein.domain.interactor.note.UpdateNoteInteractor;
import com.axel_stein.domain.interactor.note.UpdateNoteNotebookInteractor;
import com.axel_stein.noteapp.main.GoogleDriveInteractor;

import dagger.Module;
import dagger.Provides;

@Module
class NoteInteractorModule {

    @Provides
    UpdateNoteNotebookInteractor updateNotebook(SqlNoteRepository r) {
        return new UpdateNoteNotebookInteractor(r);
    }

    @Provides
    DeleteNoteInteractor delete(SqlNoteRepository r, SqlNoteLabelPairRepository p) {
        return new DeleteNoteInteractor(r, p);
    }

    @Provides
    EmptyTrashInteractor emptyTrash(QueryNoteInteractor query, DeleteNoteInteractor delete) {
        return new EmptyTrashInteractor(query, delete);
    }

    @Provides
    GetNoteInteractor get(SqlNoteRepository r, SqlNoteLabelPairRepository p) {
        return new GetNoteInteractor(r, p);
    }

    @Provides
    InsertNoteInteractor insert(SqlNoteRepository r, SetLabelsInteractor setLabelsInteractor) {
        return new InsertNoteInteractor(r, setLabelsInteractor);
    }

    @Provides
    QueryNoteInteractor query(SqlNoteRepository r, AppSettingsRepository s) {
        return new QueryNoteInteractor(r, s);
    }

    @Provides
    RestoreNoteInteractor restore(SqlNoteRepository r, SqlNoteLabelPairRepository p, GoogleDriveInteractor d) {
        return new RestoreNoteInteractor(r, p, d);
    }

    @Provides
    SetLabelsInteractor setLabels(SqlNoteLabelPairRepository r, GoogleDriveInteractor d) {
        return new SetLabelsInteractor(r, d);
    }

    @Provides
    SetNotebookInteractor setNotebook(SqlNoteRepository repository) {
        return new SetNotebookInteractor(repository);
    }

    @Provides
    TrashNoteInteractor trash(SqlNoteRepository r, SqlNoteLabelPairRepository p, GoogleDriveInteractor d) {
        return new TrashNoteInteractor(r, p, d);
    }

    @Provides
    UpdateNoteInteractor update(SqlNoteRepository r, SetLabelsInteractor setLabelsInteractor) {
        return new UpdateNoteInteractor(r, setLabelsInteractor);
    }

    @Provides
    PinNoteInteractor pin(SqlNoteRepository r) {
        return new PinNoteInteractor(r);
    }

    @Provides
    UnpinNoteInteractor unpin(SqlNoteRepository r) {
        return new UnpinNoteInteractor(r);
    }

}
