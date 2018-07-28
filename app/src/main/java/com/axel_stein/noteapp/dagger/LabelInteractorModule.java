package com.axel_stein.noteapp.dagger;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.data.label.SqlLabelRepository;
import com.axel_stein.data.note_label_pair.SqlNoteLabelPairRepository;
import com.axel_stein.domain.interactor.label.DeleteLabelInteractor;
import com.axel_stein.domain.interactor.label.GetLabelInteractor;
import com.axel_stein.domain.interactor.label.InsertLabelInteractor;
import com.axel_stein.domain.interactor.label.QueryLabelInteractor;
import com.axel_stein.domain.interactor.label.RenameLabelInteractor;
import com.axel_stein.domain.interactor.label.UpdateLabelInteractor;
import com.axel_stein.domain.interactor.label.UpdateOrderLabelInteractor;
import com.axel_stein.domain.interactor.label.UpdateViewsLabelInteractor;
import com.axel_stein.noteapp.google_drive.GoogleDriveInteractor;

import dagger.Module;
import dagger.Provides;

@Module
class LabelInteractorModule {

    @Provides
    InsertLabelInteractor insert(SqlLabelRepository r, GoogleDriveInteractor d) {
        return new InsertLabelInteractor(r, d);
    }

    @Provides
    UpdateLabelInteractor update(SqlLabelRepository r, GoogleDriveInteractor d) {
        return new UpdateLabelInteractor(r, d);
    }

    @Provides
    DeleteLabelInteractor delete(SqlLabelRepository r, SqlNoteLabelPairRepository p, GoogleDriveInteractor d) {
        return new DeleteLabelInteractor(r, p, d);
    }

    @Provides
    GetLabelInteractor get(SqlLabelRepository r) {
        return new GetLabelInteractor(r);
    }

    @Provides
    QueryLabelInteractor query(SqlLabelRepository r, SqlNoteLabelPairRepository p, AppSettingsRepository s) {
        return new QueryLabelInteractor(r, p, s);
    }

    @Provides
    UpdateOrderLabelInteractor updateOrder(SqlLabelRepository r, AppSettingsRepository s, GoogleDriveInteractor d) {
        return new UpdateOrderLabelInteractor(r, s, d);
    }

    @Provides
    RenameLabelInteractor rename(SqlLabelRepository r, GoogleDriveInteractor d) {
        return new RenameLabelInteractor(r, d);
    }

    @Provides
    UpdateViewsLabelInteractor views(SqlLabelRepository r, GoogleDriveInteractor d) {
        return new UpdateViewsLabelInteractor(r, d);
    }

}
