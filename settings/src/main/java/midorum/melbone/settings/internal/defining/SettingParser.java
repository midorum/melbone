package midorum.melbone.settings.internal.defining;

import com.midorum.win32api.struct.PointFloat;
import com.midorum.win32api.struct.PointInt;
import com.midorum.win32api.struct.PointLong;
import midorum.melbone.model.dto.KeyShortcut;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum SettingParser {
    NOOP_PARSER(s -> {
        throw new UnsupportedOperationException("Cannot parse \"" + s + "\". Parser is not defined.");
    }),
    STRING_PARSER(s -> s),
    INTEGER_PARSER(Integer::valueOf),
    LONG_PARSER(Long::valueOf),
    FLOAT_PARSER(Float::valueOf),
    BOOLEAN_PARSER(Boolean::valueOf),
    POINT_INT_PARSER(SettingParser::parsePointInt),
    POINT_LONG_PARSER(SettingParser::parsePointLong),
    POINT_FLOAT_PARSER(SettingParser::parsePointFloat),
    KEY_SHORTCUT_PARSER(KeyShortcut::valueOf);

    static final Pattern PATTERN_POINT_FLOAT = Pattern.compile("^.*\\[x=(?<x>\\d+\\.\\d+),\\s*y=(?<y>\\d+\\.\\d+)]$");
    static final Pattern PATTERN_POINT_INTEGER = Pattern.compile("^.*\\[x=(?<x>\\d+),\\s*y=(?<y>\\d+)]$");

    private final Function<String, Object> parser;

    SettingParser(final Function<String, Object> parser) {
        this.parser = parser;
    }

    public Function<String, Object> parser() {
        return parser;
    }

    static PointInt parsePointInt(final String data) {
        final Matcher matcher = PATTERN_POINT_INTEGER.matcher(data);
        if (!matcher.matches()) {
            throw new IllegalStateException("\"" + data + "\" does not match PointInt pattern");
        }
        return new PointInt(
                Integer.parseInt(matcher.group("x")),
                Integer.parseInt(matcher.group("y")));
    }

    static PointLong parsePointLong(final String data) {
        final Matcher matcher = PATTERN_POINT_INTEGER.matcher(data);
        if (!matcher.matches()) {
            throw new IllegalStateException("\"" + data + "\" does not match PointLong pattern");
        }
        return new PointLong(
                Long.parseLong(matcher.group("x")),
                Long.parseLong(matcher.group("y")));
    }

    static PointFloat parsePointFloat(final String data) {
        final Matcher matcher = PATTERN_POINT_FLOAT.matcher(data);
        if (!matcher.matches()) {
            throw new IllegalStateException("\"" + data + "\" does not match PointFloat pattern");
        }
        return new PointFloat(
                Float.parseFloat(matcher.group("x")),
                Float.parseFloat(matcher.group("y")));
    }

    public static SettingParser forType(Class<?> type) {
        if (type.isAssignableFrom(String.class)) return STRING_PARSER;
        if (type.isAssignableFrom(Integer.class)) return INTEGER_PARSER;
        if (type.isAssignableFrom(Long.class)) return LONG_PARSER;
        if (type.isAssignableFrom(Float.class)) return FLOAT_PARSER;
        if (type.isAssignableFrom(Boolean.class)) return BOOLEAN_PARSER;
        if (type.isAssignableFrom(PointInt.class)) return POINT_INT_PARSER;
        if (type.isAssignableFrom(PointLong.class)) return POINT_LONG_PARSER;
        if (type.isAssignableFrom(PointFloat.class)) return POINT_FLOAT_PARSER;
        if (type.isAssignableFrom(KeyShortcut.class)) return KEY_SHORTCUT_PARSER;
        return NOOP_PARSER;
    }
}
