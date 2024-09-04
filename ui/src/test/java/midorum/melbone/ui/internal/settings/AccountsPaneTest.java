package midorum.melbone.ui.internal.settings;

import dma.validation.Validator;
import midorum.melbone.model.dto.Account;
import midorum.melbone.ui.context.MockedContext;
import midorum.melbone.ui.util.SwingTestUtil;
import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

class AccountsPaneTest extends MockedContext {

    @Test
    @DisplayName("No accounts anymore; no any account editing and creating")
    void noAccountsAnymore() {
        new AccountsPaneInteraction()
                .scenario("Empty edit and create account forms shouldn't work")
                .verifyEditAccountPaneIsVisible()
                .clickSaveAccountButton()
                .verifyTotalAccountsInStorageIs(0)
                .clickDeleteAccountButtonWithConfirm()
                .verifyTotalAccountsInStorageIs(0)
                .clickAddButton()
                .verifyAddAccountPaneIsVisible()
                .verifyAddAccountPaneIsEmpty()
                .clickSaveNewAccountButton()
                .verifyTotalAccountsInStorageIs(0)
                .clickBackButton()
                .verifyEditAccountPaneIsVisible();
    }

    @Test
    @DisplayName("Creating, editing and deleting new account")
    void playingWithOneAccount() {
        final String acc1Name = "acc1";
        final String acc1Login = acc1Name + "_login";
        final String acc1Password = acc1Name + "_password";
        final String acc1Commentary = acc1Name + "_commentary";
        final String acc1LoginEdited = acc1Login + "_edited";
        final String acc1PasswordEdited = acc1Password + "_edited";
        final String acc1CommentaryEdited = acc1Commentary + "_edited";
        setLoggerLevel(Level.TRACE);
        final AccountsPaneInteraction interaction = new AccountsPaneInteraction();
        interaction.scenario("Switch to creating new account")
                .verifyEditAccountPaneIsVisible()
                .clickAddButton()
                .verifyAddAccountPaneIsVisible()
                .verifyAddAccountPaneIsEmpty();
        interaction.scenario("Enter new account data and save it")
                .enterNewAccountName(acc1Name)
                .clickSaveNewAccountButton()
                .verifyTotalAccountsInStorageIs(0)
                .enterNewAccountLogin(acc1Login)
                .clickSaveNewAccountButton()
                .verifyTotalAccountsInStorageIs(0)
                .enterNewAccountPassword(acc1Password)
                .verifyTotalAccountsInStorageIs(0)
                .enterNewAccountCommentary(acc1Commentary)
                .clickSaveNewAccountButton()
                .verifyTotalAccountsInStorageIs(1)
                .verifyAccountStorageContains(createAccount(acc1Name, acc1Login, acc1Password, acc1Commentary))
                .verifyTotalAccountsInUseIs(1)
                .verifyInUseStorageContains(acc1Name)
                .verifyEditAccountPaneIsVisible();
        interaction.scenario("Try enter same data again and save it")
                .verifyEditAccountPaneIsVisible()
                .clickAddButton()
                .verifyAddAccountPaneIsVisible()
                .verifyAddAccountPaneIsEmpty()
                .enterNewAccountName(acc1Name)
                .enterNewAccountLogin("another_login")
                .enterNewAccountPassword("another_password")
                .clickSaveNewAccountButton()
                .verifyTotalAccountsInStorageIs(1)
                .verifyAccountStorageContains(createAccount(acc1Name, acc1Login, acc1Password, acc1Commentary))
                .verifyTotalAccountsInUseIs(1)
                .verifyInUseStorageContains(acc1Name)
                .verifyAddAccountPaneIsVisible()
                .clickBackButton()
                .verifyEditAccountPaneIsVisible();
        interaction.scenario("Select account on edit form and validate it")
                .selectAccountInComboBox(acc1Name)
                .verifyAccountNameInTextFieldIs(acc1Name)
                .verifyAccountNameTextFieldIsNotBeEditable()
                .verifyAccountLoginInTextFieldIs(acc1Login)
                .verifyAccountPasswordInTextFieldIs(acc1Password)
                .verifyAccountCommentaryInTextFieldIs(acc1Commentary)
                .verifyInUseCheckBoxIsSelected();
        interaction.scenario("Change account data and save it")
                .editAccountLoginTo(acc1LoginEdited)
                .verifyAccountLoginInTextFieldIs(acc1LoginEdited)
                .editAccountPasswordTo(acc1PasswordEdited)
                .verifyAccountPasswordInTextFieldIs(acc1PasswordEdited)
                .editAccountCommentaryTo(acc1CommentaryEdited)
                .verifyAccountCommentaryInTextFieldIs(acc1CommentaryEdited)
                .unselectInUseCheckBox()
                .clickSaveAccountButton()
                .verifyTotalAccountsInStorageIs(1)
                .verifyTotalAccountsInUseIs(0);
        interaction.scenario("Select account on edit form again and validate it")
                .selectAccountInComboBox(acc1Name)
                .verifyAccountNameInTextFieldIs(acc1Name)
                .verifyAccountNameTextFieldIsNotBeEditable()
                .verifyAccountLoginInTextFieldIs(acc1LoginEdited)
                .verifyAccountPasswordInTextFieldIs(acc1PasswordEdited)
                .verifyAccountCommentaryInTextFieldIs(acc1CommentaryEdited)
                .verifyInUseCheckBoxIsNotSelected();
        interaction.scenario("Deleting account with decline")
                .verifyEditAccountPaneIsVisible()
                .selectAccountInComboBox(acc1Name)
                .clickDeleteAccountButtonWithDecline()
                .verifyTotalAccountsInStorageIs(1)
                .verifyTotalAccountsInUseIs(0)
                .verifyAccountNameInTextFieldIs(acc1Name)
                .verifyAccountLoginInTextFieldIs(acc1LoginEdited)
                .verifyAccountPasswordInTextFieldIs(acc1PasswordEdited)
                .verifyAccountCommentaryInTextFieldIs(acc1CommentaryEdited);
        interaction.scenario("Deleting account with confirm")
                .verifyEditAccountPaneIsVisible()
                .selectAccountInComboBox(acc1Name)
                .clickDeleteAccountButtonWithConfirm()
                .verifyTotalAccountsInStorageIs(0)
                .verifyTotalAccountsInUseIs(0)
                .verifyAccountNameTextFieldIsEmpty()
                .verifyAccountLoginTextFieldIsEmpty()
                .verifyAccountPasswordTextFieldIsEmpty()
                .verifyAccountCommentaryTextFieldIsEmpty()
                .printStateBriefly();
    }

