package midorum.melbone.window.internal.baseapp;

import com.midorum.win32api.facade.Either;
import com.midorum.win32api.facade.IMouse;
import com.midorum.win32api.facade.IProcess;
import com.midorum.win32api.facade.IWindow;
import com.midorum.win32api.facade.exception.Win32ApiException;
import com.midorum.win32api.struct.PointFloat;
import com.midorum.win32api.struct.PointInt;
import midorum.melbone.model.dto.KeyShortcut;
import midorum.melbone.model.exception.CannotGetUserInputException;
import midorum.melbone.model.exception.CriticalErrorException;
import midorum.melbone.model.settings.account.AccountBinding;
import midorum.melbone.model.settings.setting.ApplicationSettings;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.model.settings.setting.TargetBaseAppSettings;
import midorum.melbone.model.settings.stamp.Stamp;
import midorum.melbone.model.settings.stamp.Stamps;
import midorum.melbone.model.settings.stamp.TargetBaseAppStamps;
import midorum.melbone.model.window.baseapp.InGameBaseAppWindow;
import midorum.melbone.model.window.baseapp.RestoredBaseAppWindow;
import midorum.melbone.settings.StampKeys;
import midorum.melbone.window.internal.common.CommonWindowService;
import midorum.melbone.window.internal.common.ForegroundWindow;
import midorum.melbone.window.internal.common.Mouse;
import midorum.melbone.window.internal.util.ForegroundWindowMocked;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BaseAppWindowImplTest {

    private static final String RESOURCE_ID = "resource_id";
    private static final String ACCOUNT_NAME = "account_name";
    private static final float SPEED_FACTOR = 1.0f;
    private static final int TIMEOUT_FOR_TEST = 100;
    private static final int DELAY_FOR_TEST = 50;
    private static final int TIMEOUT_FOR_TEST_1 = 25;
    private static final int DELAY_FOR_TEST_1 = 10;
    private static final float ACTION_BUTTON_POINT_X = 1f;
    private static final float ACTION_BUTTON_POINT_X_OFFSET = 0.5f;
    private static final float ACTION_BUTTON_POINT_Y = 1.1f;
    private final Settings settings = mock(Settings.class);
    private final ApplicationSettings applicationSettings = mock(ApplicationSettings.class);
    private final TargetBaseAppSettings targetBaseAppSettings = mock(TargetBaseAppSettings.class);
    private final AccountBinding accountBinding = mock(AccountBinding.class);
    private final Stamps stamps = mock(Stamps.class);
    private final TargetBaseAppStamps targetBaseAppStamps = mock(TargetBaseAppStamps.class);
    private final CommonWindowService commonWindowService = mock(CommonWindowService.class);
    private final IWindow window = mock(IWindow.class);
    private final IMouse mouse = mock(IMouse.class);
    private final IProcess process = mock(IProcess.class);
    //points
    private final PointFloat manaIndicatorPoint = new PointFloat(-1f, -1f);
    private final PointFloat menuExitOptionPoint = new PointFloat(-1f, -1f);
    private final PointFloat windowMinimizeButtonPoint = new PointFloat(-1f, -1f);
    private final PointFloat disconnectedPopupCloseButtonPoint = new PointFloat(-1f, -1f);
    private final PointFloat windowCloseButtonPoint = new PointFloat(-1f, -1f);
    private final PointFloat screenSettingsTabPoint = new PointFloat(-1f, -1f);
    private final PointFloat uiScaleChooser80Point = new PointFloat(-1f, -1f);
    private final PointFloat soundSettingsTabPoint = new PointFloat(-1f, -1f);
    private final PointFloat overallVolumeChooser0Point = new PointFloat(-1f, -1f);
    private final PointFloat optionsApplyButtonPoint = new PointFloat(-1f, -1f);
    private final PointFloat needRestartPopupConfirmButtonPoint = new PointFloat(-1f, -1f);
    private final PointFloat openOptionsButtonPoint = new PointFloat(-1f, -1f);
    private final PointFloat selectServerButtonPoint = new PointFloat(-1.3487f, -1f);
    private final PointFloat connectServerButtonPoint = new PointFloat(-1.894534f, -1f);
    private final PointFloat selectCharacterButtonPoint = new PointFloat(-1.456f, -1f);
    private final PointFloat startButtonPoint = new PointFloat(-1.678f, -1f);
    private final PointFloat dailyTrackerButtonPointer = new PointFloat(-1f, -1f);
    private final PointFloat dailyTrackerTabPointer = new PointFloat(-1f, -1f);
    private final PointFloat trackLoginButtonPointer = new PointFloat(-1.1345f, -1f);
    private final PointFloat closeDailyTrackerPopupButtonPointer = new PointFloat(-1f, -1f);
    private final PointFloat actionButtonPoint = new PointFloat(ACTION_BUTTON_POINT_X, ACTION_BUTTON_POINT_Y);
    private final PointFloat actionSecondButtonPoint = new PointFloat(ACTION_BUTTON_POINT_X + ACTION_BUTTON_POINT_X_OFFSET, -1f);
    //stamps
    private final Stamp menuExitOptionStamp = mock(Stamp.class);
    private final Stamp accountInfoPopupCaptionStamp = mock(Stamp.class);
    private final Stamp disconnectedPopupStamp = mock(Stamp.class);
    private final Stamp optionsButtonBaseScaleStamp = mock(Stamp.class);
    private final Stamp optionsButtonDefaultScaleStamp = mock(Stamp.class);
    private final Stamp optionsPopupCaptionStamp = mock(Stamp.class);
    private final Stamp needRestartPopupStamp = mock(Stamp.class);
    private final Stamp serverLineUnselectedStamp = mock(Stamp.class);
    private final Stamp serverLineSelectedStamp = mock(Stamp.class);
    private final Stamp startButtonStamp = mock(Stamp.class);
    private final Stamp dailyTrackerPopupCaptionStamp = mock(Stamp.class);
    private final Set<Stamp> stampsToCheckServerPageRendering = new HashSet<>() {{
        add(optionsButtonBaseScaleStamp);
        add(optionsButtonDefaultScaleStamp);
    }};
    private final Set<Stamp> stampsToCheckServerLineRendering = new HashSet<>() {{
        add(serverLineUnselectedStamp);
        add(serverLineSelectedStamp);
    }};
    //hot keys
    final KeyShortcut stopAnimationHotkey = mock(KeyShortcut.class);
    final KeyShortcut openMenuHotkey = mock(KeyShortcut.class);
    final KeyShortcut openAccountInfoHotkey = mock(KeyShortcut.class);

    @BeforeAll
    public static void beforeAll() {
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    @BeforeEach
    public void beforeEach() throws InterruptedException, CannotGetUserInputException, Win32ApiException {
        //system
        when(commonWindowService.getUID(window)).thenReturn(Either.resultOf(() -> RESOURCE_ID));
        //settings
        when(settings.application()).thenReturn(applicationSettings);
        when(settings.targetBaseAppSettings()).thenReturn(targetBaseAppSettings);
        when(applicationSettings.speedFactor()).thenReturn(SPEED_FACTOR);
        //settings: points
        when(targetBaseAppSettings.menuExitOptionPoint()).thenReturn(menuExitOptionPoint);
        when(targetBaseAppSettings.manaIndicatorPoint()).thenReturn(manaIndicatorPoint);
        when(targetBaseAppSettings.windowMinimizeButtonPoint()).thenReturn(windowMinimizeButtonPoint);
        when(targetBaseAppSettings.disconnectedPopupCloseButtonPoint()).thenReturn(disconnectedPopupCloseButtonPoint);
        when(targetBaseAppSettings.windowCloseButtonPoint()).thenReturn(windowCloseButtonPoint);
        when(targetBaseAppSettings.screenSettingsTabPoint()).thenReturn(screenSettingsTabPoint);
        when(targetBaseAppSettings.uiScaleChooser80Point()).thenReturn(uiScaleChooser80Point);
        when(targetBaseAppSettings.soundSettingsTabPoint()).thenReturn(soundSettingsTabPoint);
        when(targetBaseAppSettings.overallVolumeZeroLevelPoint()).thenReturn(overallVolumeChooser0Point);
        when(targetBaseAppSettings.optionsApplyButtonPoint()).thenReturn(optionsApplyButtonPoint);
        when(targetBaseAppSettings.needRestartPopupConfirmButtonPoint()).thenReturn(needRestartPopupConfirmButtonPoint);
        when(targetBaseAppSettings.openOptionsButtonPoint()).thenReturn(openOptionsButtonPoint);
        when(targetBaseAppSettings.selectServerButtonPoint()).thenReturn(selectServerButtonPoint);
        when(targetBaseAppSettings.connectServerButtonPoint()).thenReturn(connectServerButtonPoint);
        when(targetBaseAppSettings.selectCharacterButtonPoint()).thenReturn(selectCharacterButtonPoint);
        when(targetBaseAppSettings.startButtonPoint()).thenReturn(startButtonPoint);
        when(targetBaseAppSettings.dailyTrackerButtonPoint()).thenReturn(dailyTrackerButtonPointer);
        when(targetBaseAppSettings.dailyTrackerTabPoint()).thenReturn(dailyTrackerTabPointer);
        when(targetBaseAppSettings.trackLoginButtonPoint()).thenReturn(trackLoginButtonPointer);
        when(targetBaseAppSettings.closeDailyTrackerPopupButtonPoint()).thenReturn(closeDailyTrackerPopupButtonPointer);
        when(targetBaseAppSettings.actionButtonPoint()).thenReturn(actionButtonPoint);
        when(targetBaseAppSettings.actionSecondButtonPoint()).thenReturn(actionSecondButtonPoint);
        //settings: timeouts
        when(targetBaseAppSettings.menuRenderingTimeout()).thenReturn(TIMEOUT_FOR_TEST);
        when(targetBaseAppSettings.checkMenuRenderingDelay()).thenReturn(DELAY_FOR_TEST);
        when(targetBaseAppSettings.windowDisappearingTimeout()).thenReturn(TIMEOUT_FOR_TEST);
        when(targetBaseAppSettings.checkWindowDisappearingDelay()).thenReturn(DELAY_FOR_TEST);
        when(targetBaseAppSettings.serverPageRenderingTimeout()).thenReturn(TIMEOUT_FOR_TEST);
        when(targetBaseAppSettings.checkServerPageRenderingDelay()).thenReturn(DELAY_FOR_TEST);
        when(targetBaseAppSettings.startButtonRenderingTimeout()).thenReturn(TIMEOUT_FOR_TEST);
        when(targetBaseAppSettings.checkStartButtonRenderingDelay()).thenReturn(DELAY_FOR_TEST);
        when(targetBaseAppSettings.disconnectedPopupRenderingTimeout()).thenReturn(TIMEOUT_FOR_TEST);
        when(targetBaseAppSettings.checkDisconnectedPopupRenderingDelay()).thenReturn(DELAY_FOR_TEST);
        when(targetBaseAppSettings.baseWindowRenderingTimeout()).thenReturn(TIMEOUT_FOR_TEST);
        when(targetBaseAppSettings.checkBaseWindowRenderingDelay()).thenReturn(DELAY_FOR_TEST);
        when(targetBaseAppSettings.optionsDialogRenderingTimeout()).thenReturn(TIMEOUT_FOR_TEST);
        when(targetBaseAppSettings.checkOptionsDialogRenderingDelay()).thenReturn(DELAY_FOR_TEST);
        when(targetBaseAppSettings.needRestartPopupRenderingTimeout()).thenReturn(TIMEOUT_FOR_TEST);
        when(targetBaseAppSettings.checkNeedRestartPopupRenderingDelay()).thenReturn(DELAY_FOR_TEST);
        when(targetBaseAppSettings.serverLineRenderingTimeout()).thenReturn(TIMEOUT_FOR_TEST);
        when(targetBaseAppSettings.checkServerLineRenderingDelay()).thenReturn(DELAY_FOR_TEST);
        when(targetBaseAppSettings.dailyTrackerPopupRenderingTimeout()).thenReturn(TIMEOUT_FOR_TEST);
        when(targetBaseAppSettings.checkDailyTrackerPopupRenderingDelay()).thenReturn(DELAY_FOR_TEST);
        when(targetBaseAppSettings.accountInfoPopupRenderingTimeout()).thenReturn(TIMEOUT_FOR_TEST_1);
        when(targetBaseAppSettings.accountInfoPopupRenderingDelay()).thenReturn(DELAY_FOR_TEST_1);
        //settings: hot keys
        when(targetBaseAppSettings.stopAnimationHotkey()).thenReturn(stopAnimationHotkey);
        when(targetBaseAppSettings.openMenuHotkey()).thenReturn(openMenuHotkey);
        when(targetBaseAppSettings.openAccountInfoHotkey()).thenReturn(openAccountInfoHotkey);
        //window
        when(window.getSystemId()).thenReturn("0xdf67");
        when(window.getWindowMouse(SPEED_FACTOR)).thenReturn(mouse);
        when(mouse.move(any(PointInt.class))).thenReturn(mouse);
        when(mouse.move(any(PointFloat.class))).thenReturn(mouse);
        when(mouse.move(anyFloat(), anyFloat())).thenReturn(mouse);
        when(mouse.leftClick()).thenReturn(mouse);
        when(window.getProcess()).thenReturn(Either.value(() -> process).whenReturnsTrue(true));
        //stamps
        when(stamps.targetBaseApp()).thenReturn(targetBaseAppStamps);
        when(targetBaseAppStamps.menuExitOption()).thenReturn(menuExitOptionStamp);
        when(menuExitOptionStamp.key()).thenReturn(StampKeys.TargetBaseApp.menuExitOption);
        when(targetBaseAppStamps.accountInfoPopupCaption()).thenReturn(accountInfoPopupCaptionStamp);
        when(accountInfoPopupCaptionStamp.key()).thenReturn(StampKeys.TargetBaseApp.accountInfoPopupCaption);
        when(targetBaseAppStamps.disconnectedPopup()).thenReturn(disconnectedPopupStamp);
        when(disconnectedPopupStamp.key()).thenReturn(StampKeys.TargetBaseApp.disconnectedPopup);
        when(targetBaseAppStamps.optionsButtonBaseScale()).thenReturn(optionsButtonBaseScaleStamp);
        when(optionsButtonBaseScaleStamp.key()).thenReturn(StampKeys.TargetBaseApp.optionsButtonBaseScale);
        when(targetBaseAppStamps.optionsButtonDefaultScale()).thenReturn(optionsButtonDefaultScaleStamp);
        when(optionsButtonDefaultScaleStamp.key()).thenReturn(StampKeys.TargetBaseApp.optionsButtonDefaultScale);
        when(targetBaseAppStamps.optionsPopupCaption()).thenReturn(optionsPopupCaptionStamp);
        when(optionsPopupCaptionStamp.key()).thenReturn(StampKeys.TargetBaseApp.optionsPopupCaption);
        when(targetBaseAppStamps.needRestartPopup()).thenReturn(needRestartPopupStamp);
        when(needRestartPopupStamp.key()).thenReturn(StampKeys.TargetBaseApp.needRestartPopup);
        when(targetBaseAppStamps.serverLineUnselected()).thenReturn(serverLineUnselectedStamp);
        when(serverLineUnselectedStamp.key()).thenReturn(StampKeys.TargetBaseApp.serverLineUnselected);
        when(targetBaseAppStamps.serverLineSelected()).thenReturn(serverLineSelectedStamp);
        when(serverLineSelectedStamp.key()).thenReturn(StampKeys.TargetBaseApp.serverLineSelected);
        when(targetBaseAppStamps.startButton()).thenReturn(startButtonStamp);
        when(startButtonStamp.key()).thenReturn(StampKeys.TargetBaseApp.startButton);
        when(targetBaseAppStamps.dailyTrackerPopupCaption()).thenReturn(dailyTrackerPopupCaptionStamp);
        when(dailyTrackerPopupCaptionStamp.key()).thenReturn(StampKeys.TargetBaseApp.dailyTrackerPopupCaption);
    }

    @AfterEach
    public void afterEach() {
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    @Test
    void getCharacterNameForBoundWindow() throws Win32ApiException {
        System.out.println("getCharacterNameForBoundWindow");
        //given
        accountBoundWithWindow();
        //when
        final Optional<String> maybeCharacterName = getBaseAppWindowInstance().getCharacterName().getOrThrow();
        //then
        assertTrue(maybeCharacterName.isPresent());
        assertEquals(ACCOUNT_NAME, maybeCharacterName.get());
    }

    @Test
    void getCharacterNameForUnboundWindow() throws Win32ApiException {
        System.out.println("getCharacterNameForUnboundWindow");
        //given
        accountNotBoundWithWindow();
        //when
        final Optional<String> maybeCharacterName = getBaseAppWindowInstance().getCharacterName().getOrThrow();
        //then
        assertTrue(maybeCharacterName.isEmpty());
    }

    @Test
    void bindWithAccount() throws Win32ApiException {
        System.out.println("bindWithAccount");
        //when
        getBaseAppWindowInstance().bindWithAccount(ACCOUNT_NAME);
        //then
        verify(accountBinding).bindResource(ACCOUNT_NAME, RESOURCE_ID);
    }

    @Test
    void terminatingWindowProcess() throws InterruptedException, CannotGetUserInputException, Win32ApiException {
        System.out.println("terminatingWindowProcess");
        //when
        when(window.isExists())
                .thenReturn(Either.resultOf(() -> true)) // restoring window
                .thenReturn(Either.resultOf(() -> true)) // trying close normally
                .thenReturn(Either.resultOf(() -> true)) // killing window process
                .thenReturn(Either.resultOf(() -> false)); // window disappeared
        windowIsCorrupted();
        getBaseAppWindowInstance().restoreAndDo(restoredBaseAppWindow -> {/*any operation*/});
        //then
        verifyWasAttemptTerminateWindowProcess();
        verifyWindowDisappeared();
    }

    @Test
    void cannotTerminateWindowProcess() throws InterruptedException, CannotGetUserInputException, Win32ApiException {
        System.out.println("cannotTerminateWindowProcess");
        //when
        when(window.isExists())
                .thenReturn(Either.resultOf(() -> true)) // restoring window
                .thenReturn(Either.resultOf(() -> true)) // trying close normally
                .thenReturn(Either.resultOf(() -> true)) // killing window process
                .thenReturn(Either.resultOf(() -> true)); // cannot terminate window process
        windowIsCorrupted();
        assertThrows(CriticalErrorException.class, () -> getBaseAppWindowInstance().restoreAndDo(restoredBaseAppWindow -> {/*any operation*/}));
        //then
        verifyWasAttemptTerminateWindowProcess();
        verifyWindowHasNotDisappeared();
    }

    @Test
    void closeDisconnectedWindow() throws InterruptedException, CannotGetUserInputException, Win32ApiException {
        System.out.println("closeDisconnectedWindow");
        //given
        final Mouse mouse = getMouseMock();
        when(window.isExists())
                .thenReturn(Either.resultOf(() -> true)) // restoring window
                .thenReturn(Either.resultOf(() -> true)) // confirm dialog
                .thenReturn(Either.resultOf(() -> false)); // window closed
        windowIsHealthy();
        baseWindowMocked().stateIs(found(disconnectedPopupStamp))
                .returnsMouse(mouse);
        //when
        getBaseAppWindowInstance().restoreAndDo(restoredBaseAppWindow -> {/*any operation*/});
        //then
        verifyDisconnectedPopupConfirmed(mouse);
        verifyWindowDisappeared();
        verifyDidNotAttemptsTerminateWindowProcess();
    }

    @Test
    void closeWindowViaFrame() throws InterruptedException, CannotGetUserInputException, Win32ApiException {
        System.out.println("closeWindowViaFrame");
        final Mouse mouse = getMouseMock();
        //when
        when(window.isExists())
                .thenReturn(Either.resultOf(() -> true)) // restoring window
                .thenReturn(Either.resultOf(() -> true)) // trying close normally
                .thenReturn(Either.resultOf(() -> true)) // closing window frame
                .thenReturn(Either.resultOf(() -> true)) // window hasn't closed yet
                .thenReturn(Either.resultOf(() -> false)); // window closed
        windowIsHealthy();
        baseWindowMocked().windowStatesAre(notFound(disconnectedPopupStamp), notFound(menuExitOptionStamp))
                .returnsMouse(mouse);
        getBaseAppWindowInstance().restoreAndDo(RestoredBaseAppWindow::close);
        //then
        verifyWindowCloseButtonClicked();
        verifyWindowDisappeared();
        verifyDidNotAttemptsTerminateWindowProcess();
    }

    @Test
    void closeWindowViaMenu() throws InterruptedException, CannotGetUserInputException, Win32ApiException {
        System.out.println("closeWindowViaMenu");
        final Mouse mouse = getMouseMock();
        //when
        when(window.isExists())
                .thenReturn(Either.resultOf(() -> true)) // restoring window
                .thenReturn(Either.resultOf(() -> true)) // waiting window disappearing
                .thenReturn(Either.resultOf(() -> false)); // window closed
        windowIsHealthy();
        baseWindowMocked().windowStatesAre(notFound(disconnectedPopupStamp), found(menuExitOptionStamp))
                .returnsMouse(mouse);
        getBaseAppWindowInstance().restoreAndDo(RestoredBaseAppWindow::close);
        //then
        verifyMenuCloseItemClicked(mouse);
        verifyWindowDisappeared();
        verifyDidNotAttemptsTerminateWindowProcess();
    }

    @Test
    void selectServer() throws InterruptedException, CannotGetUserInputException, Win32ApiException {
        System.out.println("selectServer");
        final Mouse mouse = getMouseMock();
        //when
        windowIsExists();
        windowIsHealthy();
        baseWindowMocked().windowStatesAre(notFound(disconnectedPopupStamp),
                        foundFrom(stampsToCheckServerPageRendering, optionsButtonBaseScaleStamp),
                        notFound(disconnectedPopupStamp),
                        foundFrom(stampsToCheckServerLineRendering, serverLineUnselectedStamp),
                        notFound(disconnectedPopupStamp))
                .returnsMouse(mouse);
        getBaseAppWindowInstance().restoreAndDo(RestoredBaseAppWindow::selectServer);
        //then
        verifyServerWasSelectedAndConnected(mouse);
    }

    @Test
    void chooseCharacter() throws InterruptedException, CannotGetUserInputException, Win32ApiException {
        System.out.println("chooseCharacter");
        final Mouse mouse = getMouseMock();
        //when
        windowIsExists();
        windowIsHealthy();
        baseWindowMocked().windowStatesAre(notFound(disconnectedPopupStamp), found(startButtonStamp), notFound(disconnectedPopupStamp))
                .returnsMouse(mouse);
        getBaseAppWindowInstance().restoreAndDo(RestoredBaseAppWindow::chooseCharacter);
        //then
        verifyGameStarted(mouse);
    }

    @Test
    void checkInGameWindowRenderedButFail() throws InterruptedException, CannotGetUserInputException, Win32ApiException {
        System.out.println("checkInGameWindowRenderedButFail");
        final Mouse mouse = getMouseMock();
        //when
        when(window.isExists())
                .thenReturn(Either.resultOf(() -> true)) // restoring window
                .thenReturn(Either.resultOf(() -> true)) // trying close normally
                .thenReturn(Either.resultOf(() -> true)) // closing window frame
                .thenReturn(Either.resultOf(() -> true)) // window hasn't closed yet
                .thenReturn(Either.resultOf(() -> false)); // window closed
        windowIsHealthy();
        baseWindowMocked().windowStatesAre(notFound(disconnectedPopupStamp), notFound(accountInfoPopupCaptionStamp))
                .returnsMouse(mouse);
        getBaseAppWindowInstance().restoreAndDo(RestoredBaseAppWindow::checkInGameWindowRendered);
        //then
        verifyWindowCloseButtonClicked();
        verifyWindowDisappeared();
        verifyDidNotAttemptsTerminateWindowProcess();
    }

    @Test
    void checkInGameWindowRendered() throws InterruptedException, CannotGetUserInputException, Win32ApiException {
        System.out.println("checkInGameWindowRendered");
        final Mouse mouse = getMouseMock();
        //when
        windowIsExists();
        windowIsHealthy();
        baseWindowMocked().windowStatesAre(notFound(disconnectedPopupStamp), found(accountInfoPopupCaptionStamp))
                .returnsMouse(mouse);
        getBaseAppWindowInstance().restoreAndDo(RestoredBaseAppWindow::checkInGameWindowRendered);
        //then
        verifyWindowHasNotDisappeared();
        verifyDidNotAttemptsTerminateWindowProcess();
    }

    @Test
    void inGameWindowNotRenderedProperly() throws InterruptedException, CannotGetUserInputException, Win32ApiException {
        System.out.println("inGameWindowNotRenderedProperly");
        final Mouse mouse = getMouseMock();
        //when
        when(window.isExists())
                .thenReturn(Either.resultOf(() -> true)) // restoring window
                .thenReturn(Either.resultOf(() -> true)) // trying close normally
                .thenReturn(Either.resultOf(() -> true)) // closing window frame
                .thenReturn(Either.resultOf(() -> true)) // window hasn't closed yet
                .thenReturn(Either.resultOf(() -> false)); // window closed
        windowIsHealthy();
        baseWindowMocked().windowStatesAre(notFound(disconnectedPopupStamp), notFound(accountInfoPopupCaptionStamp))
                .returnsMouse(mouse);
        getBaseAppWindowInstance().doInGameWindow(InGameBaseAppWindow::checkInLoginTracker);
        //then
        verifyWindowCloseButtonClicked();
        verifyWindowDisappeared();
    }

    @Test
    void checkInLoginTracker() throws InterruptedException, CannotGetUserInputException, Win32ApiException {
        System.out.println("checkInLoginTracker");
        final Mouse mouse = getMouseMock();
        //when
        windowIsExists();
        windowIsHealthy();
        baseWindowMocked().windowStatesAre(notFound(disconnectedPopupStamp), found(accountInfoPopupCaptionStamp), found(dailyTrackerPopupCaptionStamp))
                .returnsMouse(mouse);
        getBaseAppWindowInstance().doInGameWindow(InGameBaseAppWindow::checkInLoginTracker);
        //then
        verifyLoginTrackerWasCheckedIn(mouse);
    }

    @Test
    void checkInZeroAction() throws InterruptedException, CannotGetUserInputException, Win32ApiException {
        System.out.println("checkInZeroAction");
        //when
        final int actionsCount = 0;
        checkInActionsTest(actionsCount);
    }

    @Test
    void checkInOneAction() throws InterruptedException, CannotGetUserInputException, Win32ApiException {
        System.out.println("checkInOneAction");
        //when
        final int actionsCount = 1;
        checkInActionsTest(actionsCount);
    }

    @Test
    void checkInTwoActions() throws InterruptedException, CannotGetUserInputException, Win32ApiException {
        System.out.println("checkInTwoActions");
        //when
        final int actionsCount = 2;
        checkInActionsTest(actionsCount);
    }

    @Test
    void checkInThreeActions() throws InterruptedException, CannotGetUserInputException, Win32ApiException {
        System.out.println("checkInThreeActions");
        //when
        final int actionsCount = 3;
        checkInActionsTest(actionsCount);
    }

    private void checkInActionsTest(final int actionsCount) throws InterruptedException, CannotGetUserInputException, Win32ApiException {
        final Mouse mouse = getMouseMock();
        windowIsExists();
        windowIsHealthy();
        setActionsCount(actionsCount);
        baseWindowMocked().windowStatesAre(notFound(disconnectedPopupStamp), found(accountInfoPopupCaptionStamp))
                .returnsMouse(mouse);
        getBaseAppWindowInstance().doInGameWindow(InGameBaseAppWindow::checkInAction);
        //then
        verifyActionWasCheckedIn(mouse, actionsCount);
    }

    private ForegroundWindowMocked baseWindowMocked() throws InterruptedException, CannotGetUserInputException {
        return new ForegroundWindowMocked.Builder()
                .withCommonWindowService(commonWindowService)
                .getForegroundWindowFor(window);
    }

    private Mouse getMouseMock() {
        return mock(Mouse.class);
    }

    private void accountBoundWithWindow() {
        when(accountBinding.getBoundAccount(RESOURCE_ID)).thenReturn(Optional.of(ACCOUNT_NAME));
    }

    private void accountNotBoundWithWindow() {
        when(accountBinding.getBoundAccount(RESOURCE_ID)).thenReturn(Optional.empty());
    }

    private BaseAppWindowImpl getBaseAppWindowInstance() {
        return new BaseAppWindowImpl(window, commonWindowService, settings, accountBinding, stamps);
    }

    private void windowIsExists() {
        when(window.isExists()).thenReturn(Either.resultOf(() -> true));
    }

    private void windowIsCorrupted() throws InterruptedException, CannotGetUserInputException {
        when(window.isVisible()).thenReturn(false);
        baseWindowMocked().stateIs(throwFor(disconnectedPopupStamp, new CannotGetUserInputException()))
                .throwsWhenAskedMouse(new CannotGetUserInputException());
    }

    private void windowIsHealthy() {
        when(window.isVisible()).thenReturn(true);
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
        when(stateWaiting.forAnyStamp(any(Stamp[].class))).thenAnswer(invocation -> {
            checkValidatorInvocation(invocation, stampsToCheck);
            return Optional.of(stamp);
        });
        return stateWaiting;
    }

    private ForegroundWindow.StateWaiting notFoundAnyFrom(final Set<Stamp> stampsToCheck) throws InterruptedException, CannotGetUserInputException {
        final ForegroundWindow.StateWaiting stateWaiting = getStateWaitingMock();
        when(stateWaiting.forAnyStamp(any(Stamp[].class))).thenAnswer(invocation -> {
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

    private <T extends Throwable> ForegroundWindow.StateWaiting throwFor(final Stamp stamp, final T t) throws InterruptedException, CannotGetUserInputException {
        final ForegroundWindow.StateWaiting stateWaiting = getStateWaitingMock();
        when(stateWaiting.forStamp(stamp)).thenThrow(t);
        return stateWaiting;
    }

    private void setActionsCount(final int count) {
        when(applicationSettings.actionsCount()).thenReturn(count);
    }

    private void verifyDisconnectedPopupConfirmed(final Mouse mouse) throws InterruptedException, Win32ApiException {
        verify(mouse).clickAtPoint(disconnectedPopupCloseButtonPoint);
    }

    private void verifyWindowCloseButtonClicked() throws InterruptedException, Win32ApiException {
        final InOrder inOrderMouse = inOrder(mouse);
        inOrderMouse.verify(mouse).move(windowCloseButtonPoint);
        inOrderMouse.verify(mouse, atLeastOnce()).leftClick();
    }

    private void verifyMenuCloseItemClicked(final Mouse mouse) throws InterruptedException, Win32ApiException {
        verify(mouse).clickAtPoint(menuExitOptionPoint);
    }

    private void verifyWindowDisappeared() {
        verify(commonWindowService).fixResult(CommonWindowService.Result.baseAppWindowDisappeared);
    }

    private void verifyWindowHasNotDisappeared() {
        verify(commonWindowService, never()).fixResult(CommonWindowService.Result.baseAppWindowDisappeared);
    }

    private void verifyWasAttemptTerminateWindowProcess() throws Win32ApiException {
        verify(process).terminate();
    }

    private void verifyDidNotAttemptsTerminateWindowProcess() throws Win32ApiException {
        verify(process, never()).terminate();
    }

    private void verifyServerWasSelectedAndConnected(final Mouse mouse) throws InterruptedException, Win32ApiException {
        final InOrder inOrderMouse = inOrder(mouse);
        inOrderMouse.verify(mouse).clickAtPoint(selectServerButtonPoint);
        inOrderMouse.verify(mouse).clickAtPoint(connectServerButtonPoint);
    }

    private void verifyGameStarted(final Mouse mouse) throws InterruptedException, Win32ApiException {
        final InOrder inOrderMouse = inOrder(mouse);
        inOrderMouse.verify(mouse).clickAtPoint(selectCharacterButtonPoint);
        inOrderMouse.verify(mouse).clickAtPoint(startButtonPoint);
    }

    private void verifyLoginTrackerWasCheckedIn(final Mouse mouse) throws InterruptedException, Win32ApiException {
        verify(mouse).clickAtPoint(trackLoginButtonPointer);
    }

    private void verifyActionWasCheckedIn(final Mouse mouse, final int actionsCount) throws InterruptedException, Win32ApiException {
        final InOrder inOrderMouse = inOrder(mouse);
        if (actionsCount <= 0) {
            inOrderMouse.verify(mouse, never()).clickAtPoint(ACTION_BUTTON_POINT_X, ACTION_BUTTON_POINT_Y);
            return;
        }
        int i = actionsCount - 1;
        do {
            inOrderMouse.verify(mouse).clickAtPoint(ACTION_BUTTON_POINT_X + (i * ACTION_BUTTON_POINT_X_OFFSET), ACTION_BUTTON_POINT_Y);
        } while (--i >= 0);
    }

}