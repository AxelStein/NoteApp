package com.axel_stein.noteapp.dagger;

import com.axel_stein.data.AppSettingsRepository;
import com.axel_stein.data.label.SqlLabelRepository;
import com.axel_stein.data.note_label_pair.SqlNoteLabelPairRepository;
import com.axel_stein.domain.interactor.label.DeleteLabelInteractor;
import com.axel_stein.domain.interactor.label.GetLabelInteractor;
import com.axel_stein.domain.interactor.label.InsertLabelInteractor;
import com.axel_stein.domain.interactor.label.QueryLabelInteractor;
import com.axel_stein.domain.interactor.label.UpdateLabelInteractor;
import com.axel_stein.domain.interactor.label.UpdateLabelOrderInteractor;

import dagger.Module;
import dagger.Provides;

@Module
class LabelInteractorModule {

    @Provides
    InsertLabelInteractor insert(SqlLabelRepository repository) {
        return new InsertLabelInteractor(repository);
    }

    @Provides
    UpdateLabelInteractor update(SqlLabelRepository repository) {
        return new UpdateLabelInteractor(repository);
    }

    @Provides
    DeleteLabelInteractor delete(SqlLabelRepository repository, SqlNoteLabelPairRepository helperRepository) {
        return new DeleteLabelInteractor(repository, helperRepository);
    }

    @Provides
    GetLabelInteractor get(SqlLabelRepository repository) {
        return new GetLabelInteractor(repository);
    }

    @Provides
    QueryLabelInteractor query(SqlLabelRepository repository,
                               SqlNoteLabelPairRepository noteLabelPairRepository,
                               AppSettingsRepository settingsRepository) {
        return new QueryLabelInteractor(repository, noteLabelPairRepository, settingsRepository);
    }

    @Provides
    UpdateLabelOrderInteractor updateOrder(SqlLabelRepository repository, AppSettingsRepository settings) {
        return new UpdateLabelOrderInteractor(repository, settings);
    }


}
