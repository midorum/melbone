package midorum.melbone.ui.internal.util;

import com.midorum.win32api.facade.Rectangle;
import dma.validation.Validator;
import midorum.melbone.model.settings.stamp.Stamp;
import midorum.melbone.ui.internal.Context;
import midorum.melbone.ui.internal.common.Preview;
import midorum.melbone.ui.internal.model.OnCloseNotificator;
import midorum.melbone.ui.internal.model.FrameVisibilityOperations;
import midorum.melbone.ui.internal.settings.SettingsManagerForm;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;
import java.util.function.Consumer;

public class StandardDialogsProvider {

    private static final StandardDialogsProvider INSTANCE = new StandardDialogsProvider();

    private StandardDialogsProvider() {
    }

    public static StandardDialogsProvider getInstance() {
        return INSTANCE;
    }

    public boolean askYesNoQuestion(final Component owner, final String question) {
        return askYesNoQuestion(owner, question, "Warning");
    }

    public boolean askYesNoQuestion(final Component owner, final String question, final String title) {
        int result = JOptionPane.showConfirmDialog(owner, Validator.checkNotNull(question).orThrowForSymbol("question"), title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    public boolean askOkCancelConfirm(final Component owner, final String information, final String title) {
        final int result = JOptionPane.showConfirmDialog(owner, Validator.checkNotNull(information).orThrowForSymbol("information"), title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
        return result == JOptionPane.OK_OPTION;
    }

    public void captureRectangle(final Consumer<Optional<Rectangle>> rectangleConsumer) {
        new CaptureWindow(rectangleConsumer).display();
    }

    public void showStampPreviewDialog(final Stamp stamp) {
        JDialog dialog = new JDialog();
        dialog.add(new Preview(stamp));
        dialog.pack();
        dialog.validate();
        dialog.setVisible(true);
    }

    public IdentifyDialog createIdentifyDialog(final Frame owner, final Context context, final OnCloseNotificator onCloseNotificator) {
        return new IdentifyDialog(owner, context, onCloseNotificator);
    }

    public SettingsManagerForm createSettingsManagerForm(final Context context, final OnCloseNotificator onCloseNotificator) {
        return new SettingsManagerForm(context, onCloseNotificator);
    }

    public FrameVisibilityOperations createFrameVisibilityOperations(final Frame frame) {
        return new FrameVisibilityOperations() {
            @Override
            public void show() {
                frame.setVisible(true);
            }

            @Override
            public void hide() {
                frame.setVisible(false);
            }

            @Override
            public boolean isVisible() {
                return frame.isVisible();
            }

            @Override
            public void iconify() {
                frame.setState(Frame.ICONIFIED);
            }

            @Override
            public void restore() {
                frame.setState(Frame.NORMAL);
            }

            @Override
            public boolean isIconified() {
                return Frame.ICONIFIED == frame.getState();
            }
        };
    }
}
