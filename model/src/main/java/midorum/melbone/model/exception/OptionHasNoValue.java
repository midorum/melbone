package midorum.melbone.model.exception;

import midorum.melbone.model.settings.key.SettingKey;

public class OptionHasNoValue extends RuntimeException {

    private final SettingKey key;

    public OptionHasNoValue(SettingKey key) {
        this(key.internal().groupName() + "." + key.name() + " doesn't have value", key);
    }

    public OptionHasNoValue(String message, SettingKey key) {
        super(message);
        this.key = key;
    }

    public SettingKey getKey() {
        return key;
    }
}
