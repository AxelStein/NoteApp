package com.axel_stein.domain.utils.validators;

import com.axel_stein.domain.model.NoteLabelPair;

public class NoteLabelPairValidator {

    public static boolean isValid(NoteLabelPair pair) {
        return pair != null && pair.getNoteId() > 0 && pair.getLabelId() > 0;
    }

}
