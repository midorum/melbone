package midorum.melbone.ui.internal.settings;

import midorum.melbone.ui.internal.Context;
import midorum.melbone.ui.internal.model.FrameStateOperations;
import midorum.melbone.ui.internal.settings.experimental.RepeatableTasksPane;

import java.awt.*;
import java.util.function.Supplier;

public class TabComponents {

    private final FrameStateOperations ownerFrame;
    private final Context context;
    private Supplier<SettingsPane> settingsPaneSupplier = this::obtainAndCacheSettingsPane;
    private Supplier<AccountsPane> accountsPaneSupplier = this::obtainAndCacheAccountsPane;
    private Supplier<StampsPane> stampsPaneSupplier = this::obtainAndCacheStampsPane;
    private Supplier<RepeatableTasksPane> repeatableTasksPaneSupplier = this::obtainAndCacheRepeatableTasksPane;

    public TabComponents(final FrameStateOperations ownerFrame, final Context context) {
        this.ownerFrame = ownerFrame;
        this.context = context;
    }

    public Component getComponent(final Tab tab) {
        return switch (tab) {
            case settings -> settingsPaneSupplier.get();
            case accounts -> accountsPaneSupplier.get();
            case stamps -> stampsPaneSupplier.get();
            case repeatableTasks -> repeatableTasksPaneSupplier.get();
        };
    }

    private synchronized SettingsPane obtainAndCacheSettingsPane() {
        class Factory implements Supplier<SettingsPane> {
            private final SettingsPane settingsPane;

            Factory() {
                this.settingsPane = new SettingsPane(ownerFrame, context);
            }

            @Override
            public SettingsPane get() {
                return this.settingsPane;
            }
        }

        if (!(this.settingsPaneSupplier instanceof Factory)) {
            this.settingsPaneSupplier = new Factory();
        }
        return this.settingsPaneSupplier.get();
    }

    private synchronized AccountsPane obtainAndCacheAccountsPane() {
        class Factory implements Supplier<AccountsPane> {
            private final AccountsPane accountsPane;

            Factory() {
                this.accountsPane = new AccountsPane(context);
            }

            @Override
            public AccountsPane get() {
                return this.accountsPane;
            }
        }

        if (!(this.accountsPaneSupplier instanceof Factory)) {
            this.accountsPaneSupplier = new Factory();
        }
        return this.accountsPaneSupplier.get();
    }

    private synchronized StampsPane obtainAndCacheStampsPane() {
        class Factory implements Supplier<StampsPane> {
            private final StampsPane stampsPane;

            Factory() {
                this.stampsPane = new StampsPane(ownerFrame, context);
            }

            @Override
            public StampsPane get() {
                return this.stampsPane;
            }
        }

        if (!(this.stampsPaneSupplier instanceof Factory)) {
            this.stampsPaneSupplier = new Factory();
        }
        return this.stampsPaneSupplier.get();
    }

    private synchronized RepeatableTasksPane obtainAndCacheRepeatableTasksPane() {
        class Factory implements Supplier<RepeatableTasksPane> {
            private final RepeatableTasksPane repeatableTasksPane;

            Factory() {
                this.repeatableTasksPane = new RepeatableTasksPane(context);
            }

            @Override
            public RepeatableTasksPane get() {
                return this.repeatableTasksPane;
            }
        }

        if (!(this.repeatableTasksPaneSupplier instanceof Factory)) {
            this.repeatableTasksPaneSupplier = new Factory();
        }
        return this.repeatableTasksPaneSupplier.get();
    }
}