    @Test
    @DisplayName("Creating and deleting two accounts")
    void playingWithTwoAccounts() {
        final String acc1Name = "acc1";
        final String acc1Login = acc1Name + "_login";
        final String acc1Password = acc1Name + "_password";
        final String acc2Name = "acc2";
        final String acc2Login = acc2Name + "_login";
        final String acc2Password = acc2Name + "_password";
        final String acc2Commentary = acc2Name + "_commentary";
        final String acc3Name = "acc3";
        final String acc3Login = acc3Name + "_login";
        final String acc3Password = acc3Name + "_password";
        setLoggerLevel(Level.TRACE);
        final AccountsPaneInteraction interaction = new AccountsPaneInteraction();
        interaction.scenario("Enter new account data and save it")
                .verifyEditAccountPaneIsVisible()
                .clickAddButton()
                .verifyAddAccountPaneIsVisible()
                .verifyAddAccountPaneIsEmpty()
                .enterNewAccountName(acc1Name)
                .enterNewAccountLogin(acc1Login)
                .enterNewAccountPassword(acc1Password)
                .verifyAddAccountCommentaryTextFieldIsEmpty()
                .clickSaveNewAccountButton()
                .verifyTotalAccountsInStorageIs(1)
                .verifyAccountStorageContains(createAccount(acc1Name, acc1Login, acc1Password))
                .verifyTotalAccountsInUseIs(1)
                .verifyInUseStorageContains(acc1Name)
                .verifyEditAccountPaneIsVisible();
        interaction.scenario("Enter second account data and save it")
                .verifyEditAccountPaneIsVisible()
                .clickAddButton()
                .verifyAddAccountPaneIsVisible()
                .verifyAddAccountPaneIsEmpty()
                .enterNewAccountName(acc2Name)
                .enterNewAccountLogin(acc2Login)
                .enterNewAccountPassword(acc2Password)
                .enterNewAccountCommentary(acc2Commentary)
                .clickSaveNewAccountButton()
                .verifyTotalAccountsInStorageIs(2)
                .verifyAccountStorageContains(createAccount(acc2Name, acc2Login, acc2Password, acc2Commentary))
                .verifyTotalAccountsInUseIs(2)
                .verifyInUseStorageContains(acc2Name)
                .verifyEditAccountPaneIsVisible();
        interaction.scenario("Enter third account data and save it")
                .verifyEditAccountPaneIsVisible()
                .clickAddButton()
                .verifyAddAccountPaneIsVisible()
                .verifyAddAccountPaneIsEmpty()
                .enterNewAccountName(acc3Name)
                .enterNewAccountLogin(acc3Login)
                .enterNewAccountPassword(acc3Password)
                .clickSaveNewAccountButton()
                .verifyTotalAccountsInStorageIs(3)
                .verifyAccountStorageContains(createAccount(acc3Name, acc3Login, acc3Password))
                .verifyTotalAccountsInUseIs(3)
                .verifyInUseStorageContains(acc3Name)
                .verifyEditAccountPaneIsVisible();
        interaction.scenario("Select first account on edit form and validate it")
                .selectAccountInComboBox(acc1Name)
                .verifyAccountNameInTextFieldIs(acc1Name)
                .verifyAccountNameTextFieldIsNotBeEditable()
                .verifyAccountLoginInTextFieldIs(acc1Login)
                .verifyAccountPasswordInTextFieldIs(acc1Password)
                .verifyAccountCommentaryTextFieldIsEmpty()
                .verifyInUseCheckBoxIsSelected();
        interaction.scenario("Select second account on edit form and validate it")
                .selectAccountInComboBox(acc2Name)
                .verifyAccountNameInTextFieldIs(acc2Name)
                .verifyAccountNameTextFieldIsNotBeEditable()
                .verifyAccountLoginInTextFieldIs(acc2Login)
                .verifyAccountPasswordInTextFieldIs(acc2Password)
                .verifyAccountCommentaryInTextFieldIs(acc2Commentary)
                .verifyInUseCheckBoxIsSelected();
        interaction.scenario("Select third account on edit form and validate it")
                .selectAccountInComboBox(acc3Name)
                .verifyAccountNameInTextFieldIs(acc3Name)
                .verifyAccountNameTextFieldIsNotBeEditable()
                .verifyAccountLoginInTextFieldIs(acc3Login)
                .verifyAccountPasswordInTextFieldIs(acc3Password)
                .verifyAccountCommentaryTextFieldIsEmpty()
                .verifyInUseCheckBoxIsSelected();
        interaction.scenario("Select first account on edit form and delete it")
                .selectAccountInComboBox(acc1Name)
                .clickDeleteAccountButtonWithConfirm()
                .verifyTotalAccountsInStorageIs(2)
                .verifyAccountStorageContains(createAccount(acc2Name, acc2Login, acc2Password, acc2Commentary))
                .verifyAccountStorageContains(createAccount(acc3Name, acc3Login, acc3Password))
                .verifyTotalAccountsInUseIs(2)
                .verifyInUseStorageContains(acc2Name);
    }

