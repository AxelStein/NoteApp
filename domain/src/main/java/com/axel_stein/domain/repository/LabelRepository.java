package com.axel_stein.domain.repository;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.axel_stein.domain.model.Label;

import java.util.List;

public interface LabelRepository {

    void insert(@NonNull Label label);

    void update(@NonNull Label label);

    void rename(@NonNull Label label, String title);

    void updateViews(@NonNull Label label, long views);

    void updateOrder(@NonNull Label label, int order);

    @Nullable
    Label get(String id);

    @NonNull
    List<Label> query();

    void delete(@NonNull Label label);

    void delete();

}
