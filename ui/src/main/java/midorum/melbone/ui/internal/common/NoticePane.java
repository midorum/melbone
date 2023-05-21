package midorum.melbone.ui.internal.common;

import dma.validation.Validator;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;

public class NoticePane extends JPanel {

    private final Logger logger;
    private final JLabel noticeLabel;

    public NoticePane(final Logger logger) {
        this.logger = logger;

        this.noticeLabel = new JLabel();
        this.noticeLabel.setName("notice label");
        add(noticeLabel);
    }

    public void clearNotice() {
        noticeLabel.setText(null);
    }

    public void showError(String text) {
        noticeLabel.setText(text);
        noticeLabel.setForeground(Color.red);
    }

    public void showError(Throwable throwable) {
        logger.error(throwable.getMessage(), throwable);
        showError(Validator.checkNotNull(throwable.getMessage()).orDefault(throwable.toString()));
    }

    public void showError(String text, Throwable throwable) {
        logger.error(text + ": " + throwable.getMessage(), throwable);
        showError(text);
    }

    public void showSuccess(String text) {
        noticeLabel.setText(text);
        noticeLabel.setForeground(Color.blue);
    }

    public void showInfo(String text) {
        noticeLabel.setText(text);
        noticeLabel.setForeground(Color.gray);
    }
}
