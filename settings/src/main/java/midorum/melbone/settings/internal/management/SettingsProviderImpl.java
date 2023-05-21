package midorum.melbone.settings.internal.management;

import midorum.melbone.model.settings.SettingsProvider;
import midorum.melbone.model.settings.account.AccountBinding;
import midorum.melbone.model.settings.setting.*;
import midorum.melbone.model.settings.stamp.Stamps;
import midorum.melbone.model.settings.stamp.TargetBaseAppStamps;
import midorum.melbone.model.settings.stamp.TargetLauncherStamps;
import midorum.melbone.settings.internal.obtaining.account.AccountBindingImpl;
import midorum.melbone.settings.internal.obtaining.setting.*;
import midorum.melbone.settings.internal.obtaining.stamp.TargetBaseAppStampsImpl;
import midorum.melbone.settings.internal.obtaining.stamp.TargetLauncherStampsImpl;
import midorum.melbone.settings.internal.storage.KeyValueStorage;

public class SettingsProviderImpl implements SettingsProvider {

    private final Settings settings;
    private final AccountBinding accountBinding;
    private final Stamps stamps;

    public SettingsProviderImpl(final KeyValueStorage keyValueStorage) {
        final ApplicationSettings applicationSettings = new ApplicationSettingsImpl(keyValueStorage);
        final TargetLauncherSettings targetLauncherSettings = new TargetLauncherSettingsImpl(keyValueStorage);
        final TargetCountControlSettings targetCountControlSettings = new TargetCountControlSettingsImpl(keyValueStorage);
        final TargetBaseAppSettings targetBaseAppSettings = new TargetBaseAppSettingsImpl(keyValueStorage);
        final UacSettingsImpl uacSettings = new UacSettingsImpl(keyValueStorage);
        this.settings = new Settings() {
            @Override
            public ApplicationSettings application() {
                return applicationSettings;
            }

            @Override
            public TargetLauncherSettings targetLauncher() {
                return targetLauncherSettings;
            }

            @Override
            public TargetCountControlSettings targetCountControl() {
                return targetCountControlSettings;
            }

            @Override
            public TargetBaseAppSettings targetBaseAppSettings() {
                return targetBaseAppSettings;
            }

            @Override
            public UacSettings uacSettings() {
                return uacSettings;
            }
        };

        this.accountBinding = new AccountBindingImpl(keyValueStorage);

        final TargetLauncherStamps targetLauncherStamps = new TargetLauncherStampsImpl(keyValueStorage);
        final TargetBaseAppStamps targetBaseAppStamps = new TargetBaseAppStampsImpl(keyValueStorage);
        this.stamps = new Stamps() {
            @Override
            public TargetLauncherStamps targetLauncher() {
                return targetLauncherStamps;
            }

            @Override
            public TargetBaseAppStamps targetBaseApp() {
                return targetBaseAppStamps;
            }
        };
    }

    @Override
    public Settings settings() {
        return settings;
    }

    @Override
    public Stamps stamps() {
        return stamps;
    }

    @Override
    public AccountBinding accountBinding() {
        return accountBinding;
    }

}
