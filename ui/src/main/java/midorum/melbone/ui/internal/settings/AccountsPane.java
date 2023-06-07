package midorum.melbone.ui.internal.settings;

import dma.validation.Validator;
import midorum.melbone.ui.internal.Context;
import midorum.melbone.ui.internal.common.NoticePane;
import midorum.melbone.model.dto.Account;
import midorum.melbone.model.persistence.AccountStorage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AccountsPane extends JPanel {

    public static final String CHARACTER_CREATE_PANE = "characterCreatePane";
    public static final String CHARACTER_EDIT_PANE = "characterEditPane";

    private final Context context;
    private final AccountStorage accountStorage;
    private final DefaultComboBoxModel<String> comboBoxModel;
    private final JComboBox<String> selectAccountComboBox;
    private final JTextField nameTextField;
    private final JTextField editLoginTextField;
    private final JTextField editPasswordTextField;
    private final JTextField editCommentaryTextField;
    private final JCheckBox inUseCheckBox;
    private final JTextField addNameTextField;
    private final JTextField addLoginTextField;
    private final JTextField addPasswordTextField;
    private final JTextField addCommentaryTextField;
    private final JButton addButton;
    private final JButton backButton;
    private final JButton saveNewButton;
    private final JButton editSaveButton;
    private final JButton deleteButton;
    private final JPanel cardsPane;
    private final CardLayout cardLayout;
    private final NoticePane noticePane;

    public AccountsPane(final Context context) {
        super(false);
        this.context = context;
        this.accountStorage = context.accountStorage();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(500, 300));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        this.comboBoxModel = new DefaultComboBoxModel<>();
        this.selectAccountComboBox = createComboBox();
        this.nameTextField = createNameTextField();
        this.editLoginTextField = createEditLoginTextField();
        this.editPasswordTextField = createEditPasswordTextField();
        this.editCommentaryTextField = createEditCommentaryTextField();
        this.inUseCheckBox = createInUseCheckBox();
        this.addNameTextField = createAddNameTextField();
        this.addLoginTextField = createAddLoginTextField();
        this.addPasswordTextField = createAddPasswordTextField();
        this.addCommentaryTextField = createAddCommentaryTextField();
        this.cardLayout = new CardLayout();
        this.cardsPane = createCardPane();
        this.noticePane = new NoticePane(context.logger());
        this.addButton = createAddButton();
        this.backButton = createBackButton();
        this.editSaveButton = createSaveButton();
        this.saveNewButton = createSaveNewButton();
        this.deleteButton = createDeleteButton();

        add(cardsPane);
        add(Box.createVerticalStrut(5));
        add(createBottomPane());

        switchToEditCard();
    }

    private JPanel createCardPane() {
        final JPanel cardsPane = new JPanel(cardLayout);
        cardsPane.add(CHARACTER_EDIT_PANE, createAccountEditPane());
        cardsPane.add(CHARACTER_CREATE_PANE, createAccountCreatePane());
        return cardsPane;
    }

    private JPanel createAccountCreatePane() {
        JPanel accountCreatePane = new JPanel();
        accountCreatePane.setName("account create pane");
        accountCreatePane.setLayout(new BoxLayout(accountCreatePane, BoxLayout.PAGE_AXIS));
        accountCreatePane.setAlignmentX(Component.RIGHT_ALIGNMENT);
        accountCreatePane.add(new JLabel("Name:"));
        accountCreatePane.add(addNameTextField);
        accountCreatePane.add(new JLabel("Login:"));
        accountCreatePane.add(addLoginTextField);
        accountCreatePane.add(new JLabel("Password:"));
        accountCreatePane.add(addPasswordTextField);
        accountCreatePane.add(new JLabel("Commentary:"));
        accountCreatePane.add(addCommentaryTextField);
        return accountCreatePane;
    }

    private JPanel createAccountEditPane() {
        JPanel accountEditPane = new JPanel();
        accountEditPane.setName("account edit pane");
        accountEditPane.setLayout(new BoxLayout(accountEditPane, BoxLayout.PAGE_AXIS));
        accountEditPane.setAlignmentX(Component.RIGHT_ALIGNMENT);
        accountEditPane.add(selectAccountComboBox);
        accountEditPane.add(new JLabel("Name:"));
        accountEditPane.add(nameTextField);
        accountEditPane.add(new JLabel("Login:"));
        accountEditPane.add(editLoginTextField);
        accountEditPane.add(new JLabel("Password:"));
        accountEditPane.add(editPasswordTextField);
        accountEditPane.add(new JLabel("Commentary (you can enter several splitting by \";\"):"));
        accountEditPane.add(editCommentaryTextField);
        accountEditPane.add(inUseCheckBox);
        return accountEditPane;
    }

    private JPanel createBottomPane() {
        JPanel bottomPane = new JPanel();
        bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.LINE_AXIS));
        bottomPane.add(noticePane);
        bottomPane.add(addButton);
        bottomPane.add(backButton);
        bottomPane.add(Box.createHorizontalStrut(5));
        bottomPane.add(editSaveButton);
        bottomPane.add(saveNewButton);
        bottomPane.add(Box.createHorizontalStrut(5));
        bottomPane.add(deleteButton);
        return bottomPane;
    }

    private JComboBox<String> createComboBox() {
        JComboBox<String> comboBox = new JComboBox<>(comboBoxModel);
        comboBox.setName("select account combo box");
        comboBox.setSelectedIndex(-1);
        comboBox.addActionListener(e -> Validator.checkNotNull(((JComboBox<?>) e.getSource()).getSelectedItem())
                .thanDo(o -> displayAccountInfo((String) o))
                .elseDoNothing());
        return comboBox;
    }

    private JTextField createNameTextField() {
        final JTextField field = new JTextField();
        field.setName("account name field");
        field.setEditable(false);
        return field;
    }

    private JTextField createAddNameTextField() {
        final JTextField field = new JTextField();
        field.setName("add account name field");
        return field;
    }

    private JTextField createEditLoginTextField() {
        final JTextField field = new JTextField();
        field.setName("edit account login field");
        return field;//TODO make field editable by demand
    }

    private JTextField createAddLoginTextField() {
        final JTextField field = new JTextField();
        field.setName("add account login field");
        return field;
    }

    private JTextField createEditPasswordTextField() {
        final JTextField field = new JTextField();
        field.setName("edit account password field");
        return field;//TODO make field editable by demand
    }

    private JTextField createAddPasswordTextField() {
        final JTextField field = new JTextField();
        field.setName("add account password field");
        return field;
    }

    private JTextField createEditCommentaryTextField() {
        final JTextField field = new JTextField();
        field.setName("edit account commentary field");
        return field;//TODO make field editable by demand
    }

    private JTextField createAddCommentaryTextField() {
        final JTextField field = new JTextField();
        field.setName("add account commentary field");
        return field;
    }

    private JCheckBox createInUseCheckBox() {
        JCheckBox checkBox = new JCheckBox("using on this machine");
        checkBox.setName("in use check box");
        checkBox.addActionListener(e -> {
            try {
                Validator.checkNotNull(nameTextField.getText())
                        .andMap(String::trim)
                        .andCheckNot(String::isBlank)
                        .thanDo(s -> {
                            saveInUseState(s, checkBox.isSelected());
                            noticePane.showSuccess("Saved successfully");
                        }).elseDoNothing();
            } catch (Throwable t) {
                noticePane.showError(t);
            }
        });
        return checkBox;
    }

    private JButton createAddButton() {
        JButton button = new JButton("Add");
        button.setName("add account button");
        button.addActionListener(e -> switchToAddCard());
        return button;
    }

    private JButton createBackButton() {
        JButton button = new JButton("Back");
        button.setName("back button");
        button.addActionListener(e -> switchToEditCard());
        return button;
    }

    private JButton createSaveButton() {
        JButton button = new JButton("Save");
        button.setName("save account button");
        button.addActionListener(e -> {
            try {
                final Account account = Account.builder()
                        .name(nameTextField.getText())
                        .login(editLoginTextField.getText())
                        .password(editPasswordTextField.getText())
                        .commentary(editCommentaryTextField.getText())
                        .build();
                if (!accountStorage.isExists(account.name()))
                    throw new IllegalStateException("Account name does not exists");
                accountStorage.store(account);
                saveInUseState(account.name(), inUseCheckBox.isSelected());
                noticePane.showSuccess("Account saved successfully");
            } catch (Throwable t) {
                noticePane.showError(t);
            }
        });
        return button;
    }

    private JButton createSaveNewButton() {
        JButton button = new JButton("Save");
        button.setName("save new account button");
        button.addActionListener(e -> {
            try {
                final Account account = Account.builder()
                        .name(addNameTextField.getText())
                        .login(addLoginTextField.getText())
                        .password(addPasswordTextField.getText())
                        .commentary(addCommentaryTextField.getText())
                        .build();
                if (accountStorage.isExists(account.name()))
                    throw new IllegalStateException("Account name already exists");
                accountStorage.store(account);
                saveInUseState(account.name(), true);
                noticePane.showSuccess("Account saved successfully");
                switchToEditCard();
            } catch (Throwable t) {
                noticePane.showError(t);
            }
        });
        return button;
    }

    private JButton createDeleteButton() {
        JButton button = new JButton("Delete");
        button.setName("delete account button");
        button.addActionListener(e -> {
            try {
                final String name = nameTextField.getText();
                if (!accountStorage.isExists(name))
                    throw new IllegalStateException("Account name does not exists");
                if (context.standardDialogsProvider().askYesNoQuestion(this, "Are you sure to delete account \"" + name + "\"?")) {
                    accountStorage.removeFromUsed(name);
                    accountStorage.remove(name);
                    noticePane.showSuccess("Account removed successfully");
                    clearEditPane();
                }
            } catch (Throwable t) {
                noticePane.showError(t);
            }
        });
        return button;
    }

    private void reloadComboBoxModel() {
        comboBoxModel.removeAllElements();
        comboBoxModel.addAll(accountStorage.accountsNames());
    }

    private void displayAccountInfo(final String accountId) {
        final Account account = accountStorage.get(accountId);
        nameTextField.setText(account.name());
        editLoginTextField.setText(account.login());
        editPasswordTextField.setText(account.password());
        editCommentaryTextField.setText(account.commentary().orElse(null));
        inUseCheckBox.setSelected(accountStorage.isInUse(account.name()));
        noticePane.clearNotice();
    }

    private void saveInUseState(String s, boolean selected) {
        if (selected) accountStorage.addToUsed(s);
        else accountStorage.removeFromUsed(s);
    }

    private void switchToAddCard() {
        clearAddPane();
        cardLayout.show(cardsPane, CHARACTER_CREATE_PANE);
        addButton.setVisible(false);
        backButton.setVisible(true);
        editSaveButton.setVisible(false);
        deleteButton.setVisible(false);
        saveNewButton.setVisible(true);
    }

    private void switchToEditCard() {
        clearEditPane();
        cardLayout.show(cardsPane, CHARACTER_EDIT_PANE);
        addButton.setVisible(true);
        backButton.setVisible(false);
        editSaveButton.setVisible(true);
        deleteButton.setVisible(true);
        saveNewButton.setVisible(false);
    }

    private void clearAddPane() {
        addNameTextField.setText(null);
        addLoginTextField.setText(null);
        addPasswordTextField.setText(null);
        addCommentaryTextField.setText(null);
        noticePane.clearNotice();
    }

    private void clearEditPane() {
        reloadComboBoxModel();
        selectAccountComboBox.setSelectedIndex(-1);
        nameTextField.setText(null);
        editLoginTextField.setText(null);
        editPasswordTextField.setText(null);
        editCommentaryTextField.setText(null);
        noticePane.clearNotice();
    }
}
