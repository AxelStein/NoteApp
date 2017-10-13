package com.axel_stein.domain.utils.validators;

import com.axel_stein.domain.model.Notebook;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.axel_stein.domain.utils.validators.NotebookValidator.isValid;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NotebookValidatorTest {

    @Test
    public void isValidTest() throws Exception {
        Notebook notebook = new Notebook();
        notebook.setId(1);
        notebook.setTitle("Item");

        assertTrue(isValid(notebook));

        notebook = null;
        assertFalse(isValid(notebook));

        notebook = new Notebook();
        notebook.setTitle("Item");

        assertFalse(isValid(notebook));

        notebook = new Notebook();
        notebook.setTitle("Item");

        assertTrue(isValid(notebook, false));
    }

    @Test
    public void isValid_list() throws Exception {
        List<Notebook> notebooks = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Notebook notebook = new Notebook();
            notebook.setId(i + 1);
            notebook.setTitle("Item " + i);

            notebooks.add(notebook);
        }

        assertTrue(isValid(notebooks));

        List<Notebook> list = null;
        assertFalse(isValid(list));

        notebooks = new ArrayList<>();

        Notebook noId = new Notebook();
        noId.setTitle("Item");
        notebooks.add(noId);

        assertFalse(isValid(notebooks));

        notebooks = new ArrayList<>();

        Notebook noTitle = new Notebook();
        noTitle.setId(2);
        notebooks.add(noTitle);

        assertFalse(isValid(notebooks));
    }

}