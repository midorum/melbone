package midorum.melbone.settings.managment;

import dma.validation.Validator;
import midorum.melbone.model.experimental.task.TaskStorage;
import midorum.melbone.model.persistence.AccountStorage;
import midorum.melbone.model.persistence.SettingStorage;
import midorum.melbone.model.settings.PropertiesProvider;
import midorum.melbone.model.settings.SettingsProvider;
import midorum.melbone.settings.internal.management.SettingsFactoryInternal;

@SuppressWarnings("ClassCanBeRecord")
public class SettingsFactory implements AutoCloseable {

    private final SettingsFactoryInternal settingsFactoryInternal;

    private SettingsFactory(final SettingsFactoryInternal settingsFactoryInternal) {
        this.settingsFactoryInternal = settingsFactoryInternal;
    }

    public SettingStorage settingStorage() {
        return settingsFactoryInternal.settingStorage();
    }

    public AccountStorage accountStorage() {
        return settingsFactoryInternal.accountStorage();
    }

    public TaskStorage taskStorage() {
        return settingsFactoryInternal.taskStorage();
    }

    public SettingsProvider settingsProvider() {
        return settingsFactoryInternal.settingsProvider();
    }

    public PropertiesProvider propertiesProvider(){
        return settingsFactoryInternal.propertiesProvider();
    }

    @Override
    public void close() throws Exception {
        settingsFactoryInternal.close();
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

        public SettingsFactory build() {
            return new SettingsFactory(getSettingsFactoryInternalBuilder().build());
        }

        public boolean isValid() {
            return getSettingsFactoryInternalBuilder().isValid();
        }

        private SettingsFactoryInternal.Builder getSettingsFactoryInternalBuilder() {
            return new SettingsFactoryInternal.Builder()
                    .settingPropertyNaming(Validator.checkNotNull(settingPropertyNaming)
                            .orDefault(new SettingPropertyNaming.Builder().build()))
                    .plainMode(plainMode)
                    .propertyFileName(propertyFileName);
        }
    }
}
