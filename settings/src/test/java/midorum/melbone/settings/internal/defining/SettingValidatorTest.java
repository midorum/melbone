package midorum.melbone.settings.internal.defining;

import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.struct.PointFloat;
import com.midorum.win32api.struct.PointInt;
import com.midorum.win32api.struct.PointLong;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SettingValidatorTest {

    @Test
    void testDefault() {
        new PredicateChecker(SettingValidator.DEFAULT_PREDICATE)
                .mustBeTrue(0L, 1L, 100L, -1L, -100L, 1.0, 1.0f, 1, "text", new Object());
    }

    @Test
    void positiveInteger() {
        new PredicateChecker(SettingValidator.INTEGER_POSITIVE_PREDICATE)
                .mustBeTrue(0, 1, 100)
                .mustBeFalse(-1, -100)
                .mustThrow(1.0, 1.0f, 1L, "text", new Object());
    }

    @Test
    void positiveLong() {
        new PredicateChecker(SettingValidator.LONG_POSITIVE_PREDICATE)
                .mustBeTrue(0L, 1L, 100L)
                .mustBeFalse(-1L, -100L)
                .mustThrow(1.0, 1.0f, 1, "text", new Object());
    }

    @Test
    void positiveFloat() {
        new PredicateChecker(SettingValidator.FLOAT_POSITIVE_PREDICATE)
                .mustBeTrue(0.0F, 1.1F, 100.2F)
                .mustBeFalse(-1.8F, -100.5F)
                .mustThrow(1.0, 1L, 1, "text", new Object());
    }

    @Test
    void relativeFloat() {
        new PredicateChecker(SettingValidator.FLOAT_RELATIVE_PREDICATE)
                .mustBeTrue(0.0F, 0.5F, 1.0F)
                .mustBeFalse(-1.0F, -0.7F, 1.0001F, 100.8F, -1.0001F, -100.5F)
                .mustThrow(1.0, 1L, 1, "text", new Object());
    }

    @Test
    void pointIntegerPositive() {
        new PredicateChecker(SettingValidator.POINT_INTEGER_POSITIVE_PREDICATE)
                .mustBeTrue(new PointInt(0, 0), new PointInt(1, 0), new PointInt(0, 1), new PointInt(1, 1), new PointInt(500, 250))
                .mustBeFalse(new PointInt(-1, 0), new PointInt(0, -1))
                .mustThrow(1.0, 1L, 1, 1.0F, "text", new Object());
    }

    @Test
    void pointLongPositive() {
        new PredicateChecker(SettingValidator.POINT_LONG_POSITIVE_PREDICATE)
                .mustBeTrue(new PointLong(0, 0), new PointLong(1, 0), new PointLong(0, 1), new PointLong(1, 1), new PointLong(500, 250))
                .mustBeFalse(new PointLong(-1, 0), new PointLong(0, -1))
                .mustThrow(1.0, 1L, 1, 1.0F, "text", new Object());
    }

    @Test
    void pointFloatPositive() {
        new PredicateChecker(SettingValidator.POINT_FLOAT_POSITIVE_PREDICATE)
                .mustBeTrue(new PointFloat(0, 0), new PointFloat(1, 0), new PointFloat(0, 1), new PointFloat(1, 1), new PointFloat(500, 250))
                .mustBeFalse(new PointFloat(-1, 0), new PointFloat(0, -1))
                .mustThrow(1.0, 1L, 1, 1.0F, "text", new Object());
    }

    @Test
    void pointFloatRelative() {
        new PredicateChecker(SettingValidator.POINT_FLOAT_RELATIVE_PREDICATE)
                .mustBeTrue(new PointFloat(0, 0), new PointFloat(1, 0), new PointFloat(0, 1), new PointFloat(1, 1))
                .mustBeFalse(new PointFloat(-1, 0), new PointFloat(0, -1), new PointFloat(500, 250))
                .mustThrow(1.0, 1L, 1, 1.0F, "text", new Object());
    }

    @Test
    void windowDimensions() {
        new PredicateChecker(SettingValidator.WINDOW_DIMENSIONS_PREDICATE)
                .mustBeTrue(new Rectangle(0, 0, 0, 0), new Rectangle(1, 2, 3, 4))
                .mustBeFalse(new Rectangle(-1, 0, 0, 0), new Rectangle(0, -1, 0, 0), new Rectangle(1, 2, 0, 1))
                .mustThrow(1.0, 1L, 1, 1.0F, "text", new Object());
    }

    private static class PredicateChecker {

        private final SettingValidator validator;

        public PredicateChecker(final SettingValidator validator) {
            this.validator = validator;
        }

        public PredicateChecker mustBeTrue(Object o, Object... oo) {
            Stream.concat(Stream.of(o), Stream.of(oo)).forEach(obj -> assertThat("value " + obj + " must give true result with " + validator,
                    validator.predicate().test(obj), is(true)));
            return this;
        }

        public PredicateChecker mustBeFalse(Object o, Object... oo) {
            Stream.concat(Stream.of(o), Stream.of(oo)).forEach(obj -> assertThat("value " + obj + " must give false result with " + validator,
                    validator.predicate().test(obj), is(false)));
            return this;
        }

        public PredicateChecker mustThrow(Object o, Object... oo) {
            Stream.concat(Stream.of(o), Stream.of(oo)).forEach(obj -> assertThrows(ClassCastException.class, () -> validator.predicate().test(obj),
                    "value " + obj + " must throw ClassCastException with " + validator));
            return this;
        }

    }
}