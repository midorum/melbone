package midorum.melbone.window.internal.baseapp;

import com.midorum.win32api.facade.*;
import com.midorum.win32api.struct.PointFloat;
import com.midorum.win32api.struct.PointInt;
import midorum.melbone.model.dto.KeyShortcut;
import midorum.melbone.model.settings.account.AccountBinding;
import midorum.melbone.model.settings.stamp.Stamp;
import midorum.melbone.settings.StampKeys;
import midorum.melbone.model.exception.CriticalErrorException;
import midorum.melbone.model.settings.stamp.Stamps;
import midorum.melbone.model.settings.stamp.TargetBaseAppStamps;
import midorum.melbone.model.window.baseapp.InGameBaseAppWindow;
import midorum.melbone.model.window.baseapp.RestoredBaseAppWindow;
import midorum.melbone.model.settings.setting.ApplicationSettings;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.model.settings.setting.TargetBaseAppSettings;
import midorum.melbone.window.internal.common.CommonWindowService;
import midorum.melbone.window.internal.common.StampValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.stubbing.OngoingStubbing;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BaseAppWindowImplTest {

    private static final String RESOURCE_ID = "resource_id";
    private static final String ACCOUNT_NAME = "account_name";
    private static final float SPEED_FACTOR = 1.0f;
    private static final int TIMEOUT_FOR_TEST = 500;
    private static final int DELAY_FOR_TEST = 100;
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
    private final StampValidator stampValidator = mock(StampValidator.class);
    private final IWindow window = mock(IWindow.class);
    private final IMouse mouse = mock(IMouse.class);
    private final IKeyboard keyboard = mock(IKeyboard.class);
    private final IProcess process = mock(IProcess.class);
    //points
    private final PointFloat exitMenuOptionPoint = new PointFloat(-1f, -1f);
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
    private final PointFloat selectServerButtonPoint = new PointFloat(-1f, -1f);
    private final PointFloat connectServerButtonPoint = new PointFloat(-1f, -1f);
    private final PointFloat selectCharacterButtonPoint = new PointFloat(-1f, -1f);
    private final PointFloat startButtonPoint = new PointFloat(-1f, -1f);
    private final PointFloat dailyTrackerButtonPointer = new PointFloat(-1f, -1f);
    private final PointFloat dailyTrackerTabPointer = new PointFloat(-1f, -1f);
    private final PointFloat trackLoginButtonPointer = new PointFloat(-1f, -1f);
    private final PointFloat closeDailyTrackerPopupButtonPointer = new PointFloat(-1f, -1f);
    private final PointFloat actionButtonPoint = new PointFloat(ACTION_BUTTON_POINT_X, ACTION_BUTTON_POINT_Y);
    private final PointFloat actionSecondButtonPoint = new PointFloat(ACTION_BUTTON_POINT_X + ACTION_BUTTON_POINT_X_OFFSET, -1f);
    //stamps
    private final Stamp menuExitOptionStamp = mock(Stamp.class);
    private final Stamp disconnectedPopupStamp = mock(Stamp.class);
    private final Stamp optionsButtonBaseScaleStamp = mock(Stamp.class);
    private final Stamp optionsButtonDefaultScaleStamp = mock(Stamp.class);
    private final Stamp optionsPopupCaptionStamp = mock(Stamp.class);
    private final Stamp needRestartPopupStamp = mock(Stamp.class);
    private final Stamp serverLineUnselectedStamp = mock(Stamp.class);
    private final Stamp serverLineSelectedStamp = mock(Stamp.class);
    private final Stamp startButtonStamp = mock(Stamp.class);
    private final Stamp dailyTrackerPopupCaptionStamp = mock(Stamp.class);
    //hot keys
    final KeyShortcut stopAnimationHotkey = mock(KeyShortcut.class);
    final KeyShortcut openMenuHotkey = mock(KeyShortcut.class);

    @BeforeAll
    public static void beforeAll() {
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    @BeforeEach
    public void beforeEach() throws InterruptedException {
        //system
        when(commonWindowService.getUID(window)).thenReturn(RESOURCE_ID);
        when(commonWindowService.getStampValidator()).thenReturn(stampValidator);
        when(commonWindowService.logFailedStamps(eq(window), any(Stamp.class))).thenReturn("" + System.currentTimeMillis());
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
        //settings: hot keys
        when(targetBaseAppSettings.stopAnimationHotkey()).thenReturn(stopAnimationHotkey);
        when(targetBaseAppSettings.openMenuHotkey()).thenReturn(openMenuHotkey);
        //window
        when(window.getSystemId()).thenReturn("0xdf67");
        when(window.getWindowMouse(SPEED_FACTOR)).thenReturn(mouse);
        when(mouse.move(any(PointInt.class))).thenReturn(mouse);
        when(mouse.move(any(PointFloat.class))).thenReturn(mouse);
        when(mouse.move(anyFloat(), anyFloat())).thenReturn(mouse);
        when(mouse.leftClick()).thenReturn(mouse);
        when(window.getKeyboard()).thenReturn(keyboard);
        when(keyboard.enterHotKey(any(HotKey.class))).thenReturn(keyboard);
        when(window.getProcess()).thenReturn(process);
        //stamps
        when(stamps.targetBaseApp()).thenReturn(targetBaseAppStamps);
        when(targetBaseAppStamps.menuExitOption()).thenReturn(menuExitOptionStamp);
        when(menuExitOptionStamp.key()).thenReturn(StampKeys.TargetBaseApp.menuExitOption);
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
    void getCharacterNameForBoundWindow() {
        System.out.println("getCharacterNameForBoundWindow");
        //given
        accountBoundWithWindow();
        //when
        final Optional<String> maybeCharacterName = getBaseAppWindowInstance().getCharacterName();
        //then
        assertTrue(maybeCharacterName.isPresent());
        assertEquals(ACCOUNT_NAME, maybeCharacterName.get());
    }

    @Test
    void getCharacterNameForUnboundWindow() {
        System.out.println("getCharacterNameForUnboundWindow");
        //given
        accountNotBoundWithWindow();
        //when
        final Optional<String> maybeCharacterName = getBaseAppWindowInstance().getCharacterName();
        //then
        assertTrue(maybeCharacterName.isEmpty());
    }

    @Test
    void bindWithAccount() {
        System.out.println("bindWithAccount");
        //when
        getBaseAppWindowInstance().bindWithAccount(ACCOUNT_NAME);
        //then
        verify(accountBinding).bindResource(ACCOUNT_NAME, RESOURCE_ID);
    }

    @Test
    void terminatingWindowProcess() throws InterruptedException {
        System.out.println("terminatingWindowProcess");
        //when
        when(window.isExists())
                .thenReturn(true) // restoring window
                .thenReturn(true) // trying close normally
                .thenReturn(true) // killing window process
                .thenReturn(false); // window disappeared
        windowIsCorrupted();
        getBaseAppWindowInstance().restoreAndDo(restoredBaseAppWindow -> {/*any operation*/});
        //then
        verifyWasAttemptTerminateWindowProcess();
        verifyWindowDisappeared();
    }

    @Test
    void cannotTerminateWindowProcess() throws InterruptedException {
        System.out.println("cannotTerminateWindowProcess");
        //when
        when(window.isExists())
                .thenReturn(true) // restoring window
                .thenReturn(true) // trying close normally
                .thenReturn(true) // killing window process
                .thenReturn(true); // cannot terminate window process
        windowIsCorrupted();
        assertThrows(CriticalErrorException.class, () -> getBaseAppWindowInstance().restoreAndDo(restoredBaseAppWindow -> {/*any operation*/}));
        //then
        verifyWasAttemptTerminateWindowProcess();
        verifyWindowHasNotDisappeared();
    }

    @Test
    void closeDisconnectedWindow() throws InterruptedException {
        System.out.println("closeDisconnectedWindow");
        //when
        when(window.isExists())
                .thenReturn(true) // restoring window
                .thenReturn(true) // trying close normally
                .thenReturn(true) // closing window frame
                .thenReturn(true) // window hasn't closed yet
                .thenReturn(false); // window closed
        windowIsHealthy();
        windowIsDisconnected();
        getBaseAppWindowInstance().restoreAndDo(restoredBaseAppWindow -> {/*any operation*/});
        //then
        verifyDisconnectedPopupConfirmed();
        verifyWindowDisappeared();
        verifyDidNotAttemptsTerminateWindowProcess();
    }

    @Test
    void closeWindowViaFrame() throws InterruptedException {
        System.out.println("closeWindowViaFrame");
        //when
        when(window.isExists())
                .thenReturn(true) // restoring window
                .thenReturn(true) // trying close normally
                .thenReturn(true) // closing window frame
                .thenReturn(true) // window hasn't closed yet
                .thenReturn(false); // window closed
        windowIsHealthy();
        windowIsNotDisconnected();
        cannotOpenMenu(); // throwing BrokenWindowException
        getBaseAppWindowInstance().restoreAndDo(RestoredBaseAppWindow::close);
        //then
        verifyWindowCloseButtonClicked();
        verifyWindowDisappeared();
        verifyDidNotAttemptsTerminateWindowProcess();
    }

    @Test
    void closeWindowViaMenu() throws InterruptedException {
        System.out.println("closeWindowViaMenu");
        //when
        when(window.isExists())
                .thenReturn(true) // restoring window
                .thenReturn(true) // waiting window disappearing
                .thenReturn(false); // window closed
        windowIsHealthy();
        windowIsNotDisconnected();
        menuOpensNormally();
        getBaseAppWindowInstance().restoreAndDo(RestoredBaseAppWindow::close);
        //then
        verifyMenuCloseItemClicked();
        verifyWindowDisappeared();
        verifyDidNotAttemptsTerminateWindowProcess();
    }

    @Test
    void selectServer() throws InterruptedException {
        System.out.println("selectServer");
        //when
        windowIsExists();
        windowIsHealthy();
        windowIsNotDisconnected();
        serverPageRenderedInBaseScale();
        serverLineRenderedNormally();
        getBaseAppWindowInstance().restoreAndDo(RestoredBaseAppWindow::selectServer);
        //then
        verifyServerWasSelectedAndConnected();
    }

    @Test
    void chooseCharacter() throws InterruptedException {
        System.out.println("chooseCharacter");
        //when
        windowIsExists();
        windowIsHealthy();
        windowIsNotDisconnected();
        startButtonRenderedNormally();
        getBaseAppWindowInstance().restoreAndDo(RestoredBaseAppWindow::chooseCharacter);
        //then
        verifyGameStarted();
    }

    @Test
    void checkInLoginTracker() throws InterruptedException {
        System.out.println("checkInLoginTracker");
        //when
        windowIsExists();
        windowIsHealthy();
        windowIsNotDisconnected();
        menuOpensNormally();
        dailyTrackerPopupOpensNormally();
        getBaseAppWindowInstance().doInGameWindow(InGameBaseAppWindow::checkInLoginTracker);
        //then
        verifyLoginTrackerWasCheckedIn();
    }

    @Test
    void checkInZeroAction() throws InterruptedException {
        System.out.println("checkInZeroAction");
        //when
        final int actionsCount = 0;
        checkInActionsTest(actionsCount);
    }

    @Test
    void checkInOneAction() throws InterruptedException {
        System.out.println("checkInOneAction");
        //when
        final int actionsCount = 1;
        checkInActionsTest(actionsCount);
    }

    @Test
    void checkInTwoActions() throws InterruptedException {
        System.out.println("checkInTwoActions");
        //when
        final int actionsCount = 2;
        checkInActionsTest(actionsCount);
    }

    @Test
    void checkInThreeActions() throws InterruptedException {
        System.out.println("checkInThreeActions");
        //when
        final int actionsCount = 3;
        checkInActionsTest(actionsCount);
    }

    private void checkInActionsTest(final int actionsCount) throws InterruptedException {
        windowIsExists();
        windowIsHealthy();
        windowIsNotDisconnected();
        menuOpensNormally();
        setActionsCount(actionsCount);
        getBaseAppWindowInstance().doInGameWindow(InGameBaseAppWindow::checkInAction);
        //then
        verifyActionWasCheckedIn(actionsCount);
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
        when(window.isExists()).thenReturn(true);
    }

    private void windowIsCorrupted() throws InterruptedException {
        when(window.isVisible()).thenReturn(false);
        when(commonWindowService.bringWindowForeground(window)).thenReturn(false); // throwing BrokenWindowException
    }

    private void windowIsHealthy() throws InterruptedException {
        when(window.isVisible()).thenReturn(true);
        when(commonWindowService.bringWindowForeground(window)).thenReturn(true);
    }

    private void windowIsNotDisconnected() throws InterruptedException {
        when(stampValidator.validateStampWholeData(window, disconnectedPopupStamp, mouse, manaIndicatorPoint)).thenReturn(Optional.empty());
    }

    private void windowIsDisconnected() throws InterruptedException {
        when(stampValidator.validateStampWholeData(window, disconnectedPopupStamp, mouse, manaIndicatorPoint)).thenReturn(Optional.of(disconnectedPopupStamp));
    }

    private OngoingStubbing<Optional<Stamp>> cannotOpenMenu() throws InterruptedException {
        return when(stampValidator.validateStampWholeData(window, menuExitOptionStamp))
                .thenReturn(Optional.empty());
    }

    private void menuOpensNormally() throws InterruptedException {
        when(stampValidator.validateStampWholeData(window, menuExitOptionStamp))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(menuExitOptionStamp));
    }

    private void dailyTrackerPopupOpensNormally() throws InterruptedException {
        when(stampValidator.validateStampWholeData(window, dailyTrackerPopupCaptionStamp))
                .thenReturn(Optional.empty()) //not rendered
                .thenReturn(Optional.of(dailyTrackerPopupCaptionStamp)) //rendered
                .thenReturn(Optional.of(dailyTrackerPopupCaptionStamp)) //still rendered
                .thenReturn(Optional.empty()); //disappeared
    }

    private void serverPageRenderedInBaseScale() throws InterruptedException {
        when(stampValidator.validateStampWholeData(window, optionsButtonBaseScaleStamp, optionsButtonDefaultScaleStamp))
                .thenReturn(Optional.empty())
                .thenAnswer(invocation -> Optional.of(optionsButtonBaseScaleStamp));
    }

    private void serverPageRenderedInDefaultScale() throws InterruptedException {
        when(stampValidator.validateStampWholeData(window, optionsButtonBaseScaleStamp, optionsButtonDefaultScaleStamp))
                .thenReturn(Optional.empty())
                .thenAnswer(invocation -> Optional.of(optionsButtonDefaultScaleStamp));
    }

    private void serverLineRenderedNormally() throws InterruptedException {
        when(stampValidator.validateStampWholeData(window, serverLineUnselectedStamp, serverLineSelectedStamp))
                .thenReturn(Optional.empty())
                .thenAnswer(invocation -> Optional.of(serverLineUnselectedStamp));
    }

    private void startButtonRenderedNormally() throws InterruptedException {
        when(stampValidator.validateStampWholeData(window, startButtonStamp))
                .thenReturn(Optional.empty())
                .thenAnswer(invocation -> Optional.of(startButtonStamp));
    }

    private void setActionsCount(final int count) {
        when(applicationSettings.actionsCount()).thenReturn(count);
    }

    private void verifyDisconnectedPopupConfirmed() throws InterruptedException {
        final InOrder inOrderMouse = inOrder(mouse);
        inOrderMouse.verify(mouse).move(disconnectedPopupCloseButtonPoint);
        inOrderMouse.verify(mouse, atLeastOnce()).leftClick();
    }

    private void verifyWindowCloseButtonClicked() throws InterruptedException {
        final InOrder inOrderMouse = inOrder(mouse);
        inOrderMouse.verify(mouse).move(windowCloseButtonPoint);
        inOrderMouse.verify(mouse, atLeastOnce()).leftClick();
    }

    private void verifyMenuCloseItemClicked() throws InterruptedException {
        final InOrder inOrderMouse = inOrder(mouse);
        inOrderMouse.verify(mouse).move(exitMenuOptionPoint);
        inOrderMouse.verify(mouse, atLeastOnce()).leftClick();
    }

    private void verifyWindowDisappeared() {
        verify(commonWindowService).fixResult(CommonWindowService.Result.baseAppWindowDisappeared);
    }

    private void verifyWindowHasNotDisappeared() {
        verify(commonWindowService, never()).fixResult(CommonWindowService.Result.baseAppWindowDisappeared);
    }

    private void verifyWasAttemptTerminateWindowProcess() {
        verify(process).terminate();
    }

    private void verifyDidNotAttemptsTerminateWindowProcess() {
        verify(process, never()).terminate();
    }

    private void verifyServerWasSelectedAndConnected() throws InterruptedException {
        final InOrder inOrderMouse = inOrder(mouse);
        inOrderMouse.verify(mouse).move(selectServerButtonPoint);
        //inOrderMouse.verify(mouse, atLeastOnce()).leftClick();
        inOrderMouse.verify(mouse).move(connectServerButtonPoint);
        inOrderMouse.verify(mouse, atLeastOnce()).leftClick();
    }

    private void verifyGameStarted() throws InterruptedException {
        final InOrder inOrderMouse = inOrder(mouse);
        inOrderMouse.verify(mouse).move(selectCharacterButtonPoint);
        //inOrderMouse.verify(mouse, atLeastOnce()).leftClick();
        inOrderMouse.verify(mouse).move(startButtonPoint);
        inOrderMouse.verify(mouse, atLeastOnce()).leftClick();
    }

    private void verifyLoginTrackerWasCheckedIn() throws InterruptedException {
        final InOrder inOrderMouse = inOrder(mouse);
        inOrderMouse.verify(mouse).move(trackLoginButtonPointer);
        inOrderMouse.verify(mouse, atLeastOnce()).leftClick();
    }

    private void verifyActionWasCheckedIn(final int actionsCount) throws InterruptedException {
        final InOrder inOrderMouse = inOrder(mouse);
        if (actionsCount <= 0) {
            inOrderMouse.verify(mouse, never()).move(ACTION_BUTTON_POINT_X, ACTION_BUTTON_POINT_Y);
            return;
        }
        int i = actionsCount - 1;
        do {
            inOrderMouse.verify(mouse).move(ACTION_BUTTON_POINT_X + (i * ACTION_BUTTON_POINT_X_OFFSET), ACTION_BUTTON_POINT_Y);
        } while (--i >= 0);
    }

}