package midorum.melbone.window.internal.common;

import com.midorum.win32api.facade.*;
import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.struct.PointFloat;
import com.midorum.win32api.win32.IWinUser;
import midorum.melbone.model.exception.CannotGetUserInputException;
import midorum.melbone.model.settings.setting.ApplicationSettings;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.model.settings.stamp.Stamp;
import midorum.melbone.settings.StampKeys;
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
    }

    @Test
    void getMouse_cannotGetUserInput() {
        final IWindow nativeWindow = mock(IWindow.class);

        windowHasNotUserInput(nativeWindow);
        assertThrows(CannotGetUserInputException.class, new ForegroundWindow(nativeWindow, settings, win32System, stampValidator)::getMouse);

        verify(stampValidator).takeAndSaveWholeScreenShot(anyString());
    }

    @Test
    void getMouse() throws CannotGetUserInputException, InterruptedException {
        final float speedFactor = 1f;
        when(applicationSettings.speedFactor()).thenReturn(speedFactor);
        final PointFloat point = new PointFloat(-1, -1);
        final IWindow nativeWindow = mock(IWindow.class);
        final IMouse nativeMouse = mock(IMouse.class);
        when(nativeWindow.getWindowMouse(speedFactor)).thenReturn(nativeMouse);
        when(nativeMouse.move(any(PointFloat.class))).thenReturn(nativeMouse);

        windowHasUserInput(nativeWindow);
        final Mouse mouse = new ForegroundWindow(nativeWindow, settings, win32System, stampValidator).getMouse();
        assertNotNull(mouse);
        mouse.clickAtPoint(point);

        final InOrder inOrderMouse = inOrder(nativeMouse);
        inOrderMouse.verify(nativeMouse, atLeastOnce()).move(point);
        inOrderMouse.verify(nativeMouse).leftClick();
    }

    @Test
    void getKeyboard_cannotGetUserInput() {
        final IWindow nativeWindow = mock(IWindow.class);

        windowHasNotUserInput(nativeWindow);
        assertThrows(CannotGetUserInputException.class, new ForegroundWindow(nativeWindow, settings, win32System, stampValidator)::getKeyboard);

        verify(stampValidator).takeAndSaveWholeScreenShot(anyString());
    }

    @Test
    void getKeyboard() throws CannotGetUserInputException, InterruptedException {
        final IWindow nativeWindow = mock(IWindow.class);
        final IKeyboard nativeKeyboard = mock(IKeyboard.class);
        when(nativeWindow.getKeyboard()).thenReturn(nativeKeyboard);

        windowHasUserInput(nativeWindow);
        final IKeyboard keyboard = new ForegroundWindow(nativeWindow, settings, win32System, stampValidator).getKeyboard();
        assertEquals(nativeKeyboard, keyboard);
    }

    @Test
    void waiting_cannotGetUserInput_noAnyOverlay() {
        final Rectangle stampRectangle = new Rectangle(0, 0, 3, 2);
        final Rectangle stampWindowRectangle = new Rectangle(0, 0, 300, 200);
        final Rectangle windowRectangle = new Rectangle(0, 0, 300, 200);
        final int[] stampWholeData = {};
        final Stamp stamp = getStampMock(stampRectangle, stampWindowRectangle, stampWholeData);
        final IMouse nativeMouse = mock(IMouse.class);
        final IWindow nativeWindow = getWindowMock(windowRectangle, nativeMouse);

        windowHasNotUserInput(nativeWindow);
        noAnyForegroundWindowFound();
        assertThrows(CannotGetUserInputException.class, () -> new ForegroundWindow(nativeWindow, settings, win32System, stampValidator).waiting()
                .withTimeout(100)
                .withDelay(10)
                .forStamp(stamp));

        verify(win32System).getForegroundWindow();
        verify(stampValidator).takeAndSaveWholeScreenShot(anyString());
    }

    @Test
    void waiting_cannotGetUserInput_foundOverlay() {
        final Rectangle stampRectangle = new Rectangle(0, 0, 3, 2);
        final Rectangle stampWindowRectangle = new Rectangle(0, 0, 300, 200);
        final Rectangle windowRectangle = new Rectangle(0, 0, 300, 200);
        final int[] stampWholeData = {};
        final Stamp stamp = getStampMock(stampRectangle, stampWindowRectangle, stampWholeData);
        final IMouse nativeMouse = mock(IMouse.class);
        final IWindow nativeWindow = getWindowMock(windowRectangle, nativeMouse);
        final IWindow overlay = mock(IWindow.class);

        windowHasNotUserInput(nativeWindow);
        foundForegroundWindow(overlay);
        assertThrows(CannotGetUserInputException.class, () -> new ForegroundWindow(nativeWindow, settings, win32System, stampValidator).waiting()
                .withTimeout(100)
                .withDelay(10)
                .forStamp(stamp));

        verify(stampValidator, atLeastOnce()).takeAndSaveWholeScreenShot(anyString());
    }

    @Test
    void waiting_rectanglesDoNotMatch_withoutLogging() throws CannotGetUserInputException, InterruptedException {
        final Rectangle stampRectangle = new Rectangle(0, 0, 3, 2);
        final Rectangle stampWindowRectangle = new Rectangle(0, 0, 300, 200);
        final Rectangle windowRectangle = new Rectangle(0, 0, 301, 200);
        final int[] stampWholeData = imageToArray(createImage(stampRectangle));
        final Stamp stamp = getStampMock(stampRectangle, stampWindowRectangle, stampWholeData);
        final IMouse nativeMouse = mock(IMouse.class);
        final IWindow nativeWindow = getWindowMock(windowRectangle, nativeMouse);

        windowHasUserInput(nativeWindow);
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
    void waiting_stampNotFound_withLogging() throws CannotGetUserInputException, InterruptedException {
        final Rectangle stampRectangle = new Rectangle(0, 0, 3, 2);
        final Rectangle stampWindowRectangle = new Rectangle(0, 0, 300, 200);
        final Rectangle windowRectangle = new Rectangle(0, 0, 300, 200);
        final int[] stampWholeData = imageToArray(createImage(stampRectangle));
        final Stamp stamp = getStampMock(stampRectangle, stampWindowRectangle, stampWholeData);
        final IMouse nativeMouse = mock(IMouse.class);
        final IWindow nativeWindow = getWindowMock(windowRectangle, nativeMouse);

        windowHasUserInput(nativeWindow);
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
    void waiting_stampNotFound_withMarkedLogging() throws CannotGetUserInputException, InterruptedException {
        final Rectangle stampRectangle = new Rectangle(0, 0, 3, 2);
        final Rectangle stampWindowRectangle = new Rectangle(0, 0, 300, 200);
        final Rectangle windowRectangle = new Rectangle(0, 0, 300, 200);
        final int[] stampWholeData = imageToArray(createImage(stampRectangle));
        final Stamp stamp = getStampMock(stampRectangle, stampWindowRectangle, stampWholeData);
        final IMouse nativeMouse = mock(IMouse.class);
        final IWindow nativeWindow = getWindowMock(windowRectangle, nativeMouse);
        final String logMarker = "test-marker";

        windowHasUserInput(nativeWindow);
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
    void waiting_foundButNotCloseOverlapTopmost() throws CannotGetUserInputException, InterruptedException {
        final Rectangle stampRectangle = new Rectangle(0, 0, 3, 2);
        final Rectangle stampWindowRectangle = new Rectangle(0, 0, 300, 200);
        final Rectangle windowRectangle = new Rectangle(0, 0, 300, 200);
        final Rectangle topmostRectangle = new Rectangle(280, 178, 299, 199);
        final int[] stampWholeData = imageToArray(createImage(stampRectangle));
        final Stamp stamp = getStampMock(stampRectangle, stampWindowRectangle, stampWholeData);
        final IMouse nativeMouse = mock(IMouse.class);
        final IWindow nativeWindow = getWindowMock(windowRectangle, nativeMouse);
        final IWindow topmost = getTopmostWindowMock(topmostRectangle);

        windowHasUserInput(nativeWindow);
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
    void waiting_foundNotOverlapTopmost() throws CannotGetUserInputException, InterruptedException {
        final Rectangle stampRectangle = new Rectangle(0, 0, 3, 2);
        final Rectangle stampWindowRectangle = new Rectangle(0, 0, 300, 200);
        final Rectangle windowRectangle = new Rectangle(0, 0, 300, 200);
        final Rectangle topmostRectangle = new Rectangle(301, 178, 311, 199);
        final int[] stampWholeData = imageToArray(createImage(stampRectangle));
        final Stamp stamp = getStampMock(stampRectangle, stampWindowRectangle, stampWholeData);
        final IMouse nativeMouse = mock(IMouse.class);
        final IWindow nativeWindow = getWindowMock(windowRectangle, nativeMouse);
        final IWindow topmost = getTopmostWindowMock(topmostRectangle);

        windowHasUserInput(nativeWindow);
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
    void waiting_foundAndCloseOverlapTopmost() throws CannotGetUserInputException, InterruptedException {
        final Rectangle stampRectangle = new Rectangle(0, 0, 3, 2);
        final Rectangle stampWindowRectangle = new Rectangle(0, 0, 300, 200);
        final Rectangle windowRectangle = new Rectangle(0, 0, 300, 200);
        final Rectangle topmostRectangle = new Rectangle(280, 178, 299, 199);
        final int[] stampWholeData = imageToArray(createImage(stampRectangle));
        final Stamp stamp = getStampMock(stampRectangle, stampWindowRectangle, stampWholeData);
        final IMouse nativeMouse = mock(IMouse.class);
        final IWindow nativeWindow = getWindowMock(windowRectangle, nativeMouse);
        final IWindow topmost = getTopmostWindowMock(topmostRectangle);

        windowHasUserInput(nativeWindow);
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
        verify(topmost).close();
    }

    @Test
    void waitingWindowStateWithSuccess() throws CannotGetUserInputException, InterruptedException {
        final Rectangle stampRectangle = new Rectangle(0, 0, 3, 2);
        final Rectangle stampWindowRectangle = new Rectangle(0, 0, 300, 200);
        final Rectangle windowRectangle = new Rectangle(0, 0, 300, 200);
        final int[] stampWholeData = imageToArray(createImage(stampRectangle));
        final Stamp stamp = getStampMock(stampRectangle, stampWindowRectangle, stampWholeData);
        final IMouse nativeMouse = mock(IMouse.class);
        final IWindow nativeWindow = getWindowMock(windowRectangle, nativeMouse);

        windowHasUserInput(nativeWindow);
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

    private Stamp getStampMock(final Rectangle stampRectangle, final Rectangle stampWindowRectangle, final int[] stampWholeData) {
        final Stamp stamp = mock(Stamp.class);
        when(stamp.key()).thenReturn(StampKeys.TargetBaseApp.dailyTrackerPopupCaption);
        when(stamp.location()).thenReturn(stampRectangle);
        when(stamp.wholeData()).thenReturn(stampWholeData);
        when(stamp.windowRect()).thenReturn(stampWindowRectangle);
        return stamp;
    }

    private IWindow getWindowMock(final Rectangle windowRectangle, final IMouse mouse) {
        final IWindow window = mock(IWindow.class);
        when(window.getSystemId()).thenReturn("0xdf67");
        when(window.getWindowMouse(anyFloat())).thenReturn(mouse);
        when(window.getWindowRectangle()).thenReturn(windowRectangle);
        return window;
    }

    private IWindow getTopmostWindowMock(final Rectangle windowRectangle) {
        final IWindow window = mock(IWindow.class);
        when(window.getSystemId()).thenReturn("0xca83");
        when(window.getStyle()).thenReturn(IWinUser.WS_VISIBLE);
        when(window.hasStyles(IWinUser.WS_VISIBLE)).thenReturn(true);
        when(window.getExtendedStyle()).thenReturn(IWinUser.WS_EX_TOPMOST);
        when(window.hasExtendedStyles(IWinUser.WS_EX_TOPMOST)).thenReturn(true);
        when(window.getWindowRectangle()).thenReturn(windowRectangle);
        return window;
    }

    private void windowHasNotUserInput(final IWindow nativeWindow) {
        when(nativeWindow.bringForeground()).thenReturn(false);
        when(nativeWindow.isForeground()).thenReturn(false);
    }

    private void windowHasUserInput(final IWindow nativeWindow) {
        when(nativeWindow.bringForeground()).thenReturn(true);
        when(nativeWindow.isForeground()).thenReturn(true);
    }

    private void noAnyForegroundWindowFound() {
        when(win32System.getForegroundWindow()).thenReturn(Optional.empty());
    }

    private void foundForegroundWindow(final IWindow window) {
        when(win32System.getForegroundWindow()).thenReturn(Optional.of(window));
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
}