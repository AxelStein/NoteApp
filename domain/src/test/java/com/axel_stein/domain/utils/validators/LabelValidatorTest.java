package com.axel_stein.domain.utils.validators;

import com.axel_stein.domain.model.Label;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.axel_stein.domain.utils.validators.LabelValidator.isValidIds;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LabelValidatorTest {

    @Test
    public void isValid() throws Exception {
        Label label = null;
        assertFalse(LabelValidator.isValid(label));

        label = new Label();
        label.setId(1);
        label.setTitle("Item");

        assertTrue(LabelValidator.isValid(label));

        label = new Label();
        label.setTitle("Item");

        assertFalse(LabelValidator.isValid(label));
    }

    @Test
    public void isValid_checkId() throws Exception {
        Label label = new Label();
        label.setTitle("Item");

        assertTrue(LabelValidator.isValid(label, false));

        label = new Label();
        label.setTitle("Item");

        assertFalse(LabelValidator.isValid(label, true));
    }

    @Test
    public void isValid_list() throws Exception {
        List<Label> labels = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Label label = new Label();
            label.setId(i + 1);
            label.setTitle("Item " + i);

            labels.add(label);
        }

        assertTrue(LabelValidator.isValid(labels));

        // listWithNonValidLabels
        labels = new ArrayList<>();

        Label noId = new Label();
        noId.setTitle("Item");
        labels.add(noId);

        assertFalse(LabelValidator.isValid(labels));

        labels = new ArrayList<>();

        Label noTitle = new Label();
        noTitle.setId(2);
        labels.add(noTitle);

        assertFalse(LabelValidator.isValid(labels));
    }

    @Test
    public void isValidIdsTest() throws Exception {
        assertFalse(isValidIds(null));

        List<Long> list = new ArrayList<>();
        list.add(4L);
        list.add(56L);

        assertTrue(isValidIds(list));

        list = new ArrayList<>();
        list.add(4L);
        list.add(56L);
        list.add(-1L);

        assertFalse(isValidIds(list));

        list = new ArrayList<>();
        list.add(4L);
        list.add(56L);
        list.add(null);

        assertFalse(isValidIds(list));
    }

}