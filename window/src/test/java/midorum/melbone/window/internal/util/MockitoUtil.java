package midorum.melbone.window.internal.util;

import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;

import java.util.List;

public class MockitoUtil {

    public static final MockitoUtil INSTANCE = new MockitoUtil();

    private MockitoUtil() {
    }

    public <T> void mockReturnVararg(final T methodCall, final List<T> results) {
        assert results.size() > 0;
        OngoingStubbing<T> ongoingStubbing = Mockito.when(methodCall);
        for (T t : results) {
            ongoingStubbing = ongoingStubbing.thenReturn(t);
        }
    }
}