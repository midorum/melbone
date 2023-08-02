package midorum.melbone.window.internal.common;

import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.facade.*;
import com.midorum.win32api.facade.exception.Win32ApiException;
import com.midorum.win32api.struct.PointFloat;
import com.midorum.win32api.win32.IWinUser;
import midorum.melbone.model.exception.CannotGetUserInputException;
import midorum.melbone.model.settings.setting.ApplicationSettings;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.model.settings.stamp.Stamp;
import midorum.melbone.settings.StampKeys;
import midorum.melbone.window.internal.util.MockitoUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ForegroundWindowTest {

    public static final String SKIPPED_TOPMOST_PROCESS_NAME = "skipped_topmost.exe";
    public static final String TOPMOST_PROCESS_NAME = "topmost.exe";
    private final Settings settings = mock(Settings.class);
    private final ApplicationSettings applicationSettings = mock(ApplicationSettings.class);
    private final Win32System win32System = mock(Win32System.class);
    private final StampValidator stampValidator = mock(StampValidator.class);

    @BeforeEach
    void beforeEach() {
        when(settings.application()).thenReturn(applicationSettings);
        when(applicationSettings.bringWindowForegroundTimeout()).thenReturn(100);
        when(applicationSettings.bringWindowForegroundDelay()).thenReturn(10);
        when(applicationSettings.stampDeviation()).thenReturn(0);
        when(applicationSettings.overlappingWindowsToSkip()).thenReturn(new String[]{});
        when(applicationSettings.overlappingWindowsToClose()).thenReturn(new String[]{});
    }

    @Test
    void getMouse_cannotGetUserInput() throws Win32ApiException {
        final Rectangle windowRectangle = new Rectangle(0, 0, 300, 200);
        final IMouse nativeMouse = mock(IMouse.class);
        final IKeyboard nativeKeyboard = mock(IKeyboard.class);
        final IWindow nativeWindow = getWindowMock(windowRectangle, nativeMouse, nativeKeyboard);

        windowHasNotUserInput(nativeWindow);
        assertThrows(CannotGetUserInputException.class, new ForegroundWindow(nativeWindow, settings, win32System, stampValidator)::getMouse);

        verify(stampValidator).takeAndSaveWholeScreenShot(anyString());
    }

    @Test
    void getMouse() throws CannotGetUserInputException, InterruptedException, Win32ApiException {
        final float speedFactor = 1f;
        when(applicationSettings.speedFactor()).thenReturn(speedFactor);
        final PointFloat point = new PointFloat(-1, -1);
        final Rectangle windowRectangle = new Rectangle(0, 0, 300, 200);
        final IMouse nativeMouse = mock(IMouse.class);
        when(nativeMouse.move(any(PointFloat.class))).thenReturn(nativeMouse);
        final IKeyboard nativeKeyboard = mock(IKeyboard.class);
        final IWindow nativeWindow = getWindowMock(windowRectangle, nativeMouse, nativeKeyboard);
        when(nativeWindow.getWindowMouse(speedFactor)).thenReturn(nativeMouse);

        windowHasUserInput(nativeWindow);
        noAnyTopWindows();
        noAnyAboveWindows(nativeWindow);
        final Mouse mouse = new ForegroundWindow(nativeWindow, settings, win32System, stampValidator).getMouse();
        assertNotNull(mouse);
        mouse.clickAtPoint(point);

        final InOrder inOrderMouse = inOrder(nativeMouse);
        inOrderMouse.verify(nativeMouse, atLeastOnce()).move(point);
        inOrderMouse.verify(nativeMouse).leftClick();
    }

    @Test
    void getKeyboard_cannotGetUserInput() throws Win32ApiException {
        final Rectangle windowRectangle = new Rectangle(0, 0, 300, 200);
        final IMouse nativeMouse = mock(IMouse.class);
        final IKeyboard nativeKeyboard = mock(IKeyboard.class);
        final IWindow nativeWindow = getWindowMock(windowRectangle, nativeMouse, nativeKeyboard);

        windowHasNotUserInput(nativeWindow);
        assertThrows(CannotGetUserInputException.class, new ForegroundWindow(nativeWindow, settings, win32System, stampValidator)::getKeyboard);

        verify(stampValidator).takeAndSaveWholeScreenShot(anyString());
    }

    @Test
    void getKeyboard() throws CannotGetUserInputException, InterruptedException, Win32ApiException {
        final Rectangle windowRectangle = new Rectangle(0, 0, 300, 200);
        final IMouse nativeMouse = mock(IMouse.class);
        final IKeyboard nativeKeyboard = mock(IKeyboard.class);
        final IWindow nativeWindow = getWindowMock(windowRectangle, nativeMouse, nativeKeyboard);
        when(nativeWindow.getKeyboard()).thenReturn(nativeKeyboard);

        windowHasUserInput(nativeWindow);
        noAnyTopWindows();
        noAnyAboveWindows(nativeWindow);
        final IKeyboard keyboard = new ForegroundWindow(nativeWindow, settings, win32System, stampValidator).getKeyboard();
        assertEquals(nativeKeyboard, keyboard);
    }

    @Test
    void waiting_cannotGetUserInput_noAnyOverlay() throws Win32ApiException {
        final Rectangle stampRectangle = new Rectangle(0, 0, 3, 2);
        final Rectangle windowRectangleInStamp = new Rectangle(0, 0, 300, 200);
        final Rectangle windowRectangle = new Rectangle(0, 0, 300, 200);
        final int[] stampWholeData = {};
        final Stamp stamp = getStampMock(stampRectangle, windowRectangleInStamp, stampWholeData);
        final IMouse nativeMouse = mock(IMouse.class);
        final IKeyboard nativeKeyboard = mock(IKeyboard.class);
        final IWindow nativeWindow = getWindowMock(windowRectangle, nativeMouse, nativeKeyboard);

        windowHasNotUserInput(nativeWindow);
        noAnyTopWindows();
        noAnyForegroundWindowFound();
        assertThrows(CannotGetUserInputException.class, () -> new ForegroundWindow(nativeWindow, settings, win32System, stampValidator).waiting()
                .withTimeout(100)
                .withDelay(10)
                .forStamp(stamp));

        verify(win32System, atLeastOnce()).getForegroundWindow();
        verify(stampValidator).takeAndSaveWholeScreenShot(anyString());
    }

    @Test
    void waiting_cannotGetUserInput_foundOverlayButCannotCloseIt() throws Win32ApiException {
        final Rectangle stampRectangle = new Rectangle(0, 0, 3, 2);
        final Rectangle windowRectangleInStamp = new Rectangle(0, 0, 300, 200);
        final Rectangle windowRectangle = new Rectangle(0, 0, 300, 200);
        final int[] stampWholeData = {};
        final Stamp stamp = getStampMock(stampRectangle, windowRectangleInStamp, stampWholeData);
        final IMouse nativeMouse = mock(IMouse.class);
        final IKeyboard nativeKeyboard = mock(IKeyboard.class);
        final IWindow nativeWindow = getWindowMock(windowRectangle, nativeMouse, nativeKeyboard);
        final IWindow overlay = getOverlayWindowMock();

        windowHasNotUserInput(nativeWindow);
        foundForegroundWindow(overlay);
        assertThrows(CannotGetUserInputException.class, () -> new ForegroundWindow(nativeWindow, settings, win32System, stampValidator).waiting()
                .withTimeout(100)
                .withDelay(10)
                .forStamp(stamp));

        verify(stampValidator, atLeastOnce()).takeAndSaveWholeScreenShot(anyString());
    }

    @Test
    void waiting_rectanglesDoNotMatch_withoutLogging() throws CannotGetUserInputException, InterruptedException, Win32ApiException {
        final Rectangle stampRectangle = new Rectangle(0, 0, 3, 2);
        final Rectangle windowRectangleInStamp = new Rectangle(0, 0, 300, 200);
        final Rectangle windowRectangle = new Rectangle(0, 0, 301, 200);
        final int[] stampWholeData = imageToArray(createImage(stampRectangle));
        final Stamp stamp = getStampMock(stampRectangle, windowRectangleInStamp, stampWholeData);
        final IMouse nativeMouse = mock(IMouse.class);
        final IKeyboard nativeKeyboard = mock(IKeyboard.class);
        final IWindow nativeWindow = getWindowMock(windowRectangle, nativeMouse, nativeKeyboard);

        windowHasUserInput(nativeWindow);
        noAnyTopWindows();
        noAnyAboveWindows(nativeWindow);
        final Optional<Stamp> result = new ForegroundWindow(nativeWindow, settings, win32System, stampValidator).waiting()
                .withTimeout(100)
                .withDelay(10)
                .withMousePosition(new PointFloat(0.5f, 0.7f))
                .forStamp(stamp);
        assertEquals(Optional.empty(), result);
        verify(stampValidator, never()).validateStamp(any(Stamp.class));
        verify(stampValidator, never()).logStampsMismatching(anyString(), any(Stamp.class));
    }

    @Test
    void waiting_stampNotFound_withLogging() throws CannotGetUserInputException, InterruptedException, Win32ApiException {
        final Rectangle stampRectangle = new Rectangle(0, 0, 3, 2);
        final Rectangle windowRectangleInStamp = new Rectangle(0, 0, 300, 200);
        final Rectangle windowRectangle = new Rectangle(0, 0, 300, 200);
        final int[] stampWholeData = imageToArray(createImage(stampRectangle));
        final Stamp stamp = getStampMock(stampRectangle, windowRectangleInStamp, stampWholeData);
        final IMouse nativeMouse = mock(IMouse.class);
        final IKeyboard nativeKeyboard = mock(IKeyboard.class);
        final IWindow nativeWindow = getWindowMock(windowRectangle, nativeMouse, nativeKeyboard);

        windowHasUserInput(nativeWindow);
        noAnyTopWindows();
        noAnyAboveWindows(nativeWindow);
        when(stampValidator.validateStamp(stamp)).thenReturn(Optional.empty());
        final Optional<Stamp> result = new ForegroundWindow(nativeWindow, settings, win32System, stampValidator).waiting()
                .withTimeout(100)
                .withDelay(10)
                .withMousePosition(new PointFloat(0.5f, 0.7f))
                .logFailedStamps()
                .forStamp(stamp);
        assertEquals(Optional.empty(), result);
        verify(stampValidator, atLeastOnce()).validateStamp(stamp);
        verify(stampValidator, atLeastOnce()).logStampsMismatching(anyString(), eq(stamp));
    }

    @Test
    void waiting_stampNotFound_withMarkedLogging() throws CannotGetUserInputException, InterruptedException, Win32ApiException {
        final Rectangle stampRectangle = new Rectangle(0, 0, 3, 2);
        final Rectangle windowRectangleInStamp = new Rectangle(0, 0, 300, 200);
        final Rectangle windowRectangle = new Rectangle(0, 0, 300, 200);
        final int[] stampWholeData = imageToArray(createImage(stampRectangle));
        final Stamp stamp = getStampMock(stampRectangle, windowRectangleInStamp, stampWholeData);
        final IMouse nativeMouse = mock(IMouse.class);
        final IKeyboard nativeKeyboard = mock(IKeyboard.class);
        final IWindow nativeWindow = getWindowMock(windowRectangle, nativeMouse, nativeKeyboard);
        final String logMarker = "test-marker";

        windowHasUserInput(nativeWindow);
        noAnyTopWindows();
        noAnyAboveWindows(nativeWindow);
        when(stampValidator.validateStamp(stamp)).thenReturn(Optional.empty());
        final Optional<Stamp> result = new ForegroundWindow(nativeWindow, settings, win32System, stampValidator).waiting()
                .withTimeout(100)
                .withDelay(10)
                .withMousePosition(new PointFloat(0.5f, 0.7f))
                .logFailedStampsWithMarker(logMarker)
                .forStamp(stamp);
        assertEquals(Optional.empty(), result);
        verify(stampValidator, atLeastOnce()).validateStamp(stamp);
        verify(stampValidator, atLeastOnce()).logStampsMismatching(eq(logMarker), eq(stamp));
    }

    @Test
    void waiting_foundButNotCloseOverlapTopmost() throws CannotGetUserInputException, InterruptedException, Win32ApiException {
        final Rectangle stampRectangle = new Rectangle(0, 0, 3, 2);
        final Rectangle windowRectangleInStamp = new Rectangle(0, 0, 300, 200);
        final Rectangle windowRectangle = new Rectangle(0, 0, 300, 200);
        final Rectangle topmostRectangle = new Rectangle(280, 178, 299, 199);
        final int[] stampWholeData = imageToArray(createImage(stampRectangle));
        final Stamp stamp = getStampMock(stampRectangle, windowRectangleInStamp, stampWholeData);
        final IMouse nativeMouse = mock(IMouse.class);
        final IKeyboard nativeKeyboard = mock(IKeyboard.class);
        final IWindow nativeWindow = getWindowMock(windowRectangle, nativeMouse, nativeKeyboard);
        final IWindow topmost = getTopmostWindowMock(topmostRectangle, TOPMOST_PROCESS_NAME);

        windowHasUserInput(nativeWindow);
        noAnyTopWindows();
        noAnyAboveWindows(nativeWindow);
        when(applicationSettings.closeOverlappingWindows()).thenReturn(false);
        when(win32System.listAllWindows()).thenReturn(List.of(nativeWindow, topmost));
        when(stampValidator.validateStamp(stamp)).thenReturn(Optional.of(stamp));
        final Optional<Stamp> result = new ForegroundWindow(nativeWindow, settings, win32System, stampValidator).waiting()
                .withTimeout(100)
                .withDelay(10)
                .withMousePosition(new PointFloat(0.5f, 0.7f))
                .logFailedStamps()
                .forStamp(stamp);
        assertEquals(Optional.of(stamp), result);
        verify(topmost, never()).close();
    }

    @Test
    void waiting_foundButNotAllowedToCloseOverlapTopmost() throws CannotGetUserInputException, InterruptedException, Win32ApiException {
        final Rectangle stampRectangle = new Rectangle(0, 0, 3, 2);
        final Rectangle windowRectangleInStamp = new Rectangle(0, 0, 300, 200);
        final Rectangle windowRectangle = new Rectangle(0, 0, 300, 200);
        final Rectangle topmostRectangle = new Rectangle(280, 178, 299, 199);
        final int[] stampWholeData = imageToArray(createImage(stampRectangle));
        final Stamp stamp = getStampMock(stampRectangle, windowRectangleInStamp, stampWholeData);
        final IMouse nativeMouse = mock(IMouse.class);
        final IKeyboard nativeKeyboard = mock(IKeyboard.class);
        final IWindow nativeWindow = getWindowMock(windowRectangle, nativeMouse, nativeKeyboard);
        final IWindow topmost = getTopmostWindowMock(topmostRectangle, TOPMOST_PROCESS_NAME);

        windowHasUserInput(nativeWindow);
        noAnyTopWindows();
        noAnyAboveWindows(nativeWindow);
        when(applicationSettings.closeOverlappingWindows()).thenReturn(true);
        when(win32System.listAllWindows()).thenReturn(List.of(nativeWindow, topmost));
        when(stampValidator.validateStamp(stamp)).thenReturn(Optional.of(stamp));
        final Optional<Stamp> result = new ForegroundWindow(nativeWindow, settings, win32System, stampValidator).waiting()
                .withTimeout(100)
                .withDelay(10)
                .withMousePosition(new PointFloat(0.5f, 0.7f))
                .logFailedStamps()
                .forStamp(stamp);
        assertEquals(Optional.of(stamp), result);
        verify(topmost, never()).close();
    }

    @Test
    void waiting_foundNotOverlapTopmost() throws CannotGetUserInputException, InterruptedException, Win32ApiException {
        final Rectangle stampRectangle = new Rectangle(0, 0, 3, 2);
        final Rectangle windowRectangleInStamp = new Rectangle(0, 0, 300, 200);
        final Rectangle windowRectangle = new Rectangle(0, 0, 300, 200);
        final Rectangle topmostRectangle = new Rectangle(301, 178, 311, 199);
        final int[] stampWholeData = imageToArray(createImage(stampRectangle));
        final Stamp stamp = getStampMock(stampRectangle, windowRectangleInStamp, stampWholeData);
        final IMouse nativeMouse = mock(IMouse.class);
        final IKeyboard nativeKeyboard = mock(IKeyboard.class);
        final IWindow nativeWindow = getWindowMock(windowRectangle, nativeMouse, nativeKeyboard);
        final IWindow topmost = getTopmostWindowMock(topmostRectangle, TOPMOST_PROCESS_NAME);

        windowHasUserInput(nativeWindow);
        noAnyTopWindows();
        noAnyAboveWindows(nativeWindow);
        when(applicationSettings.closeOverlappingWindows()).thenReturn(true);
        when(win32System.listAllWindows()).thenReturn(List.of(nativeWindow, topmost));
        when(stampValidator.validateStamp(stamp)).thenReturn(Optional.of(stamp));
        final Optional<Stamp> result = new ForegroundWindow(nativeWindow, settings, win32System, stampValidator).waiting()
                .withTimeout(100)
                .withDelay(10)
                .withMousePosition(new PointFloat(0.5f, 0.7f))
                .logFailedStamps()
                .forStamp(stamp);
        assertEquals(Optional.of(stamp), result);
        verify(topmost, never()).close();
    }

    @Test
    void waiting_foundAndCloseOverlapTopmost() throws CannotGetUserInputException, InterruptedException, Win32ApiException {
        final Rectangle stampRectangle = new Rectangle(0, 0, 3, 2);
        final Rectangle windowRectangleInStamp = new Rectangle(0, 0, 300, 200);
        final Rectangle windowRectangle = new Rectangle(0, 0, 300, 200);
        final Rectangle topmostRectangle = new Rectangle(280, 178, 299, 199);
        final int[] stampWholeData = imageToArray(createImage(stampRectangle));
        final Stamp stamp = getStampMock(stampRectangle, windowRectangleInStamp, stampWholeData);
        final IMouse nativeMouse = mock(IMouse.class);
        final IKeyboard nativeKeyboard = mock(IKeyboard.class);
        final IWindow nativeWindow = getWindowMock(windowRectangle, nativeMouse, nativeKeyboard);
        final IWindow topmost = getTopmostWindowMock(topmostRectangle, TOPMOST_PROCESS_NAME);

        windowHasUserInput(nativeWindow);
        foundTopWindow(topmost);
        noAnyAboveWindows(nativeWindow);
        when(applicationSettings.closeOverlappingWindows()).thenReturn(true);
        when(applicationSettings.overlappingWindowsToClose()).thenReturn(new String[]{TOPMOST_PROCESS_NAME});
        when(win32System.listAllWindows()).thenReturn(List.of(nativeWindow, topmost));
        when(stampValidator.validateStamp(stamp)).thenReturn(Optional.of(stamp));
        final Optional<Stamp> result = new ForegroundWindow(nativeWindow, settings, win32System, stampValidator).waiting()
                .withTimeout(100)
                .withDelay(10)
                .withMousePosition(new PointFloat(0.5f, 0.7f))
                .logFailedStamps()
                .forStamp(stamp);
        assertEquals(Optional.of(stamp), result);
        verify(topmost).close();
    }

    @Test
    void waiting_foundAndSkipOrCloseOverlapTopmost() throws CannotGetUserInputException, InterruptedException, Win32ApiException {
        final Rectangle stampRectangle = new Rectangle(0, 0, 3, 2);
        final Rectangle windowRectangleInStamp = new Rectangle(0, 0, 300, 200);
        final Rectangle windowRectangle = new Rectangle(0, 0, 300, 200);
        final Rectangle topmostRectangle = new Rectangle(280, 178, 299, 199);
        final int[] stampWholeData = imageToArray(createImage(stampRectangle));
        final Stamp stamp = getStampMock(stampRectangle, windowRectangleInStamp, stampWholeData);
        final IMouse nativeMouse = mock(IMouse.class);
        final IKeyboard nativeKeyboard = mock(IKeyboard.class);
        final IWindow nativeWindow = getWindowMock(windowRectangle, nativeMouse, nativeKeyboard);
        final IWindow topmostToSkip = getTopmostWindowMock(topmostRectangle, SKIPPED_TOPMOST_PROCESS_NAME);
        final IWindow topmostToClose = getTopmostWindowMock(topmostRectangle, TOPMOST_PROCESS_NAME);

        windowHasUserInput(nativeWindow);
        foundTopWindow(topmostToClose);
        noAnyAboveWindows(nativeWindow);
        when(applicationSettings.closeOverlappingWindows()).thenReturn(true);
        when(applicationSettings.overlappingWindowsToSkip()).thenReturn(new String[]{SKIPPED_TOPMOST_PROCESS_NAME});
        when(applicationSettings.overlappingWindowsToClose()).thenReturn(new String[]{TOPMOST_PROCESS_NAME});
        when(win32System.listAllWindows()).thenReturn(List.of(nativeWindow, topmostToSkip, topmostToClose));
        when(stampValidator.validateStamp(stamp)).thenReturn(Optional.of(stamp));
        final Optional<Stamp> result = new ForegroundWindow(nativeWindow, settings, win32System, stampValidator).waiting()
                .withTimeout(100)
                .withDelay(10)
                .withMousePosition(new PointFloat(0.5f, 0.7f))
                .logFailedStamps()
                .forStamp(stamp);
        assertEquals(Optional.of(stamp), result);
        verify(topmostToSkip, never()).close();
        verify(topmostToClose).close();
    }

    @Test
    void waitingWindowStateWithSuccess() throws CannotGetUserInputException, InterruptedException, Win32ApiException {
        final Rectangle stampRectangle = new Rectangle(0, 0, 3, 2);
        final Rectangle windowRectangleInStamp = new Rectangle(0, 0, 300, 200);
        final Rectangle windowRectangle = new Rectangle(0, 0, 300, 200);
        final int[] stampWholeData = imageToArray(createImage(stampRectangle));
        final Stamp stamp = getStampMock(stampRectangle, windowRectangleInStamp, stampWholeData);
        final IMouse nativeMouse = mock(IMouse.class);
        final IKeyboard nativeKeyboard = mock(IKeyboard.class);
        final IWindow nativeWindow = getWindowMock(windowRectangle, nativeMouse, nativeKeyboard);

        windowHasUserInput(nativeWindow);
        noAnyTopWindows();
        noAnyAboveWindows(nativeWindow);
        when(applicationSettings.closeOverlappingWindows()).thenReturn(true);
        when(win32System.listAllWindows()).thenReturn(List.of(nativeWindow));
        when(stampValidator.validateStamp(stamp)).thenReturn(Optional.of(stamp));
        final Optional<Stamp> result = new ForegroundWindow(nativeWindow, settings, win32System, stampValidator).waiting()
                .withTimeout(100)
                .withDelay(10)
                .withMousePosition(new PointFloat(0.5f, 0.7f))
                .logFailedStamps()
                .forStamp(stamp);
        assertEquals(Optional.of(stamp), result);
    }

    @Test
    void waiting_cannotGetUserInput_foundOverlayAndCloseIt() throws Win32ApiException, CannotGetUserInputException, InterruptedException {
        final Rectangle stampRectangle = new Rectangle(0, 0, 3, 2);
        final Rectangle windowRectangleInStamp = new Rectangle(0, 0, 300, 200);
        final Rectangle windowRectangle = new Rectangle(0, 0, 300, 200);
        final int[] stampWholeData = {};
        final Stamp stamp = getStampMock(stampRectangle, windowRectangleInStamp, stampWholeData);
        final IMouse nativeMouse = mock(IMouse.class);
        final IKeyboard nativeKeyboard = mock(IKeyboard.class);
        final IWindow nativeWindow = getWindowMock(windowRectangle, nativeMouse, nativeKeyboard);
        final IWindow overlay = getOverlayWindowMock();

        when(applicationSettings.bringWindowForegroundTimeout()).thenReturn(0);// to prevent waiting and get result straight away
        when(applicationSettings.bringWindowForegroundDelay()).thenReturn(0);// to prevent waiting and get result straight away
        windowHasUserInput(nativeWindow, new ResultHolder<>(List.of(false, true)));// emulate closing overlay
        foundForegroundWindow(overlay, nativeWindow);// emulate closing overlay
        noAnyTopWindows();
        noAnyAboveWindows(nativeWindow);
        when(stampValidator.validateStamp(stamp)).thenReturn(Optional.of(stamp));
        final Optional<Stamp> result = new ForegroundWindow(nativeWindow, settings, win32System, stampValidator).waiting()
                .withTimeout(100)
                .withDelay(10)
                .withMousePosition(new PointFloat(0.5f, 0.7f))
                .logFailedStamps()
                .forStamp(stamp);

        assertEquals(Optional.of(stamp), result);
        verify(stampValidator, atLeastOnce()).takeAndSaveWholeScreenShot(anyString());
    }

    private Stamp getStampMock(final Rectangle stampRectangle, final Rectangle windowRectangleInStamp, final int[] stampWholeData) {
        final Stamp stamp = mock(Stamp.class);
        when(stamp.key()).thenReturn(StampKeys.TargetBaseApp.dailyTrackerPopupCaption);
        when(stamp.location()).thenReturn(stampRectangle);
        when(stamp.wholeData()).thenReturn(stampWholeData);
        when(stamp.windowRect()).thenReturn(windowRectangleInStamp);
        return stamp;
    }

    private IWindow getWindowMock(final Rectangle windowRectangle, final IMouse mouse, final IKeyboard keyboard) {
        final IWindow window = mock(IWindow.class);
        final IProcess process = mock(IProcess.class);
        when(window.getSystemId()).thenReturn("0xdf67");
        when(window.getProcessId()).thenReturn(Either.resultOf(window::hashCode));
        when(window.getWindowMouse(anyFloat())).thenReturn(mouse);
        when(window.getKeyboard()).thenReturn(keyboard);
        when(window.getWindowRectangle()).thenReturn(Either.resultOf(() -> windowRectangle));
        when(window.getProcess()).thenReturn(Either.resultOf(() -> process));
        when(process.name()).thenReturn(Optional.of("target.exe"));
        return window;
    }

    private IWindow getTopmostWindowMock(final Rectangle windowRectangle, final String processName) {
        final IWindow window = mock(IWindow.class);
        final IProcess process = mock(IProcess.class);
        when(window.getSystemId()).thenReturn("0xca83");
        when(window.getProcessId()).thenReturn(Either.resultOf(window::hashCode));
        when(window.getStyle()).thenReturn(Either.resultOf(() -> IWinUser.WS_VISIBLE));
        when(window.hasStyles(IWinUser.WS_VISIBLE)).thenReturn(Either.resultOf(() -> true));
        when(window.getExtendedStyle()).thenReturn(Either.resultOf(() -> IWinUser.WS_EX_TOPMOST));
        when(window.hasExtendedStyles(IWinUser.WS_EX_TOPMOST)).thenReturn(Either.resultOf(() -> true));
        when(window.getWindowRectangle()).thenReturn(Either.resultOf(() -> windowRectangle));
        when(window.getProcess()).thenReturn(Either.resultOf(() -> process));
        when(process.name()).thenReturn(Optional.of(processName));
        return window;
    }

    private IWindow getOverlayWindowMock() {
        final IWindow window = mock(IWindow.class);
        final IProcess process = mock(IProcess.class);
        when(window.getSystemId()).thenReturn("0xd4e7");
        when(window.getProcessId()).thenReturn(Either.resultOf(window::hashCode));
        when(window.getStyle()).thenReturn(Either.resultOf(() -> IWinUser.WS_VISIBLE));
        when(window.getExtendedStyle()).thenReturn(Either.resultOf(() -> IWinUser.WS_EX_OVERLAPPEDWINDOW));
        when(window.getWindowRectangle()).thenReturn(Either.resultOf(() -> new Rectangle(0, 0, 100, 50)));
        when(window.getProcess()).thenReturn(Either.resultOf(() -> process));
        when(process.name()).thenReturn(Optional.of("overlay.exe"));
        return window;
    }

    private void windowHasNotUserInput(final IWindow nativeWindow) throws Win32ApiException {
        when(nativeWindow.bringForeground()).thenReturn(false);
        when(nativeWindow.isForeground()).thenReturn(false);
    }

    private void windowHasUserInput(final IWindow nativeWindow) throws Win32ApiException {
        when(nativeWindow.bringForeground()).thenReturn(true);
        when(nativeWindow.isForeground()).thenReturn(true);
    }

    private void windowHasUserInput(final IWindow nativeWindow, final List<Boolean> results) throws Win32ApiException {
        MockitoUtil.INSTANCE.mockReturnVararg(nativeWindow.bringForeground(), results);
        MockitoUtil.INSTANCE.mockReturnVararg(nativeWindow.isForeground(), results);
    }

    private void windowHasUserInput(final IWindow nativeWindow, final ResultHolder<Boolean> resultHolder) throws Win32ApiException {
        when(nativeWindow.isForeground()).thenReturn(resultHolder.startFrom());
        when(nativeWindow.bringForeground()).thenAnswer(invocationOnMock -> {
            final Boolean result = resultHolder.next();
            when(nativeWindow.isForeground()).thenReturn(result);
            return result;
        });
    }

    private void noAnyForegroundWindowFound() {
        when(win32System.getForegroundWindow()).thenReturn(Optional.empty());
    }

    private void foundForegroundWindow(final IWindow window) {
        when(win32System.getForegroundWindow()).thenReturn(Optional.of(window));
    }

    private void foundForegroundWindow(final IWindow window1, final IWindow window2) {
        when(win32System.getForegroundWindow()).thenReturn(Optional.of(window1)).thenReturn(Optional.of(window2));
    }

    private void noAnyTopWindows() {
        when(win32System.getTopWindow()).thenReturn(Either.resultOf(Optional::empty));
    }

    private void foundTopWindow(final IWindow topmost) {
        when(win32System.getTopWindow()).thenReturn(Either.resultOf(() -> Optional.of(topmost)));
    }

    private void noAnyAboveWindows(final IWindow nativeWindow) {
        when(nativeWindow.getAboveOnZOrderWindow()).thenReturn(Either.resultOf(Optional::empty));
    }

    private BufferedImage createImage(final Rectangle rectangle) {
        // https://riptutorial.com/java/example/19496/creating-a-simple-image-programmatically-and-displaying-it
        final int width = rectangle.width();
        final int height = rectangle.height();
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.RED);
        g2d.drawLine(0, 0, width, height);
        g2d.drawLine(0, height, width, 0);
        g2d.dispose();
        return img;
    }

    private int[] imageToArray(final BufferedImage image) {
        return image.getRGB(
                image.getMinX(),
                image.getMinY(),
                image.getWidth(),
                image.getHeight(),
                null,
                0,
                image.getWidth());
    }

    private static class ResultHolder<T> {
        private final List<T> results;
        private volatile int index = 0;

        private ResultHolder(final List<T> results) {
            assert !results.isEmpty();
            this.results = results;
        }

        private synchronized int nextIndex() {
            if (index < results.size() - 1) return index++;
            return index;
        }

        public T next() {
            return results.get(nextIndex());
        }

        public T startFrom() {
            return results.get(0);
        }
    }
}