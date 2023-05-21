package midorum.melbone.settings.internal.storage;

public interface VersionableKeyValueStorage extends KeyValueStorage {

    int getVersion();

    void setVersion(int version);
}
