package midorum.melbone.settings.internal.management;

import dma.validation.Validator;
import midorum.melbone.model.persistence.SettingStorage;
import midorum.melbone.model.persistence.StorageKey;
import midorum.melbone.model.settings.key.NoopKey;
import midorum.melbone.model.settings.key.SettingDataHolder;
import midorum.melbone.settings.internal.storage.KeyValueStorage;

import java.util.Optional;
import java.util.Set;

public class SettingStorageImpl implements SettingStorage {

    private final KeyValueStorage keyValueStorage;

    public SettingStorageImpl(final KeyValueStorage keyValueStorage) {
        this.keyValueStorage = keyValueStorage;
    }

    @Override
    public Set<String> getNames(final StorageKey map) {
        return keyValueStorage.getKeySet(map);
    }

    @Override
    public boolean containsKey(final SettingDataHolder key) {
        return keyValueStorage.containsKey(key.internal().storageKey(), key.name());
    }

    @Override
    public <T> Optional<T> read(final SettingDataHolder key) {
        return keyValueStorage.read(key.internal().storageKey(), key.name());
    }

    @Override
    public <T> T write(final SettingDataHolder key, final T value) {
        final SettingDataHolder checkedKey = Validator.checkNotNull(key).andCheckNot(NoopKey.class::isInstance).orThrow(() -> new IllegalArgumentException("Storing key cannot be null; storing no-op keys is forbidden (passed " + key + ")"));
        final T nonNullValue = Validator.checkNotNull(value).orThrowForSymbol("value");
        final T typeSafeValue = Validator.check(nonNullValue, v -> checkedKey.internal().checkValueType(v)).orThrow(() -> new IllegalArgumentException("value \"" + value + "\" must be of " + key.internal().type() + " but is " + value.getClass()));
        @SuppressWarnings("unchecked") final T validValue = (T) Validator.check(typeSafeValue, checkedKey.internal().validator()).orThrow(() -> new IllegalArgumentException("value " + value + " is not valid"));
        return keyValueStorage.write(checkedKey.internal().storageKey(), checkedKey.name(), validValue);
    }
}
