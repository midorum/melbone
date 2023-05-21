package midorum.melbone.ui.internal.settings.experimental;

import dma.validation.Validator;
import dma.validation.flow.VerifyThat;
import midorum.melbone.model.experimental.task.Action;
import midorum.melbone.model.experimental.task.ActionType;
import midorum.melbone.model.experimental.task.TaskStorage;
import midorum.melbone.ui.internal.Context;
import midorum.melbone.ui.internal.common.NoticePane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;
import java.util.Optional;

public class ActionsPane extends JPanel {

    private static final String ACTION_VIEW_PANE = "actionViewPane";
    private static final String ACTION_EDIT_PANE = "actionEditPane";
    private static final int NO_EDITING_ACTION_ID = -1;

    private final TaskStorage taskStorage;
    private final JComboBox<Action> selectActionComboBox;
    private final JComboBox<ActionType> selectActionTypeComboBox;
    private final JCheckBox actionEnabledCheckBox;
    private final JLabel actionTypeLabel;
    private final NoticePane noticePane;
    private final JPanel cardsPane;
    private final CardLayout cardLayout;
    private final JTextField actionNameTextField;
    private final JButton addButton;
    private final JButton editButton;
    private final JButton backButton;
    private final JButton saveButton;
    private int editingActionId = NO_EDITING_ACTION_ID;

    public ActionsPane(final Context context) {
        super(false);
        this.taskStorage = context.taskStorage();

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        //setBorder(new LineBorder(Color.RED));

        this.selectActionComboBox = createActionComboBox();
        this.actionTypeLabel = new JLabel();
        this.actionEnabledCheckBox = createActionEnabledCheckBox();
        this.actionNameTextField = new JTextField();
        this.selectActionTypeComboBox = createActionTypeComboBox();
        this.noticePane = new NoticePane(context.logger());
        this.saveButton = createSaveButton();
        this.addButton = createAddButton();
        this.editButton = createEditButton();
        this.backButton = createBackButton();
        this.cardLayout = new CardLayout();
        this.cardsPane = createCardPane();

        add(cardsPane, BorderLayout.CENTER);
        add(createBottomPane(), BorderLayout.SOUTH);

        reloadComboBoxModel();
        switchToViewCard();
    }

    private JPanel createCardPane() {
        final JPanel cardsPane;
        cardsPane = new JPanel(cardLayout);
        cardsPane.add(ACTION_VIEW_PANE, createActionViewPane());
        cardsPane.add(ACTION_EDIT_PANE, createActionEditPane());
        return cardsPane;
    }

