package midorum.melbone.ui.internal.main;

import com.midorum.win32api.facade.exception.Win32ApiException;
import com.midorum.win32api.win32.IWinUser;
import midorum.melbone.model.window.baseapp.BaseAppWindow;
import midorum.melbone.ui.context.MockedContext;
import midorum.melbone.ui.util.SwingTestUtil;
import midorum.melbone.ui.internal.model.OnCloseNotificator;
import midorum.melbone.ui.internal.util.IdentifyDialog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IdentifyDialogTest extends MockedContext {

    private final OnCloseNotificator onCloseNotificator = mock(OnCloseNotificator.class);

    @Test
    @DisplayName("No any unbound accounts")
    void noAnyUnboundAccounts() {
        new IdentifyDialogInteraction()
                .unboundWindowsCount(1)
                .show()
                .clickCloseButton();
    }

    @Test
    @DisplayName("No any unbound windows")
    void noAnyUnboundWindows() {
        final String acc1 = "acc1";
        new IdentifyDialogInteraction()
                .accountsInUse(acc1)
                .unboundWindowsCount(0)
                .show()
                .clickCloseButton();
    }

    @Test
    @DisplayName("One unbound window")
    void oneUnboundWindow() throws Win32ApiException {
        final String acc1 = "acc1";
        final String acc2 = "acc2";
        final String acc3 = "acc3";
        new IdentifyDialogInteraction()
                .accountsInUse(acc1, acc2, acc3)
                .unboundWindowsCount(1)
                .show()
                .clickCheckWindowButtonWithSuccess()
                .selectAccountToBind(acc1)
                .clickBindButton()
                .clickCloseButton();
    }

    @Test
    @DisplayName("Two unbound windows")
    void twoUnboundWindows() throws Win32ApiException {
        final String acc1 = "acc1";
        final String acc2 = "acc2";
        final String acc3 = "acc3";
        new IdentifyDialogInteraction()
                .accountsInUse(acc1, acc2, acc3)
                .unboundWindowsCount(2)
                .show()
                .clickCheckWindowButtonWithError()
                .clickCheckWindowButtonWithSuccess()
                .selectAccountToBind(acc1)
                .clickBindButton()
                .clickCheckWindowButtonWithSuccess()
                .selectAccountToBind(acc2)
                .clickBindButton()
                .clickCloseButton();
    }

    private class IdentifyDialogInteraction {

        private final Interaction interaction;
        private IdentifyDialog identifyDialog;
        private BaseAppWindow selectedWindow;
        private String selectedAccount;
        private String currentOperation;
        private int checkWindowButtonInteractionCount = 0;

        private IdentifyDialogInteraction() {
            interaction = new Interaction();
        }

        /*-- setup --*/

        public IdentifyDialogInteraction accountsInUse(final String... accounts) {
            interaction.setTotalAccounts(accounts);
            interaction.setAccountsInUse(accounts);
            return this;
        }

        public IdentifyDialogInteraction unboundWindowsCount(final int count) {
            if (count < 0) throw new IllegalArgumentException("Count cannot be negative");
            interaction.createUnboundWindows(count);
            return this;
        }

        /*-- actions --*/

        public IdentifyDialogInteraction show() {
            if (this.identifyDialog != null) throw new IllegalStateException("You can show dialog only once");
            this.currentOperation = "show";
            this.identifyDialog = new IdentifyDialog(new Frame(), context, onCloseNotificator);
            verifyPaneState();
            return this;
        }

        public IdentifyDialogInteraction clickCheckWindowButtonWithSuccess() {
            this.currentOperation = "clickCheckWindowButtonWithSuccess";
            interaction.whenTryCatchMouseKeyEvent(IWinUser.WM_LBUTTONDOWN)
                    .thenCatchWithSuccessAndDoAfter(() -> {
                        //this is select window operation, it must change dialog state
                        this.checkWindowButtonInteractionCount++;
                        this.selectedWindow = interaction.getFirstUnboundWindow().orElseThrow(() -> new IllegalStateException("Probably error in test environment: cannot select unbound window because there are not any"));
                    });
            getCheckWindowButton().doClick();
            verifyPaneState();
            return this;
        }

        public IdentifyDialogInteraction clickCheckWindowButtonWithError() {
            this.currentOperation = "clickCheckWindowButtonWithFail";
            interaction.whenTryCatchMouseKeyEvent(IWinUser.WM_LBUTTONDOWN)
                    .thenCatchWithErrorAndDoAfter(new Throwable("test"),
                            (throwable) -> {
                                //this is failed select window operation, it must change notice state only
                                this.checkWindowButtonInteractionCount++;
                                this.selectedWindow = null;
                            });
            getCheckWindowButton().doClick();
            verifyPaneState();
            return this;
        }

        public IdentifyDialogInteraction selectAccountToBind(final String accountName) {
            this.currentOperation = "selectAccountToBind: " + accountName;
            this.selectedAccount = accountName;
            getSelectAccountComboBox().setSelectedItem(accountName);
            verifyPaneState();
            return this;
        }

        public IdentifyDialogInteraction clickBindButton() throws Win32ApiException {
            this.currentOperation = "clickBindButton";
            getBindButton().doClick();
            verifyBindingWindowAndAccount();
            verifyPaneState();
            return this;
        }

        public void clickCloseButton() {
            this.currentOperation = "clickCloseButton";
            getCloseButton().doClick();
            verifyPaneState();
            assertFalse(identifyDialog.isVisible(), "Dialog shouldn't be visible after close");
            verify(mouseHookHelper, times(checkWindowButtonInteractionCount)).setGlobalHookForKey(eq(IWinUser.WM_LBUTTONDOWN), any(), any());
        }

        /*-- dialog components --*/

        @SuppressWarnings("unchecked")
        private JComboBox<String> getSelectAccountComboBox() {
            return (JComboBox<String>) SwingTestUtil.INSTANCE.getChildNamed(identifyDialog, "select account combo box")
                    .orElseThrow(() -> new IllegalStateException("Dialog should have select account combo box"));
        }

        private JButton getCheckWindowButton() {
            return (JButton) SwingTestUtil.INSTANCE.getChildNamed(identifyDialog, "check window button")
                    .orElseThrow(() -> new IllegalStateException("Dialog should have \"Check window\" button"));
        }

        private JButton getBindButton() {
            return (JButton) SwingTestUtil.INSTANCE.getChildNamed(identifyDialog, "bind button")
                    .orElseThrow(() -> new IllegalStateException("Dialog should have \"Bind\" button"));
        }

        private JButton getCloseButton() {
            return (JButton) SwingTestUtil.INSTANCE.getChildNamed(identifyDialog, "close button")
                    .orElseThrow(() -> new IllegalStateException("Dialog should have \"Close\" button"));
        }

        private JLabel getNoticeLabel() {
            return (JLabel) SwingTestUtil.INSTANCE.getChildNamed(identifyDialog, "notice label")
                    .orElseThrow(() -> new IllegalStateException("Dialog should have notice pane"));
        }

        /*-- verifying --*/

        private void verifyPaneState() {
            checkInternalState();
            logger.info(stringifyInternalState());
            verifyComboBoxState();
            verifyCheckWindowButtonState();
            verifyBindButtonState();
            verifyCloseButtonState();
        }

        private void checkInternalState() {
            if (this.identifyDialog == null)
                throw new IllegalStateException("You must build interaction (show dialog) before");
        }

        private String stringifyInternalState() {
            final JComboBox<String> selectAccountComboBox = getSelectAccountComboBox();
            final JButton checkWindowButton = getCheckWindowButton();
            final JButton bindButton = getBindButton();
            final JButton closeButton = getCloseButton();
            final JLabel noticeLabel = getNoticeLabel();
            return "\nCurrent environment state after operation \"" + this.currentOperation + "\":"
                    + "\n\tunbound accounts: " + interaction.getUnboundAccounts()
                    + "\n\tunbound windows count: " + interaction.getUnboundWindows().size()
                    + "\n\tselected unbound window: " + selectedWindow
                    + "\n\tselected account: " + selectedAccount
                    + "\nCurrent dialog state after operation \"" + this.currentOperation + "\":"
                    + "\n\t\"Select account\" combo box state:"
                    + "\n\t\tvisible: " + selectAccountComboBox.isVisible() + " (" + stringifyCheckResult(true, selectAccountComboBox.isVisible()) + ")"
                    + "\n\t\tenabled: " + selectAccountComboBox.isEnabled() + " (" + stringifyCheckResult(isComboBoxShouldBeEnabled(), selectAccountComboBox.isEnabled()) + ")"
                    + "\n\t\tselected item: " + selectAccountComboBox.getSelectedItem() + " (" + stringifyCheckResult(selectedAccount, selectAccountComboBox.getSelectedItem()) + ")"
                    + "\n\t\"Check window\" button state:"
                    + "\n\t\tvisible: " + checkWindowButton.isVisible() + " (" + stringifyCheckResult(true, checkWindowButton.isVisible()) + ")"
                    + "\n\t\tenabled: " + checkWindowButton.isEnabled() + " (" + stringifyCheckResult(isCheckWindowButtonShouldBeEnabled(), checkWindowButton.isEnabled()) + ")"
                    + "\n\t\"Bind\" button state:"
                    + "\n\t\tvisible: " + bindButton.isVisible() + " (" + stringifyCheckResult(true, bindButton.isVisible()) + ")"
                    + "\n\t\tenabled: " + bindButton.isEnabled() + " (" + stringifyCheckResult(isBindButtonShouldBeEnabled(), bindButton.isEnabled()) + ")"
                    + "\n\t\"Close\" button state:"
                    + "\n\t\tvisible: " + closeButton.isVisible() + " (" + stringifyCheckResult(true, closeButton.isVisible()) + ")"
                    + "\n\t\tenabled: " + closeButton.isEnabled() + " (" + stringifyCheckResult(true, closeButton.isEnabled()) + ")"
                    + "\n\tNotice label state:"
                    + "\n\t\tvisible: " + noticeLabel.isVisible() + " (" + stringifyCheckResult(true, noticeLabel.isVisible()) + ")"
                    + "\n\t\ttext: " + noticeLabel.getText()
                    + "\n";
        }

        private String stringifyCheckResult(final boolean shouldBe, final boolean checkResult) {
            return shouldBe == checkResult ? "OK" : "FAIL";
        }

        private String stringifyCheckResult(final Object shouldBe, final Object checkResult) {
            return Objects.equals(shouldBe, checkResult) ? "OK" : "FAIL - should be \"" + shouldBe + "\"";
        }

        private boolean isComboBoxShouldBeEnabled() {
            return this.selectedWindow != null;
        }

        private boolean isCheckWindowButtonShouldBeEnabled() {
            return !interaction.getUnboundAccounts().isEmpty() && interaction.getFirstUnboundWindow().isPresent() && this.selectedWindow == null;
        }

        private boolean isBindButtonShouldBeEnabled() {
            return !interaction.getUnboundAccounts().isEmpty() && interaction.getFirstUnboundWindow().isPresent() && this.selectedWindow != null && this.selectedAccount != null;
        }

        private void verifyComboBoxState() {
            final JComboBox<String> selectAccountComboBox = getSelectAccountComboBox();
            if (isComboBoxShouldBeEnabled())
                assertTrue(selectAccountComboBox.isEnabled(), "Select account combo box should be enabled");
            else
                assertFalse(selectAccountComboBox.isEnabled(), "Select account combo box should be disabled");
            assertEquals(selectedAccount, selectAccountComboBox.getSelectedItem(), "Selected account should be " + selectedAccount);
        }

        private void verifyCheckWindowButtonState() {
            final JButton checkWindowButton = getCheckWindowButton();
            if (isCheckWindowButtonShouldBeEnabled())
                assertTrue(checkWindowButton.isEnabled(), "\"Check window\" button should be enabled");
            else
                assertFalse(checkWindowButton.isEnabled(), "\"Check window\" button should be disabled");
        }

        private void verifyBindButtonState() {
            final JButton bindButton = getBindButton();
            if (isBindButtonShouldBeEnabled())
                assertTrue(bindButton.isEnabled(), "\"Bind\" button should be enabled");
            else
                assertFalse(bindButton.isEnabled(), "\"Bind\" button should be disabled");

        }

        private void verifyCloseButtonState() {
            final JButton closeButton = getCloseButton();
            assertTrue(closeButton.isEnabled(), "\"Bind\" button should be enabled");
        }

        private void verifyBindingWindowAndAccount() throws Win32ApiException {
            verify(selectedWindow).bindWithAccount(selectedAccount);
            this.selectedWindow = null;
            this.selectedAccount = null;
        }
    }
}