package midorum.melbone.settings.internal.storage;

import midorum.melbone.model.persistence.StorageKey;

import java.io.Closeable;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface KeyValueStorage extends Closeable {

    <K> Set<K> getKeySet(final StorageKey map);

    <K> boolean containsKey(final StorageKey map, final K key);

    <V> Collection<V> getValues(final StorageKey map);

    <K, V> Optional<V> read(final StorageKey map, final K key);

    <K, V> V write(final StorageKey map, final K key, final V value);

    <K, V> V removeKey(final StorageKey map, final K key);

    void removeMap(final StorageKey map);

    boolean isEmpty();
}
