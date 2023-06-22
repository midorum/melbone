package midorum.melbone.ui.internal.main;

import midorum.melbone.model.dto.Account;
import midorum.melbone.model.persistence.AccountStorage;
import midorum.melbone.model.settings.setting.ApplicationSettings;
import midorum.melbone.model.window.baseapp.BaseAppWindow;
import midorum.melbone.ui.internal.model.AccountsHolder;
import midorum.melbone.ui.internal.model.TargetWindowOperations;
import midorum.melbone.ui.internal.model.Updatable;
import midorum.melbone.ui.internal.util.DataLoader;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class AccountsPane extends JPanel implements AccountsHolder, Updatable {

    public static final String ACCOUNT_ID_PROPERTY = "accountId";
    public static final String FILTER_SHOW_ALL = "-show all-";
    private final AccountStorage accountStorage;
    private final TargetWindowOperations targetWindowOperations;
    private final ApplicationSettings applicationSettings;
    private final DataLoader dataLoader;
    private final Logger logger;
    private final List<JCheckBox> renderedCheckBoxes = new ArrayList<>();
    private final GridLayout grid;
    private final JPanel content;
    private final JPanel gridPane;
    private final DefaultComboBoxModel<String> comboBoxModel;
    private List<JCheckBox> notFilteredCheckBoxes;

    public AccountsPane(final AccountStorage accountStorage,
                        final TargetWindowOperations targetWindowOperations,
                        final ApplicationSettings applicationSettings,
                        final DataLoader dataLoader,
                        final Logger logger) {
        super(new GridLayout()); //dirty: force to fill all available space
        this.accountStorage = accountStorage;
        this.targetWindowOperations = targetWindowOperations;
        this.applicationSettings = applicationSettings;
        this.dataLoader = dataLoader;
        this.logger = logger;
        this.setName("AccountsPane");

        this.comboBoxModel = new DefaultComboBoxModel<>();
        this.grid = new GridLayout(1, 1, 10, 10);
        this.gridPane = new JPanel(this.grid);
        final JPanel filterPane = createFilterPane(createFilterComboBox(comboBoxModel));
        this.content = createContentPane(filterPane, gridPane);

        add(createVerticalScroll(this.content, 10));
    }

    private JPanel createContentPane(final JComponent... components) {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setName("content");
        for (JComponent component : components) {
            panel.add(component);
        }
        return panel;
    }

    private JPanel createFilterPane(final JComponent... components) {
        final JPanel panel = new JPanel();
        panel.setName("filter pane");
        for (JComponent component : components) {
            panel.add(component);
        }
        panel.setMaximumSize(panel.getPreferredSize());//dirty: block pane resizing
        return panel;
    }

    private JComboBox<String> createFilterComboBox(final ComboBoxModel<String> comboBoxModel) {
        final JComboBox<String> comboBox = new JComboBox<>(comboBoxModel);
        comboBox.setName("filter combo box");
        final Dimension defaultPreferredSize = comboBox.getPreferredSize();
        comboBox.setPreferredSize(new Dimension(400, defaultPreferredSize.height));
        comboBox.addActionListener(e -> filterCheckBoxes((String) comboBox.getSelectedItem()));
        return comboBox;
    }

    private void filterCheckBoxes(final String filter) {
        if (filter == null) return;
        final boolean showAll = FILTER_SHOW_ALL.equals(filter);
        final List<JCheckBox> checkBoxesToRender = this.notFilteredCheckBoxes.stream()
                .filter(checkBox -> showAll || checkBox.isSelected() || checkBox.getText().contains(filter))
                .toList();
        renderCheckBoxes(checkBoxesToRender);
        content.revalidate();
    }

    private JScrollPane createVerticalScroll(final JPanel panel, final int scrollIncrement) {
        final JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(scrollIncrement);
        return scrollPane;
    }

    @Override
    public int getAccountsCount() {
        return grid.getRows();
    }

    @Override
    public Account[] getSelectedAccounts() {
        return renderedCheckBoxes.stream()
                .filter(JCheckBox::isSelected)
                .map(checkBox -> (String) checkBox.getClientProperty(ACCOUNT_ID_PROPERTY))
                .map(accountStorage::get)
                .toArray(Account[]::new);
    }

    @Override
    public void update() {
        renderForm();
    }

    private void renderForm() {
        dataLoader.loadGuiData(() -> {
                    final Collection<Account> accountsInUse = accountStorage.accountsInUse().stream().map(accountStorage::get).toList();
                    logger.info("accounts in use: {}", accountsInUse);
                    final Collection<String> commentaries = accountStorage.commentaries();
                    final List<BaseAppWindow> allBaseAppWindows = targetWindowOperations.getAllWindows();
                    final List<String> boundAccounts = allBaseAppWindows.stream().map(BaseAppWindow::getCharacterName).filter(Optional::isPresent).map(Optional::get).toList();
                    final int maxAccountsSimultaneously = applicationSettings.maxAccountsSimultaneously();
                    return new DataToUpdateForm(accountsInUse, commentaries, allBaseAppWindows, boundAccounts, maxAccountsSimultaneously);
                },
                data -> {
                    notFilteredCheckBoxes = data.accountsInUse.stream()
                            .map(account -> createCheckBox(account, data.boundAccounts.contains(account.name())))
                            .toList();
                    updateFilterComboBox(data.commentaries);
                    renderCheckBoxes(notFilteredCheckBoxes);
                    updateCheckBoxesAccessible(data.allBaseAppWindows.size(), data.boundAccounts, data.maxAccountsSimultaneously);
                    content.revalidate();
                });
    }

    private void updateFilterComboBox(final Collection<String> commentaries) {
        comboBoxModel.removeAllElements();
        comboBoxModel.addElement(FILTER_SHOW_ALL);
        comboBoxModel.addAll(commentaries);
        comboBoxModel.setSelectedItem(FILTER_SHOW_ALL);
    }

    private void renderCheckBoxes(final List<JCheckBox> checkBoxesToRender) {
        grid.setRows(checkBoxesToRender.size());
        renderedCheckBoxes.forEach(this.gridPane::remove);
        renderedCheckBoxes.clear();
        renderedCheckBoxes.addAll(checkBoxesToRender);
        renderedCheckBoxes.sort(Comparator.comparing(AbstractButton::getText));
        renderedCheckBoxes.forEach(this.gridPane::add);
    }

    private JCheckBox createCheckBox(final Account account, final boolean selected) {
        final JCheckBox checkBox = new JCheckBox(getTextForCheckBox(account));
        checkBox.addActionListener(e -> updateCheckBoxesAccessible());
        checkBox.setSelected(selected);
        checkBox.putClientProperty(ACCOUNT_ID_PROPERTY, account.name());
        return checkBox;
    }

    private String getTextForCheckBox(final Account account) {
        return account.name() + account.commentary().map(s -> " (" + s + ")").orElse("");
    }

    private void updateCheckBoxesAccessible(final int totalBaseWindows, final List<String> boundAccounts, final int maxAccountsSimultaneously) {
        final long selectedCheckBoxes = renderedCheckBoxes.stream().filter(JCheckBox::isSelected).count();
        renderedCheckBoxes.forEach(checkBox -> checkBox.setEnabled(checkBox.isSelected()
                || (totalBaseWindows - boundAccounts.size() + selectedCheckBoxes) < maxAccountsSimultaneously));
    }

    private void updateCheckBoxesAccessible() {
        dataLoader.loadGuiData(() -> {
                    final List<BaseAppWindow> allBaseAppWindows = targetWindowOperations.getAllWindows();
                    final List<String> boundAccounts = allBaseAppWindows.stream().map(BaseAppWindow::getCharacterName).filter(Optional::isPresent).map(Optional::get).toList();
                    final int maxAccountsSimultaneously = applicationSettings.maxAccountsSimultaneously();
                    return new DataToUpdateCheckBoxes(allBaseAppWindows, boundAccounts, maxAccountsSimultaneously);
                },
                data -> updateCheckBoxesAccessible(data.allBaseAppWindows.size(), data.boundAccounts, data.maxAccountsSimultaneously));
    }

    public record DataToUpdateForm(
            Collection<Account> accountsInUse,
            Collection<String> commentaries,
            List<BaseAppWindow> allBaseAppWindows,
            List<String> boundAccounts,
            int maxAccountsSimultaneously
    ) {
    }

    public record DataToUpdateCheckBoxes(
            List<BaseAppWindow> allBaseAppWindows,
            List<String> boundAccounts,
            int maxAccountsSimultaneously
    ) {
    }
}
