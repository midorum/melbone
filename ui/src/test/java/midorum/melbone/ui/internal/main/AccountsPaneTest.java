package midorum.melbone.ui.internal.main;

import midorum.melbone.model.dto.Account;
import midorum.melbone.ui.context.MockedContext;
import midorum.melbone.ui.util.SwingTestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountsPaneTest extends MockedContext {

    @Test
    @DisplayName("Two total accounts; no accounts in use; no bound accounts")
    void zero() {
        final String acc1 = "acc1";
        final String acc2 = "acc2";
        new AccountPaneInteraction()
                .accountsLimit(2)
                .totalAccounts(acc1, acc2)
                .updatePane()
                .printStateBriefly();
    }

    @Test
    @DisplayName("Two total accounts; one account in use; no bound accounts; one account selected later")
    void one() {
        final String acc1 = "acc1";
        final String acc2 = "acc2";
        new AccountPaneInteraction()
                .accountsLimit(2)
                .totalAccounts(acc1, acc2)
                .accountsInUse(acc1)
                .updatePane()
                .clickCheckbox(acc1)
                .printStateBriefly();
    }

    @Test
    @DisplayName("Five total accounts; three accounts in use; one bound account; one account selected later")
    void two() {
        final String acc1 = "acc1";
        final String acc2 = "acc2";
        final String acc3 = "acc3";
        final String acc4 = "acc4";
        final String acc5 = "acc5";
        new AccountPaneInteraction()
                .accountsLimit(2)
                .totalAccounts(acc1, acc2, acc3, acc4, acc5)
                .accountsInUse(acc2, acc4, acc5)
                .bindAccounts(acc4)
                .updatePane()
                .clickCheckbox(acc2)
                .printStateBriefly();
    }

    @Test
    @DisplayName("Five total accounts; three accounts in use; two bound accounts; one account unselected later")
    void three() {
        final String acc1 = "acc1";
        final String acc2 = "acc2";
        final String acc3 = "acc3";
        final String acc4 = "acc4";
        final String acc5 = "acc5";
        new AccountPaneInteraction()
                .accountsLimit(2)
                .totalAccounts(acc1, acc2, acc3, acc4, acc5)
                .accountsInUse(acc2, acc4, acc5)
                .bindAccounts(acc2, acc4)
                .updatePane()
                .clickCheckbox(acc2)
                .printStateBriefly();
    }

    @Test
    @DisplayName("Three total accounts; three accounts in use; one bound account; one account unselected later")
    void four() {
        final String acc1 = "acc1";
        final String acc2 = "acc2";
        final String acc3 = "acc3";
        new AccountPaneInteraction()
                .accountsLimit(1)
                .totalAccounts(acc1, acc2, acc3)
                .accountsInUse(acc1, acc2, acc3)
                .bindAccounts(acc3)
                .updatePane()
                .clickCheckbox(acc3)
                .printStateBriefly();
    }

    @Test
    @DisplayName("Three total accounts; three accounts in use; one bound account; one account unselected later; another account selected later")
    void five() {
        final String acc1 = "acc1";
        final String acc2 = "acc2";
        final String acc3 = "acc3";
        new AccountPaneInteraction()
                .accountsLimit(1)
                .totalAccounts(acc1, acc2, acc3)
                .accountsInUse(acc1, acc2, acc3)
                .bindAccounts(acc3)
                .updatePane()
                .clickCheckbox(acc3)
                .clickCheckbox(acc1)
                .printStateBriefly();
    }

    private class AccountPaneInteraction {
        private final Interaction interaction;
        private final AccountsPane accountsPane;
        private final List<String> selectedCheckBoxes = new ArrayList<>();

        public AccountPaneInteraction() {
            interaction = new Interaction();
            accountsPane = new AccountsPane(accountStorage, context.targetWindowOperations(), applicationSettings, context.dataLoader(), context.logger());
        }

        public AccountPaneInteraction accountsLimit(final int accountsLimit) {
            interaction.setAccountsLimit(accountsLimit);
            return this;
        }

        public AccountPaneInteraction totalAccounts(final String... accounts) {
            interaction.setTotalAccounts(accounts);
            return this;
        }

        public AccountPaneInteraction accountsInUse(final String... accounts) {
            interaction.setAccountsInUse(accounts);
            return this;
        }

        public AccountPaneInteraction bindAccounts(final String... accounts) {
            interaction.setBoundAccounts(accounts);
            selectedCheckBoxes.addAll(Arrays.stream(accounts).toList());
            return this;
        }

        public AccountPaneInteraction updatePane() {
            checkInternalState();
            this.accountsPane.update();
            return verifyPaneState();
        }

        public AccountPaneInteraction verifyPaneState() {
            checkInternalState();
            final List<JCheckBox> checkBoxes = SwingTestUtil.INSTANCE.getChildrenOfType(accountsPane, JCheckBox.class);
            verifyCheckboxesInContainer(checkBoxes);
            verifyCheckboxesDisablingWhenLimitReached(checkBoxes);
            verifyTotalAccountsCountReturned();
            verifySelectedAccountsReturned();
            return this;
        }

        public AccountPaneInteraction clickCheckbox(final String account) {
            checkInternalState();
            final Collection<String> accountsInUse = accountStorage.accountsInUse();
            if (!accountsInUse.contains(account)) {
                throw new IllegalStateException("Selecting account must be included into accounts in use");
            }
            SwingTestUtil.INSTANCE.getChildrenOfType(accountsPane, JCheckBox.class).stream()
                    .filter(checkBox -> account.equals(checkBox.getText()))
                    .filter(Component::isEnabled)
                    .findFirst()
                    .ifPresentOrElse(checkBox -> {
                        checkBox.doClick();
                        if (checkBox.isSelected()) selectedCheckBoxes.add(account);
                        else selectedCheckBoxes.remove(account);
                    }, () -> {
                        throw new IllegalStateException("Cannot click checkbox for \"" + account + "\". It's disabled or doesn't exist");
                    });
            return verifyPaneState();
        }

        public AccountPaneInteraction printContextInstances() {
            interaction.printContextInstances();
            return this;
        }

        public AccountPaneInteraction printState() {
            interaction.printState("selectedCheckBoxes: " + selectedCheckBoxes);
            return this;
        }

        public AccountPaneInteraction printStateBriefly() {
            interaction.printStateBriefly("selectedCheckBoxes: " + selectedCheckBoxes);
            return this;
        }

        private void checkInternalState() {
            if (interaction.getAccountsLimit() == -1)
                throw new IllegalStateException("You must set accounts limit before");
        }

        private void verifyCheckboxesInContainer(final List<JCheckBox> checkBoxes) {
            final Collection<String> accountsInUse = accountStorage.accountsInUse();
            final int totalCountShouldBe = accountsInUse.size();
            assertEquals(totalCountShouldBe, checkBoxes.size(), "Should have " + totalCountShouldBe + " checkboxes when there are " + totalCountShouldBe + " accounts in use");
            checkBoxes.forEach(checkBox -> {
                final String checkBoxText = checkBox.getText();
                assertTrue(accountsInUse.contains(checkBoxText), "Checkbox text should correspond the account name");
                if (selectedCheckBoxes.contains(checkBoxText)) {
                    assertTrue(checkBox.isSelected(), "Checkbox should be selected");
                } else {
                    assertFalse(checkBox.isSelected(), "Checkbox shouldn't be selected");
                }
            });
        }

        private void verifyCheckboxesDisablingWhenLimitReached(final List<JCheckBox> checkBoxes) {
            final int accountsLimit = interaction.getAccountsLimit();
            if (selectedCheckBoxes.size() < accountsLimit)
                checkBoxes.forEach(checkBox -> assertTrue(checkBox.isEnabled(), "Checkbox should be enabled because bound accounts limit (" + accountsLimit + ") do not reached"));
            else
                checkBoxes.forEach(checkBox -> {
                    final String checkBoxText = checkBox.getText();
                    if (selectedCheckBoxes.contains(checkBoxText)) {
                        assertTrue(checkBox.isEnabled(), "Checkbox should be enabled when checked");
                    } else {
                        assertFalse(checkBox.isEnabled(), "Checkbox shouldn't be enabled because bound accounts limit reached");
                    }
                });
        }

        private void verifyTotalAccountsCountReturned() {
            final int shouldBeAccountsCount = accountStorage.accountsInUse().size();
            assertEquals(shouldBeAccountsCount, accountsPane.getAccountsCount(), "Should return accounts count equals " + shouldBeAccountsCount);
        }

        private void verifySelectedAccountsReturned() {
            final int shouldBeSelectedAccountsCount = selectedCheckBoxes.size();
            final Account[] obtainedSelectedAccounts = accountsPane.getSelectedAccounts();
            assertEquals(shouldBeSelectedAccountsCount, obtainedSelectedAccounts.length, "Should return " + shouldBeSelectedAccountsCount + " selected accounts");
            Arrays.stream(obtainedSelectedAccounts).map(Account::name).forEach(s -> assertTrue(selectedCheckBoxes.contains(s), "Should return correspond account"));
        }
    }
}