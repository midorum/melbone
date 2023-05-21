package midorum.melbone.model.settings;

public interface PropertiesProvider {

    String mode();

    boolean isModeSet(String mode);

    String appName();

    String storageName(String storageFilePropertyName);
}
