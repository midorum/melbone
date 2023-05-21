package midorum.melbone.model.settings.key;

public interface SettingDataHolder {

    String name();

    Class<?> getDeclaringClass();

    SettingData internal();
}
