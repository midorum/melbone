package midorum.melbone.window.internal.util;

import com.midorum.win32api.facade.IWindow;
import com.midorum.win32api.facade.IKeyboard;
import midorum.melbone.model.exception.CannotGetUserInputException;
import midorum.melbone.window.internal.common.CommonWindowService;
import midorum.melbone.window.internal.common.ForegroundWindow;
import midorum.melbone.window.internal.common.Mouse;

import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ForegroundWindowMocked {

    private final ForegroundWindow foregroundWindow;

    private ForegroundWindowMocked(final CommonWindowService commonWindowService, final IWindow window) throws CannotGetUserInputException, InterruptedException {
        final ForegroundWindow foregroundWindowMock = mock(ForegroundWindow.class);
        final CommonWindowService.ForegroundWindowSupplier foregroundWindowSupplier = mock(CommonWindowService.ForegroundWindowSupplier.class);
        when(commonWindowService.bringForeground(window)).thenReturn(foregroundWindowSupplier);
        doAnswer(invocation -> {
            final CommonWindowService.ForegroundWindowSupplier.ForegroundWindowConsumer consumer = invocation.getArgument(0);
            consumer.accept(foregroundWindowMock);
            return null;
        }).when(foregroundWindowSupplier).andDo(any(CommonWindowService.ForegroundWindowSupplier.ForegroundWindowConsumer.class));
        when(foregroundWindowSupplier.andDo(any(CommonWindowService.ForegroundWindowSupplier.ForegroundWindowFunction.class))).thenAnswer(invocation -> {
            final CommonWindowService.ForegroundWindowSupplier.ForegroundWindowFunction<ForegroundWindow> function = invocation.getArgument(0);
            return function.apply(foregroundWindowMock);
        });
        this.foregroundWindow = foregroundWindowMock;
    }

    public ForegroundWindowMocked returnsMouse(final Mouse mouse) throws InterruptedException, CannotGetUserInputException {
        when(foregroundWindow.getMouse()).thenReturn(mouse);
        return this;
    }

    public ForegroundWindowMocked returnsKeyboard(final IKeyboard keyboard) throws InterruptedException, CannotGetUserInputException {
        when(foregroundWindow.getKeyboard()).thenReturn(keyboard);
        return this;
    }

    public ForegroundWindowMocked stateIs(final ForegroundWindow.StateWaiting state) {
        when(foregroundWindow.waiting()).thenReturn(state);
        return this;
    }

    public ForegroundWindowMocked windowStatesAre(final ForegroundWindow.StateWaiting... states) {
        MockitoUtil.INSTANCE.mockReturnVararg(foregroundWindow.waiting(), List.of(states));
        return this;
    }

    public ForegroundWindowMocked throwsWhenAskedMouse(final Throwable throwable) throws CannotGetUserInputException, InterruptedException {
        when(foregroundWindow.getMouse()).thenThrow(throwable);
        return this;
    }

    public ForegroundWindowMocked throwsWhenAskedKeyboard(final Throwable throwable) throws CannotGetUserInputException, InterruptedException {
        when(foregroundWindow.getKeyboard()).thenThrow(throwable);
        return this;
    }

    public static class Builder {

        private CommonWindowService commonWindowService;

        public Builder withCommonWindowService(final CommonWindowService commonWindowService) {
            this.commonWindowService = commonWindowService;
            return this;
        }

        public ForegroundWindowMocked getForegroundWindowFor(final IWindow window) throws CannotGetUserInputException, InterruptedException {

            return new ForegroundWindowMocked(Objects.requireNonNull(commonWindowService), Objects.requireNonNull(window));
        }
    }

}
