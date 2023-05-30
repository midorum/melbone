package midorum.melbone.settings.internal.setting;

import midorum.melbone.model.settings.key.SettingDataHolder;
import midorum.melbone.settings.internal.management.SettingsFactoryInternal;
import org.junit.jupiter.api.Assertions;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class SettingTester {

    private final SettingsFactoryInternal settingsFactory;
    private final SettingDataHolder setting;
    private Object[] normalValues;
    private Object[] wrongTypeValues;
    private Object[] invalidValues;
    private Supplier<Object> settingGetter;
    private Object[] valuesToExtract;

    public SettingTester(final SettingsFactoryInternal settingsFactory, final SettingDataHolder setting) {
        this.settingsFactory = settingsFactory;
        this.setting = setting;
    }

    public SettingTester normalValues(final Object... normalValues) {
        this.normalValues = normalValues;
        return this;
    }

    public SettingTester wrongTypeValues(final Object... wrongTypeValues) {
        this.wrongTypeValues = wrongTypeValues;
        return this;
    }

    public SettingTester invalidValues(final Object... invalidValues) {
        this.invalidValues = invalidValues;
        return this;
    }

    public SettingTester settingGetter(final Supplier<Object> settingGetter) {
        this.settingGetter = settingGetter;
        return this;
    }

    public SettingTester extractFrom(final Object... valuesToExtract) {
        this.valuesToExtract = valuesToExtract;
        return this;
    }

    public void test() {
        Objects.requireNonNull(setting, "setting cannot be null");
        Objects.requireNonNull(normalValues, "normalValues cannot be null");
        Objects.requireNonNull(wrongTypeValues, "wrongTypeValues cannot be null");
        Objects.requireNonNull(invalidValues, "invalidValues cannot be null");
        Objects.requireNonNull(settingGetter, "settingGetter cannot be null");
        Objects.requireNonNull(valuesToExtract, "valuesToExtract cannot be null");

        System.out.println("testing setting: " + setting);

        for (Object value : normalValues) {
            //setup setting
            Assertions.assertDoesNotThrow(() -> settingsFactory.settingStorage().write(setting, value), () -> "failed storing normal value: " + value);
            //get and verify setting
            assertEquals(value, settingGetter.get(), () -> "failed getting normal value: " + value);
        }

        for (Object value : wrongTypeValues) {
            assertThrows(IllegalArgumentException.class, () -> settingsFactory.settingStorage().write(setting, value), () -> "failed testing wrong type value: " + value);
        }

        for (Object value : invalidValues) {
            assertThrows(IllegalArgumentException.class, () -> settingsFactory.settingStorage().write(setting, value), () -> "failed testing invalid value: " + value);
        }

        final Function<Object, Object> extractor = setting.internal().obtainWay().extractor();
        assertNotNull(extractor);
        for (Object value : valuesToExtract) {
            final Object result = extractor.apply(value);
            assertNotNull(result, "extractor result cannot be null");
            assertTrue(setting.internal().checkValueType(result), () -> "failed testing extractor result type: " + result + " (must be " + setting.internal().type() + " but is " + result.getClass() + " )");
            assertTrue(setting.internal().validator().test(result), () -> "failed validating extractor result: " + result);
        }
    }

}