    private class AccountsPaneInteraction {
        private final Interaction interaction;
        private final AccountsPane accountsPane;
        private String enteredAccountName;
        private String enteredAccountLogin;
        private String enteredAccountPassword;
        private String enteredAccountCommentary;

        private AccountsPaneInteraction() {
            interaction = new Interaction();
            accountsPane = new AccountsPane(context);
        }

        /* info */

        public AccountsPaneInteraction scenario(final String scenarioName) {
            logger.info("Scenario: {}", scenarioName);
            return this;
        }

        public AccountsPaneInteraction printState() {
            interaction.printState(getAdditionalInfo());
            return this;
        }

        public AccountsPaneInteraction printStateBriefly() {
            interaction.printStateBriefly(getAdditionalInfo());
            return this;
        }

        private String getAdditionalInfo() {
            return String.format("""
                            enteredAccountName: %s
                            \tenteredAccountLogin: %s
                            \tenteredAccountPassword: %s
                            \tenteredAccountCommentary: %s
                            """,
                    enteredAccountName,
                    enteredAccountLogin,
                    enteredAccountPassword,
                    enteredAccountCommentary);
        }

        /* obtaining components */

        @SuppressWarnings("unchecked")
        private JComboBox<String> getSelectAccountComboBox() {
            return (JComboBox<String>) SwingTestUtil.INSTANCE.getChildNamedOrThrow(accountsPane, "select account combo box", JComboBox.class);
        }

        private JTextField getNameTextField() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(accountsPane, "account name field", JTextField.class);
        }

