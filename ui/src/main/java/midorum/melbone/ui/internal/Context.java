package midorum.melbone.ui.internal;

import com.midorum.win32api.facade.IScreenShotMaker;
import com.midorum.win32api.facade.IWindow;
import com.midorum.win32api.facade.WindowPoint;
import com.midorum.win32api.struct.PointInt;
import dma.validation.Validator;
import midorum.melbone.executor.ExecutorFactory;
import midorum.melbone.model.experimental.task.TaskStorage;
import midorum.melbone.model.settings.PropertiesProvider;
import midorum.melbone.ui.internal.model.TargetWindowOperations;
import midorum.melbone.ui.internal.util.DataLoader;
import midorum.melbone.ui.internal.util.GlobalKeyHookManager;
import midorum.melbone.ui.internal.util.MouseKeyHookManager;
import midorum.melbone.ui.internal.util.StandardDialogsProvider;
import midorum.melbone.model.persistence.AccountStorage;
import midorum.melbone.model.persistence.SettingStorage;
import midorum.melbone.model.processing.IExecutor;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.model.window.baseapp.BaseAppWindow;
import midorum.melbone.window.WindowFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

public class Context {

    private final ExecutorFactory executorFactory;
    private final Settings settings;
    private final SettingStorage settingStorage;
    private final AccountStorage accountStorage;
    private final TaskStorage taskStorage;
    private final WindowFactory windowFactory;
    private final GlobalKeyHookManager globalKeyHookManager;
    private final MouseKeyHookManager mouseKeyHookManager;
    private final StandardDialogsProvider standardDialogsProvider;
    private final TargetWindowOperations targetWindowOperations;
    private final DataLoader dataLoader;
    private final PropertiesProvider propertiesProvider;
    private final Logger logger;

    private Context(final ExecutorFactory executorFactory,
                    final Settings settings,
                    final SettingStorage settingStorage,
                    final AccountStorage accountStorage,
                    final TaskStorage taskStorage,
                    final WindowFactory windowFactory,
                    final GlobalKeyHookManager globalKeyHookManager,
                    final MouseKeyHookManager mouseKeyHookManager,
                    final StandardDialogsProvider standardDialogsProvider,
                    final DataLoader dataLoader,
                    final PropertiesProvider propertiesProvider) {
        this.executorFactory = executorFactory;
        this.settings = settings;
        this.settingStorage = settingStorage;
        this.accountStorage = accountStorage;
        this.taskStorage = taskStorage;
        this.windowFactory = windowFactory;
        this.globalKeyHookManager = globalKeyHookManager;
        this.mouseKeyHookManager = mouseKeyHookManager;
        this.standardDialogsProvider = standardDialogsProvider;
        this.dataLoader = dataLoader;
        this.propertiesProvider = propertiesProvider;
        this.targetWindowOperations = createTargetWindowOperations();
        this.logger = LogManager.getLogger("ui");
    }

    public Logger logger() {
        return logger;
    }

    public IExecutor taskExecutor() {
        return executorFactory.getExecutor();
    }

    public Settings settings() {
        return settings;
    }

    public SettingStorage settingStorage() {
        return settingStorage;
    }

    public AccountStorage accountStorage() {
        return accountStorage;
    }

    public GlobalKeyHookManager globalKeyHookManager() {
        return globalKeyHookManager;
    }

    public MouseKeyHookManager mouseKeyHookManager() {
        return mouseKeyHookManager;
    }

    public StandardDialogsProvider standardDialogsProvider() {
        return standardDialogsProvider;
    }

    public TargetWindowOperations targetWindowOperations() {
        return targetWindowOperations;
    }

    public DataLoader dataLoader() {
        return dataLoader;
    }

    public PropertiesProvider propertiesProvider() {
        return propertiesProvider;
    }

