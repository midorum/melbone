package midorum.melbone.ui;

import dma.validation.Validator;
import midorum.melbone.executor.ExecutorFactory;
import midorum.melbone.model.experimental.task.TaskStorage;
import midorum.melbone.model.persistence.AccountStorage;
import midorum.melbone.model.persistence.SettingStorage;
import midorum.melbone.model.settings.PropertiesProvider;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.ui.internal.Context;
import midorum.melbone.ui.internal.main.MainForm;
import midorum.melbone.window.WindowFactory;

import java.util.function.Supplier;

public class UserInterface {

    private final ExecutorFactory executorFactory;
    private final Settings settings;
    private final SettingStorage settingStorage;
    private final AccountStorage accountStorage;
    private final TaskStorage taskStorage;
    private final WindowFactory windowFactory;
    private final PropertiesProvider propertiesProvider;
    private Supplier<Displayable> displayableFormSupplier = this::obtainAndCacheMainForm;

    private UserInterface(final ExecutorFactory executorFactory,
                          final Settings settings,
                          final SettingStorage settingStorage,
                          final AccountStorage accountStorage,
                          final TaskStorage taskStorage,
                          final WindowFactory windowFactory,
                          final PropertiesProvider propertiesProvider) {
        this.executorFactory = executorFactory;
        this.settings = settings;
        this.settingStorage = settingStorage;
        this.accountStorage = accountStorage;
        this.taskStorage = taskStorage;
        this.windowFactory = windowFactory;
        this.propertiesProvider = propertiesProvider;
    }

    public Displayable mainForm() {
        return displayableFormSupplier.get();
    }

    private synchronized Displayable obtainAndCacheMainForm() {
        class Factory implements Supplier<Displayable> {
            private final Displayable displayable;

            Factory() {
                final Context context = new Context.Builder()
                        .executorFactory(executorFactory)
                        .settings(settings)
                        .settingStorage(settingStorage)
                        .accountStorage(accountStorage)
                        .taskStorage(taskStorage)
                        .windowFactory(windowFactory)
                        .propertiesProvider(propertiesProvider)
                        .build();
                final MainForm mainForm = new MainForm(propertiesProvider.appName() + " v" + getVersion(), context);
                this.displayable = mainForm::display;
            }

            @Override
            public Displayable get() {
                return this.displayable;
            }
        }

        if (!(this.displayableFormSupplier instanceof Factory)) {
            this.displayableFormSupplier = new Factory();
        }
        return this.displayableFormSupplier.get();
    }

    private String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    public static class Builder {

        private ExecutorFactory executorFactory;
        private Settings settings;
        private SettingStorage settingStorage;
        private AccountStorage accountStorage;
        private TaskStorage taskStorage;
        private WindowFactory windowFactory;
        private PropertiesProvider propertiesProvider;

        public Builder executorFactory(final ExecutorFactory executorFactory) {
            this.executorFactory = executorFactory;
            return this;
        }

        public Builder settings(final Settings settings) {
            this.settings = settings;
            return this;
        }

        public Builder settingStorage(final SettingStorage settingStorage) {
            this.settingStorage = settingStorage;
            return this;
        }

        public Builder accountStorage(final AccountStorage accountStorage) {
            this.accountStorage = accountStorage;
            return this;
        }

        public Builder taskStorage(final TaskStorage taskStorage) {
            this.taskStorage = taskStorage;
            return this;
        }

        public Builder windowFactory(final WindowFactory windowFactory) {
            this.windowFactory = windowFactory;
            return this;
        }

        public Builder propertiesProvider(final PropertiesProvider propertiesProvider) {
            this.propertiesProvider = propertiesProvider;
            return this;
        }

        public UserInterface build() {
            return new UserInterface(
                    Validator.checkNotNull(executorFactory).orThrowForSymbol("executorFactory"),
                    Validator.checkNotNull(settings).orThrowForSymbol("settings"),
                    Validator.checkNotNull(settingStorage).orThrowForSymbol("settingStorage"),
                    Validator.checkNotNull(accountStorage).orThrowForSymbol("accountStorage"),
                    Validator.checkNotNull(taskStorage).orThrowForSymbol("taskStorage"),
                    Validator.checkNotNull(windowFactory).orThrowForSymbol("windowFactory"),
                    Validator.checkNotNull(propertiesProvider).orThrowForSymbol("propertiesProvider"));
        }
    }
}
