package midorum.melbone.ui.internal.main;

import dma.function.VoidAction;
import dma.validation.Validator;
import midorum.melbone.model.exception.OptionHasNoValue;
import midorum.melbone.ui.internal.model.*;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ActionPane extends JPanel implements Updatable {

    private final Logger logger;
    private final AccountsHolder accountsHolder;
    private final TargetWindowOperations targetWindowOperations;
    private final JButton identifyButton;
    private final JButton confirmButton;
    private final JButton cancelButton;
    private final JButton settingsButton;
    private final JButton addAccountButton;

    public ActionPane(final AccountsHolder accountsHolder,
                      final TaskExecutorOperations taskExecutorOperations,
                      final SettingsManagerOperations settingsManagerOperations,
                      final TargetWindowOperations targetWindowOperations,
                      final VoidAction identifyDialogShowingAction, final Logger logger) {
        this.accountsHolder = Validator.checkNotNull(accountsHolder).orThrowForSymbol("accountsHolder");
        this.targetWindowOperations = Validator.checkNotNull(targetWindowOperations).orThrowForSymbol("targetWindowOperations");
        this.logger = logger;
        final TaskExecutorOperations taskExecutorOperationsChecked = Validator.checkNotNull(taskExecutorOperations).orThrowForSymbol("taskExecutorOperations");
        final SettingsManagerOperations settingsManagerOperationsChecked = Validator.checkNotNull(settingsManagerOperations).orThrowForSymbol("settingsManagerOperations");
        final VoidAction identifyDialogShowingActionChecked = Validator.checkNotNull(identifyDialogShowingAction).orThrowForSymbol("identifyDialogShowingAction");

        identifyButton = createButton("identify button", "Identify", e -> identifyDialogShowingActionChecked.perform());
        confirmButton = createButton("confirm button", "Confirm", e -> taskExecutorOperationsChecked.sendRoutineTask());
        cancelButton = createButton("cancel button", "Cancel current task", e -> taskExecutorOperationsChecked.cancelCurrentTask());
        settingsButton = createButton("settings button", "Settings", e -> settingsManagerOperationsChecked.showSettingsTab());
        addAccountButton = createButton("add account button", "Add account", e -> settingsManagerOperationsChecked.showAccountsTab());

        setLayout(new BorderLayout());
        final JPanel buttonPane = new JPanel();
        buttonPane.add(identifyButton);
        buttonPane.add(confirmButton);
        buttonPane.add(cancelButton);
        buttonPane.add(settingsButton);
        buttonPane.add(addAccountButton);
        add(buttonPane, BorderLayout.NORTH);

        final JLabel noticeLabel = new JLabel("Press Ctrl+Shift+S any time to stop executing current task");
        final JPanel tipPane = new JPanel();
        tipPane.add(noticeLabel);
        add(tipPane, BorderLayout.SOUTH);
    }

    private JButton createButton(final String name, final String text, final ActionListener actionListener) {
        JButton button = new JButton(text);
        button.setName(name);
        button.addActionListener(actionListener);
        return button;
    }

    @Override
    public void update() {
        try {
            if (accountsHolder.getAccountsCount() > 0) renderWhenFoundAccountsInUse();
            else renderWhenNotFoundAnyAccountInUse();
        } catch (OptionHasNoValue e) {
            logger.warn("Option has not found in storage:", e);
            renderWhenNotFoundSettingInStorage();
        }
    }

    private void renderWhenFoundAccountsInUse() {
        identifyButton.setVisible(targetWindowOperations.isExistUnboundWindows());
        confirmButton.setVisible(true);
        cancelButton.setVisible(true);
        settingsButton.setVisible(true);
        addAccountButton.setVisible(false);
    }

    private void renderWhenNotFoundAnyAccountInUse() {
        identifyButton.setVisible(targetWindowOperations.isExistUnboundWindows());
        confirmButton.setVisible(false);
        cancelButton.setVisible(false);
        settingsButton.setVisible(false);
        addAccountButton.setVisible(true);
    }

    private void renderWhenNotFoundSettingInStorage() {
        identifyButton.setVisible(false);
        confirmButton.setVisible(false);
        cancelButton.setVisible(false);
        settingsButton.setVisible(true);
        addAccountButton.setVisible(false);
    }
}
