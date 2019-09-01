package com.axel_stein.domain.utils.validators;

import com.axel_stein.domain.model.Notebook;

import java.util.List;

import static com.axel_stein.domain.utils.TextUtil.notEmpty;

public class NotebookValidator {

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isValid(Notebook notebook) {
        return isValid(notebook, true);
    }

    public static boolean isValid(Notebook notebook, boolean checkId) {
        if (notebook == null) {
            return false;
        }
        boolean validTitle = notEmpty(notebook.getTitle());
        if (checkId) {
            return validTitle && notEmpty(notebook.getId());
        }
        return validTitle;
    }

    public static boolean isValid(List<Notebook> notebooks) {
        if (notebooks == null) {
            return false;
        }
        for (Notebook notebook : notebooks) {
            if (!isValid(notebook)) {
                return false;
            }
        }
        return true;
    }

}
