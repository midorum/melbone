package midorum.melbone.settings.internal.management;

import dma.validation.Validator;
import midorum.melbone.model.experimental.task.TaskStorage;
import midorum.melbone.model.persistence.AccountStorage;
import midorum.melbone.model.persistence.SettingStorage;
import midorum.melbone.model.settings.PropertiesProvider;
import midorum.melbone.model.settings.SettingsProvider;
import midorum.melbone.settings.SettingKeys;
import midorum.melbone.settings.StampKeys;
import midorum.melbone.settings.internal.management.experimental.TaskStorageImpl;
import midorum.melbone.settings.internal.storage.KeyValueStorage;
import midorum.melbone.settings.internal.storage.StorageHolder;
import midorum.melbone.settings.internal.storage.VersionableKeyValueStorage;
import midorum.melbone.settings.managment.SettingPropertyNaming;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.stream.Stream;

public class SettingsFactoryInternal implements AutoCloseable {

    private static final int VERSION = 1;
    private final Logger logger = LogManager.getLogger("settings");
    private final PropertiesProvider propertiesProvider;
    private final KeyValueStorage keyValueStorage;
    private final SettingsProvider settingsProvider;
    private final SettingStorage settingStorage;
    private final AccountStorage accountStorage;
    private final TaskStorage taskStorage;

    private SettingsFactoryInternal(final SettingPropertyNaming settingPropertyNaming, final boolean plainMode, final String propertyFileName) {
        this.propertiesProvider = new PropertiesProviderImpl(propertyFileName);
        this.keyValueStorage = openKeyValueStorage(settingPropertyNaming, plainMode);
        this.settingsProvider = new SettingsProviderImpl(keyValueStorage);
        this.settingStorage = new SettingStorageImpl(keyValueStorage);
        this.accountStorage = new AccountStorageImpl(keyValueStorage);
        this.taskStorage = new TaskStorageImpl();
        checkAllSettingsState();
    }

    public KeyValueStorage getKeyValueStorage() {
        return keyValueStorage;
    }

    public SettingStorage settingStorage() {
        return settingStorage;
    }

    public AccountStorage accountStorage() {
        return accountStorage;
    }

    public TaskStorage taskStorage() {
        return taskStorage;
    }

    public SettingsProvider settingsProvider() {
        return settingsProvider;
    }

    public PropertiesProvider propertiesProvider() {
        return propertiesProvider;
    }

    private KeyValueStorage openKeyValueStorage(final SettingPropertyNaming settingPropertyNaming, final boolean plainMode) {
        final StorageHolder storageHolder = new StorageHolder(propertiesProvider, settingPropertyNaming, plainMode);
        final VersionableKeyValueStorage storage = storageHolder.getStorage();
        final int storageVersion = storage.getVersion();
        if (storageVersion == VERSION)
            logger.info("settings storage version is {}", storageVersion);
        else if (storageVersion == 0 && storage.isEmpty())
            registerStorageAsNew(storage);
        else {
            storageHolder.close();
            throw new IllegalStateException("Settings storage version should be " + VERSION + " but is " + storageVersion);
        }
        return storage;
    }

    private void registerStorageAsNew(final VersionableKeyValueStorage storage) {
        storage.setVersion(VERSION);
        logger.info("registered a new storage with version {}", VERSION);
    }

    @Override
    public void close() throws Exception {
        keyValueStorage.close();
    }

    private void checkAllSettingsState() {
        if (!propertiesProvider.isModeSet("check_settings_on_start")) return;
        Stream.concat(Arrays.stream(SettingKeys.values()), Arrays.stream(StampKeys.values())).forEach(key ->
                settingStorage.read(key).ifPresentOrElse(o -> logger.info("{} is kept in storage", key),
                        () -> key.internal().defaultValue().ifPresentOrElse(o -> logger.info("{} used default value", key),
                                () -> logger.warn("{} not found in storage nor has default value", key))));
    }

    public static class Builder {


        private SettingPropertyNaming settingPropertyNaming;
        private boolean plainMode = false;
        private String propertyFileName;

        public Builder settingPropertyNaming(final SettingPropertyNaming settingPropertyNaming) {
            this.settingPropertyNaming = settingPropertyNaming;
            return this;
        }

        public Builder plainMode(final boolean plainMode) {
            this.plainMode = plainMode;
            return this;
        }

        public Builder propertyFileName(final String propertyFileName) {
            this.propertyFileName = propertyFileName;
            return this;
        }

        public SettingsFactoryInternal build() {
            return new SettingsFactoryInternal(Validator.checkNotNull(settingPropertyNaming)
                    .orDefault(new SettingPropertyNaming.Builder().build()), plainMode, propertyFileName);
        }

        public boolean isValid() {
            final SettingPropertyNaming settingPropertyNamingChecked = Validator.checkNotNull(settingPropertyNaming)
                    .orDefault(new SettingPropertyNaming.Builder().build());
            try (final StorageHolder storageHolder = new StorageHolder(new PropertiesProviderImpl(propertyFileName), settingPropertyNamingChecked)) {
                final VersionableKeyValueStorage storage = storageHolder.getStorage();
                return storage.getVersion() == VERSION;
            }
        }
    }
}
