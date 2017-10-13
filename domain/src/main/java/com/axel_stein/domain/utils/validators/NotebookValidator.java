package com.axel_stein.domain.utils.validators;

import com.axel_stein.domain.model.Notebook;

import java.util.List;

import static com.axel_stein.domain.utils.TextUtil.isEmpty;

public class NotebookValidator {

    public static boolean isValid(Notebook notebook) {
        return isValid(notebook, true);
    }

    public static boolean isValid(Notebook notebook, boolean checkId) {
        if (notebook == null) {
            return false;
        }
        boolean validTitle = !isEmpty(notebook.getTitle());
        if (checkId) {
            return validTitle && notebook.getId() > 0;
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
