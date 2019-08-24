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

import dagger.Module;
import dagger.Provides;

@Module
class NoteInteractorModule {

    @Provides
    SetNotebookNoteInteractor updateNotebook(SqlNoteRepository r) {
        return new SetNotebookNoteInteractor(r);
    }

    @Provides
    DeleteNoteInteractor delete(SqlNoteRepository r, SqlNoteLabelPairRepository p) {
        return new DeleteNoteInteractor(r, p);
    }

    @Provides
    EmptyTrashInteractor emptyTrash(QueryNoteInteractor q, DeleteNoteInteractor d) {
        return new EmptyTrashInteractor(q, d);
    }

    @Provides
    GetNoteInteractor get(SqlNoteRepository r, SqlNoteLabelPairRepository p) {
        return new GetNoteInteractor(r, p);
    }

    @Provides
    InsertNoteInteractor insert(SqlNoteRepository r, SetLabelsInteractor s) {
        return new InsertNoteInteractor(r, s);
    }

    @Provides
    QueryNoteInteractor query(SqlNoteRepository r, AppSettingsRepository s) {
        return new QueryNoteInteractor(r, s);
    }

    @Provides
    SetLabelsInteractor setLabels(SqlNoteLabelPairRepository r) {
        return new SetLabelsInteractor(r);
    }

    @Provides
    SetTrashedNoteInteractor trash(SqlNoteRepository r, SqlNoteLabelPairRepository p) {
        return new SetTrashedNoteInteractor(r, p);
    }

    @Provides
    UpdateNoteInteractor update(SqlNoteRepository r) {
        return new UpdateNoteInteractor(r);
    }

    @Provides
    SetPinnedNoteInteractor pin(SqlNoteRepository r) {
        return new SetPinnedNoteInteractor(r);
    }

    @Provides
    SetStarredNoteInteractor star(SqlNoteRepository r) {
        return new SetStarredNoteInteractor(r);
    }

}
