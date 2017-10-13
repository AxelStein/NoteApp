package com.axel_stein.domain.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class NoteTest {
    private Note mNote;

    @Before
    public void setUp() throws Exception {
        mNote = new Note();
    }

    @Test
    public void setTitle_null() throws Exception {
        mNote.setTitle(null);
        assertNotNull(mNote.getTitle());
    }

    @Test
    public void setTitle_largeString() throws Exception {
        mNote.setTitle("В процессе символического торжества вы получите оригинальный сертификат, который вместе с уникальными фото ни у кого не вызовут сомнений в подлинности церемонии. Важное достоинство фиктивного брака &mdash; это возможность еще раз отметить мероприятие давно состоящим в браке супругам. Романтичное приключение в прекрасную страну &mdash; самый действенный способ вновь зажечь страсть и освежить полноту чувств мужу и жене.</p>\n" + "<p>Определить точную стоимость заграничного мероприятия безусловно сложно. Ценовая шкала варьируется в зависимости от ваших пожеланий и бюджета");
        assertNotNull(mNote.getTitle());
        assertTrue(mNote.getTitle().length() <= Note.MAX_TITLE_LENGTH);
        assertFalse(mNote.getTitle().length() > Note.MAX_TITLE_LENGTH);
    }

    @Test(expected = NullPointerException.class)
    public void addLabel_null() throws Exception {
        mNote.addLabel(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addLabel_emptyId() throws Exception {
        Label label = new Label();
        mNote.addLabel(label);
    }

    @Test(expected = NullPointerException.class)
    public void containsLabel_null() throws Exception {
        mNote.containsLabel(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void containsLabel_emptyId() throws Exception {
        Label label = new Label();
        mNote.containsLabel(label);
    }

    @Test(expected = NullPointerException.class)
    public void removeLabel_null() throws Exception {
        mNote.removeLabel(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeLabel_emptyId() throws Exception {
        Label label = new Label();
        mNote.removeLabel(label);
    }

    @Test
    public void setLabels() throws Exception {
        // todo
    }

}