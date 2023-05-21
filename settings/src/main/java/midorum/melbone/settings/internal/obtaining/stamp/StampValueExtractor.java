package midorum.melbone.settings.internal.obtaining.stamp;

import midorum.melbone.model.exception.StampHasNoValue;
import midorum.melbone.model.settings.stamp.Stamp;
import midorum.melbone.model.settings.key.StampKey;
import midorum.melbone.settings.internal.dto.StampImpl;
import midorum.melbone.settings.internal.storage.KeyValueStorage;

public class StampValueExtractor {

    private final KeyValueStorage keyValueStorage;

    public StampValueExtractor(final KeyValueStorage keyValueStorage) {
        this.keyValueStorage = keyValueStorage;
    }

    protected Stamp getStamp(StampKey key) {
        return (StampImpl) keyValueStorage.read(key.internal().storageKey(), key.name())
                .orElseThrow(() -> new StampHasNoValue(key));
    }

}
