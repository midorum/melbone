package midorum.melbone.model.persistence;

import midorum.melbone.model.settings.key.SettingDataHolder;

import java.util.Optional;
import java.util.Set;

public interface SettingStorage {

    Set<String> getNames(final StorageKey map);

    boolean containsKey(final SettingDataHolder key);

    <T> Optional<T> read(final SettingDataHolder key);

    <T> T write(final SettingDataHolder key, final T value);
}
