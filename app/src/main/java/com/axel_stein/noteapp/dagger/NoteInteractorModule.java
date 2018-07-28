package com.axel_stein.noteapp.dagger;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.data.note.SqlNoteRepository;
import com.axel_stein.data.note_label_pair.SqlNoteLabelPairRepository;
import com.axel_stein.domain.interactor.label_helper.SetLabelsInteractor;
import com.axel_stein.domain.interactor.note.DeleteNoteInteractor;
import com.axel_stein.domain.interactor.note.EmptyTrashInteractor;
import com.axel_stein.domain.interactor.note.GetNoteInteractor;
import com.axel_stein.domain.interactor.note.InsertNoteInteractor;
import com.axel_stein.domain.interactor.note.QueryNoteInteractor;
import com.axel_stein.domain.interactor.note.SetNotebookNoteInteractor;
import com.axel_stein.domain.interactor.note.SetPinnedNoteInteractor;
import com.axel_stein.domain.interactor.note.SetStarredNoteInteractor;
import com.axel_stein.domain.interactor.note.SetTrashedNoteInteractor;
import com.axel_stein.domain.interactor.note.UpdateNoteInteractor;
import com.axel_stein.noteapp.google_drive.GoogleDriveInteractor;

import dagger.Module;
import dagger.Provides;

@Module
class NoteInteractorModule {

    @Provides
    SetNotebookNoteInteractor updateNotebook(SqlNoteRepository r, GoogleDriveInteractor d) {
        return new SetNotebookNoteInteractor(r, d);
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
    SetLabelsInteractor setLabels(SqlNoteLabelPairRepository r, GoogleDriveInteractor d) {
        return new SetLabelsInteractor(r, d);
    }

    @Provides
    SetTrashedNoteInteractor trash(SqlNoteRepository r, SqlNoteLabelPairRepository p, GoogleDriveInteractor d) {
        return new SetTrashedNoteInteractor(r, p, d);
    }

    @Provides
    UpdateNoteInteractor update(SqlNoteRepository r, GoogleDriveInteractor d) {
        return new UpdateNoteInteractor(r, d);
    }

    @Provides
    SetPinnedNoteInteractor pin(SqlNoteRepository r, GoogleDriveInteractor d) {
        return new SetPinnedNoteInteractor(r, d);
    }

    @Provides
    SetStarredNoteInteractor star(SqlNoteRepository r, GoogleDriveInteractor d) {
        return new SetStarredNoteInteractor(r, d);
    }

}
