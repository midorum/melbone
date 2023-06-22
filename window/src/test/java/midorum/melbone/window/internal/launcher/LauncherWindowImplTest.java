package midorum.melbone.window.internal.launcher;

import com.midorum.win32api.facade.*;
import com.midorum.win32api.struct.PointFloat;
import com.midorum.win32api.struct.PointInt;
import midorum.melbone.model.dto.Account;
import midorum.melbone.model.exception.CannotGetUserInputException;
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
import midorum.melbone.window.internal.common.ForegroundWindow;
import midorum.melbone.window.internal.common.Mouse;
import midorum.melbone.window.internal.util.MockitoUtil;
import org.junit.jupiter.api.*;
import org.mockito.invocation.InvocationOnMock;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Tests for launcher window implementation")
class LauncherWindowImplTest {

    private static final String CONFIRM_DIALOG_TITLE = "confirm_dialog_title";
    private static final float SPEED_FACTOR = 1.0F;
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
    //points
    private final PointFloat loginInputPoint = new PointFloat(-1f, -1f);
    private final PointFloat passwordInputPoint = new PointFloat(-2f, -1f);
    private final PointFloat loginButtonPoint = new PointFloat(-3f, -1f);
    private final PointFloat startButtonPoint = new PointFloat(-4f, -1f);
    private final PointFloat windowCloseButtonPoint = new PointFloat(-5f, -1f);
    private final PointFloat closeQuitConfirmPopupButtonPoint = new PointFloat(-6f, -1f);

