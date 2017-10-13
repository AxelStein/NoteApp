package com.axel_stein.domain.repository;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.NoteLabelPair;

import java.util.List;

public interface NoteLabelPairRepository {

    void insert(NoteLabelPair pair);

    void delete(@NonNull Label label);

    void delete(@NonNull Note note);

    void trash(@NonNull Note note);

    void restore(@NonNull Note note);

    List<NoteLabelPair> query();

    List<Long> queryLabelsOfNote(@NonNull Note note);

    void deleteAll();

    long count(@NonNull Label label);

}
