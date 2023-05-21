package midorum.melbone.settings.internal.defining;

import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.struct.PointFloat;
import com.midorum.win32api.struct.PointInt;
import com.midorum.win32api.struct.PointLong;

import java.util.function.Predicate;

/**
 * predefined validators
 */
public enum SettingValidator {

    DEFAULT_PREDICATE(o -> true),
    INTEGER_POSITIVE_PREDICATE(o -> ((int) o) >= 0),
    FLOAT_POSITIVE_PREDICATE(o -> ((float) o) >= 0),
    LONG_POSITIVE_PREDICATE(o -> ((long) o) >= 0),
    FLOAT_RELATIVE_PREDICATE(o -> ((float) o) >= 0 && ((float) o) <= 1),
    POINT_INTEGER_POSITIVE_PREDICATE(o -> {
        PointInt p = (PointInt) o;
        return p.x() >= 0 && p.y() >= 0;
    }),
    POINT_LONG_POSITIVE_PREDICATE(o -> {
        PointLong p = (PointLong) o;
        return p.x() >= 0 && p.y() >= 0;
    }),
    POINT_FLOAT_POSITIVE_PREDICATE(o -> {
        PointFloat p = (PointFloat) o;
        return p.x() >= 0 && p.y() >= 0;
    }),
    POINT_FLOAT_RELATIVE_PREDICATE(o -> {
        PointFloat p = (PointFloat) o;
        return p.x() >= 0 && p.x() <= 1 && p.y() >= 0 && p.y() <= 1;
    }),
    WINDOW_DIMENSIONS_PREDICATE(o -> {
        Rectangle p = (Rectangle) o;
        return p.left() >= 0 && p.top() >= 0 && p.width() >= 0 && p.height() >= 0;
    });

    private final Predicate<Object> predicate;

    SettingValidator(final Predicate<Object> predicate) {
        this.predicate = predicate;
    }

    public Predicate<Object> predicate() {
        return predicate;
    }
}
