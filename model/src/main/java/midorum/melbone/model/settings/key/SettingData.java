package midorum.melbone.model.settings.key;

import midorum.melbone.model.persistence.StorageKey;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public interface SettingData {

    Class<?> type();

    String description();

    default String groupName() {
        return this.storageKey().name();
    }

    StorageKey storageKey();

    boolean checkValueType(Object value);

    Predicate<Object> validator();

    Function<String, Object> parser();

    SettingObtainWay obtainWay();

    Optional<Object> defaultValue();

    boolean isEnabled();



}
