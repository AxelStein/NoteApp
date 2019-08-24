package com.axel_stein.noteapp.dagger;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.data.label.SqlLabelRepository;
import com.axel_stein.data.note_label_pair.SqlNoteLabelPairRepository;
import com.axel_stein.domain.interactor.label.DeleteLabelInteractor;
import com.axel_stein.domain.interactor.label.GetLabelInteractor;
import com.axel_stein.domain.interactor.label.InsertLabelInteractor;
import com.axel_stein.domain.interactor.label.QueryLabelInteractor;
import com.axel_stein.domain.interactor.label.UpdateLabelInteractor;
import com.axel_stein.domain.interactor.label.UpdateOrderLabelInteractor;
import com.axel_stein.domain.interactor.label.UpdateViewsLabelInteractor;

import dagger.Module;
import dagger.Provides;

@Module
class LabelInteractorModule {

    @Provides
    InsertLabelInteractor insert(SqlLabelRepository r) {
        return new InsertLabelInteractor(r);
    }

    @Provides
    UpdateLabelInteractor update(SqlLabelRepository r) {
        return new UpdateLabelInteractor(r);
    }

    @Provides
    DeleteLabelInteractor delete(SqlLabelRepository r, SqlNoteLabelPairRepository p) {
        return new DeleteLabelInteractor(r, p);
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
    UpdateOrderLabelInteractor updateOrder(SqlLabelRepository r, AppSettingsRepository s) {
        return new UpdateOrderLabelInteractor(r, s);
    }

    @Provides
    UpdateViewsLabelInteractor views(SqlLabelRepository r) {
        return new UpdateViewsLabelInteractor(r);
    }

}
