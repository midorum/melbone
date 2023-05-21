package midorum.melbone.ui.util;

import midorum.melbone.model.settings.stamp.Stamp;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Objects;

public class StampMatcher extends TypeSafeMatcher<Stamp> {

    private final Stamp ethalon;

    private StampMatcher(final Stamp stamp) {
        Objects.requireNonNull(stamp);
        this.ethalon = stamp;
    }

    @Override
    protected boolean matchesSafely(final Stamp stamp) {
        if (stamp == null) return false;
        return Objects.equals(ethalon.key(), stamp.key()) &&
                Objects.equals(ethalon.description(), stamp.description()) &&
                (ethalon.wholeData().length == stamp.wholeData().length) &&
                (ethalon.firstLine().length == stamp.firstLine().length) &&
                Objects.equals(ethalon.location(), stamp.location()) &&
                Objects.equals(ethalon.windowRect(), stamp.windowRect()) &&
                Objects.equals(ethalon.windowClientRect(), stamp.windowClientRect()) &&
                Objects.equals(ethalon.windowClientToScreenRect(), stamp.windowClientToScreenRect());
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("equals to " + ethalon);
    }

    public static Matcher<Stamp> equalToStamp(final Stamp stamp) {
        return new StampMatcher(stamp);
    }
}
