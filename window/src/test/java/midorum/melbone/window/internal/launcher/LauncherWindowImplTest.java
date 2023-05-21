package midorum.melbone.window.internal.launcher;

import com.midorum.win32api.facade.*;
import com.midorum.win32api.struct.PointFloat;
import com.midorum.win32api.struct.PointInt;
import com.midorum.win32api.win32.Win32VirtualKey;
import midorum.melbone.model.dto.Account;
import midorum.melbone.model.settings.stamp.Stamp;
import midorum.melbone.settings.StampKeys;
import midorum.melbone.model.exception.NeedRetryException;
import midorum.melbone.model.settings.stamp.Stamps;
import midorum.melbone.model.settings.stamp.TargetLauncherStamps;
import midorum.melbone.model.window.launcher.RestoredLauncherWindow;
import midorum.melbone.model.settings.setting.ApplicationSettings;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.model.settings.setting.TargetLauncherSettings;
import midorum.melbone.window.internal.common.CommonWindowService;
import midorum.melbone.window.internal.common.StampValidator;
import org.junit.jupiter.api.*;
import org.mockito.invocation.InvocationOnMock;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Tests for launcher window implementation")
class LauncherWindowImplTest {

    private static final String CONFIRM_DIALOG_TITLE = "confirm_dialog_title";
    private static final float SPEED_FACTOR = 1.0F;
    private static final PointFloat POINT_FLOAT = new PointFloat(-1f, -1f);
    private static final PointFloat START_BUTTON_POINT = new PointFloat(-2f, -2f);
    private static final int LAUNCHER_WINDOW_WIDTH = 150;
    private static final int LAUNCHER_WINDOW_HEIGHT = 100;
    private static final int CONFIRM_QUIT_DIALOG_WINDOW_WIDTH = 125;
    private static final int CONFIRM_QUIT_DIALOG_WINDOW_HEIGHT = 75;
    private final CommonWindowService commonWindowService = mock(CommonWindowService.class);
    private final Win32System win32System = mock(Win32System.class);
    private final Settings settings = mock(Settings.class);
    private final ApplicationSettings applicationSettings = mock(ApplicationSettings.class);
    private final TargetLauncherSettings targetLauncherSettings = mock(TargetLauncherSettings.class);
    private final IWindow window = mock(IWindow.class);
    private final IMouse mouse = mock(IMouse.class);
    private final IKeyboard keyboard = mock(IKeyboard.class);
    private final IProcess process = mock(IProcess.class);
    private final Account account = mock(Account.class);
    //stamps
    private final Stamps stamps = mock(Stamps.class);
    private final TargetLauncherStamps targetLauncherStamps = mock(TargetLauncherStamps.class);
    private final StampValidator stampValidator = mock(StampValidator.class);
    private final Stamp quitConfirmPopupStamp = mock(Stamp.class);
    private final Stamp clientIsAlreadyRunningStamp = mock(Stamp.class);
    private final Stamp loginButtonNoErrorActiveStamp = mock(Stamp.class);
    private final Stamp loginButtonNoErrorInactiveStamp = mock(Stamp.class);
    private final Stamp loginButtonWithErrorActiveStamp = mock(Stamp.class);
    private final Stamp loginButtonWithErrorInactiveStamp = mock(Stamp.class);
    private final Stamp startButtonActiveStamp = mock(Stamp.class);
    private final Stamp startButtonInactiveStamp = mock(Stamp.class);
    private final Set<Stamp> stampsToCheckLauncherRendering = new HashSet<>() {{
        add(loginButtonNoErrorActiveStamp);
        add(loginButtonNoErrorInactiveStamp);
        add(loginButtonWithErrorActiveStamp);
        add(loginButtonWithErrorInactiveStamp);
        add(startButtonActiveStamp);
        add(startButtonInactiveStamp);
    }};

