package com.axel_stein.domain.utils.validators;

import com.axel_stein.domain.model.NoteLabelPair;
import com.axel_stein.domain.utils.TextUtil;

public class NoteLabelPairValidator {

    public static boolean isValid(NoteLabelPair pair) {
        return pair != null && !TextUtil.isEmpty(pair.getNoteId()) && pair.getLabelId() > 0;
    }

}