    @BeforeAll
    public static void beforeAll() {
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    @BeforeEach
    public void beforeEach() throws InterruptedException {
        //system
        when(commonWindowService.getWin32System()).thenReturn(win32System);
        //settings
        when(settings.application()).thenReturn(applicationSettings);
        when(applicationSettings.speedFactor()).thenReturn(SPEED_FACTOR);
        when(settings.targetLauncher()).thenReturn(targetLauncherSettings);
        when(targetLauncherSettings.closingWindowDelay()).thenReturn(1);
        when(targetLauncherSettings.loginInputPoint()).thenReturn(loginInputPoint);
        when(targetLauncherSettings.passwordInputPoint()).thenReturn(passwordInputPoint);
        when(targetLauncherSettings.loginButtonPoint()).thenReturn(loginButtonPoint);
        when(targetLauncherSettings.startButtonPoint()).thenReturn(startButtonPoint);
        when(targetLauncherSettings.windowCloseButtonPoint()).thenReturn(windowCloseButtonPoint);
        when(targetLauncherSettings.closeQuitConfirmPopupButtonPoint()).thenReturn(closeQuitConfirmPopupButtonPoint);
        when(targetLauncherSettings.windowDimensions()).thenReturn(new Rectangle(0, 0, LAUNCHER_WINDOW_WIDTH, LAUNCHER_WINDOW_HEIGHT));
        when(targetLauncherSettings.confirmQuitDialogDimensions()).thenReturn(new Rectangle(0, 0, CONFIRM_QUIT_DIALOG_WINDOW_WIDTH, CONFIRM_QUIT_DIALOG_WINDOW_HEIGHT));
        when(targetLauncherSettings.confirmQuitDialogTitle()).thenReturn(CONFIRM_DIALOG_TITLE);
        when(targetLauncherSettings.confirmQuitDialogRenderingTimeout()).thenReturn(50);
        when(targetLauncherSettings.confirmQuitDialogRenderingDelay()).thenReturn(10);
        when(targetLauncherSettings.closingWindowTimeout()).thenReturn(100);
        when(targetLauncherSettings.closingWindowDelay()).thenReturn(10);
        //window
        when(window.getWindowMouse(SPEED_FACTOR)).thenReturn(mouse);
        when(mouse.move(any(PointInt.class))).thenReturn(mouse);
        when(mouse.move(any(PointFloat.class))).thenReturn(mouse);
        when(window.getKeyboard()).thenReturn(keyboard);
        when(keyboard.enterHotKey(any(HotKey.class))).thenReturn(keyboard);
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
    void clientIsAlreadyRunningWindowRendered() throws InterruptedException, CannotGetUserInputException {
        System.out.println("clientIsAlreadyRunningWindowRendered");
        //given
        windowIsAlive();
        launcherWindowMocked().stateIs(found(clientIsAlreadyRunningStamp));
        //when
        getLauncherWindowInstance().restoreAndDo(restoredLauncherWindow -> assertTrue(restoredLauncherWindow.checkClientIsAlreadyRunningWindowRendered()));
        //then
        verify(targetLauncherStamps).clientIsAlreadyRunning();
    }

    @Test
    void clientIsAlreadyRunningWindowNotRendered() throws InterruptedException, CannotGetUserInputException {
        System.out.println("clientIsAlreadyRunningWindowNotRendered");
        //given
        windowIsAlive();
        launcherWindowMocked().stateIs(notFound(clientIsAlreadyRunningStamp));
        //when
        getLauncherWindowInstance().restoreAndDo(restoredLauncherWindow -> assertFalse(restoredLauncherWindow.checkClientIsAlreadyRunningWindowRendered()));
        //then
        verify(targetLauncherStamps).clientIsAlreadyRunning();
    }

    @Test
    void launcherNotRenderedError() throws InterruptedException, CannotGetUserInputException {
        System.out.println("launcherNotRenderedError");
        final Mouse launcherMouse = getMouseMock();
        //given
        windowIsAlive();
        launcherWindowMocked().stateIs(notFoundAnyFrom(stampsToCheckLauncherRendering)).returnsMouse(launcherMouse);
        //when and then
        assertThrows(NeedRetryException.class,
                () -> getLauncherWindowInstance().restoreAndDo(restoredLauncherWindow -> restoredLauncherWindow.login(account)));
    }

    @Test
    void loginSuccessfully() throws InterruptedException, CannotGetUserInputException {
        System.out.println("loginSuccessfully");
        final Mouse launcherMouse = getMouseMock();
        //given
        windowIsAlive();
        launcherWindowMocked().stateIs(foundFrom(stampsToCheckLauncherRendering, loginButtonNoErrorInactiveStamp)).returnsMouse(launcherMouse);
        //when
        getLauncherWindowInstance().restoreAndDo(restoredLauncherWindow -> restoredLauncherWindow.login(account));
        //then
        verify(account, atLeastOnce()).login();
        verify(account, atLeastOnce()).password();
        verify(launcherMouse).clickAtPoint(loginButtonPoint);
    }

    @Test
    void startGameWhenGetReady() throws InterruptedException, CannotGetUserInputException {
        System.out.println("startGameWhenGetReady");
        final Mouse launcherMouse = getMouseMock();
        //given
        windowIsAlive();
        launcherWindowMocked().stateIs(found(startButtonActiveStamp)).returnsMouse(launcherMouse);
        //when
        getLauncherWindowInstance().restoreAndDo(RestoredLauncherWindow::startGameWhenGetReady);
        //then
        verify(launcherMouse).clickAtPoint(startButtonPoint);
    }

    @Test
    void startGameNotReady_windowClosedNormally() throws InterruptedException, CannotGetUserInputException {
        System.out.println("startGameNotReady_windowClosedNormally");
        final Mouse launcherMouse = getMouseMock();
        final Mouse confirmDialogMouse = getMouseMock();
        //given
        when(window.isExists())
                .thenReturn(true) // restoring window
                .thenReturn(false); // confirm quit dialog accepted
        launcherWindowMocked().stateIs(notFound(startButtonActiveStamp)).returnsMouse(launcherMouse);
        confirmCloseDialogWindowMocked().stateIs(found(quitConfirmPopupStamp)).returnsMouse(confirmDialogMouse);
        //when and then
        assertThrows(NeedRetryException.class, () -> getLauncherWindowInstance().restoreAndDo(RestoredLauncherWindow::startGameWhenGetReady));
        verify(targetLauncherSettings).closeQuitConfirmPopupButtonPoint();
        verify(confirmDialogMouse).clickAtPoint(closeQuitConfirmPopupButtonPoint);
        verify(launcherMouse, never()).clickAtPoint(startButtonPoint);
        verify(process, never()).terminate();
    }

    @Test
    void startGameNotReady_windowProcessTerminated() throws InterruptedException, CannotGetUserInputException {
        System.out.println("startGameNotReady_windowProcessTerminated");
        //given
        windowIsCorrupted();
        launcherWindowMocked().stateIs(notFound(startButtonActiveStamp));
        //when and then
        assertThrows(NeedRetryException.class, () -> getLauncherWindowInstance().restoreAndDo(RestoredLauncherWindow::startGameWhenGetReady));
        verify(targetLauncherSettings, never()).closeQuitConfirmPopupButtonPoint();
        verify(mouse, never()).move(startButtonPoint);
        verify(process).terminate();
    }

    private LauncherWindowImpl getLauncherWindowInstance() {
        return new LauncherWindowImpl(window, commonWindowService, settings, stamps);
    }

    private void windowIsAlive() {
        when(window.isExists()).thenReturn(true);
        when(window.isVisible()).thenReturn(true);
    }

    private void windowIsCorrupted() {
        when(window.isExists()).thenReturn(true);
        when(window.isVisible()).thenReturn(false);
    }

    private Mouse getMouseMock() {
        return mock(Mouse.class);
    }

    private ForegroundWindowMocked launcherWindowMocked() throws InterruptedException, CannotGetUserInputException {
        return getForegroundWindowFor(window);
    }

    private ForegroundWindowMocked confirmCloseDialogWindowMocked() throws InterruptedException, CannotGetUserInputException {
        final IWindow confirmDialogWindowMock = getConfirmDialogWindowMock();
        final ForegroundWindowMocked foregroundWindow = getForegroundWindowFor(confirmDialogWindowMock);
        final List<IWindow> foundConfirmDialogWindow = List.of(confirmDialogWindowMock);
        when(win32System.findAllWindows(CONFIRM_DIALOG_TITLE, null, true)).thenReturn(foundConfirmDialogWindow);
        return foregroundWindow;
    }

    private IWindow getConfirmDialogWindowMock() {
        final IWindow mock = mock(IWindow.class);
        when(mock.getSystemId()).thenReturn("0xa8d5");
        when(mock.getWindowRectangle()).thenReturn(new Rectangle(0, 0, CONFIRM_QUIT_DIALOG_WINDOW_WIDTH, CONFIRM_QUIT_DIALOG_WINDOW_HEIGHT));
        return mock;
    }

    @SuppressWarnings("unchecked")
    private ForegroundWindowMocked getForegroundWindowFor(final IWindow window) throws CannotGetUserInputException, InterruptedException {
        final ForegroundWindow foregroundWindow = mock(ForegroundWindow.class);
        when(foregroundWindow.getKeyboard()).thenReturn(keyboard);
        final CommonWindowService.ForegroundWindowSupplier foregroundWindowSupplier = mock(CommonWindowService.ForegroundWindowSupplier.class);
        when(commonWindowService.bringForeground(window)).thenReturn(foregroundWindowSupplier);
        doAnswer(invocation -> {
            final CommonWindowService.ForegroundWindowSupplier.ForegroundWindowConsumer consumer = invocation.getArgument(0);
            consumer.accept(foregroundWindow);
            return null;
        }).when(foregroundWindowSupplier).andDo(any(CommonWindowService.ForegroundWindowSupplier.ForegroundWindowConsumer.class));
        when(foregroundWindowSupplier.andDo(any(CommonWindowService.ForegroundWindowSupplier.ForegroundWindowFunction.class))).thenAnswer(invocation -> {
            final CommonWindowService.ForegroundWindowSupplier.ForegroundWindowFunction<ForegroundWindow> function = invocation.getArgument(0);
            return function.apply(foregroundWindow);
        });
        return new ForegroundWindowMocked(foregroundWindow);
    }

    private ForegroundWindow.StateWaiting getStateWaitingMock() {
        final ForegroundWindow.StateWaiting stateWaiting = mock(ForegroundWindow.StateWaiting.class);
        when(stateWaiting.withTimeout(anyInt())).thenReturn(stateWaiting);
        when(stateWaiting.withDelay(anyInt())).thenReturn(stateWaiting);
        when(stateWaiting.withMousePosition(any(PointFloat.class))).thenReturn(stateWaiting);
        when(stateWaiting.usingHotKey(any())).thenReturn(stateWaiting);
        when(stateWaiting.usingHotKeyEnclose(any())).thenReturn(stateWaiting);
        when(stateWaiting.usingMouseClickAt(any())).thenReturn(stateWaiting);
        when(stateWaiting.logFailedStampsWithMarker(anyString())).thenReturn(stateWaiting);
        return stateWaiting;
    }

    private ForegroundWindow.StateWaiting found(final Stamp stamp) throws InterruptedException, CannotGetUserInputException {
        final ForegroundWindow.StateWaiting stateWaiting = getStateWaitingMock();
        when(stateWaiting.forStamp(stamp)).thenReturn(Optional.of(stamp));
        return stateWaiting;
    }

    private ForegroundWindow.StateWaiting notFound(final Stamp stamp) throws InterruptedException, CannotGetUserInputException {
        final ForegroundWindow.StateWaiting stateWaiting = getStateWaitingMock();
        when(stateWaiting.forStamp(stamp)).thenReturn(Optional.empty());
        return stateWaiting;
    }

    private ForegroundWindow.StateWaiting foundFrom(final Set<Stamp> stampsToCheck, final Stamp stamp) throws InterruptedException, CannotGetUserInputException {
        final ForegroundWindow.StateWaiting stateWaiting = getStateWaitingMock();
        when(stateWaiting.forAnyStamp(any())).thenAnswer(invocation -> {
            checkValidatorInvocation(invocation, stampsToCheck);
            return Optional.of(stamp);
        });
        return stateWaiting;
    }

    private ForegroundWindow.StateWaiting notFoundAnyFrom(final Set<Stamp> stampsToCheck) throws InterruptedException, CannotGetUserInputException {
        final ForegroundWindow.StateWaiting stateWaiting = getStateWaitingMock();
        when(stateWaiting.forAnyStamp(any())).thenAnswer(invocation -> {
            checkValidatorInvocation(invocation, stampsToCheck);
            return Optional.empty();
        });
        return stateWaiting;
    }

    private void checkValidatorInvocation(final InvocationOnMock invocation, final Set<Stamp> stampsToCheck) {
        final int shouldBePassedArguments = stampsToCheck.size();
        final Object[] arguments = invocation.getArguments();
        assertEquals(shouldBePassedArguments, arguments.length, () -> "wrong arguments passed to check (should be " + shouldBePassedArguments + ")");
        final Set<Stamp> passedStamps = Arrays.stream(arguments)
                .map(Stamp.class::cast)
                .collect(Collectors.toSet());
        assertTrue(passedStamps.containsAll(stampsToCheck));
        assertTrue(stampsToCheck.containsAll(passedStamps));
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static class ForegroundWindowMocked {

        private final ForegroundWindow foregroundWindow;

        ForegroundWindowMocked(final ForegroundWindow foregroundWindow) {
            this.foregroundWindow = foregroundWindow;
        }

        private ForegroundWindowMocked returnsMouse(final Mouse mouse) throws InterruptedException, CannotGetUserInputException {
            when(foregroundWindow.getMouse()).thenReturn(mouse);
            return this;
        }

        private ForegroundWindowMocked stateIs(final ForegroundWindow.StateWaiting state) {
            when(foregroundWindow.waiting()).thenReturn(state);
            return this;
        }

        private ForegroundWindowMocked windowStatesAre(final ForegroundWindow.StateWaiting... states) {
            MockitoUtil.INSTANCE.mockReturnVararg(foregroundWindow.waiting(), List.of(states));
            return this;
        }
    }
}