    @BeforeAll
    public static void beforeAll() {
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    @BeforeEach
    public void beforeEach() throws InterruptedException {
        //system
        when(commonWindowService.getStampValidator()).thenReturn(stampValidator);
        when(commonWindowService.getWin32System()).thenReturn(win32System);
        //settings
        when(settings.application()).thenReturn(applicationSettings);
        when(applicationSettings.speedFactor()).thenReturn(SPEED_FACTOR);
        when(settings.targetLauncher()).thenReturn(targetLauncherSettings);
        when(targetLauncherSettings.attemptsToWindowRendering()).thenReturn(2);
        when(targetLauncherSettings.attemptToFindStartButton()).thenReturn(2);
        when(targetLauncherSettings.closingWindowDelay()).thenReturn(1);
        when(targetLauncherSettings.loginInputPoint()).thenReturn(POINT_FLOAT);
        when(targetLauncherSettings.passwordInputPoint()).thenReturn(POINT_FLOAT);
        when(targetLauncherSettings.loginButtonPoint()).thenReturn(POINT_FLOAT);
        when(targetLauncherSettings.startButtonPoint()).thenReturn(START_BUTTON_POINT);
        when(targetLauncherSettings.windowCloseButtonPoint()).thenReturn(POINT_FLOAT);
        when(targetLauncherSettings.closeQuitConfirmPopupButtonPoint()).thenReturn(POINT_FLOAT);
        when(targetLauncherSettings.windowDimensions()).thenReturn(new Rectangle(0, 0, LAUNCHER_WINDOW_WIDTH, LAUNCHER_WINDOW_HEIGHT));
        when(targetLauncherSettings.confirmQuitDialogDimensions()).thenReturn(new Rectangle(0, 0, CONFIRM_QUIT_DIALOG_WINDOW_WIDTH, CONFIRM_QUIT_DIALOG_WINDOW_HEIGHT));
        when(targetLauncherSettings.confirmQuitDialogTitle()).thenReturn(CONFIRM_DIALOG_TITLE);
        //window
        when(window.getWindowMouse(SPEED_FACTOR)).thenReturn(mouse);
        when(mouse.move(any(PointInt.class))).thenReturn(mouse);
        when(mouse.move(any(PointFloat.class))).thenReturn(mouse);
        when(window.getKeyboard()).thenReturn(keyboard);
        when(keyboard.typeControlled(anyChar())).thenReturn(keyboard);
        when(keyboard.pressAndRelease(any(Win32VirtualKey.class))).thenReturn(keyboard);
        when(window.getProcess()).thenReturn(process);
        //launcher normal metrics
        when(window.isVisible()).thenReturn(true);
        when(window.getSystemId()).thenReturn("0x435f");
        when(window.getWindowRectangle()).thenReturn(new Rectangle(0, 0, LAUNCHER_WINDOW_WIDTH, LAUNCHER_WINDOW_HEIGHT));
        //account
        when(account.name()).thenReturn("account_name");
        when(account.login()).thenReturn("account_login");
        when(account.password()).thenReturn("account_password");
        //stamps
        when(stamps.targetLauncher()).thenReturn(targetLauncherStamps);
        when(targetLauncherStamps.quitConfirmPopup()).thenReturn(quitConfirmPopupStamp);
        when(targetLauncherStamps.clientIsAlreadyRunning()).thenReturn(clientIsAlreadyRunningStamp);
        when(targetLauncherStamps.loginButtonNoErrorActive()).thenReturn(loginButtonNoErrorActiveStamp);
        when(targetLauncherStamps.loginButtonNoErrorInactive()).thenReturn(loginButtonNoErrorInactiveStamp);
        when(targetLauncherStamps.loginButtonWithErrorActive()).thenReturn(loginButtonWithErrorActiveStamp);
        when(targetLauncherStamps.loginButtonWithErrorInactive()).thenReturn(loginButtonWithErrorInactiveStamp);
        when(targetLauncherStamps.startButtonActive()).thenReturn(startButtonActiveStamp);
        when(targetLauncherStamps.startButtonInactive()).thenReturn(startButtonInactiveStamp);
        when(loginButtonNoErrorInactiveStamp.key()).thenReturn(StampKeys.TargetLauncher.loginButtonNoErrorInactive);
        when(startButtonActiveStamp.key()).thenReturn(StampKeys.TargetLauncher.playButtonActive);
    }

    @AfterEach
    public void afterEach() {
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    @Test
    @DisplayName("\"Client is already running\" window rendered")
    void clientIsAlreadyRunningWindowRendered() throws InterruptedException {
        System.out.println("clientIsAlreadyRunningWindowRendered");
        //given
        clientIsAlreadyRunningAlertDetected();
        //when and then
        getLauncherWindowInstance().restoreAndDo(restoredLauncherWindow -> {
            assertTrue(restoredLauncherWindow.checkClientIsAlreadyRunningWindowRendered());
        });
    }

    @Test
    void clientIsAlreadyRunningWindowNotRendered() throws InterruptedException {
        System.out.println("clientIsAlreadyRunningWindowNotRendered");
        //given
        clientIsAlreadyRunningAlertNotDetected();
        //when and then
        getLauncherWindowInstance().restoreAndDo(restoredLauncherWindow -> {
            assertFalse(restoredLauncherWindow.checkClientIsAlreadyRunningWindowRendered());
        });
    }

    @Test
    void launcherNotRenderedError() throws InterruptedException {
        System.out.println("launcherNotRenderedError");
        //given
        launcherWindowNotRenderedNormally();
        //when
        assertThrows(NeedRetryException.class,
                () -> getLauncherWindowInstance().restoreAndDo(restoredLauncherWindow -> restoredLauncherWindow.login(account)));
        //then
        verify(stampValidator, atLeastOnce()).validateStampWholeData(eq(window), (Stamp[]) any());
    }

    @Test
    void loginSuccessfully() throws InterruptedException {
        System.out.println("loginSuccessfully");
        //given
        launcherWindowRenderedNormallyOnLoginForm();
        //when
        getLauncherWindowInstance().restoreAndDo(restoredLauncherWindow -> restoredLauncherWindow.login(account));
        //then
        verify(account, atLeastOnce()).login();
        verify(account, atLeastOnce()).password();
    }

    @Test
    void startGameWhenGetReady() throws InterruptedException {
        System.out.println("startGameWhenGetReady");
        //given
        activeStartButtonDetected();
        //when
        getLauncherWindowInstance().restoreAndDo(RestoredLauncherWindow::startGameWhenGetReady);
        //then
        verify(mouse).move(START_BUTTON_POINT);
        verify(mouse).leftClick();
    }

    @Test
    void startGameNotReady_windowClosedNormally() throws InterruptedException {
        System.out.println("startGameNotReady_windowClosedNormally");
        //given
        activeStartButtonIsNotDetected();
        //when and then
        windowIsAlive_confirmCloseDialogWindowDetected();
        assertThrows(NeedRetryException.class, () -> getLauncherWindowInstance().restoreAndDo(RestoredLauncherWindow::startGameWhenGetReady));
        verify(mouse, never()).move(START_BUTTON_POINT);
    }

    @Test
    void startGameNotReady_windowProcessTerminated() throws InterruptedException {
        System.out.println("startGameNotReady_windowProcessTerminated");
        //given
        activeStartButtonIsNotDetected();
        windowIsCorrupted();
        //when and then
        assertThrows(NeedRetryException.class, () -> getLauncherWindowInstance().restoreAndDo(RestoredLauncherWindow::startGameWhenGetReady));
        verify(mouse, never()).move(START_BUTTON_POINT);
        verify(process).terminate();
    }

    private LauncherWindowImpl getLauncherWindowInstance() {
        return new LauncherWindowImpl(window, commonWindowService, settings, stamps);
    }

    private void clientIsAlreadyRunningAlertDetected() throws InterruptedException {
        when(stampValidator.validateStampWholeData(window, clientIsAlreadyRunningStamp)).thenReturn(Optional.of(clientIsAlreadyRunningStamp));
    }

    private void clientIsAlreadyRunningAlertNotDetected() throws InterruptedException {
        when(stampValidator.validateStampWholeData(window, clientIsAlreadyRunningStamp)).thenReturn(Optional.empty());
    }

    private void launcherWindowNotRenderedNormally() throws InterruptedException {
        when(stampValidator.validateStampWholeData(eq(window), (Stamp[]) any())).thenAnswer(invocation -> {
            checkLauncherRenderingValidatorInvocation(invocation);
            return Optional.empty();
        });
    }

    private void launcherWindowRenderedNormallyOnLoginForm() throws InterruptedException {
        when(stampValidator.validateStampWholeData(eq(window), (Stamp[]) any())).thenAnswer(invocation -> {
            checkLauncherRenderingValidatorInvocation(invocation);
            return Optional.of(loginButtonNoErrorInactiveStamp);
        });
    }

    private void checkLauncherRenderingValidatorInvocation(final InvocationOnMock invocation) {
        final int mustBePassedArguments = 7;
        final Object[] arguments = invocation.getArguments();
        assertEquals(mustBePassedArguments, arguments.length, () -> "wrong arguments passed to check window rendering (must be 1 window and 6 stamps)");
        final IWindow windowArg = invocation.getArgument(0, IWindow.class);
        assertEquals(window, windowArg);
        final Set<Stamp> passedStamps = Arrays.stream(arguments)
                .dropWhile(o -> !(o instanceof Stamp))
                .map(Stamp.class::cast)
                .collect(Collectors.toSet());
        assertTrue(passedStamps.containsAll(stampsToCheckLauncherRendering));
        assertTrue(stampsToCheckLauncherRendering.containsAll(passedStamps));
    }

    private void activeStartButtonDetected() throws InterruptedException {
        final Stamp stampToValidate = this.startButtonActiveStamp;
        when(stampValidator.validateStampWholeData(window, new Stamp[]{stampToValidate})).thenReturn(Optional.of(stampToValidate));
    }

    private void activeStartButtonIsNotDetected() throws InterruptedException {
        when(stampValidator.validateStampWholeData(window, new Stamp[]{startButtonActiveStamp})).thenReturn(Optional.empty());
    }

    private void windowIsAlive_confirmCloseDialogWindowDetected() throws InterruptedException {
        when(window.isExists()).thenReturn(true).thenReturn(true).thenReturn(false);
        final List<IWindow> confirmDialogWindows = getConfirmDialogWindows();
        when(win32System.findAllWindows(CONFIRM_DIALOG_TITLE, null, true)).thenReturn(confirmDialogWindows);
    }

    private List<IWindow> getConfirmDialogWindows() throws InterruptedException {
        return List.of(getConfirmDialogWindowMock());
    }

    private IWindow getConfirmDialogWindowMock() throws InterruptedException {
        final IWindow mock = mock(IWindow.class);
        when(mock.getSystemId()).thenReturn("0xa8d5");
        when(mock.getWindowRectangle()).thenReturn(new Rectangle(0, 0, CONFIRM_QUIT_DIALOG_WINDOW_WIDTH, CONFIRM_QUIT_DIALOG_WINDOW_HEIGHT));
        when(stampValidator.validateStampWholeData(mock, quitConfirmPopupStamp)).thenReturn(Optional.of(quitConfirmPopupStamp));
        return mock;
    }

    private void windowIsCorrupted() {
        when(window.isExists()).thenReturn(true);
        when(window.isVisible()).thenReturn(false);
    }
}