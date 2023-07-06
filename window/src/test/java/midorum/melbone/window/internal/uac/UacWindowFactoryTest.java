package midorum.melbone.window.internal.uac;

import com.midorum.win32api.facade.Either;
import com.midorum.win32api.facade.IWindow;
import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.facade.Win32System;
import midorum.melbone.model.window.uac.UacWindow;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.model.settings.setting.UacSettings;
import midorum.melbone.window.internal.common.CommonWindowService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UacWindowFactoryTest {

    private static final String UAC_CLASSNAME = "UacClassname";
    private static final int UAC_WINDOW_WIDTH = 150;
    private static final int UAC_WINDOW_HEIGHT = 75;

    private final CommonWindowService commonWindowService = mock(CommonWindowService.class);
    private final Win32System win32System = mock(Win32System.class);
    private final Settings settings = mock(Settings.class);
    private final UacSettings uacSettings = mock(UacSettings.class);

    @BeforeAll
    public static void beforeAll() {
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    @BeforeEach
    public void beforeEach() {
        // system
        when(commonWindowService.getWin32System()).thenReturn(win32System);
        // uac window
        when(settings.uacSettings()).thenReturn(uacSettings);
        when(uacSettings.windowClassName()).thenReturn(UAC_CLASSNAME);
        when(uacSettings.windowDimensions()).thenReturn(new Rectangle(0, 0, UAC_WINDOW_WIDTH, UAC_WINDOW_HEIGHT));
    }

    @AfterEach
    public void afterEach() {
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    @Test
    void findUacOverlayWindow() {
        System.out.println("findUacOverlayWindow");
        final IWindow foregroundWindowMock = getUacWindowMock();
        when(win32System.getForegroundWindow()).thenReturn(Optional.of(foregroundWindowMock));
        final UacWindowFactory instance = new UacWindowFactory(commonWindowService, settings);
        final Optional<UacWindow> maybeUacOverlayWindow = instance.findUacOverlayWindow();
        assertTrue(maybeUacOverlayWindow.isPresent());
    }

    @Test
    void uacOverlayWindowNotFound() {
        System.out.println("uacOverlayWindowNotFound");
        final IWindow foregroundWindowMock = getAnotherWindowMock();
        when(win32System.getForegroundWindow()).thenReturn(Optional.of(foregroundWindowMock));
        final UacWindowFactory instance = new UacWindowFactory(commonWindowService, settings);
        final Optional<UacWindow> maybeUacOverlayWindow = instance.findUacOverlayWindow();
        assertTrue(maybeUacOverlayWindow.isEmpty());
    }

    private IWindow getUacWindowMock() {
        final IWindow mock = mock(IWindow.class);
        when(mock.getClassName()).thenReturn(Either.resultOf(() -> UAC_CLASSNAME));
        when(mock.getWindowRectangle()).thenReturn(Either.resultOf(() -> new Rectangle(0, 0, UAC_WINDOW_WIDTH, UAC_WINDOW_HEIGHT)));
        when(mock.getSystemId()).thenReturn("0x7f34");
        return mock;
    }

    private IWindow getAnotherWindowMock() {
        final IWindow mock = mock(IWindow.class);
        when(mock.getClassName()).thenReturn(Either.resultOf(() -> "another_classname"));
        when(mock.getWindowRectangle()).thenReturn(Either.resultOf(() -> new Rectangle(0, 0, UAC_WINDOW_WIDTH + 1, UAC_WINDOW_HEIGHT + 1)));
        when(mock.getSystemId()).thenReturn("0xac54");
        return mock;
    }
}