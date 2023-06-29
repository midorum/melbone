package midorum.melbone.settings.internal.dto;

import com.midorum.win32api.facade.Rectangle;
import midorum.melbone.model.exception.CriticalErrorException;
import midorum.melbone.model.settings.stamp.Stamp;
import midorum.melbone.model.settings.key.StampKey;
import midorum.melbone.settings.StampKeys;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents stamp info
 */
public class StampImpl implements Stamp, Serializable {
    @Serial
    private static final long serialVersionUID = 0L;

    private transient StampKey key; //effectively final
    private final String description;
    private final int[] wholeData;
    private final int[] firstLine;
    private final Rectangle location;
    private final Rectangle windowRect;
    private final Rectangle windowClientRect;
    private final Rectangle windowClientToScreenRect;

    /**
     * @param key                      stamp key
     * @param description              stamp description
     * @param wholeData                stamp whole data
     * @param firstLine                stamp first line data for fast search
     * @param location                 stamp location inside screenshot
     * @param windowRect               target window rectangle
     * @param windowClientRect         target window client rectangle
     * @param windowClientToScreenRect target window client rectangle in screen coordinates
     */
    public StampImpl(StampKey key,
                     String description,
                     int[] wholeData,
                     int[] firstLine,
                     Rectangle location,
                     Rectangle windowRect,
                     Rectangle windowClientRect,
                     Rectangle windowClientToScreenRect) {
        this.key = key;
        this.description = description;
        this.wholeData = wholeData;
        this.firstLine = firstLine;
        this.location = location;
        this.windowRect = windowRect;
        this.windowClientRect = windowClientRect;
        this.windowClientToScreenRect = windowClientToScreenRect;
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        //could not serialize StampKey in standard way because it is instantiated as enum,
        // and we lose ability of remove enum keys if necessary that breaks deserialization
        out.writeObject(key.getDeclaringClass().getSimpleName());
        out.writeObject(key.name());
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        final String enumClassName = (String) in.readObject();
        final String enumValue = (String) in.readObject();
        //restoring key
        final Class<?>[] nestMembers = StampKeys.class.getNestMembers();
        if (nestMembers == null) throw new CriticalErrorException("Could not find any StampKey in StampKeys");
        final Optional<Class<?>> maybeEnumClass = Arrays.stream(nestMembers)
                .filter(nested -> nested.isEnum()
                        && nested.getSimpleName().equals(enumClassName)
                        && Arrays.stream(nested.getInterfaces()).anyMatch(aClass -> aClass.isAssignableFrom(StampKey.class)))
                .findFirst();
        if (maybeEnumClass.isEmpty())
            throw new CriticalErrorException("Could not find " + enumClassName + " in StampKeys");
        final Class<?> enumClass = maybeEnumClass.get();
        if (enumClass.isAssignableFrom(StampKeys.TargetLauncher.class)) {
            this.key = extractKeyForType(StampKeys.TargetLauncher.class, enumValue);
        } else if (enumClass.isAssignableFrom(StampKeys.TargetBaseApp.class)) {
            this.key = extractKeyForType(StampKeys.TargetBaseApp.class, enumValue);
        } else if (enumClass.isAssignableFrom(StampKeys.Noop.class)) {
            this.key = extractKeyForType(StampKeys.Noop.class, enumValue);
        } else {
            throw new CriticalErrorException("Could not find " + enumClassName + "." + enumValue + " in StampKeys");
        }
    }

    private <T extends StampKey> StampKey extractKeyForType(final Class<T> type, final String enumValue) {
        return Arrays.stream(type.getEnumConstants())
                .filter(constant -> constant.name().equals(enumValue))
                .findFirst()
                .map(StampKey.class::cast)
                .orElse(StampKeys.Noop.noop);
    }

    @Override
    public StampKey key() {
        return key;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public int[] wholeData() {
        return wholeData;
    }

    @Override
    public int[] firstLine() {
        return firstLine;
    }

    @Override
    public Rectangle location() {
        return location;
    }

    @Override
    public Rectangle windowRect() {
        return windowRect;
    }

    @Override
    public Rectangle windowClientRect() {
        return windowClientRect;
    }

    @Override
    public Rectangle windowClientToScreenRect() {
        return windowClientToScreenRect;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (StampImpl) obj;
        return Objects.equals(this.key, that.key) &&
                Objects.equals(this.description, that.description) &&
                Arrays.equals(this.wholeData, that.wholeData) &&
                Arrays.equals(this.firstLine, that.firstLine) &&
                Objects.equals(this.location, that.location) &&
                Objects.equals(this.windowRect, that.windowRect) &&
                Objects.equals(this.windowClientRect, that.windowClientRect) &&
                Objects.equals(this.windowClientToScreenRect, that.windowClientToScreenRect);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, description, Arrays.hashCode(wholeData), Arrays.hashCode(firstLine), location, windowRect, windowClientRect, windowClientToScreenRect);
    }

    @Override
    public String toString() {
        return "Stamp[" +
                "key=" + key + ", " +
                "description=" + description + ", " +
                "location=" + location + ", " +
                "windowRect=" + windowRect + ", " +
                "windowClientRect=" + windowClientRect + ", " +
                "windowClientToScreenRect=" + windowClientToScreenRect + ']';
    }

    public String firstLineToString() {
        return Arrays.toString(firstLine);
    }

    public String wholeDataToString() {
        return Arrays.toString(wholeData);
    }

}
