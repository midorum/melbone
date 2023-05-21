package midorum.melbone.settings.internal.obtaining.setting;

import com.midorum.win32api.facade.Rectangle;
import midorum.melbone.model.settings.setting.UacSettings;
import midorum.melbone.settings.SettingKeys;
import midorum.melbone.settings.internal.storage.KeyValueStorage;

public class UacSettingsImpl extends SettingValueExtractor implements UacSettings {

    public UacSettingsImpl(final KeyValueStorage keyValueStorage) {
        super(keyValueStorage);
    }

    @Override
    public String windowClassName() {
        return (String) getValue(SettingKeys.Uac.windowClassName);
    }

    @Override
    public Rectangle windowDimensions() {
        return (Rectangle) getValue(SettingKeys.Uac.windowDimensions);
    }
}
