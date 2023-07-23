package midorum.melbone.settings.internal.obtaining.setting;

import midorum.melbone.model.settings.setting.ApplicationSettings;
import midorum.melbone.settings.SettingKeys;
import midorum.melbone.settings.internal.storage.KeyValueStorage;

import java.util.Set;

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
    public long mouseClickDelay() {
        return (long) getValue(SettingKeys.Application.mouseClickDelay);
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

    @Override
    public boolean closeOverlappingWindows() {
        return (boolean) getValue(SettingKeys.Application.closeOverlappingWindows);
    }

    @Override
    public boolean shotOverlappingWindows() {
        return (boolean) getValue(SettingKeys.Application.shotOverlappingWindows);
    }

    @Override
    public String[] overlappingWindowsToSkip() {
        return (String[]) getValue(SettingKeys.Application.overlappingWindowsToSkip);
    }

    @Override
    public String[] overlappingWindowsToClose() {
        return (String[]) getValue(SettingKeys.Application.overlappingWindowsToClose);
    }

    @Override
    public int bringWindowForegroundTimeout() {
        return 5_000;
    }

    @Override
    public int bringWindowForegroundDelay() {
        return 100;
    }
}