    private JPanel createBottomPane() {
        final JPanel bottomPane = new JPanel();
        bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.PAGE_AXIS));
        bottomPane.add(noticePane);
        bottomPane.add(createButtonPane());
        return bottomPane;
    }

    private JPanel createButtonPane() {
        final JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(saveButton);
        buttonPane.add(editButton);
        buttonPane.add(addButton);
        buttonPane.add(backButton);
        return buttonPane;
    }

    private JPanel createActionViewPane() {
        final JPanel actionSelectorPane = new JPanel();
        //actionSelectorPane.setBorder(new LineBorder(Color.RED));
        actionSelectorPane.setLayout(new BoxLayout(actionSelectorPane, BoxLayout.PAGE_AXIS));
        actionSelectorPane.add(selectActionComboBox);
        actionSelectorPane.add(Box.createVerticalStrut(5));

        final JPanel infoPane = new JPanel();
        infoPane.setLayout(new BoxLayout(infoPane, BoxLayout.LINE_AXIS));
        infoPane.add(new JLabel("Action type:"));
        infoPane.add(Box.createHorizontalStrut(5));
        infoPane.add(actionTypeLabel);
        infoPane.add(Box.createHorizontalStrut(5));
        infoPane.add(actionEnabledCheckBox);
        infoPane.add(Box.createHorizontalGlue());
        actionSelectorPane.add(infoPane);

        final JPanel actionViewPane = new JPanel();
        actionViewPane.setLayout(new BorderLayout());
        actionViewPane.add(actionSelectorPane, BorderLayout.NORTH);
        return actionViewPane;
    }

    private JPanel createActionEditPane() {
        final JPanel namePane = new JPanel();
        namePane.setLayout(new BoxLayout(namePane, BoxLayout.PAGE_AXIS));
        final JLabel label = new JLabel("Name:");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        namePane.add(label);
        actionNameTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
        namePane.add(actionNameTextField);

        final JPanel actionTypePane = new JPanel();
        actionTypePane.setLayout(new BoxLayout(actionTypePane, BoxLayout.LINE_AXIS));
        actionTypePane.add(new JLabel("Action type:"));
        actionTypePane.add(Box.createHorizontalStrut(5));
        actionTypePane.add(selectActionTypeComboBox);
        actionTypePane.add(Box.createHorizontalGlue());
        actionTypePane.setAlignmentX(Component.LEFT_ALIGNMENT);

        final JPanel inputsPane = new JPanel();
        inputsPane.setLayout(new BoxLayout(inputsPane, BoxLayout.PAGE_AXIS));
        inputsPane.add(namePane);
        inputsPane.add(Box.createVerticalStrut(5));
        inputsPane.add(actionTypePane);

        final JPanel actionEditPane = new JPanel();
        actionEditPane.setLayout(new BorderLayout());
        actionEditPane.add(inputsPane, BorderLayout.NORTH);
        return actionEditPane;
    }

    private JCheckBox createActionEnabledCheckBox() {
        final JCheckBox checkBox = new JCheckBox("enabled");
        checkBox.addActionListener(e ->
                Validator.checkNotNull(selectActionComboBox.getSelectedItem())
                        .cast(Action.class)
                        .thanDo(action -> saveActionEnabledState(action, checkBox.isSelected()))
                        .elseDoNothing());
        return checkBox;
    }

    private void saveActionEnabledState(final Action action, final boolean enabled) {
        try {
            //settingsProvider.updateAction(action.id(), enabled);
            noticePane.showSuccess("Saved successfully");
        } catch (Throwable t) {
            t.printStackTrace();
            noticePane.showError(t.getMessage());
        }
    }

    private JButton createAddButton() {
        final JButton button = new JButton("Add");
        button.addActionListener(e -> switchToEditCard());
        return button;
    }

    private JButton createEditButton() {
        final JButton button = new JButton("Edit");
        button.addActionListener(e ->
                Validator.checkNotNull(selectActionComboBox.getSelectedItem())
                        .cast(Action.class)
                        .thanDo(this::switchToEditCard)
                        .elseDoNothing());
        return button;
    }

    private JButton createBackButton() {
        final JButton button = new JButton("Back");
        button.addActionListener(e -> switchToViewCard());
        return button;
    }

    private JButton createSaveButton() {
        final JButton button = new JButton("Save");
        button.addActionListener(e ->
                VerifyThat.executionOf(() -> saveAction(getEditedActionName(), getSelectedActionType()))
                        .onSuccess(() -> noticePane.showSuccess("Action saved successfully"))
                        .onError(noticePane::showError)
                        .isComplete());
        return button;
    }

    private JComboBox<Action> createActionComboBox() {
        final JComboBox<Action> comboBox = new JComboBox<>(new DefaultComboBoxModel<>());
        fixComponentPreferredHeight(comboBox);
        comboBox.setRenderer(new ActionListCellRenderer());
        comboBox.setSelectedIndex(-1);
        comboBox.addActionListener(e -> getSelectedAction().ifPresent(this::displayActionInfo));
        return comboBox;
    }

    private JComboBox<ActionType> createActionTypeComboBox() {
        final DefaultComboBoxModel<ActionType> model = new DefaultComboBoxModel<>();
        model.addAll(Arrays.asList(ActionType.values()));
        final JComboBox<ActionType> comboBox = new JComboBox<>(model);
        fixComponentPreferredHeight(comboBox);
        comboBox.setRenderer(new ActionTypeListCellRenderer());
        comboBox.setSelectedIndex(-1);
        return comboBox;
    }

    private void fixComponentPreferredHeight(Component component) {
        final Dimension maximumSize = component.getMaximumSize();
        maximumSize.height = component.getPreferredSize().height;
        component.setMaximumSize(maximumSize);
    }

    private ActionType getSelectedActionType() {
        return (ActionType) selectActionTypeComboBox.getSelectedItem();
    }

    private String getEditedActionName() {
        return actionNameTextField.getText();
    }

    private void saveAction(final String name, final ActionType type) {
        final String checkedName = Validator.checkNotNull(name).andCheckNot(String::isBlank).andMap(String::trim).orThrow("Action name should present");
        final ActionType checkedType = Validator.checkNotNull(type).orThrow("Action type should present");
        if (editingActionId == NO_EDITING_ACTION_ID)
            taskStorage.createAction(checkedName, checkedType);
        else
            taskStorage.updateAction(editingActionId, checkedName, checkedType);
    }

    private void switchToViewCard() {
        reloadComboBoxModel();
        selectActionComboBox.setSelectedIndex(-1);
        addButton.setVisible(true);
        editButton.setVisible(true);
        backButton.setVisible(false);
        saveButton.setVisible(false);
        noticePane.clearNotice();
        cardLayout.show(cardsPane, ACTION_VIEW_PANE);
    }

    private void switchToEditCard() {
        this.editingActionId = NO_EDITING_ACTION_ID;
        clearEditPane();
        renderEditCard();
    }

    private void switchToEditCard(final Action action) {
        this.editingActionId = action.id();
        fillEditPane(action);
        renderEditCard();
    }

    private void renderEditCard() {
        addButton.setVisible(false);
        editButton.setVisible(false);
        backButton.setVisible(true);
        saveButton.setVisible(true);
        noticePane.clearNotice();
        cardLayout.show(cardsPane, ACTION_EDIT_PANE);
    }

    private void clearEditPane() {
        actionNameTextField.setText(null);
        selectActionTypeComboBox.setSelectedIndex(-1);
    }

    private void fillEditPane(final Action action) {
        actionNameTextField.setText(action.name());
    }

    private Optional<Action> getSelectedAction() {
        return Validator.checkNotNull(selectActionComboBox.getSelectedItem())
                .andIsInstance(Action.class)
                .cast(Action.class)
                .asOptional();
    }

    private void displayActionInfo(final Action action) {
        taskStorage.getActionById(action.id())
                .ifPresentOrElse(loaded -> {
                    actionTypeLabel.setText(loaded.type().name());
                    actionEnabledCheckBox.setSelected(loaded.enabled());
                    noticePane.showInfo("Loaded from storage");
                }, () -> noticePane.showError("Action with id " + action.id() + " not found"));
    }

    private void reloadComboBoxModel() {
        selectActionComboBox.removeAllItems();
        taskStorage.listAllActions().forEach(selectActionComboBox::addItem);//TODO lazy loading
    }

    private static class ActionListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Object formattedString;
            if (value instanceof Action key) {
                formattedString = key.name();
            } else {
                formattedString = value;
            }
            return super.getListCellRendererComponent(list, formattedString, index, isSelected, cellHasFocus);
        }
    }

    private static class ActionTypeListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Object formattedString;
            if (value instanceof ActionType key) {
                formattedString = key.name();
            } else {
                formattedString = value;
            }
            return super.getListCellRendererComponent(list, formattedString, index, isSelected, cellHasFocus);
        }
    }
}
