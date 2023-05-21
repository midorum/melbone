package midorum.melbone.settings.internal.obtaining.setting;

import com.midorum.win32api.struct.PointFloat;
import midorum.melbone.model.settings.setting.TargetCountControlSettings;
import midorum.melbone.settings.SettingKeys;
import midorum.melbone.settings.internal.storage.KeyValueStorage;

public class TargetCountControlSettingsImpl extends SettingValueExtractor implements TargetCountControlSettings {

    public TargetCountControlSettingsImpl(final KeyValueStorage keyValueStorage) {
        super(keyValueStorage);
    }

    @Override
    public long windowTimeout() {
        return (long) getValue(SettingKeys.TargetCountControl.windowTimeout);
    }

    @Override
    public String windowTitle() {
        return (String) getValue(SettingKeys.TargetCountControl.windowTitle);
    }

    @Override
    public PointFloat confirmButtonPoint() {
        return (PointFloat) getValue(SettingKeys.TargetCountControl.confirmButtonPoint);
    }
}
