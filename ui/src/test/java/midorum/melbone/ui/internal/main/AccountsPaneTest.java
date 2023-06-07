package midorum.melbone.ui.internal.main;

import midorum.melbone.model.dto.Account;
import midorum.melbone.ui.context.MockedContext;
import midorum.melbone.ui.util.SwingTestUtil;
import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class AccountsPaneTest extends MockedContext {

    @Test
    @DisplayName("Two total accounts; no accounts in use; no bound accounts")
    void zero() {
        final String acc1 = "acc1";
        final String acc2 = "acc2";
        new AccountPaneInteraction()
                .accountsLimit(2)
                .totalAccounts(createAccount(acc1), createAccountWithCommentary(acc2))
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
                .totalAccounts(createAccount(acc1), createAccountWithCommentary(acc2))
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
                .totalAccounts(createAccount(acc1), createAccountWithCommentary(acc2), createAccountWithCommentary(acc3), createAccount(acc4), createAccountWithCommentary(acc5))
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

    @Test
    @DisplayName("Nine total accounts; nine accounts in use; some accounts with commentary; filtering accounts by commentary")
    void testFiltering() {
        final int accountsLimit = 3;
        final String acc1 = "acc1";
        final String acc2 = "acc2";
        final String acc3 = "acc3";
        final String acc4 = "acc4";
        final String acc5 = "acc5";
        final String acc6 = "acc6";
        final String acc7 = "acc7";
        final String acc8 = "acc8";
        final String acc9 = "acc9";
        final String commentaryGroup1 = "group1";
        final String commentaryGroup2 = "group2";
        final String commentaryGroup3 = "group3";
        new AccountPaneInteraction()
                .accountsLimit(accountsLimit)
                .totalAccounts(
                        createAccount(acc1),
                        createAccountWithCommentary(acc2, commentaryGroup1),
                        createAccountWithCommentary(acc3, commentaryGroup2),
                        createAccount(acc4),
                        createAccountWithCommentary(acc5, commentaryGroup2),
                        createAccountWithCommentary(acc6, commentaryGroup1),
                        createAccountWithCommentary(acc7, commentaryGroup1 + "; " + commentaryGroup3),
                        createAccountWithCommentary(acc8, commentaryGroup3 + ";" + commentaryGroup1),
                        createAccountWithCommentary(acc9, commentaryGroup1 + "; " + commentaryGroup2)
                )
                .accountsInUse(acc1, acc2, acc3, acc4, acc5, acc6, acc7, acc8, acc9)
                .bindAccounts(acc1, acc2, acc3)
                .updatePane()
                .scenario("Check initial filter state")
                .verifyFilterComboBoxHasValue(AccountsPane.FILTER_SHOW_ALL)
                .verifyCheckBoxesRendering(acc1, acc2, acc3, acc4, acc5, acc6, acc7, acc8, acc9)
                .printStateBriefly()
                .scenario("Filter accounts by first comment")
                .selectFilter(commentaryGroup1)
                .verifyCheckBoxesRendering(acc1, acc2, acc3, acc6, acc7, acc8, acc9)
                .printStateBriefly()
                .scenario("Filter accounts by second comment")
                .selectFilter(commentaryGroup2)
                .verifyCheckBoxesRendering(acc1, acc2, acc3, acc5, acc9)
                .printStateBriefly()
                .scenario("Filter accounts by third comment")
                .selectFilter(commentaryGroup3)
                .verifyCheckBoxesRendering(acc1, acc2, acc3, acc7, acc8)
                .printStateBriefly()
                .scenario("Clear filter")
                .selectFilter(AccountsPane.FILTER_SHOW_ALL)
                .verifyCheckBoxesRendering(acc1, acc2, acc3, acc4, acc5, acc6, acc7, acc8, acc9)
                .printStateBriefly()
                .scenario("Filter accounts and then update")
                .selectFilter(commentaryGroup3)
                .verifyCheckBoxesRendering(acc1, acc2, acc3, acc7, acc8)
                .updatePane()
                .verifyCheckBoxesRendering(acc1, acc2, acc3, acc4, acc5, acc6, acc7, acc8, acc9)
                .printStateBriefly();
    }

    @Test
    @DisplayName("Nine total accounts; nine accounts in use; some accounts with commentary; filtering accounts by commentary")
    void testOrdering() {
        ///setLoggerLevel(Level.TRACE);
        final int accountsLimit = 3;
        final String commentaryGroup1 = "group1";
        final String commentaryGroup2 = "group2";
        final String commentaryGroup3 = "group3";
        final Account account1 = createAccount("John");
        final Account account2 = createAccountWithCommentary("Bob", commentaryGroup1);
        final Account account3 = createAccountWithCommentary("Robert", commentaryGroup2);
        final Account account4 = createAccount("David");
        final Account account5 = createAccountWithCommentary("Anthony", commentaryGroup2);
        final Account account6 = createAccountWithCommentary("Paul", commentaryGroup1);
        final Account account7 = createAccountWithCommentary("Kevin", commentaryGroup1 + "; " + commentaryGroup3);
        final Account account8 = createAccountWithCommentary("1_Robert", commentaryGroup3 + ";" + commentaryGroup1);
        final Account account9 = createAccountWithCommentary("2_Bob", commentaryGroup1 + "; " + commentaryGroup2);
        final List<String> orderedAccounts = orderAccounts(account4, account2, account8, account3, account1, account6, account7, account9, account5);
        new AccountPaneInteraction()
                .accountsLimit(accountsLimit)
                .totalAccounts(account4, account2, account8, account3, account1, account6, account7, account9, account5)
                .accountsInUse(account3, account1, account4, account2, account9, account6, account7, account5, account8)
                .bindAccounts(account1.name(), account2.name(), account3.name())
                .updatePane()
                .verifyCheckBoxesOrdering(orderedAccounts)
                .printStateBriefly();
    }

    private List<String> orderAccounts(final Account... accounts) {
        return Arrays.stream(accounts)
                .sorted(Comparator.comparing(account -> account.name() + account.commentary().map(s -> " (" + s + ")").orElse("")))
                .map(Account::name)
                .toList();
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

        public AccountPaneInteraction totalAccounts(final Account... accounts) {
            interaction.setTotalAccounts(accounts);
            return this;
        }

        public AccountPaneInteraction accountsInUse(final String... accounts) {
            interaction.setAccountsInUse(accounts);
            return this;
        }

        public AccountPaneInteraction accountsInUse(final Account... accounts) {
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
                    .filter(checkBox -> account.equals(checkBox.getClientProperty(AccountsPane.ACCOUNT_ID_PROPERTY)))
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

        /* info */

        public AccountPaneInteraction scenario(final String scenarioName) {
            logger.info("Scenario: {}", scenarioName);
            return this;
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
            interaction.printStateBriefly("selectedCheckBoxes: " + selectedCheckBoxes
                    + "\n\tfilter combo box selected value: " + getFilterComboBoxCurrentValue()
                    + "\n\tcheckboxes state: " + stringifyCheckBoxesState());
            return this;
        }

        private String stringifyCheckBoxesState() {
            final StringBuilder sb = new StringBuilder();
            getRenderedCheckBoxes().forEach(checkBox -> sb.append("\n\t\t").append(checkBox.getText())
                    .append("\t(visible=").append(checkBox.isVisible())
                    .append(", enabled=").append(checkBox.isEnabled())
                    .append(", selected=").append(checkBox.isSelected())
                    .append(")"));
            return sb.toString();
        }

        /* obtaining */

        private JComboBox<?> getFilterComboBox() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(accountsPane, "filter combo box", JComboBox.class);
        }

        private List<JCheckBox> getRenderedCheckBoxes() {
            return SwingTestUtil.INSTANCE.getChildrenOfType(accountsPane, JCheckBox.class);
        }

        private String getFilterComboBoxCurrentValue() {
            return (String) getFilterComboBox().getSelectedItem();
        }

        /* actions */

        public AccountPaneInteraction selectFilter(final String value) {
            getFilterComboBox().setSelectedItem(value);
            return this;
        }

        /* verifying */

        private void checkInternalState() {
            if (interaction.getAccountsLimit() == -1)
                throw new IllegalStateException("You must set accounts limit before");
        }

        private void verifyCheckboxesInContainer(final List<JCheckBox> checkBoxes) {
            final Collection<String> accountsInUse = accountStorage.accountsInUse();
            final int totalCountShouldBe = accountsInUse.size();
            assertEquals(totalCountShouldBe, checkBoxes.size(), "Should have " + totalCountShouldBe + " checkboxes when there are " + totalCountShouldBe + " accounts in use");
            checkBoxes.forEach(checkBox -> {
                final String accountId = (String) checkBox.getClientProperty(AccountsPane.ACCOUNT_ID_PROPERTY);
                assertTrue(accountsInUse.contains(accountId), "Account in-use storage should contain account \"" + accountId + "\" but is hasn't");
                final Account accountInStorage = accountStorage.get(accountId);
                assertNotNull(accountInStorage, "Account storage should contain account \"" + accountId + "\" but is hasn't");
                final String checkBoxText = checkBox.getText();
                assertEquals(getAccountTextForCheckBox(accountInStorage), checkBoxText, "Checkbox text should correspond the account name with comment");
                if (selectedCheckBoxes.contains(accountId)) {
                    assertTrue(checkBox.isSelected(), "Checkbox \"" + accountId + "\" should be selected");
                } else {
                    assertFalse(checkBox.isSelected(), "Checkbox \"" + accountId + "\" shouldn't be selected");
                }
            });
        }

        private String getAccountTextForCheckBox(final Account account) {
            final Optional<String> maybeCommentary = account.commentary();
            if (maybeCommentary.isEmpty()) return account.name();
            final String commentary = maybeCommentary.get();
            return account.name() + " (" + commentary + ")";
        }

        private void verifyCheckboxesDisablingWhenLimitReached(final List<JCheckBox> checkBoxes) {
            final int accountsLimit = interaction.getAccountsLimit();
            if (selectedCheckBoxes.size() < accountsLimit)
                checkBoxes.forEach(checkBox -> assertTrue(checkBox.isEnabled(), "Checkbox should be enabled because bound accounts limit (" + accountsLimit + ") do not reached"));
            else
                checkBoxes.forEach(checkBox -> {
                    final String accountId = (String) checkBox.getClientProperty(AccountsPane.ACCOUNT_ID_PROPERTY);
                    if (selectedCheckBoxes.contains(accountId)) {
                        assertTrue(checkBox.isEnabled(), "Checkbox \"" + accountId + "\" should be enabled when checked");
                    } else {
                        assertFalse(checkBox.isEnabled(), "Checkbox \"" + accountId + "\" shouldn't be enabled because bound accounts limit reached");
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
            assertEquals(shouldBeSelectedAccountsCount, obtainedSelectedAccounts.length, "Accounts pane should return " + shouldBeSelectedAccountsCount + " selected accounts");
            Arrays.stream(obtainedSelectedAccounts).forEach(account -> assertNotNull(account, "Accounts pane should not return \"null\" account but returns " + Arrays.toString(obtainedSelectedAccounts)));
            Arrays.stream(obtainedSelectedAccounts).map(Account::name).forEach(s -> assertTrue(selectedCheckBoxes.contains(s), "Accounts pane should return correspond account"));
        }

        public AccountPaneInteraction verifyCheckBoxesRendering(final String... accounts) {
            final Set<String> set = Arrays.stream(accounts).collect(Collectors.toSet());
            final Set<Object> renderedAccounts = getRenderedCheckBoxes().stream()
                    .map(checkBox -> checkBox.getClientProperty(AccountsPane.ACCOUNT_ID_PROPERTY))
                    .collect(Collectors.toSet());
            accountStorage.accountsInUse().forEach(accountId -> {
                if (set.contains(accountId))
                    assertTrue(renderedAccounts.contains(accountId), "Checkbox \"" + accountId + "\" should be rendered");
                else
                    assertFalse(renderedAccounts.contains(accountId), "Checkbox \"" + accountId + "\" shouldn't be rendered");
            });
            return this;
        }

        public AccountPaneInteraction verifyFilterComboBoxHasValue(final String value) {
            assertEquals(value, getFilterComboBox().getSelectedItem());
            return this;
        }

        public AccountPaneInteraction verifyCheckBoxesOrdering(final List<String> orderedAccounts) {
            final List<String> checkboxes = getRenderedCheckBoxes().stream().map(checkBox -> checkBox.getClientProperty(AccountsPane.ACCOUNT_ID_PROPERTY)).map(String.class::cast).toList();
            assertEquals(orderedAccounts, checkboxes);
            return this;
        }
    }
}