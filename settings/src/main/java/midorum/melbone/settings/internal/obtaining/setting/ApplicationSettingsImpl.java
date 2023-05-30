package midorum.melbone.settings.internal.obtaining.setting;

import midorum.melbone.model.settings.setting.ApplicationSettings;
import midorum.melbone.settings.SettingKeys;
import midorum.melbone.settings.internal.storage.KeyValueStorage;

public class ApplicationSettingsImpl extends SettingValueExtractor implements ApplicationSettings {

    public ApplicationSettingsImpl(final KeyValueStorage keyValueStorage) {
        super(keyValueStorage);
    }

    @Override
    public int maxAccountsSimultaneously() {
        return (int) getValue(SettingKeys.Application.maxAccountsSimultaneously);
    }

    @Override
    public int taskPerformingDelay() {
        return (int) getValue(SettingKeys.Application.taskPerformingDelay);
    }

    @Override
    public int scheduledTaskPeriod() {
        return 10; // predefined value
    }

    @Override
    public float speedFactor() {
        return (float) getValue(SettingKeys.Application.speedFactor);
    }

    @Override
    public long randomRoutineDelayMax() {
        return (long) getValue(SettingKeys.Application.randomRoutineDelayMax);
    }

    @Override
    public int stampDeviation() {
        return (int) getValue(SettingKeys.Application.stampDeviation);
    }

    @Override
    public int actionsCount() {
        return (int) getValue(SettingKeys.Application.actionsCount);
    }

    @Override
    public boolean checkHealthBeforeLaunch() {
        return (boolean) getValue(SettingKeys.Application.checkHealthBeforeLaunch);
    }
}
