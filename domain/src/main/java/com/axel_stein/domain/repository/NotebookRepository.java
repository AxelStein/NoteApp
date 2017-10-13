package com.axel_stein.domain.repository;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.axel_stein.domain.model.Notebook;

import java.util.List;

public interface NotebookRepository {

    /**
     * Inserts notebook into repository
     *
     * @param notebook to insert
     * @return id of the created notebook
     */
    long insert(@NonNull Notebook notebook);

    /**
     * @param notebook to update
     */
    void update(@NonNull Notebook notebook);

    /**
     * @param notebook to delete
     */
    void delete(@NonNull Notebook notebook);

    /**
     * @param id notebook`s id
     * @return null, if there is no notebook with requested id
     */
    @Nullable
    Notebook get(long id);

    /**
     * @return all notebooks
     */
    @NonNull
    List<Notebook> query();

    void deleteAll();

}