    private TargetWindowOperations createTargetWindowOperations() {
        return new TargetWindowOperations() {

            @Override
            public boolean isExistUnboundWindows() {
                return windowFactory.findFirstUnboundBaseAppWindow().isPresent();
            }

            @Override
            public Optional<BaseAppWindow> getFirstNotBoundWindow() {
                return windowFactory.findFirstUnboundBaseAppWindow();
            }

            @Override
            public List<BaseAppWindow> getAllWindows() {
                return windowFactory.getAllBaseAppWindows();
            }

            @Override
            public List<String> getBoundAccounts() {
                return windowFactory.getAllBaseAppWindows().stream()
                        .map(BaseAppWindow::getCharacterName)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList();
            }

            @Override
            public List<String> getNotBoundAccounts() {
                final List<String> boundAccounts = getBoundAccounts();
                return accountStorage.accountsInUse().stream()
                        .filter(s -> !boundAccounts.contains(s))
                        .toList();
            }

            @Override
            public void minimizeAllWindows() {
                windowFactory.minimizeAllWindows();
            }

            @Override
            public Optional<WindowPoint> getPointInWindow(final PointInt point) {
                return windowFactory.getPointInWindow(point);
            }

            @Override
            public Optional<IWindow> getWindowByPoint(final PointInt point) {
                return windowFactory.getWindowByPoint(point);
            }
        };
    }

    public IScreenShotMaker getScreenShotMaker() {
        return windowFactory.getScreenShotMaker();
    }

    public TaskStorage taskStorage() {
        return taskStorage;
    }

    public static class Builder {

        private ExecutorFactory executorFactory;
        private Settings settings;
        private SettingStorage settingStorage;
        private AccountStorage accountStorage;
        private TaskStorage taskStorage;
        private WindowFactory windowFactory;
        private StandardDialogsProvider standardDialogsProvider;
        private GlobalKeyHookManager globalKeyHookManager;
        private MouseKeyHookManager mouseKeyHookManager;
        private DataLoader dataLoader;
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

        public Builder standardDialogsProvider(final StandardDialogsProvider standardDialogsProvider) {
            this.standardDialogsProvider = standardDialogsProvider;
            return this;
        }

        public Builder globalKeyHookManager(final GlobalKeyHookManager globalKeyHookManager) {
            this.globalKeyHookManager = globalKeyHookManager;
            return this;
        }

        public Builder mouseKeyHookManager(final MouseKeyHookManager mouseKeyHookManager) {
            this.mouseKeyHookManager = mouseKeyHookManager;
            return this;
        }

        public Builder dataLoader(final DataLoader dataLoader) {
            this.dataLoader = dataLoader;
            return this;
        }

        public Builder propertiesProvider(final PropertiesProvider propertiesProvider) {
            this.propertiesProvider = propertiesProvider;
            return this;
        }

        public Context build() {
            return new Context(
                    Validator.checkNotNull(executorFactory).orThrowForSymbol("executorFactory"),
                    Validator.checkNotNull(settings).orThrowForSymbol("settings"),
                    Validator.checkNotNull(settingStorage).orThrowForSymbol("settingStorage"),
                    Validator.checkNotNull(accountStorage).orThrowForSymbol("accountStorage"),
                    Validator.checkNotNull(taskStorage).orThrowForSymbol("taskStorage"),
                    Validator.checkNotNull(windowFactory).orThrowForSymbol("windowFactory"),
                    Validator.checkNotNull(globalKeyHookManager).orDefault(GlobalKeyHookManager.getInstance()),
                    Validator.checkNotNull(mouseKeyHookManager).orDefault(MouseKeyHookManager.getInstance()),
                    Validator.checkNotNull(standardDialogsProvider).orDefault(StandardDialogsProvider.getInstance()),
                    Validator.checkNotNull(dataLoader).orDefault(DataLoader.getInstance()),
                    Validator.checkNotNull(propertiesProvider).orThrowForSymbol("propertiesProvider"));
        }
    }
}
