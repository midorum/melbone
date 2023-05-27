package midorum.melbone.settings.internal.defining;

import dma.validation.Validator;
import midorum.melbone.model.persistence.StorageKey;
import midorum.melbone.model.settings.key.SettingData;
import midorum.melbone.model.settings.key.SettingObtainWay;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class SettingDataImpl implements SettingData {

    private final Class<?> type;
    private final String description;
    private final StorageKey storageKey;
    private final Predicate<Object> validator;
    private final Function<String, Object> parser;
    private final SettingObtainWay obtainWay;
    private final Object defaultValue;
    private final boolean isEnabled;

    private SettingDataImpl(final Class<?> type,
                            final String description,
                            final StorageKey storageKey,
                            final Predicate<Object> validator,
                            final Function<String, Object> parser,
                            final SettingObtainWay obtainWay,
                            final Object defaultValue,
                            final boolean isEnabled) {
        this.type = type;
        this.description = description;
        this.storageKey = storageKey;
        this.validator = validator;
        this.parser = parser;
        this.obtainWay = obtainWay;
        this.defaultValue = defaultValue;
        this.isEnabled = isEnabled;
    }

    @Override
    public Class<?> type() {
        return type;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public StorageKey storageKey() {
        return storageKey;
    }

    @Override
    public boolean checkValueType(final Object value) {
        return type.isInstance(value);
    }

    @Override
    public Predicate<Object> validator() {
        return validator;
    }

    @Override
    public Function<String, Object> parser() {
        return parser;
    }

    @Override
    public SettingObtainWay obtainWay() {
        return obtainWay;
    }

    @Override
    public Optional<Object> defaultValue() {
        return Optional.ofNullable(defaultValue);
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    public static class Builder {

        private Class<?> type;
        private String description;
        private StorageKey storageKey;
        private Predicate<Object> validator;
        private Function<String, Object> parser;
        private SettingObtainWay obtainWay;
        private Object defaultValue;
        private boolean isEnabled = true;

        public Builder type(final Class<?> type) {
            this.type = type;
            return this;
        }

        public Builder description(final String description) {
            this.description = description;
            return this;
        }

        public Builder storageKey(final StorageKey storageKey) {
            this.storageKey = storageKey;
            return this;
        }

        public Builder validator(final Predicate<Object> validator) {
            this.validator = validator;
            return this;
        }

        public Builder parser(final Function<String, Object> parser) {
            this.parser = parser;
            return this;
        }

        public Builder obtainWay(final SettingObtainWay obtainWay) {
            this.obtainWay = obtainWay;
            return this;
        }

        public Builder defaultValue(final Object defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder isEnabled(final boolean enabled) {
            isEnabled = enabled;
            return this;
        }

        private void validate(Object value, Class<?> type, Predicate<Object> validator) {
            Validator.check(value, type::isInstance)
                    .orThrow(() -> new IllegalArgumentException("value \"" + value + "\" must be of " + type + " but is " + value.getClass()));
            Validator.check(value, validator)
                    .orThrow(() -> new IllegalArgumentException("value " + value + " is not valid"));
        }

        public SettingData build() {
            final Class<?> checkedType = Validator.checkNotNull(this.type).orThrowForSymbol("type");
            final Predicate<Object> checkedValidator = Validator.checkNotNull(validator).orDefault(SettingValidator.DEFAULT_PREDICATE.predicate());
            if(defaultValue != null) validate(defaultValue, checkedType, checkedValidator);
            return new SettingDataImpl(
                    checkedType,
                    Validator.checkNotNull(description).andCheckNot(String::isBlank).orThrow("setting description must not be null or blank"),
                    Validator.checkNotNull(storageKey).orThrowForSymbol("storageKey"),
                    checkedValidator,
                    Validator.checkNotNull(parser).orDefault(SettingParser.forType(checkedType).parser()),
                    Validator.checkNotNull(obtainWay).orDefault(SettingObtainWays.insertManually),
                    defaultValue,
                    isEnabled
            );
        }

    }
}
