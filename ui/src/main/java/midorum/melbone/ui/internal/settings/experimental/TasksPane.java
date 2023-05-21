package midorum.melbone.ui.internal.settings.experimental;

import dma.validation.Validator;
import dma.validation.flow.VerifyThat;
import midorum.melbone.model.experimental.task.Task;
import midorum.melbone.model.experimental.task.TaskStorage;
import midorum.melbone.ui.internal.Context;
import midorum.melbone.ui.internal.common.NoticePane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TasksPane extends JPanel {

    private static final String TASK_VIEW_PANE = "taskViewPane";
    private static final String TASK_EDIT_PANE = "taskEditPane";
    private static final int NO_EDITING_TASK_ID = -1;
    public static final int DEFAULT_PERIOD = 5;

    private final TaskStorage taskStorage;
    private final JComboBox<Task> selectTaskComboBox;
    private final JCheckBox taskEnabledCheckBox;
    private final JSpinner periodSpinner;
    private final JLabel periodLabel;
    private final NoticePane noticePane;
    private final JPanel cardsPane;
    private final CardLayout cardLayout;
    private final JTextField taskNameTextField;
    private final JButton addButton;
    private final JButton editButton;
    private final JButton backButton;
    private final JButton saveButton;
    private DefaultComboBoxModel<Task> comboBoxModel;
    private int editingTaskId = NO_EDITING_TASK_ID;

    public TasksPane(final Context context) {
        super(false);
        this.taskStorage = context.taskStorage();

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        //setBorder(new LineBorder(Color.RED));

        this.selectTaskComboBox = createComboBox();
        this.periodLabel = new JLabel();
        this.taskEnabledCheckBox = createTaskEnabledCheckBox();
        this.taskNameTextField = new JTextField();
        this.periodSpinner = createPeriodSpinner();
        this.noticePane = new NoticePane(context.logger());
        this.saveButton = createSaveButton();
        this.addButton = createAddButton();
        this.editButton = createEditButton();
        this.backButton = createBackButton();
        this.cardLayout = new CardLayout();
        this.cardsPane = createCardPane();

        add(cardsPane, BorderLayout.CENTER);
        add(createBottomPane(), BorderLayout.SOUTH);

        switchToViewCard();
    }

    private JPanel createCardPane() {
        final JPanel cardsPane;
        cardsPane = new JPanel(cardLayout);
        cardsPane.add(TASK_VIEW_PANE, createTaskViewPane());
        cardsPane.add(TASK_EDIT_PANE, createTaskEditPane());
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

    private JPanel createTaskViewPane() {
        final JPanel taskSelectorPane = new JPanel();
        //taskSelectorPane.setBorder(new LineBorder(Color.RED));
        taskSelectorPane.setLayout(new BoxLayout(taskSelectorPane, BoxLayout.PAGE_AXIS));
        taskSelectorPane.add(selectTaskComboBox);
        taskSelectorPane.add(Box.createVerticalStrut(5));

        final JPanel infoPane = new JPanel();
        infoPane.setLayout(new BoxLayout(infoPane, BoxLayout.LINE_AXIS));
        infoPane.add(new JLabel("Period in minutes:"));
        infoPane.add(Box.createHorizontalStrut(5));
        infoPane.add(periodLabel);
        infoPane.add(Box.createHorizontalStrut(5));
        infoPane.add(taskEnabledCheckBox);
        infoPane.add(Box.createHorizontalGlue());
        taskSelectorPane.add(infoPane);

        final JPanel taskViewPane = new JPanel();
        taskViewPane.setLayout(new BorderLayout());
        taskViewPane.add(taskSelectorPane, BorderLayout.NORTH);
        return taskViewPane;
    }

    private JPanel createTaskEditPane() {
        final JPanel namePane = new JPanel();
        namePane.setLayout(new BoxLayout(namePane, BoxLayout.PAGE_AXIS));
        final JLabel label = new JLabel("Name:");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        namePane.add(label);
        taskNameTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
        namePane.add(taskNameTextField);

        final JPanel spinnerPane = new JPanel();
        spinnerPane.setLayout(new BoxLayout(spinnerPane, BoxLayout.LINE_AXIS));
        spinnerPane.add(new JLabel("Period in minutes:"));
        spinnerPane.add(Box.createHorizontalStrut(5));
        spinnerPane.add(periodSpinner);
        spinnerPane.add(Box.createHorizontalGlue());
        spinnerPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        final JPanel inputsPane = new JPanel();
        inputsPane.setLayout(new BoxLayout(inputsPane, BoxLayout.PAGE_AXIS));
        inputsPane.add(namePane);
        inputsPane.add(Box.createVerticalStrut(5));
        inputsPane.add(spinnerPane);

        final JPanel taskEditPane = new JPanel();
        taskEditPane.setLayout(new BorderLayout());
        taskEditPane.add(inputsPane, BorderLayout.NORTH);
        return taskEditPane;
    }

    private JSpinner createPeriodSpinner() {
        final JSpinner spinner = new JSpinner(new SpinnerNumberModel(DEFAULT_PERIOD, 1, null, 1));
        fixComponentPreferredHeight(spinner);
        return spinner;
    }

    private JCheckBox createTaskEnabledCheckBox() {
        final JCheckBox checkBox = new JCheckBox("enabled");
        checkBox.addActionListener(e ->
                Validator.checkNotNull(selectTaskComboBox.getSelectedItem())
                        .cast(Task.class)
                        .thanDo(task -> saveTaskEnabledState(task, checkBox.isSelected()))
                        .elseDoNothing());
        return checkBox;
    }

    private void saveTaskEnabledState(final Task task, final boolean enabled) {
        try {
            taskStorage.updateTask(task.id(), enabled);
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
                Validator.checkNotNull(selectTaskComboBox.getSelectedItem())
                        .cast(Task.class)
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
                VerifyThat.executionOf(() -> saveTask(getEditedTaskName(), getPeriodSpinnerValue()))
                        .onSuccess(() -> noticePane.showSuccess("Task saved successfully"))
                        .onError(noticePane::showError)
                        .isComplete());
        return button;
    }

    private Integer getPeriodSpinnerValue() {
        return (Integer) periodSpinner.getValue();
    }

    private String getEditedTaskName() {
        return taskNameTextField.getText();
    }

    private void saveTask(final String name, final Integer period) {
        final String checkedName = Validator.checkNotNull(name).andCheckNot(String::isBlank).andMap(String::trim).orThrow("Task name should present");
        final Integer checkedPeriod = Validator.checkNotNull(period).orThrow("Task period should present");
        if (editingTaskId == NO_EDITING_TASK_ID)
            taskStorage.createTask(checkedName, checkedPeriod);
        else
            taskStorage.updateTask(editingTaskId, checkedName, checkedPeriod);
    }

    private JComboBox<Task> createComboBox() {
        comboBoxModel = new DefaultComboBoxModel<>();
        reloadComboBoxModel();
        final JComboBox<Task> comboBox = new JComboBox<>(comboBoxModel);
        fixComponentPreferredHeight(comboBox);
        comboBox.setRenderer(new TaskListCellRenderer());
        comboBox.setSelectedIndex(-1);
        comboBox.addActionListener(e -> Validator.checkNotNull(((JComboBox<?>) e.getSource()).getSelectedItem())
                .cast(Task.class)
                .thanDo(this::displayTaskInfo)
                .elseDoNothing());
        return comboBox;
    }

    private void fixComponentPreferredHeight(Component component) {
        final Dimension maximumSize = component.getMaximumSize();
        maximumSize.height = component.getPreferredSize().height;
        component.setMaximumSize(maximumSize);
    }

    private void switchToViewCard() {
        reloadComboBoxModel();
        cardLayout.show(cardsPane, TASK_VIEW_PANE);
        addButton.setVisible(true);
        editButton.setVisible(true);
        backButton.setVisible(false);
        saveButton.setVisible(false);
        noticePane.clearNotice();
    }

    private void switchToEditCard() {
        this.editingTaskId = NO_EDITING_TASK_ID;
        clearEditPane();
        renderEditCard();
    }

    private void switchToEditCard(final Task task) {
        this.editingTaskId = task.id();
        fillEditPane(task);
        renderEditCard();
    }

    private void renderEditCard() {
        cardLayout.show(cardsPane, TASK_EDIT_PANE);
        addButton.setVisible(false);
        editButton.setVisible(false);
        backButton.setVisible(true);
        saveButton.setVisible(true);
        noticePane.clearNotice();
    }

    private void clearEditPane() {
        taskNameTextField.setText(null);
        periodSpinner.setValue(DEFAULT_PERIOD);
    }

    private void fillEditPane(final Task task) {
        taskNameTextField.setText(task.name());
        periodSpinner.setValue(task.period());
    }

    private void displayTaskInfo(final Task task) {
        taskStorage.getTaskById(task.id())
                .ifPresentOrElse(loaded -> {
                    periodLabel.setText(Integer.toString(loaded.period()));
                    taskEnabledCheckBox.setSelected(loaded.enabled());
                    noticePane.showInfo("Loaded from storage");
                }, () -> noticePane.showError("Task with id " + task.id() + " not found"));
    }

    private void reloadComboBoxModel() {
        comboBoxModel.removeAllElements();
        //TODO lazy loading
        comboBoxModel.addAll(taskStorage.listAllTasks());
    }

    private static class TaskListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Object formattedString;
            if (value instanceof Task key) {
                formattedString = key.name();
            } else {
                formattedString = value;
            }
            return super.getListCellRendererComponent(list, formattedString, index, isSelected, cellHasFocus);
        }
    }
}
