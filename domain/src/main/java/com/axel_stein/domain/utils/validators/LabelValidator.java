package com.axel_stein.domain.utils.validators;

import com.axel_stein.domain.model.Label;

import java.util.List;

import static com.axel_stein.domain.utils.TextUtil.isEmpty;

public class LabelValidator {

    public static boolean isValid(Label label) {
        return isValid(label, true);
    }

    public static boolean isValid(Label label, boolean checkId) {
        if (label == null) {
            return false;
        }
        boolean validTitle = !isEmpty(label.getTitle());
        if (checkId) {
            return validTitle && label.getId() > 0;
        }
        return validTitle;
    }

    public static boolean isValid(List<Label> labels) {
        if (labels == null) {
            return false;
        }
        for (Label l : labels) {
            if (!isValid(l)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidIds(List<Long> labelIds) {
        if (labelIds == null) {
            return false;
        }
        for (int i = 0, count = labelIds.size(); i < count; i++) {
            if (labelIds.get(i) == null) {
                return false;
            } else if (labelIds.get(i) <= 0) {
                return false;
            }
        }
        return true;
    }

}