        private JTextField getAddNameTextField() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(accountsPane, "add account name field", JTextField.class);
        }

        private JTextField getEditLoginTextField() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(accountsPane, "edit account login field", JTextField.class);
        }

        private JTextField getAddLoginTextField() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(accountsPane, "add account login field", JTextField.class);
        }

        private JTextField getEditPasswordTextField() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(accountsPane, "edit account password field", JTextField.class);
        }

        private JTextField getAddPasswordTextField() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(accountsPane, "add account password field", JTextField.class);
        }

        private JTextField getEditCommentaryTextField() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(accountsPane, "edit account commentary field", JTextField.class);
        }

        private JTextField getAddCommentaryTextField() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(accountsPane, "add account commentary field", JTextField.class);
        }

        private JCheckBox getInUseCheckBox() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(accountsPane, "in use check box", JCheckBox.class);
        }

        private JPanel getAccountCreatePane() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(accountsPane, "account create pane", JPanel.class);
        }

        private JPanel getAccountEditPane() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(accountsPane, "account edit pane", JPanel.class);
        }

        private JButton getAddAccountButton() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(accountsPane, "add account button", JButton.class);
        }

        private JButton getBackButton() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(accountsPane, "back button", JButton.class);
        }

        private JButton getSaveAccountButton() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(accountsPane, "save account button", JButton.class);
        }

        private JButton getSaveNewAccountButton() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(accountsPane, "save new account button", JButton.class);
        }

        private JButton getDeleteAccountButton() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(accountsPane, "delete account button", JButton.class);
        }

        /* actions */

        public AccountsPaneInteraction clickSaveAccountButton() {
            getSaveAccountButton().doClick();
            return this;
        }

        public AccountsPaneInteraction clickDeleteAccountButtonWithConfirm() {
            interaction.whenTryAskYesNoQuestionThenChooseYes();
            getDeleteAccountButton().doClick();
            return this;
        }

        public AccountsPaneInteraction clickDeleteAccountButtonWithDecline() {
            interaction.whenTryAskYesNoQuestionThenChooseNo();
            getDeleteAccountButton().doClick();
            return this;
        }

        public AccountsPaneInteraction clickAddButton() {
            getAddAccountButton().doClick();
            return this;
        }

        public AccountsPaneInteraction clickSaveNewAccountButton() {
            getSaveNewAccountButton().doClick();
            return this;
        }

        public AccountsPaneInteraction clickBackButton() {
            getBackButton().doClick();
            return this;
        }

        public AccountsPaneInteraction enterNewAccountName(final String accountName) {
            getAddNameTextField().setText(accountName);
            enteredAccountName = accountName;
            return this;
        }

        public AccountsPaneInteraction enterNewAccountLogin(final String accountLogin) {
            getAddLoginTextField().setText(accountLogin);
            enteredAccountLogin = accountLogin;
            return this;
        }

        public AccountsPaneInteraction enterNewAccountPassword(final String accountPassword) {
            getAddPasswordTextField().setText(accountPassword);
            enteredAccountPassword = accountPassword;
            return this;
        }

        public AccountsPaneInteraction enterNewAccountCommentary(final String commentary) {
            getAddCommentaryTextField().setText(commentary);
            enteredAccountCommentary = commentary;
            return this;
        }

        public AccountsPaneInteraction editAccountLoginTo(final String accountLogin) {
            getEditLoginTextField().setText(accountLogin);
            return this;
        }

        public AccountsPaneInteraction editAccountPasswordTo(final String accountPassword) {
            getEditPasswordTextField().setText(accountPassword);
            return this;
        }

        public AccountsPaneInteraction editAccountCommentaryTo(final String commentary) {
            getEditCommentaryTextField().setText(commentary);
            return this;
        }

        public AccountsPaneInteraction selectAccountInComboBox(final String accountName) {
            getSelectAccountComboBox().setSelectedItem(accountName);
            return this;
        }

        public AccountsPaneInteraction unselectInUseCheckBox() {
            getInUseCheckBox().setSelected(false);
            return this;
        }

        public AccountsPaneInteraction selectInUseCheckBox() {
            getInUseCheckBox().setSelected(true);
            return this;
        }

        /* verifying */

        public AccountsPaneInteraction verifyEditAccountPaneIsVisible() {
            assertTrue(getAccountEditPane().isVisible());
            assertFalse(getAccountCreatePane().isVisible());
            return this;
        }

        public AccountsPaneInteraction verifyAddAccountPaneIsVisible() {
            assertTrue(getAccountCreatePane().isVisible());
            assertFalse(getAccountEditPane().isVisible());
            return this;
        }

        public AccountsPaneInteraction verifyTotalAccountsInStorageIs(final int count) {
            Assertions.assertEquals(count, accountStorage.accounts().size());
            return this;
        }

        public AccountsPaneInteraction verifyTotalAccountsInUseIs(final int count) {
            Assertions.assertEquals(count, accountStorage.accountsInUse().size());
            return this;
        }

        public AccountsPaneInteraction verifyAccountNameInTextFieldIs(final String accountName) {
            assertEquals(accountName, getNameTextField().getText());
            return this;
        }

        public AccountsPaneInteraction verifyAccountLoginInTextFieldIs(final String accountLogin) {
            assertEquals(accountLogin, getEditLoginTextField().getText());
            return this;
        }

        public AccountsPaneInteraction verifyAccountPasswordInTextFieldIs(final String accountPassword) {
            assertEquals(accountPassword, getEditPasswordTextField().getText());
            return this;
        }

        public AccountsPaneInteraction verifyAccountCommentaryInTextFieldIs(final String commentary) {
            assertEquals(commentary, getEditCommentaryTextField().getText());
            return this;
        }

        public AccountsPaneInteraction verifyInUseCheckBoxIsSelected() {
            assertTrue(getInUseCheckBox().isSelected());
            return this;
        }

        public AccountsPaneInteraction verifyInUseCheckBoxIsNotSelected() {
            assertFalse(getInUseCheckBox().isSelected());
            return this;
        }

        public AccountsPaneInteraction verifyAccountNameTextFieldIsNotBeEditable() {
            assertFalse(getNameTextField().isEditable());
            return this;
        }

        public AccountsPaneInteraction verifyAccountStorageContains(final Account account) {
            Assertions.assertTrue(accountStorage.accounts().contains(account), "Account storage should contain account \"" + account + "\" but it hasn't");
            return this;
        }

        public AccountsPaneInteraction verifyInUseStorageContains(final String accountName) {
            Assertions.assertTrue(accountStorage.isInUse(accountName));
            return this;
        }

        public AccountsPaneInteraction verifyAccountNameTextFieldIsEmpty() {
            final String value = getNameTextField().getText();
            assertFalse(Validator.checkNotNull(value).andCheckNot(String::isBlank).isValid(), "Account edit name text field should be empty but it has value \"" + value + "\"");
            return this;
        }

        public AccountsPaneInteraction verifyAccountLoginTextFieldIsEmpty() {
            final String value = getEditLoginTextField().getText();
            assertFalse(Validator.checkNotNull(value).andCheckNot(String::isBlank).isValid(), "Account edit login text field should be empty but it has value \"" + value + "\"");
            return this;
        }

        public AccountsPaneInteraction verifyAccountPasswordTextFieldIsEmpty() {
            final String value = getEditPasswordTextField().getText();
            assertFalse(Validator.checkNotNull(value).andCheckNot(String::isBlank).isValid(), "Account edit password text field should be empty but it has value \"" + value + "\"");
            return this;
        }

        public AccountsPaneInteraction verifyAccountCommentaryTextFieldIsEmpty() {
            final String value = getEditCommentaryTextField().getText();
            assertFalse(Validator.checkNotNull(value).andCheckNot(String::isBlank).isValid(), "Account edit commentary text field should be empty but it has value \"" + value + "\"");
            return this;
        }

        private AccountsPaneInteraction verifyAddAccountNameTextFieldIsEmpty() {
            final String value = getAddNameTextField().getText();
            assertFalse(Validator.checkNotNull(value).andCheckNot(String::isBlank).isValid(), "Account add name text field should be empty but it has value \"" + value + "\"");
            return this;
        }

        private AccountsPaneInteraction verifyAddAccountLoginTextFieldIsEmpty() {
            final String value = getAddLoginTextField().getText();
            assertFalse(Validator.checkNotNull(value).andCheckNot(String::isBlank).isValid(), "Account add login text field should be empty but it has value \"" + value + "\"");
            return this;
        }

        private AccountsPaneInteraction verifyAddAccountPasswordTextFieldIsEmpty() {
            final String value = getAddPasswordTextField().getText();
            assertFalse(Validator.checkNotNull(value).andCheckNot(String::isBlank).isValid(), "Account add password text field should be empty but it has value \"" + value + "\"");
            return this;
        }

        public AccountsPaneInteraction verifyAddAccountCommentaryTextFieldIsEmpty() {
            final String value = getAddCommentaryTextField().getText();
            assertFalse(Validator.checkNotNull(value).andCheckNot(String::isBlank).isValid(), "Account add commentary text field should be empty but it has value \"" + value + "\"");
            return this;
        }

        public AccountsPaneInteraction verifyAddAccountPaneIsEmpty() {
            verifyAddAccountNameTextFieldIsEmpty();
            verifyAddAccountLoginTextFieldIsEmpty();
            verifyAddAccountPasswordTextFieldIsEmpty();
            verifyAddAccountCommentaryTextFieldIsEmpty();
            return this;
        }
    }

}