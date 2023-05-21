package midorum.melbone.settings.internal.storage;

import dma.validation.Validator;
import midorum.melbone.model.settings.PropertiesProvider;
import midorum.melbone.settings.internal.storage.vendor.H2KeyValueStorage;
import midorum.melbone.settings.managment.SettingPropertyNaming;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StorageHolder implements AutoCloseable {

    private final Logger logger = LogManager.getLogger("settings");
    private final PropertiesProvider propertiesProvider;
    private final VersionableKeyValueStorage store;
    private final Runnable cleanResourceAction;
    private final Thread shutdownHook;

    public StorageHolder(final PropertiesProvider propertiesProvider, final SettingPropertyNaming settingPropertyNaming, final boolean plainMode) {
        this.propertiesProvider = propertiesProvider;
        this.store = settingPropertyNaming.isInMemoryStorage()
                ? openInMemoryKeyValueStorage(propertiesProvider)
                : plainMode
                ? openFileBasedKeyValueStorageInPlainMode(propertiesProvider, settingPropertyNaming)
                : openFileBasedKeyValueStorage(propertiesProvider, settingPropertyNaming);
        this.cleanResourceAction = () -> {
            logger.info("releasing resources");
            try {
                store.close();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        };
        this.shutdownHook = new Thread(cleanResourceAction);
        setShutdownHook();
    }

    public StorageHolder(final PropertiesProvider propertiesProvider, final SettingPropertyNaming settingPropertyNaming) {
        this(propertiesProvider, settingPropertyNaming, false);
    }

    @Override
    public void close() {
        cleanResourceAction.run();
        removeShutdownHook();
    }

    private VersionableKeyValueStorage openFileBasedKeyValueStorage(final PropertiesProvider propertiesProvider, final SettingPropertyNaming settingPropertyNaming) {
        return new H2KeyValueStorage(obtainStorageFileName(settingPropertyNaming), obtainStoragePassword(settingPropertyNaming), propertiesProvider);
    }

    private VersionableKeyValueStorage openFileBasedKeyValueStorageInPlainMode(final PropertiesProvider propertiesProvider, final SettingPropertyNaming settingPropertyNaming) {
        return new H2KeyValueStorage(obtainStorageFileName(settingPropertyNaming), propertiesProvider);
    }

    private VersionableKeyValueStorage openInMemoryKeyValueStorage(final PropertiesProvider propertiesProvider) {
        return new H2KeyValueStorage(propertiesProvider);
    }

    private String obtainStorageFileName(final SettingPropertyNaming settingPropertyNaming) {
        final String storageFilePropertyName = settingPropertyNaming.getStorageFilePropertyName();
        return Validator.checkNotNull(System.getProperty(storageFilePropertyName))
                .orDefault(() -> Validator.checkNotNull(propertiesProvider.storageName(storageFilePropertyName))
                        .orThrow("You must provide storage file name"));
    }

    private char[] obtainStoragePassword(final SettingPropertyNaming settingPropertyNaming) {
        final String storagePasswordPropertyName = settingPropertyNaming.getStoragePasswordPropertyName();
        final char[] password = Validator.checkNotNull(System.getProperty(storagePasswordPropertyName))
                .andMap(String::toCharArray)
                .orDefault(() -> {
                    final char[] enteredPassword = System.console() != null ? System.console().readPassword("Enter password: ") : null;
                    if (enteredPassword == null)
                        throw new IllegalArgumentException("You must provide storage password");
                    if (settingPropertyNaming.holdPassword())
                        System.setProperty(storagePasswordPropertyName, String.valueOf(enteredPassword));
                    return enteredPassword;
                });
        if (!settingPropertyNaming.holdPassword())
            System.clearProperty(storagePasswordPropertyName);
        return password;
    }

    private void setShutdownHook() {
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        logger.debug("shutdown hook added");
    }

    private void removeShutdownHook() {
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
        logger.debug("shutdown hook removed");
    }

    public VersionableKeyValueStorage getStorage() {
        return store;
    }
}
