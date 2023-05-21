package midorum.melbone.settings.internal.setting;

import com.midorum.win32api.struct.PointInt;
import midorum.melbone.model.settings.key.SettingDataHolder;
import midorum.melbone.model.settings.key.WindowHolder;
import midorum.melbone.settings.internal.management.SettingsFactoryInternal;
import org.junit.jupiter.api.Assertions;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class SettingTester {

    private final SettingsFactoryInternal settingsFactory;
    private final SettingDataHolder setting;
    private Object[] normalValues;
    private Object[] wrongTypeValues;
    private Object[] invalidValues;
    private Supplier<Object> settingGetter;
    private ExtractorParameter[] extractorParameters;

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

    public SettingTester extractors(final ExtractorParameter... extractorParameters) {
        this.extractorParameters = extractorParameters;
        return this;
    }

    public void test() {
        Objects.requireNonNull(setting, "setting cannot be null");
        Objects.requireNonNull(normalValues, "normalValues cannot be null");
        Objects.requireNonNull(wrongTypeValues, "wrongTypeValues cannot be null");
        Objects.requireNonNull(invalidValues, "invalidValues cannot be null");
        Objects.requireNonNull(settingGetter, "settingGetter cannot be null");
        Objects.requireNonNull(extractorParameters, "extractors cannot be null");

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

        for (ExtractorParameter extractorParameter : extractorParameters) {
            new ExtractorTester(extractorParameter.windowHolder(), extractorParameter.point()).test();
        }
    }

    public record ExtractorParameter(WindowHolder windowHolder, PointInt point) {
    }

    private class ExtractorTester {
        private final WindowHolder windowHolder;
        private final PointInt point;

        public ExtractorTester(final WindowHolder windowHolder, final PointInt point) {
            this.windowHolder = windowHolder;
            this.point = point;
        }

        public void test() {
            final BiFunction<WindowHolder, PointInt, Object> extractor = setting.internal().extractor();
            assertNotNull(extractor);
            final Object result = extractor.apply(windowHolder, point);
            assertNotNull(result, "extractor result cannot be null");
            assertTrue(setting.internal().checkValueType(result), () -> "failed testing extractor result type: " + result + " (must be " + setting.internal().type() + " but is " + result.getClass() + " )");
            assertTrue(setting.internal().validator().test(result), () -> "failed validating extractor result: " + result);
        }
    }

}
