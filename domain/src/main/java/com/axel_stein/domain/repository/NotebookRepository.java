package com.axel_stein.domain.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.axel_stein.domain.model.Notebook;

import java.util.List;

public interface NotebookRepository {

    void insert(@NonNull Notebook notebook);

    void update(@NonNull Notebook notebook);

    void rename(@NonNull Notebook notebook, String title);

    void updateViews(@NonNull Notebook notebook, long views);

    void updateOrder(@NonNull Notebook notebook, int order);

    void updateColor(@NonNull Notebook notebook, String color);

    @Nullable
    Notebook get(String id);

    @NonNull
    List<Notebook> query();

    void delete(@NonNull Notebook notebook);

    void delete();

}
