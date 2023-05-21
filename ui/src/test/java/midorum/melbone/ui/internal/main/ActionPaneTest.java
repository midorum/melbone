package midorum.melbone.ui.internal.main;

import dma.function.VoidAction;
import midorum.melbone.ui.util.SwingTestUtil;
import midorum.melbone.ui.internal.model.AccountsHolder;
import midorum.melbone.ui.internal.model.SettingsManagerOperations;
import midorum.melbone.ui.internal.model.TargetWindowOperations;
import midorum.melbone.ui.internal.model.TaskExecutorOperations;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ActionPaneTest {

    private final AccountsHolder accountsHolder = mock(AccountsHolder.class);
    private final TaskExecutorOperations taskExecutorOperations = mock(TaskExecutorOperations.class);
    private final SettingsManagerOperations settingsManagerOperations = mock(SettingsManagerOperations.class);
    private final TargetWindowOperations targetWindowOperations = mock(TargetWindowOperations.class);
    private final VoidAction identifyDialogShowingAction = mock(VoidAction.class);

    @Test
    void noAccounts_noUnboundWindows() {
        when(accountsHolder.getAccountsCount()).thenReturn(0);
        when(targetWindowOperations.isExistUnboundWindows()).thenReturn(false);

        final ActionPane actionPane = new ActionPane(accountsHolder, taskExecutorOperations, settingsManagerOperations, targetWindowOperations, identifyDialogShowingAction, LogManager.getLogger());
        actionPane.update();

        final Optional<Component> identifyButton = SwingTestUtil.INSTANCE.getChildNamed(actionPane, "identify button");
        assertTrue(identifyButton.isPresent(), "Identify button should present");
        assertFalse(identifyButton.get().isVisible(), "Identify button shouldn't be visible when not exists any unbound window");

        final Optional<Component> confirmButton = SwingTestUtil.INSTANCE.getChildNamed(actionPane, "confirm button");
        assertTrue(confirmButton.isPresent(), "Confirm button should present");
        assertFalse(confirmButton.get().isVisible(), "Confirm button shouldn't be visible when accounts count equals 0");

        final Optional<Component> cancelButton = SwingTestUtil.INSTANCE.getChildNamed(actionPane, "cancel button");
        assertTrue(cancelButton.isPresent(), "Cancel button should present");
        assertFalse(cancelButton.get().isVisible(), "Cancel button shouldn't be visible when accounts count equals 0");

        final Optional<Component> settingsButton = SwingTestUtil.INSTANCE.getChildNamed(actionPane, "settings button");
        assertTrue(settingsButton.isPresent(), "Settings button should present");
        assertFalse(settingsButton.get().isVisible(), "Settings button shouldn't be visible when accounts count equals 0");

        final Optional<Component> addAccountButton = SwingTestUtil.INSTANCE.getChildNamed(actionPane, "add account button");
        assertTrue(addAccountButton.isPresent(), "Add account button should present");
        assertTrue(addAccountButton.get().isVisible(), "Add account button should be visible when accounts count equals 0");
        ((JButton) addAccountButton.get()).doClick();
        verify(settingsManagerOperations).showAccountsTab();
    }

    @Test
    void oneAccount_oneUnboundWindow() throws InterruptedException {
        when(accountsHolder.getAccountsCount()).thenReturn(1);
        when(targetWindowOperations.isExistUnboundWindows()).thenReturn(true);

        final ActionPane actionPane = new ActionPane(accountsHolder, taskExecutorOperations, settingsManagerOperations, targetWindowOperations, identifyDialogShowingAction, LogManager.getLogger());
        actionPane.update();

        final Optional<Component> identifyButton = SwingTestUtil.INSTANCE.getChildNamed(actionPane, "identify button");
        assertTrue(identifyButton.isPresent(), "Identify button should present");
        assertTrue(identifyButton.get().isVisible(), "Identify button should be visible when exists any unbound window");
        ((JButton) identifyButton.get()).doClick();
        verify(identifyDialogShowingAction).perform();

        final Optional<Component> confirmButton = SwingTestUtil.INSTANCE.getChildNamed(actionPane, "confirm button");
        assertTrue(confirmButton.isPresent(), "Confirm button should present");
        assertTrue(confirmButton.get().isVisible(), "Confirm button should be visible when accounts count greater than 0");
        ((JButton) confirmButton.get()).doClick();
        verify(taskExecutorOperations).sendRoutineTask();

        final Optional<Component> cancelButton = SwingTestUtil.INSTANCE.getChildNamed(actionPane, "cancel button");
        assertTrue(cancelButton.isPresent(), "Cancel button should present");
        assertTrue(cancelButton.get().isVisible(), "Cancel button shouldn't be visible when accounts count greater than 0");
        ((JButton) cancelButton.get()).doClick();
        verify(taskExecutorOperations).cancelCurrentTask();

        final Optional<Component> settingsButton = SwingTestUtil.INSTANCE.getChildNamed(actionPane, "settings button");
        assertTrue(settingsButton.isPresent(), "Settings button should present");
        assertTrue(settingsButton.get().isVisible(), "Settings button shouldn't be visible when accounts count greater than 0");
        ((JButton) settingsButton.get()).doClick();
        verify(settingsManagerOperations).showSettingsTab();

        final Optional<Component> addAccountButton = SwingTestUtil.INSTANCE.getChildNamed(actionPane, "add account button");
        assertTrue(addAccountButton.isPresent(), "Add account button should present");
        assertFalse(addAccountButton.get().isVisible(), "Add account button shouldn't be visible when accounts count greater than 0");
    }
}