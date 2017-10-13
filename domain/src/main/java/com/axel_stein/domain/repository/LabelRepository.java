package com.axel_stein.domain.repository;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.axel_stein.domain.model.Label;

import java.util.List;

public interface LabelRepository {

    /**
     * @param label to insert
     */
    long insert(@NonNull Label label);

    /**
     * @param label to update
     */
    void update(@NonNull Label label);

    /**
     * @param label to delete
     */
    void delete(@NonNull Label label);

    /**
     * @param id label`s id
     * @return null, if there is no label with requested id
     */
    @Nullable
    Label get(long id);

    /**
     * @return all labels
     */
    @NonNull
    List<Label> query();

    void deleteAll();

}
