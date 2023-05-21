package midorum.melbone.settings.internal.storage.vendor;

import midorum.melbone.model.persistence.StorageKey;
import midorum.melbone.model.settings.PropertiesProvider;
import midorum.melbone.settings.internal.storage.VersionableKeyValueStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class H2KeyValueStorage implements VersionableKeyValueStorage {

    private final Logger logger = LogManager.getLogger("settings");

    private final MVStore store;
    private final PropertiesProvider propertiesProvider;

    public H2KeyValueStorage(final String storeFileName, final char[] password, final PropertiesProvider propertiesProvider) {
        this.propertiesProvider = propertiesProvider;
        this.store = openStore(storeFileName, password);
        logger.info("opened store: {}", getStoreFileName());
        traceStore("storage opened");
    }

    public H2KeyValueStorage(final String storeFileName, final PropertiesProvider propertiesProvider) {
        this.propertiesProvider = propertiesProvider;
        this.store = openStoreInPlainMode(storeFileName);
        logger.info("opened store in plain mode: {}", getStoreFileName());
        traceStore("storage opened");
    }

    public H2KeyValueStorage(final PropertiesProvider propertiesProvider) {
        this.propertiesProvider = propertiesProvider;
        this.store = openInMemoryStore();
        logger.info("opened in-memory store");
    }

    @Override
    public <K> Set<K> getKeySet(final StorageKey map) {
        final MVMap<K, ?> mvMap = store.openMap(map.name());
        return mvMap.keySet();
    }

    @Override
    public <K> boolean containsKey(final StorageKey map, final K key) {
        final MVMap<K, ?> mvMap = store.openMap(map.name());
        return mvMap.containsKey(key);
    }

    @Override
    public <K, V> Optional<V> read(final StorageKey map, final K key) {
        final MVMap<K, V> mvMap = store.openMap(map.name());
        traceMap(map, mvMap, "read");
        return Optional.ofNullable(mvMap.get(key));
    }

    @Override
    public <K, V> V write(final StorageKey map, final K key, final V value) {
        final MVMap<K, V> mvMap = store.openMap(map.name());
        traceMap(map, mvMap, "before write");
        final V oldValue = mvMap.put(key, value);
        traceMap(map, mvMap, "after write");
        return oldValue;
    }

    @Override
    public <V> Collection<V> getValues(final StorageKey map) {
        final MVMap<?, V> mvMap = this.store.openMap(map.name());
        return mvMap.values();
    }

    @Override
    public <K, V> V removeKey(final StorageKey map, final K key) {
        final MVMap<K, V> mvMap = store.openMap(map.name());
        return mvMap.remove(key);
    }

    @Override
    public void removeMap(final StorageKey map) {
        this.store.removeMap(map.name());
    }

    @Override
    public boolean isEmpty() {
        final Set<String> mapNames = store.getMapNames();
        if (mapNames.isEmpty()) return true;
        return mapNames.stream().allMatch(map -> store.openMap(map).isEmpty());
    }

    @Override
    public void close() {
        store.close();
        logger.info("store has been closed: {}", getStoreFileName());
    }

    private MVStore openStore(final String storeFileName, final char[] password) {
        return new MVStore.Builder()
                .fileName(storeFileName)
                .encryptionKey(password)
                .compress()
                .open();
    }

    private MVStore openStoreInPlainMode(final String storeFileName) {
        return new MVStore.Builder()
                .fileName(storeFileName)
                .open();
    }

    private MVStore openInMemoryStore() {
        return MVStore.open(null);
    }

    private String getStoreFileName() {
        return store.getFileStore() != null ? store.getFileStore().getFileName() : "in-memory";
    }

    private <V, K> void traceStore(final String marker) {
        if (!propertiesProvider.isModeSet("trace_store")) return;
        logger.trace("----- trace \"{}\" storage content (marker: {})", getStoreFileName(), marker);
        final Set<String> mapNames = store.getMapNames();
        logger.trace("maps: {}", mapNames);
        mapNames.forEach(mapName -> {
            try {
                logger.trace("--- map {} values:", mapName);
                final MVMap<Object, Object> mvMap = store.openMap(mapName);
                try {
                    mvMap.forEach((k, v) -> logger.trace("{} : {}", k, v));
                } catch (Throwable t) {
                    logger.error("Unable read map " + mapName + " due to " + t.getMessage(), t);
                }
            } catch (Throwable t) {
                logger.error("Unable open map " + mapName + " due to " + t.getMessage(), t);
            }
            logger.trace("--- map {} end", mapName);
        });
        logger.trace("----- store {} end", getStoreFileName());
    }

    private <V, K> void traceMap(final StorageKey map, final MVMap<K, V> mvMap, final String marker) {
        if (!propertiesProvider.isModeSet("trace_store_map")) return;
        logger.trace("trace map \"{}\" (marker: {})", map, marker);
        mvMap.forEach((k, v) -> logger.trace("> key: {} value: {}", k, v));
    }

    @Override
    public int getVersion() {
        return store.getStoreVersion();
    }

    @Override
    public void setVersion(final int version) {
        store.setStoreVersion(version);
    }
}
