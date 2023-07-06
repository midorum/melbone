package midorum.melbone.window.internal.baseapp;

import com.midorum.win32api.facade.IMouse;
import com.midorum.win32api.facade.IWindow;
import com.midorum.win32api.facade.exception.Win32ApiException;
import com.midorum.win32api.struct.PointFloat;
import dma.flow.Waiting;
import dma.function.VoidActionThrowing;
import dma.util.Delay;
import dma.util.DurationFormatter;
import midorum.melbone.model.exception.CannotGetUserInputException;
import midorum.melbone.model.exception.CriticalErrorException;
import midorum.melbone.model.settings.account.AccountBinding;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.model.settings.setting.TargetBaseAppSettings;
import midorum.melbone.model.settings.stamp.Stamp;
import midorum.melbone.model.settings.stamp.Stamps;
import midorum.melbone.model.window.WindowConsumer;
import midorum.melbone.model.window.baseapp.BaseAppWindow;
import midorum.melbone.model.window.baseapp.InGameBaseAppWindow;
import midorum.melbone.model.window.baseapp.RestoredBaseAppWindow;
import midorum.melbone.settings.StampKeys;
import midorum.melbone.window.internal.common.CommonWindowService;
import midorum.melbone.window.internal.common.ForegroundWindow;
import midorum.melbone.window.internal.common.Mouse;
import midorum.melbone.window.internal.util.Log;
import midorum.melbone.window.internal.util.StaticResources;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class BaseAppWindowImpl implements BaseAppWindow {

    private final IWindow window;
    private final CommonWindowService commonWindowService;
    private final Settings settings;
    private final TargetBaseAppSettings targetBaseAppSettings;
    private final AccountBinding accountBinding;
    private final Stamps stamps;
    private final Log log;
    private Supplier<String> logMarkerSupplier;

    public BaseAppWindowImpl(final IWindow window,
                             final CommonWindowService commonWindowService,
                             final Settings settings,
                             final AccountBinding accountBinding,
                             final Stamps stamps) {
        this.window = window;
        this.settings = settings;
        this.targetBaseAppSettings = settings.targetBaseAppSettings();
        this.accountBinding = accountBinding;
        this.commonWindowService = commonWindowService;
        this.stamps = stamps;
        final String windowName = getCharacterNameInternal().orElse(window.getSystemId());
        this.logMarkerSupplier = () -> "base window [" + windowName + "]";
        this.log = new Log(StaticResources.LOGGER, logMarkerSupplier);
    }

    @Override
    public Optional<String> getCharacterName() {
        log.debug("get window {} account name", window.getSystemId());
        final Optional<String> maybeName = getCharacterNameInternal();
        log.debug("window {}{} bound with account {}", window.getSystemId(), maybeName.isPresent() ? "" : " not", maybeName.orElse(""));
        return maybeName;
    }

    @Override
    public void bindWithAccount(final String accountId) {
        accountBinding.bindResource(accountId, commonWindowService.getUID(window));
        setLogMarker(accountId);
        log.info("window has bound with account {}", accountId);
    }

    @Override
    public void restoreAndDo(final WindowConsumer<RestoredBaseAppWindow> consumer) throws InterruptedException {
        if (!this.window.isExists()) {
            log.warn("window not found - skip");
            return;
        }
        try {
            commonWindowService.bringForeground(window).andDo(foregroundWindow -> {
                try {
                    checkWindowIsDisconnected(foregroundWindow);
                    consumer.accept(new RestoredBaseAppWindowImpl(foregroundWindow));
                    minimizeOpenedWindow(foregroundWindow);
                } catch (DisconnectedWindowException e) {
                    try {
                        log.warn("widow is disconnected: " + e.getMessage() + " - close:", e);
                        closeDisconnectedWindow(foregroundWindow);
                    } catch (Win32ApiException ex) {
                        throw new CannotGetUserInputException(e.getMessage(), e);
                    }
                } catch (Win32ApiException e) {
                    throw new CannotGetUserInputException(e.getMessage(), e);
                }
            });
        } catch (BrokenWindowException | CannotGetUserInputException e) {
            log.warn("widow is broken: " + e.getMessage() + " - close:", e);
            closeOrKillBrokenWindow();
        }
    }

    @Override
    public void doInGameWindow(final WindowConsumer<InGameBaseAppWindow> consumer) throws InterruptedException {
        if (!this.window.isExists()) {
            log.warn("window not found - skip");
            return;
        }
        try {
            commonWindowService.bringForeground(window).andDo(foregroundWindow -> {
                try {
                    checkWindowIsDisconnected(foregroundWindow);
                    checkIfInGameWindowRendered(foregroundWindow);
                    consumer.accept(new InGameBaseAppWindowImpl(foregroundWindow));
                    minimizeOpenedWindow(foregroundWindow);
                } catch (DisconnectedWindowException e) {
                    try {
                        log.warn("widow is disconnected: " + e.getMessage() + " - close", e);
                        closeDisconnectedWindow(foregroundWindow);
                    } catch (Win32ApiException ex) {
                        throw new CannotGetUserInputException(e.getMessage(), e);
                    }
                } catch (Win32ApiException e) {
                    throw new CannotGetUserInputException(e.getMessage(), e);
                }
            });
        } catch (BrokenWindowException | CannotGetUserInputException e) {
            log.warn("widow has broken: " + e.getMessage() + " - close:", e);
            closeOrKillBrokenWindow();
        }
    }

    private Optional<String> getCharacterNameInternal() {
        return accountBinding.getBoundAccount(commonWindowService.getUID(window));
    }

    private synchronized void setLogMarker(final String name) {
        this.logMarkerSupplier = () -> this.getClass().getSimpleName() + " [" + name + "]";
    }

    private IMouse getMouse() {
        return window.getWindowMouse(settings.application().speedFactor());
    }

    private void checkWindowIsDisconnected(final ForegroundWindow foregroundWindow) throws InterruptedException, CannotGetUserInputException {
        if (foregroundWindow.waiting()
                .withTimeout(targetBaseAppSettings.disconnectedPopupRenderingTimeout())
                .withDelay(targetBaseAppSettings.checkDisconnectedPopupRenderingDelay())
                .withMousePosition(targetBaseAppSettings.manaIndicatorPoint())
                .forStamp(stamps.targetBaseApp().disconnectedPopup()).isPresent())
            throw new DisconnectedWindowException("window is disconnected");
    }

    private void checkIfInGameWindowRendered(final ForegroundWindow foregroundWindow) throws InterruptedException {
        if (!waitBaseWindowRendering(foregroundWindow)) {
            throw new BrokenWindowException("base window is not rendered");
        }
    }

    private boolean waitBaseWindowRendering(final ForegroundWindow foregroundWindow) throws InterruptedException {
        log.info("wait for base window rendering");
        return new Waiting()
                .timeout(targetBaseAppSettings.baseWindowRenderingTimeout(), TimeUnit.MILLISECONDS)
                .withDelay(targetBaseAppSettings.checkBaseWindowRenderingDelay(), TimeUnit.MILLISECONDS)
                .doOnEveryFailedIteration(i -> log.debug("{}: base window has not rendered yet", new DurationFormatter(i.fromStart()).toStringWithoutZeroParts()))
                .waitForBoolean(() -> {
                    try {
                        return checkAccountInfoRendering(foregroundWindow);
                    } catch (CannotGetUserInputException e) {
                        throw new BrokenWindowException(e.getMessage(), e);
                    }
                });
    }

    private void minimizeOpenedWindow(final ForegroundWindow foregroundWindow) throws InterruptedException, CannotGetUserInputException, Win32ApiException {
        if (!this.window.isExists()) return;
        log.info("minimizing window");
        foregroundWindow.getMouse().clickAtPoint(targetBaseAppSettings.windowMinimizeButtonPoint());
    }

    private void closeDisconnectedWindow(final ForegroundWindow foregroundWindow) throws InterruptedException, CannotGetUserInputException, Win32ApiException {
        log.info("close disconnected popup");
        foregroundWindow.getMouse().clickAtPoint(targetBaseAppSettings.disconnectedPopupCloseButtonPoint());
        if (!waitWindowDisappearing(() -> {
        })) throw getBrokenWindowException("Cannot close disconnected window", getLogMarker());
    }

    private void closeOrKillBrokenWindow() throws InterruptedException {
        if (!this.window.isExists()) return;
        try {
            if (!tryCloseBrokenWindowAndCheckIfClosedNormally()) killWindowProcess();
        } catch (Win32ApiException e) {
            log.error("cannot close window normally - kill process", e);
            killWindowProcess();
        }
    }

    private boolean tryCloseBrokenWindowAndCheckIfClosedNormally() throws InterruptedException, Win32ApiException {
        if (!windowIsVisibleAndHasNormalMetrics()) return false;
        log.info("close broken window");
        closeWindowFrame();
        return waitWindowDisappearing(() -> {
            if (windowIsVisibleAndHasNormalMetrics()) closeWindowFrame();
        });
    }

    private boolean windowIsVisibleAndHasNormalMetrics() {
        if (!this.window.isVisible()) {
            log.warn("window is not visible");
            return false;
        }
        // TODO check window metrics like launcher
        return true;
    }

    private void closeWindowFrame() throws InterruptedException, Win32ApiException {
        if (!this.window.isExists()) return;
        log.info("close window frame");
        //press Close frame button
        getMouse().move(targetBaseAppSettings.windowCloseButtonPoint()).leftClick();
    }

    private void killWindowProcess() throws InterruptedException {
        if (!this.window.isExists()) return;
        log.info("terminating window process");
        this.window.getProcess().terminate();
        if (!waitWindowDisappearing(() -> {
        })) throw new CriticalErrorException("cannot terminate window process");
    }

    private boolean waitWindowDisappearing(final VoidActionThrowing<? extends Throwable> closeWindowAction) throws InterruptedException {
        assert closeWindowAction != null;
        log.info("wait for window disappearing");
        final boolean result = new Waiting()
                .timeout(targetBaseAppSettings.windowDisappearingTimeout(), TimeUnit.MILLISECONDS)
                .withDelay(targetBaseAppSettings.checkWindowDisappearingDelay(), TimeUnit.MILLISECONDS)
                .doOnEveryFailedIteration(i -> {
                    log.debug("{}: window has not disappeared yet", new DurationFormatter(i.fromStart()).toStringWithoutZeroParts());
                    try {
                        closeWindowAction.perform();
                    } catch (Throwable e) {
                        log.error("cannot perform close window action - skip", e);
                    }
                })
                .waitForBoolean(() -> !window.isExists());
        if (result) {
            log.info("window has disappeared");
            commonWindowService.fixResult(CommonWindowService.Result.baseAppWindowDisappeared);
        } else {
            log.warn("window has not disappeared");
        }
        return result;
    }

    private boolean openMenuAndCheck(final ForegroundWindow foregroundWindow) throws InterruptedException, CannotGetUserInputException {
        log.info("wait for menu rendering");
        return foregroundWindow.waiting()
                .withTimeout(targetBaseAppSettings.menuRenderingTimeout())
                .withDelay(targetBaseAppSettings.checkMenuRenderingDelay())
                .usingHotKey(targetBaseAppSettings.openMenuHotkey().toHotKey())
                .forStamp(stamps.targetBaseApp().menuExitOption()).isPresent();
    }

    private Stamp waitServerPageRendering(final ForegroundWindow foregroundWindow) throws CannotGetUserInputException, InterruptedException {
        log.info("wait for server page rendering");
        final String logMarker = getLogMarker();
        return foregroundWindow.waiting()
                .withTimeout(targetBaseAppSettings.serverPageRenderingTimeout())
                .withDelay(targetBaseAppSettings.checkServerPageRenderingDelay())
                .usingHotKey(targetBaseAppSettings.stopAnimationHotkey().toHotKey())
                .logFailedStampsWithMarker(logMarker)
                // сначала ищем базовый масштаб, а затем дефолтный, чтобы не менять масштаб если не нужно
                .forAnyStamp(stamps.targetBaseApp().optionsButtonBaseScale(), stamps.targetBaseApp().optionsButtonDefaultScale())
                .orElseThrow(() -> getBrokenWindowException("server page has not rendered - maybe window broken", logMarker));
    }

    private void changeUISizeToBaseAndMute(final ForegroundWindow foregroundWindow) throws InterruptedException, CannotGetUserInputException, Win32ApiException {
        log.info("try change settings");
        final String logMarker = getLogMarker();
        foregroundWindow.waiting()
                .withTimeout(targetBaseAppSettings.optionsDialogRenderingTimeout())
                .withDelay(targetBaseAppSettings.checkOptionsDialogRenderingDelay())
                .usingMouseClickAt(targetBaseAppSettings.openOptionsButtonPoint())
                .logFailedStampsWithMarker(logMarker)
                .forStamp(stamps.targetBaseApp().optionsPopupCaption())
                .orElseThrow(() -> getBrokenWindowException("options popup not found", logMarker));

        final Mouse mouse = foregroundWindow.getMouse();
        log.info("changing window settings");
        //scale window
        log.info("changing window scale");
        mouse.clickAtPoint(targetBaseAppSettings.screenSettingsTabPoint());
        mouse.clickAtPoint(targetBaseAppSettings.uiScaleChooser80Point()); // 100% -> 90%
        mouse.clickAtPoint(targetBaseAppSettings.uiScaleChooser80Point()); // 90% -> 80%
        //mute
        log.info("muting window");
        mouse.clickAtPoint(targetBaseAppSettings.soundSettingsTabPoint());
        mouse.clickAtPoint(targetBaseAppSettings.overallVolumeZeroLevelPoint());
        //apply
        log.info("applying settings");
        mouse.clickAtPoint(targetBaseAppSettings.optionsApplyButtonPoint());

        //check need restart popup is appeared
        if (foregroundWindow.waiting()
                .withTimeout(targetBaseAppSettings.needRestartPopupRenderingTimeout())
                .withDelay(targetBaseAppSettings.checkNeedRestartPopupRenderingDelay())
                .forStamp(stamps.targetBaseApp().needRestartPopup()).isPresent()) {
            log.info("need restart popup has rendered");
            mouse.clickAtPoint(targetBaseAppSettings.needRestartPopupConfirmButtonPoint());
        }
        log.info("options has changed");
    }

    private void waitServerLineRendering(final ForegroundWindow foregroundWindow) throws CannotGetUserInputException, InterruptedException {
        log.info("wait server line rendering");
        final String logMarker = getLogMarker();
        foregroundWindow.waiting()
                .withTimeout(targetBaseAppSettings.serverLineRenderingTimeout())
                .withDelay(targetBaseAppSettings.checkServerLineRenderingDelay())
                .logFailedStampsWithMarker(logMarker)
                .forAnyStamp(stamps.targetBaseApp().serverLineUnselected(),
                        stamps.targetBaseApp().serverLineSelected())
                .orElseThrow(() -> getBrokenWindowException("server line not found", logMarker));
    }

    private void connectServer(final ForegroundWindow foregroundWindow) throws InterruptedException, CannotGetUserInputException, Win32ApiException {
        final Mouse mouse = foregroundWindow.getMouse();
        //select server
        log.info("selecting server");
        mouse.clickAtPoint(targetBaseAppSettings.selectServerButtonPoint());
        //connect server
        log.info("connecting server");
        mouse.clickAtPoint(targetBaseAppSettings.connectServerButtonPoint());
    }

    private void waitStartButtonRendering(final ForegroundWindow foregroundWindow) throws InterruptedException, CannotGetUserInputException {
        log.info("wait for start button rendering");
        final String logMarker = getLogMarker();
        foregroundWindow.waiting()
                .withTimeout(targetBaseAppSettings.startButtonRenderingTimeout())
                .withDelay(targetBaseAppSettings.checkStartButtonRenderingDelay())
                .logFailedStampsWithMarker(logMarker)
                .forStamp(stamps.targetBaseApp().startButton())
                .orElseThrow(() -> getBrokenWindowException("start button not rendered", logMarker));
    }

    private boolean openDailyTrackerPopupAndCheckRendering(final ForegroundWindow foregroundWindow) throws InterruptedException, CannotGetUserInputException {
        log.info("wait for daily tracker popup");
        return foregroundWindow.waiting()
                .withTimeout(targetBaseAppSettings.dailyTrackerPopupRenderingTimeout())
                .withDelay(targetBaseAppSettings.checkDailyTrackerPopupRenderingDelay())
                .usingMouseClickAt(targetBaseAppSettings.dailyTrackerButtonPoint())
                .forStamp(stamps.targetBaseApp().dailyTrackerPopupCaption())
                .isPresent();
    }

    private boolean closeDailyTrackerPopupAndCheckRendering(final ForegroundWindow foregroundWindow) throws InterruptedException, CannotGetUserInputException {
        log.info("close daily tracker popup");
        return foregroundWindow.waiting()
                .withTimeout(targetBaseAppSettings.dailyTrackerPopupRenderingTimeout())
                .withDelay(targetBaseAppSettings.checkDailyTrackerPopupRenderingDelay())
                .usingMouseClickAt(targetBaseAppSettings.closeDailyTrackerPopupButtonPoint())
                .forStampDisappearing(stamps.targetBaseApp().dailyTrackerPopupCaption());
    }

    private boolean checkAccountInfoRendering(final ForegroundWindow foregroundWindow) throws InterruptedException, CannotGetUserInputException {
        log.info("check account info rendering");
        return foregroundWindow.waiting()
                .withTimeout(targetBaseAppSettings.accountInfoPopupRenderingTimeout())
                .withDelay(targetBaseAppSettings.accountInfoPopupRenderingDelay())
                .usingHotKeyEnclose(targetBaseAppSettings.openAccountInfoHotkey().toHotKey())
                .forStamp(stamps.targetBaseApp().accountInfoPopupCaption())
                .isPresent();
    }

    private String getLogMarker() {
        return Long.toString(System.currentTimeMillis());
    }

    private BrokenWindowException getBrokenWindowException(final String message, final String marker) {
        return new BrokenWindowException(message + " (marker=" + marker + ")");
    }

    public class RestoredBaseAppWindowImpl implements RestoredBaseAppWindow {

        private final ForegroundWindow foregroundWindow;

        private RestoredBaseAppWindowImpl(final ForegroundWindow foregroundWindow) {
            this.foregroundWindow = foregroundWindow;
        }

        @Override
        public void close() throws InterruptedException, CannotGetUserInputException {
            log.info("close window via menu");
            try {
                if (!openMenuAndCheck(foregroundWindow))
                    throw new BrokenWindowException("cannot open base window menu");
                foregroundWindow.getMouse().clickAtPoint(targetBaseAppSettings.menuExitOptionPoint());
                if (!waitWindowDisappearing(() -> {
                })) throw new BrokenWindowException("cannot close base window via menu");
            } catch (Win32ApiException e) {
                throw new CannotGetUserInputException(e.getMessage(), e);
            }
        }

        @Override
        public void selectServer() throws InterruptedException, CannotGetUserInputException {
            try {
                final Stamp foundStamp = waitServerPageRendering(foregroundWindow);
                checkWindowIsDisconnected(foregroundWindow);
                if (!foundStamp.key().equals(StampKeys.TargetBaseApp.optionsButtonBaseScale)) {
                    changeUISizeToBaseAndMute(foregroundWindow);
                }
                waitServerLineRendering(foregroundWindow);
                checkWindowIsDisconnected(foregroundWindow);
                connectServer(foregroundWindow);
                log.info("processing select server page done");
            } catch (Win32ApiException e) {
                throw new CannotGetUserInputException(e.getMessage(), e);
            }
        }

        @Override
        public void chooseCharacter() throws InterruptedException, CannotGetUserInputException {
            try {
                waitStartButtonRendering(foregroundWindow);
                checkWindowIsDisconnected(foregroundWindow);

                log.info("select character");
                final Mouse mouse = foregroundWindow.getMouse();
                mouse.clickAtPoint(targetBaseAppSettings.selectCharacterButtonPoint());

                new Delay(settings.application().speedFactor()).sleep(500, TimeUnit.MILLISECONDS);//FIXME get rid

                log.info("start game");
                mouse.clickAtPoint(targetBaseAppSettings.startButtonPoint());
                log.info("character has been selected");
            } catch (Win32ApiException e) {
                throw new CannotGetUserInputException(e.getMessage(), e);
            }
        }

        @Override
        public void checkInGameWindowRendered() throws InterruptedException {
            checkIfInGameWindowRendered(foregroundWindow);
        }

    }

    private class InGameBaseAppWindowImpl implements InGameBaseAppWindow {

        private final ForegroundWindow foregroundWindow;

        private InGameBaseAppWindowImpl(final ForegroundWindow foregroundWindow) {
            this.foregroundWindow = foregroundWindow;
        }

        @Override
        public void checkInLoginTracker() throws InterruptedException, CannotGetUserInputException {
            log.info("check in login tracker");
            try {
                if (!openDailyTrackerPopupAndCheckRendering(foregroundWindow)) {
                    log.warn("skip daily tracking, because tracker popup has not rendered");
                    return;
                }
                final Delay delay = new Delay(settings.application().speedFactor());
                final Mouse mouse = foregroundWindow.getMouse();
                mouse.clickAtPoint(targetBaseAppSettings.dailyTrackerTabPoint());
                delay.sleep(1, TimeUnit.SECONDS); //FIXME вместо задержки проверять отрисовку
                mouse.clickAtPoint(targetBaseAppSettings.trackLoginButtonPoint());
                log.info("login tacker has clicked");
                delay.sleep(1, TimeUnit.SECONDS); //FIXME вместо задержки проверять отрисовку
                closeDailyTrackerPopupAndCheckRendering(foregroundWindow);
                log.info("checked in login tracker");
            } catch (Win32ApiException e) {
                throw new CannotGetUserInputException(e.getMessage(), e);
            }
        }

        @Override
        public void checkInAction() throws InterruptedException, CannotGetUserInputException {
            final int actionsCount = settings.application().actionsCount();
            if (actionsCount < 1) {
                log.info("checking in action disabled");
                return;
            }
            final Mouse mouse = foregroundWindow.getMouse();
            log.info("try check in action (actions count = {})", actionsCount);
            try {
                int index = actionsCount;
                while (index-- > 0) {
                    final PointFloat actionButtonPoint = targetBaseAppSettings.actionButtonPoint();
                    final PointFloat actionSecondButtonPoint = targetBaseAppSettings.actionSecondButtonPoint();
                    final float xOffset = actionSecondButtonPoint.x() - actionButtonPoint.x();
                    final float buttonOffset = xOffset * index;
                    mouse.clickAtPoint(actionButtonPoint.x() + buttonOffset, actionButtonPoint.y());
                }
                new Delay(settings.application().speedFactor()).sleep(1, TimeUnit.SECONDS); //FIXME вместо задержки проверять отрисовку
                log.info("checked in action");
            } catch (Win32ApiException e) {
                e.printStackTrace();
            }
        }
    }

    private static class BrokenWindowException extends RuntimeException {
        public BrokenWindowException(final String message) {
            super(message);
        }

        public BrokenWindowException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

    private static class DisconnectedWindowException extends RuntimeException {
        public DisconnectedWindowException(final String message) {
            super(message);
        }

    }
}
