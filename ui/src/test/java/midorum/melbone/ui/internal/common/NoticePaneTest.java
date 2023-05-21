package midorum.melbone.ui.internal.common;

import midorum.melbone.ui.context.MockedContext;
import midorum.melbone.ui.util.SwingTestUtil;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoticePaneTest extends MockedContext {

    @Test
    void testNotice() {
        final NoticePane noticePane = new NoticePane(context.logger());
        final Optional<Component> noticeLabel = SwingTestUtil.INSTANCE.getChildNamed(noticePane, "notice label");
        assertTrue(noticeLabel.isPresent(), "Notice label should present");
        assertTrue(noticeLabel.get().isVisible(), "Notice label should be visible");
        assertTrue(textIsNullOrBlank(noticeLabel), "Just created notice label should be blank");

        final String testInfo = "test";
        noticePane.showInfo(testInfo);
        assertEquals(testInfo, getLabelText(noticeLabel), "Shown info should be " + testInfo);
        assertEquals(Color.gray, noticeLabel.get().getForeground(), "Shown info color should be " + Color.gray);

        noticePane.clearNotice();
        assertTrue(textIsNullOrBlank(noticeLabel), "Cleared notice label should be blank");

        final String errorText = "error";
        noticePane.showError(errorText);
        assertEquals(errorText, getLabelText(noticeLabel), "Shown error should be " + errorText);
        assertEquals(Color.red, noticeLabel.get().getForeground(), "Shown error color should be " + Color.red);

        noticePane.clearNotice();
        assertTrue(textIsNullOrBlank(noticeLabel), "Cleared notice label should be blank");

        final Exception exception = new Exception("test exception");
        noticePane.showError(exception);
        assertEquals(exception.getMessage(), getLabelText(noticeLabel), "Shown error should be " + exception.getMessage());
        assertEquals(Color.red, noticeLabel.get().getForeground(), "Shown error color should be " + Color.red);

        noticePane.clearNotice();
        assertTrue(textIsNullOrBlank(noticeLabel), "Cleared notice label should be blank");
    }

    private boolean textIsNullOrBlank(final Optional<Component> noticeLabel) {
        final String labelText = getLabelText(noticeLabel);
        return labelText == null || labelText.isBlank();
    }

    private String getLabelText(final Optional<Component> noticeLabel) {
        return ((JLabel) noticeLabel.get()).getText();
    }

}