package midorum.melbone.ui.context;

import midorum.melbone.model.persistence.SettingStorage;
import midorum.melbone.model.persistence.StorageKey;
import midorum.melbone.model.settings.key.SettingDataHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MockedSettingStorage implements SettingStorage {

    private final Logger logger = LogManager.getLogger();

    private final Map<StorageKey, Map<String, Object>> storage = new ConcurrentHashMap<>();

    @Override
    public Set<String> getNames(final StorageKey map) {
        return Optional.ofNullable(storage.get(map))
                .map(Map::keySet)
                .map(Set::copyOf)
                .orElse(Collections.emptySet());
    }

    @Override
    public boolean containsKey(final SettingDataHolder key) {
        return Optional.ofNullable(storage.get(key.internal().storageKey()))
                .map(stringObjectMap -> stringObjectMap.containsKey(key.name()))
                .orElse(false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> read(final SettingDataHolder key) {
        return Optional.ofNullable(storage.get(key.internal().storageKey()))
                .map(stringObjectMap -> (T) stringObjectMap.get(key.name()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T write(final SettingDataHolder key, final T value) {
        final StorageKey storageKey = key.internal().storageKey();
        final Map<String, Object> map = Optional.ofNullable(storage.get(key.internal().storageKey())).orElse(new HashMap<>());
        final Object oldValue = map.put(key.name(), value);
        storage.put(storageKey, map);
        return (T) oldValue;
    }

    public int mapsInStorage() {
        return storage.size();
    }
}
