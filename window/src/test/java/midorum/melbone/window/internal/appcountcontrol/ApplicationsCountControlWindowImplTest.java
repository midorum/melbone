package midorum.melbone.window.internal.appcountcontrol;

import com.midorum.win32api.facade.IWindow;
import com.midorum.win32api.facade.exception.Win32ApiException;
import com.midorum.win32api.struct.PointFloat;
import midorum.melbone.model.exception.CannotGetUserInputException;
import midorum.melbone.model.settings.setting.ApplicationSettings;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.model.settings.setting.TargetCountControlSettings;
import midorum.melbone.window.internal.common.CommonWindowService;
import midorum.melbone.window.internal.common.Mouse;
import midorum.melbone.window.internal.util.ForegroundWindowMocked;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class ApplicationsCountControlWindowImplTest {

    private static final float SPEED_FACTOR = 1.0F;
    private static final PointFloat CONFIRM_BUTTON_POINT = new PointFloat(0.5F, 0.5F);
    private final CommonWindowService commonWindowService = mock(CommonWindowService.class);
    private final Settings settings = mock(Settings.class);
    private final ApplicationSettings applicationSettings = mock(ApplicationSettings.class);
    private final TargetCountControlSettings targetCountControlSettings = mock(TargetCountControlSettings.class);

    @BeforeAll
    public static void beforeAll() {
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    @BeforeEach
    public void beforeEach() throws InterruptedException, CannotGetUserInputException {
        // target window
        when(settings.application()).thenReturn(applicationSettings);
        when(settings.targetCountControl()).thenReturn(targetCountControlSettings);
        when(applicationSettings.speedFactor()).thenReturn(SPEED_FACTOR);
        when(targetCountControlSettings.confirmButtonPoint()).thenReturn(CONFIRM_BUTTON_POINT);
    }

    @AfterEach
    public void afterEach() {
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    @Test
    void clickConfirmButton() throws InterruptedException, CannotGetUserInputException, Win32ApiException {
        System.out.println("clickConfirmButton");
        final IWindow window = mock(IWindow.class);
        final Mouse mouse = mock(Mouse.class);
        appCountControlWindowMocked(window).returnsMouse(mouse);
        final ApplicationsCountControlWindowImpl instance = new ApplicationsCountControlWindowImpl(window, commonWindowService, settings);
        instance.clickConfirmButton();
        verify(mouse).clickAtPoint(CONFIRM_BUTTON_POINT);
    }

    @Test
    void cannotBringWindowForeground() throws InterruptedException, CannotGetUserInputException {
        System.out.println("cannotBringWindowForeground");
        final IWindow window = mock(IWindow.class);
        appCountControlWindowMocked(window).throwsWhenAskedMouse(new CannotGetUserInputException());
        final ApplicationsCountControlWindowImpl instance = new ApplicationsCountControlWindowImpl(window, commonWindowService, settings);
        instance.clickConfirmButton();
        verify(commonWindowService).takeAndSaveWholeScreenShot(anyString(), anyString());
    }

    private ForegroundWindowMocked appCountControlWindowMocked(final IWindow window) throws CannotGetUserInputException, InterruptedException {
        return new ForegroundWindowMocked.Builder()
                .withCommonWindowService(commonWindowService)
                .getForegroundWindowFor(window);
    }
}