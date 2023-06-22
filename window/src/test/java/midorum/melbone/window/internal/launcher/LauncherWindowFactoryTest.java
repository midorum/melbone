package midorum.melbone.window.internal.launcher;

import com.midorum.win32api.facade.IMouse;
import com.midorum.win32api.facade.IWindow;
import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.facade.Win32System;
import com.midorum.win32api.struct.PointFloat;
import com.midorum.win32api.struct.PointInt;
import midorum.melbone.model.settings.setting.ApplicationSettings;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.model.settings.setting.TargetLauncherSettings;
import midorum.melbone.model.settings.stamp.Stamps;
import midorum.melbone.model.window.launcher.LauncherWindow;
import midorum.melbone.window.internal.common.CommonWindowService;
import midorum.melbone.window.internal.uac.UacWindowFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class LauncherWindowFactoryTest {

    private static final float SPEED_FACTOR = 0.1F;
    private static final String LAUNCHER_WINDOW_TITLE = "LauncherTitle";
    private static final int LAUNCHER_WINDOW_WIDTH = 150;
    private static final int LAUNCHER_WINDOW_HEIGHT = 100;
    private static final int DESKTOP_ICON_LOCATION_X = 35;
    private static final int DESKTOP_ICON_LOCATION_Y = 25;
    private static final PointInt DESKTOP_ICON_LOCATION_POINT_INT = new PointInt(DESKTOP_ICON_LOCATION_X, DESKTOP_ICON_LOCATION_Y);
    private static final String INITIALIZATION_ERROR_DIALOG_TITLE = "LauncherTitle";
    private static final int INITIALIZATION_ERROR_DIALOG_WIDTH = 75;
    private static final int INITIALIZATION_ERROR_DIALOG_HEIGHT = 55;
    private static final float INITIALIZATION_ERROR_DIALOG_CONFIRM_X = 0.5F;
    private static final float INITIALIZATION_ERROR_DIALOG_CONFIRM_Y = 0.75F;
    private final PointFloat INITIALIZATION_ERROR_DIALOG_CONFIRM_POINT_FLOAT = new PointFloat(INITIALIZATION_ERROR_DIALOG_CONFIRM_X, INITIALIZATION_ERROR_DIALOG_CONFIRM_Y);

    private final CommonWindowService commonWindowService = mock(CommonWindowService.class);
    private final Win32System win32System = mock(Win32System.class);
    private final IMouse mouse = mock(IMouse.class);
    private final UacWindowFactory uacWindowFactory = mock(UacWindowFactory.class);
    private final Settings settings = mock(Settings.class);
    private final ApplicationSettings applicationSettings = mock(ApplicationSettings.class);
    private final TargetLauncherSettings targetLauncherSettings = mock(TargetLauncherSettings.class);
    private final Stamps stamps = mock(Stamps.class);

    @BeforeAll
    public static void beforeAll() {
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    @BeforeEach
    public void beforeEach() throws InterruptedException {
        // system
        when(settings.application()).thenReturn(applicationSettings);
        when(settings.targetLauncher()).thenReturn(targetLauncherSettings);
        when(commonWindowService.getWin32System()).thenReturn(win32System);
        when(win32System.getScreenMouse(SPEED_FACTOR)).thenReturn(mouse);
        when(mouse.move(any(PointInt.class))).thenReturn(mouse);
        when(mouse.move(any(PointFloat.class))).thenReturn(mouse);
        when(mouse.move((PointInt) null)).thenThrow(new IllegalStateException("Passed null to mouse move method"));
        when(mouse.move((PointFloat) null)).thenThrow(new IllegalStateException("Passed null to mouse move method"));
        when(mouse.leftClick()).thenReturn(mouse);
        when(applicationSettings.speedFactor()).thenReturn(SPEED_FACTOR);
        // search launcher
        when(targetLauncherSettings.desktopShortcutLocationPoint()).thenReturn(DESKTOP_ICON_LOCATION_POINT_INT);
        // launcher window
        when(settings.targetLauncher().windowTitle()).thenReturn(LAUNCHER_WINDOW_TITLE);
        when(targetLauncherSettings.windowDimensions()).thenReturn(new Rectangle(0, 0, LAUNCHER_WINDOW_WIDTH, LAUNCHER_WINDOW_HEIGHT));
        // initialization dialog window
        when(settings.targetLauncher().initializationErrorDialogTitle()).thenReturn(INITIALIZATION_ERROR_DIALOG_TITLE);
        when(targetLauncherSettings.initializationErrorDialogDimensions()).thenReturn(new Rectangle(0, 0, INITIALIZATION_ERROR_DIALOG_WIDTH, INITIALIZATION_ERROR_DIALOG_HEIGHT));
        when(targetLauncherSettings.closeInitializationErrorDialogButtonPoint()).thenReturn(INITIALIZATION_ERROR_DIALOG_CONFIRM_POINT_FLOAT);
    }

    @AfterEach
    public void afterEach() {
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    @Test
    void launcherNotFoundAndNotStarted() throws InterruptedException {
        System.out.println("launcherNotFoundAndNotStarted");
        final LauncherWindowFactory instance = new LauncherWindowFactory(commonWindowService, settings, uacWindowFactory, stamps);
        final Optional<LauncherWindow> maybeLauncher = instance.findWindowOrTryStartLauncher();
        assertTrue(maybeLauncher.isEmpty());
    }

    @Test
    void closeInitializationErrorDialog() throws InterruptedException {
        System.out.println("closeInitializationErrorDialog");
        final List<IWindow> initializationDialogWindows = getInitializationDialogWindows();
        when(win32System.findAllWindows(INITIALIZATION_ERROR_DIALOG_TITLE, null, true)).thenReturn(initializationDialogWindows);
        final LauncherWindowFactory instance = new LauncherWindowFactory(commonWindowService, settings, uacWindowFactory, stamps);
        final Optional<LauncherWindow> maybeLauncher = instance.findWindowOrTryStartLauncher();
        assertTrue(maybeLauncher.isEmpty());
        verify(targetLauncherSettings, atLeast(1)).closeInitializationErrorDialogButtonPoint();
        verify(mouse, atLeast(1)).move(INITIALIZATION_ERROR_DIALOG_CONFIRM_POINT_FLOAT);
        verify(mouse, atLeast(1)).leftClick();
    }

    @SuppressWarnings("unchecked")
    @Test
    void findOrTryStartLauncherWindow() throws InterruptedException {
        System.out.println("findOrTryStartLauncherWindow");
        final List<IWindow> emptyList = List.of();
        final List<IWindow> launcherWindows = getLauncherWindows();
        when(win32System.findAllWindows(LAUNCHER_WINDOW_TITLE, null, true)).thenReturn(emptyList, launcherWindows);
        final LauncherWindowFactory instance = new LauncherWindowFactory(commonWindowService, settings, uacWindowFactory, stamps);
        final Optional<LauncherWindow> maybeLauncher = instance.findWindowOrTryStartLauncher();
        assertTrue(maybeLauncher.isPresent());
        verify(targetLauncherSettings, atLeast(1)).desktopShortcutLocationPoint();
        verify(mouse, atLeast(1)).move(DESKTOP_ICON_LOCATION_POINT_INT);
        verify(mouse, atLeast(1)).leftClick();
    }

    @Test
    void findLauncherWindow() {
        System.out.println("findLauncherWindow");
        final List<IWindow> launcherWindows = getLauncherWindows();
        when(settings.targetLauncher().windowTitle()).thenReturn(LAUNCHER_WINDOW_TITLE);
        when(win32System.findAllWindows(LAUNCHER_WINDOW_TITLE, null, true)).thenReturn(launcherWindows);
        final LauncherWindowFactory instance = new LauncherWindowFactory(commonWindowService, settings, uacWindowFactory, stamps);
        final Optional<LauncherWindow> maybeLauncher = instance.findWindow();
        assertTrue(maybeLauncher.isPresent());
    }

    private List<IWindow> getLauncherWindows() {
        return List.of(createLauncherWindowMock());
    }

    private IWindow createLauncherWindowMock() {
        final IWindow mock = mock(IWindow.class);
        when(mock.getWindowRectangle()).thenReturn(new Rectangle(0, 0, LAUNCHER_WINDOW_WIDTH, LAUNCHER_WINDOW_HEIGHT));
        when(mock.getSystemId()).thenReturn("0x7f34");
        return mock;
    }

    private List<IWindow> getInitializationDialogWindows() {
        return List.of(createInitializationDialogWindowMock());
    }

    private IWindow createInitializationDialogWindowMock() {
        final IWindow mock = mock(IWindow.class);
        when(mock.getWindowRectangle()).thenReturn(new Rectangle(0, 0, INITIALIZATION_ERROR_DIALOG_WIDTH, INITIALIZATION_ERROR_DIALOG_HEIGHT));
        when(mock.getSystemId()).thenReturn("0xff06");
        when(mock.getWindowMouse(SPEED_FACTOR)).thenReturn(mouse);
        return mock;
    }
}