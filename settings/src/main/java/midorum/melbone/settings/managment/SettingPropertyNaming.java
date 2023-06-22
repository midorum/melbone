package midorum.melbone.settings.managment;

import dma.validation.Validator;

@SuppressWarnings("ClassCanBeRecord")
public class SettingPropertyNaming {

    public static final String IN_MEMORY = "mem";
    public static final String STORAGE_FILE_PROPERTY_NAME = "storage";
    public static final String STORAGE_PASSWORD_PROPERTY_NAME = "storage.password";
    public static final String DATABASE_PROPERTY_NAME = "db";

    private final String storageFilePropertyName;
    private final String storagePasswordPropertyName;
    private final String databasePropertyName;
    private final boolean holdPassword;

    private SettingPropertyNaming(final String storageFilePropertyName, final String storagePasswordPropertyName, final String databasePropertyName, final boolean holdPassword) {
        this.storageFilePropertyName = storageFilePropertyName;
        this.storagePasswordPropertyName = storagePasswordPropertyName;
        this.databasePropertyName = databasePropertyName;
        this.holdPassword = holdPassword;
    }

    public String getStorageFilePropertyName() {
        return storageFilePropertyName;
    }

    public String getStoragePasswordPropertyName() {
        return storagePasswordPropertyName;
    }

    public String getDatabasePropertyName() {
        return databasePropertyName;
    }

    public boolean isInMemoryStorage() {
        return IN_MEMORY.equals(storageFilePropertyName);
    }

    public boolean isInMemoryDatabase() {
        return IN_MEMORY.equals(databasePropertyName);
    }

    public boolean holdPassword() {
        return holdPassword;
    }

    public static class Builder {
        private String storageFilePropertyName;
        private String storagePasswordPropertyName;
        private String databasePropertyName;
        private boolean holdPassword = false;

        public Builder storageFilePropertyName(final String storageFilePropertyName) {
            this.storageFilePropertyName = storageFilePropertyName;
            return this;
        }

        public Builder inMemoryStorage() {
            this.storageFilePropertyName = IN_MEMORY;
            return this;
        }

        public Builder storagePasswordPropertyName(final String storagePasswordPropertyName) {
            this.storagePasswordPropertyName = storagePasswordPropertyName;
            return this;
        }

        public Builder databasePropertyName(final String databasePropertyName) {
            this.databasePropertyName = databasePropertyName;
            return this;
        }

        public Builder inMemoryDatabase() {
            this.databasePropertyName = IN_MEMORY;
            return this;
        }

        public Builder holdPassword(final boolean holdPassword) {
            this.holdPassword = holdPassword;
            return this;
        }

        public SettingPropertyNaming build() {
            return new SettingPropertyNaming(Validator.checkNotNull(storageFilePropertyName).orDefault(STORAGE_FILE_PROPERTY_NAME),
                    Validator.checkNotNull(storagePasswordPropertyName).orDefault(STORAGE_PASSWORD_PROPERTY_NAME),
                    Validator.checkNotNull(databasePropertyName).orDefault(DATABASE_PROPERTY_NAME),
                    holdPassword);
        }

    }
}
