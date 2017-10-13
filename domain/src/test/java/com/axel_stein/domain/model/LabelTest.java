package com.axel_stein.domain.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LabelTest {
    private Label mLabel;

    @Before
    public void setUp() throws Exception {
        mLabel = new Label();
    }

    @Test
    public void setTitle_null() throws Exception {
        mLabel.setTitle(null);
        assertNotNull(mLabel.getTitle());
    }

    @Test
    public void setTitle_largeString() throws Exception {
        mLabel.setTitle("В процессе символического торжества вы получите оригинальный сертификат, который вместе с уникальными фото ни у кого не вызовут сомнений в подлинности церемонии. Важное достоинство фиктивного брака &mdash; это возможность еще раз отметить мероприятие давно состоящим в браке супругам. Романтичное приключение в прекрасную страну &mdash; самый действенный способ вновь зажечь страсть и освежить полноту чувств мужу и жене.</p>\n" + "<p>Определить точную стоимость заграничного мероприятия безусловно сложно. Ценовая шкала варьируется в зависимости от ваших пожеланий и бюджета");
        assertNotNull(mLabel.getTitle());
        assertTrue(mLabel.getTitle().length() <= Label.MAX_TITLE_LENGTH);
        assertFalse(mLabel.getTitle().length() > Label.MAX_TITLE_LENGTH);
    }

}