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

    private final AccountStorage accountStorage;
    private final TargetWindowOperations targetWindowOperations;
    private final ApplicationSettings applicationSettings;
    private final DataLoader dataLoader;
    private final Logger logger;
    private final List<JCheckBox> checkBoxes = new ArrayList<>();
    private final GridLayout grid;
    private final JPanel content;

    public AccountsPane(final AccountStorage accountStorage,
                        final TargetWindowOperations targetWindowOperations,
                        final ApplicationSettings applicationSettings,
                        final DataLoader dataLoader,
                        final Logger logger) {
        super(new GridLayout()); //FIXME force to fill all available space
        this.accountStorage = accountStorage;
        this.targetWindowOperations = targetWindowOperations;
        this.applicationSettings = applicationSettings;
        this.dataLoader = dataLoader;
        this.logger = logger;
        this.setName("AccountsPane");

        this.grid = new GridLayout(1, 1, 10, 10);
        this.content = new JPanel(this.grid);
        this.content.setName("content");

        add(createVerticalScroll(10));
    }

    private JScrollPane createVerticalScroll(final int scrollIncrement) {
        final JScrollPane scrollPane = new JScrollPane(this.content);
        scrollPane.getVerticalScrollBar().setUnitIncrement(scrollIncrement);
        return scrollPane;
    }

    @Override
    public int getAccountsCount() {
        return grid.getRows();
    }

    @Override
    public Account[] getSelectedAccounts() {
        return checkBoxes.stream()
                .filter(JCheckBox::isSelected)
                .map(AbstractButton::getText)
                .map(accountStorage::get)
                .toArray(Account[]::new);
    }

    @Override
    public void update() {
        renderForm();
    }

    private void renderForm() {
        dataLoader.loadGuiData(() -> {
                    final Collection<String> accountsInUse = accountStorage.accountsInUse();
                    logger.info("accounts in use: {}", accountsInUse);
                    final List<BaseAppWindow> allBaseAppWindows = targetWindowOperations.getAllWindows();
                    final List<String> boundAccounts = allBaseAppWindows.stream().map(BaseAppWindow::getCharacterName).filter(Optional::isPresent).map(Optional::get).toList();
                    final int maxAccountsSimultaneously = applicationSettings.maxAccountsSimultaneously();
                    return new DataToUpdateForm(accountsInUse, allBaseAppWindows, boundAccounts, maxAccountsSimultaneously);
                },
                data -> {
                    grid.setRows(data.accountsInUse.size());
                    renderCheckBoxes(data.accountsInUse, data.boundAccounts);
                    updateCheckBoxesAccessible(data.allBaseAppWindows.size(), data.boundAccounts, data.maxAccountsSimultaneously);
                    content.revalidate();
                });
    }

    private void renderCheckBoxes(final Collection<String> accountsInUse, final Collection<String> boundAccounts) {
        checkBoxes.forEach(this.content::remove);
        checkBoxes.clear();
        checkBoxes.addAll(accountsInUse.stream()
                .map(s -> createCheckBox(s, boundAccounts.contains(s)))
                .toList());
        checkBoxes.forEach(this.content::add);
    }

    private JCheckBox createCheckBox(final String s, final boolean selected) {
        final JCheckBox checkBox = new JCheckBox(s);
        checkBox.addActionListener(e -> updateCheckBoxesAccessible());
        checkBox.setSelected(selected);
        return checkBox;
    }

    private void updateCheckBoxesAccessible(final int totalBaseWindows, final List<String> boundAccounts, final int maxAccountsSimultaneously) {
        final long selectedCheckBoxes = checkBoxes.stream().filter(JCheckBox::isSelected).count();
        checkBoxes.forEach(checkBox -> checkBox.setEnabled(checkBox.isSelected()
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

    public record DataToUpdateForm (
            Collection<String> accountsInUse,
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
