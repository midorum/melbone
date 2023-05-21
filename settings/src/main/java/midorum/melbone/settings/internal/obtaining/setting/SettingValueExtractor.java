package midorum.melbone.settings.internal.obtaining.setting;

import midorum.melbone.model.exception.OptionHasNoValue;
import midorum.melbone.model.settings.key.SettingKey;
import midorum.melbone.settings.internal.storage.KeyValueStorage;

public class SettingValueExtractor {

    private final KeyValueStorage keyValueStorage;

    public SettingValueExtractor(final KeyValueStorage keyValueStorage) {
        this.keyValueStorage = keyValueStorage;
    }

    protected Object getValue(SettingKey key) {
        return keyValueStorage.read(key.internal().storageKey(), key.name())
                .orElseGet(() -> key.internal().defaultValue()
                        .orElseThrow(() -> new OptionHasNoValue(key)));
    }
}
