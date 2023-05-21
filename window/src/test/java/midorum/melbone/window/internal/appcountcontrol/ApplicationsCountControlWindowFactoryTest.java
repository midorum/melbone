package midorum.melbone.window.internal.appcountcontrol;

import com.midorum.win32api.facade.IWindow;
import com.midorum.win32api.facade.Win32System;
import midorum.melbone.model.window.appcountcontrol.ApplicationsCountControlWindow;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.model.settings.setting.TargetCountControlSettings;
import midorum.melbone.window.internal.common.CommonWindowService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationsCountControlWindowFactoryTest {


    public static final String APP_COUNT_CONTROL_WINDOW_TITLE = "AppCountControl";
    private final CommonWindowService commonWindowService = mock(CommonWindowService.class);
    private final Win32System win32System = mock(Win32System.class);
    private final Settings settings = mock(Settings.class);
    private final TargetCountControlSettings targetCountControlSettings = mock(TargetCountControlSettings.class);

    @BeforeAll
    public static void beforeAll() {
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    @BeforeEach
    public void beforeEach() throws InterruptedException {
        // system
        when(commonWindowService.getWin32System()).thenReturn(win32System);
        // target window
        when(settings.targetCountControl()).thenReturn(targetCountControlSettings);
        when(targetCountControlSettings.windowTitle()).thenReturn(APP_COUNT_CONTROL_WINDOW_TITLE);
    }

    @AfterEach
    public void afterEach() {
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    @Test
    void findApplicationsCountControlWindow() {
        System.out.println("findApplicationsCountControlWindow");
        final IWindow appCountControlWindowMock = getAppCountControlWindowMock();
        when(win32System.findWindow(APP_COUNT_CONTROL_WINDOW_TITLE)).thenReturn(Optional.of(appCountControlWindowMock));
        final ApplicationsCountControlWindowFactory instance = new ApplicationsCountControlWindowFactory(commonWindowService, settings);
        final Optional<ApplicationsCountControlWindow> maybeWindow = instance.findWindow();
        assertTrue(maybeWindow.isPresent());
    }

    private IWindow getAppCountControlWindowMock() {
        final IWindow mock = mock(IWindow.class);
        when(mock.getSystemId()).thenReturn("0xff06");
        return mock;
    }
}