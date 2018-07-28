package com.axel_stein.domain.utils.validators;

import com.axel_stein.domain.model.NoteLabelPair;

import static com.axel_stein.domain.utils.TextUtil.notEmpty;

public class NoteLabelPairValidator {

    public static boolean isValid(NoteLabelPair pair) {
        return pair != null && notEmpty(pair.getNoteId()) && notEmpty(pair.getLabelId());
    }

}
