package com.axel_stein.data.label;

import com.axel_stein.domain.model.Label;

import org.junit.Test;

import static com.axel_stein.data.label.LabelMapper.map;
import static com.axel_stein.domain.utils.validators.LabelValidator.isValid;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LabelMapperTest {

    @Test
    public void mapEntity() {
        LabelEntity entity = new LabelEntity();
        assertFalse(isValid(map(entity)));

        entity = new LabelEntity();
        entity.setId(1);
        assertFalse(isValid(map(entity)));

        entity = new LabelEntity();
        entity.setTitle("Test");
        assertFalse(isValid(map(entity)));

        entity = new LabelEntity();
        entity.setId(1);
        entity.setTitle("Test");

        assertTrue(isValid(map(entity)));
    }

    @Test
    public void mapLabel() {
        Label label = new Label();
        assertFalse(isValidEntity(map(label)));

        label = new Label();
        label.setId(1);
        assertFalse(isValidEntity(map(label)));

        label = new Label();
        label.setTitle("Test");
        assertFalse(isValidEntity(map(label)));

        label = new Label();
        label.setId(1);
        label.setTitle("Test");
        assertTrue(isValidEntity(map(label)));
    }

    private boolean isValidEntity(LabelEntity entity) {
        return entity != null && entity.getId() > 0 && !isEmpty(entity.getTitle());
    }

    private boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

}