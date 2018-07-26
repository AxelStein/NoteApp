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
import com.axel_stein.noteapp.google_drive.GoogleDriveInteractor;

import dagger.Module;
import dagger.Provides;

@Module
class NoteInteractorModule {

    @Provides
    UpdateNoteNotebookInteractor updateNotebook(SqlNoteRepository r, GoogleDriveInteractor d) {
        return new UpdateNoteNotebookInteractor(r, d);
    }

    @Provides
    DeleteNoteInteractor delete(SqlNoteRepository r, SqlNoteLabelPairRepository p, GoogleDriveInteractor d) {
        return new DeleteNoteInteractor(r, p, d);
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
    InsertNoteInteractor insert(SqlNoteRepository r, SetLabelsInteractor s, GoogleDriveInteractor d) {
        return new InsertNoteInteractor(r, s, d);
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
    SetNotebookInteractor setNotebook(SqlNoteRepository r, GoogleDriveInteractor d) {
        return new SetNotebookInteractor(r, d);
    }

    @Provides
    TrashNoteInteractor trash(SqlNoteRepository r, SqlNoteLabelPairRepository p, GoogleDriveInteractor d) {
        return new TrashNoteInteractor(r, p, d);
    }

    @Provides
    UpdateNoteInteractor update(SqlNoteRepository r, GoogleDriveInteractor d) {
        return new UpdateNoteInteractor(r, d);
    }

    @Provides
    PinNoteInteractor pin(SqlNoteRepository r, GoogleDriveInteractor d) {
        return new PinNoteInteractor(r, d);
    }

    @Provides
    UnpinNoteInteractor unpin(SqlNoteRepository r, GoogleDriveInteractor d) {
        return new UnpinNoteInteractor(r, d);
    }

}
