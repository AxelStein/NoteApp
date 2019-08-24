package com.axel_stein.domain.repository;

import androidx.annotation.NonNull;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.NoteLabelPair;

import java.util.List;

public interface NoteLabelPairRepository {

    void insert(NoteLabelPair pair);

    void setTrashed(@NonNull Note note, boolean trashed);

    List<NoteLabelPair> query();

    List<String> queryLabels(@NonNull Note note);

    long count(@NonNull Label label);

    void delete(@NonNull Label label);

    void delete(@NonNull Note note);

    void deleteAll();

}
