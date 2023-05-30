package midorum.melbone.window.internal.baseapp;

import com.midorum.win32api.facade.HotKey;
import com.midorum.win32api.facade.IKeyboard;
import com.midorum.win32api.facade.IMouse;
import com.midorum.win32api.facade.IWindow;
import com.midorum.win32api.struct.PointFloat;
import dma.flow.Waiting;
import dma.function.ConsumerThrowing;
import dma.util.Delay;
import dma.util.DurationFormatter;
import midorum.melbone.model.exception.ControlledInterruptedException;
import midorum.melbone.model.exception.CriticalErrorException;
import midorum.melbone.model.settings.account.AccountBinding;
import midorum.melbone.model.settings.stamp.Stamps;
import midorum.melbone.model.settings.stamp.Stamp;
import midorum.melbone.model.window.baseapp.BaseAppWindow;
import midorum.melbone.model.window.baseapp.InGameBaseAppWindow;
import midorum.melbone.model.window.baseapp.RestoredBaseAppWindow;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.settings.StampKeys;
import midorum.melbone.window.internal.common.CommonWindowService;
import midorum.melbone.window.internal.util.Log;
import midorum.melbone.window.internal.util.StaticResources;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class BaseAppWindowImpl implements BaseAppWindow {

    private final IWindow window;
    private final CommonWindowService commonWindowService;
    private final Settings settings;
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
    public void restoreAndDo(final ConsumerThrowing<RestoredBaseAppWindow, InterruptedException> consumer) throws InterruptedException {
        try {
            if (!this.window.isExists()) {
                log.warn("window not found - skip");
                return;
            }
            checkIfNotDisconnected();
            consumer.accept(new RestoredBaseAppWindowImpl());
            minimizeOpenedWindow();
        } catch (DisconnectedWindowException e) {
            try {
                log.warn("widow is disconnected: " + e.getMessage() + " - close and skip:", e);
                closeOrKillDisconnectedWindow();
            } catch (Throwable t) {
                t.addSuppressed(e);
                throw t;
            }
        } catch (BrokenWindowException e) {
            try {
                log.warn("widow has broken: " + e.getMessage() + " - close and skip:", e);
                closeOrKillBrokenWindow();
            } catch (Throwable t) {
                t.addSuppressed(e);
                throw t;
            }
        }
    }

    @Override
    public void doInGameWindow(final ConsumerThrowing<InGameBaseAppWindow, InterruptedException> consumer) throws InterruptedException {
        try {
            if (!this.window.isExists()) {
                log.warn("window not found - skip");
                return;
            }
            checkIfNotDisconnected();
            checkIfInGameWindowRendered();
            consumer.accept(new InGameBaseAppWindowImpl());
            minimizeOpenedWindow();
        } catch (DisconnectedWindowException e) {
            try {
                log.warn("widow is disconnected: " + e.getMessage() + " - close and skip", e);
                closeOrKillDisconnectedWindow();
            } catch (Throwable t) {
                t.addSuppressed(e);
                throw t;
            }
        } catch (BrokenWindowException e) {
            try {
                log.warn("widow has broken: " + e.getMessage() + " - close and skip", e);
                closeOrKillBrokenWindow();
            } catch (Throwable t) {
                t.addSuppressed(e);
                throw t;
            }
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

    private void checkIfNotDisconnected() throws InterruptedException {
        if (!commonWindowService.bringWindowForeground(window)) {
            throw new BrokenWindowException("cannot bring window foreground");
        }
        if (isDisconnected()) {
            throw new DisconnectedWindowException("window is disconnected");
        }
    }

    private boolean isDisconnected() throws InterruptedException {
        log.info("check if window disconnected");
        return waitDisconnectedPopupRendering().isPresent();
    }

    private void checkIfInGameWindowRendered() throws InterruptedException {
        if (!waitBaseWindowRendering()) {
            throw new BrokenWindowException("base window is not rendered");
        }
    }

    private boolean waitBaseWindowRendering() throws InterruptedException {
        log.info("wait for base window rendering");
        return new Waiting()
                .timeout(settings.targetBaseAppSettings().baseWindowRenderingTimeout(), TimeUnit.MILLISECONDS)
                .withDelay(settings.targetBaseAppSettings().checkBaseWindowRenderingDelay(), TimeUnit.MILLISECONDS)
                .doOnEveryFailedIteration(i -> log.debug("{}: base window has not rendered yet", new DurationFormatter(i.fromStart()).toStringWithoutZeroParts()))
                .waitForBoolean(this::checkAccountInfoRendering);
    }

    private Optional<Stamp> waitDisconnectedPopupRendering() throws InterruptedException {
        log.debug("wait for disconnected popup rendering");
        final IMouse mouse = getMouse();
        final Stamp checkingStamp = stamps.targetBaseApp().disconnectedPopup();
        final Optional<Stamp> foundStamp = new Waiting()
                .timeout(settings.targetBaseAppSettings().disconnectedPopupRenderingTimeout(), TimeUnit.MILLISECONDS)
                .withDelay(settings.targetBaseAppSettings().checkDisconnectedPopupRenderingDelay(), TimeUnit.MILLISECONDS)
                .doOnEveryFailedIteration(i -> log.debug("{}: disconnected popup has not rendered yet", new DurationFormatter(i.fromStart()).toStringWithoutZeroParts()))
                .waitFor(() -> commonWindowService.getStampValidator().validateStampWholeData(this.window, checkingStamp, mouse, settings.targetBaseAppSettings().manaIndicatorPoint()));
        if (foundStamp.isPresent()) {
            final Stamp stamp = foundStamp.get();
            log.debug("found stamp {}", stamp.key().internal().groupName() + "." + stamp.key().name());
        }
        return foundStamp;
    }

    private void minimizeOpenedWindow() throws InterruptedException {
        if (!this.window.isExists()) return;
        log.info("minimizing window");
        //press Minimize frame button
        getMouse().move(settings.targetBaseAppSettings().windowMinimizeButtonPoint()).leftClick();
    }

    private boolean closeWindowViaMenu() throws InterruptedException {
        log.info("close window via menu");
        final IMouse mouse = getMouse();
        if (!waitForMenuRendering()) {
            return false;
        }
        log.debug("click Close menu");
        mouse.move(settings.targetBaseAppSettings().menuExitOptionPoint()).leftClick();
        return waitWindowDisappearing(() -> {
        });
    }

    private void closeWindowFrame() throws InterruptedException {
        if (!this.window.isExists()) return;
        log.info("close window frame");
        //press Close frame button
        getMouse().move(settings.targetBaseAppSettings().windowCloseButtonPoint()).leftClick();
    }

    private void closeOrKillDisconnectedWindow() throws InterruptedException {
        log.info("close disconnected popup");
        getMouse().move(settings.targetBaseAppSettings().disconnectedPopupCloseButtonPoint()).leftClick();
        if (!waitWindowDisappearing(() -> {
        })) {
            closeOrKillBrokenWindow();
        }
    }

    private void closeOrKillBrokenWindow() throws InterruptedException {
        if (!this.window.isExists()) return;
        if (!tryCloseBrokenWindowAndCheckIfClosedNormally()) {
            killWindowProcess();
        }
    }

    private void killWindowProcess() throws InterruptedException {
        if (!this.window.isExists()) return;
        log.info("terminating window process");
        this.window.getProcess().terminate();
        if (!waitWindowDisappearing(() -> {
        })) throw new CriticalErrorException("cannot terminate window process");
    }

    private boolean tryCloseBrokenWindowAndCheckIfClosedNormally() throws InterruptedException {
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

    private boolean waitWindowDisappearing(final Waiting.EmptyConsumer closeWindowAction) throws InterruptedException {
        assert closeWindowAction != null;
        log.info("waiting window disappearing");
        final boolean result = new Waiting()
                .timeout(settings.targetBaseAppSettings().windowDisappearingTimeout(), TimeUnit.MILLISECONDS)
                .withDelay(settings.targetBaseAppSettings().checkWindowDisappearingDelay(), TimeUnit.MILLISECONDS)
                .doOnEveryFailedIteration(i -> {
                    log.debug("{}: window has not disappeared yet", new DurationFormatter(i.fromStart()).toStringWithoutZeroParts());
                    closeWindowAction.accept();
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

    private boolean waitForMenuRendering() throws InterruptedException {
        log.info("waiting for menu rendering");
        final IKeyboard keyboard = window.getKeyboard();
        final Stamp checkingStamp = stamps.targetBaseApp().menuExitOption();
        final HotKey openMenuHotKey = settings.targetBaseAppSettings().openMenuHotkey().toHotKey();
        final boolean result = new Waiting()
                .timeout(settings.targetBaseAppSettings().menuRenderingTimeout(), TimeUnit.MILLISECONDS)
                .withDelay(settings.targetBaseAppSettings().checkMenuRenderingDelay(), TimeUnit.MILLISECONDS)
                .doOnEveryFailedIteration(i -> {
                    log.debug("{}: menu has not rendered yet - try to open again", new DurationFormatter(i.fromStart()).toStringWithoutZeroParts());
                    keyboard.enterHotKey(openMenuHotKey);
                })
                .waitFor(() -> commonWindowService.getStampValidator().validateStampWholeData(this.window, checkingStamp)).isPresent();
        if (result)
            log.debug("menu has rendered");
        else
            log.warn("menu has not rendered");
        return result;
    }

    private Stamp waitServerPageRendering() throws InterruptedException {
        final IKeyboard keyboard = window.getKeyboard();
        final HotKey stopAnimationHotKey = settings.targetBaseAppSettings().stopAnimationHotkey().toHotKey();
        log.info("waiting for server page rendering");
        // сначала ищем базовый масштаб, а затем дефолтный, чтобы не менять масштаб если не нужно
        final Stamp[] checkingStamps = {stamps.targetBaseApp().optionsButtonBaseScale(),
                stamps.targetBaseApp().optionsButtonDefaultScale()};
        final Stamp foundStamp = new Waiting()
                .timeout(settings.targetBaseAppSettings().serverPageRenderingTimeout(), TimeUnit.MILLISECONDS)
                .withDelay(settings.targetBaseAppSettings().checkServerPageRenderingDelay(), TimeUnit.MILLISECONDS)
                .doOnEveryFailedIteration(i -> {
                    log.debug("{}: server page has not rendered yet - try to stop animation", new DurationFormatter(i.fromStart()).toStringWithoutZeroParts());
                    keyboard.enterHotKey(stopAnimationHotKey);
                })
                .waitFor(() -> commonWindowService.getStampValidator().validateStampWholeData(this.window, checkingStamps))
                .orElseThrow(() -> getBrokenWindowException("server page has not rendered - maybe window broken", checkingStamps));
        log.debug("found stamp {}", foundStamp.key().internal().groupName() + "." + foundStamp.key().name());
        return foundStamp;
    }

    private void changeUISizeToBaseAndMute(IMouse mouse) throws InterruptedException {
        openOptionsPopupAndWaitRendering(mouse);
        log.info("changing window settings");
        //scale window
        log.info("changing window scale");
        mouse.move(settings.targetBaseAppSettings().screenSettingsTabPoint()).leftClick();
        mouse.move(settings.targetBaseAppSettings().uiScaleChooser80Point()).leftClick(); // 100% -> 90%
        mouse.move(settings.targetBaseAppSettings().uiScaleChooser80Point()).leftClick(); // 90% -> 80%
        //mute
        log.info("muting window");
        mouse.move(settings.targetBaseAppSettings().soundSettingsTabPoint()).leftClick();
        mouse.move(settings.targetBaseAppSettings().overallVolumeZeroLevelPoint()).leftClick();
        //apply
        log.info("applying settings");
        mouse.move(settings.targetBaseAppSettings().optionsApplyButtonPoint()).leftClick();

        //check need restart popup is appeared
        waitNeedRestartPopupRendering()
                .ifPresent(stamp -> {
                    log.info("need restart popup has rendered");
                    try {
                        mouse.move(settings.targetBaseAppSettings().needRestartPopupConfirmButtonPoint()).leftClick();
                    } catch (InterruptedException e) {
                        throw new ControlledInterruptedException(e.getMessage(), e);
                    }
                });

        log.info("options has changed");
    }

    private void openOptionsPopupAndWaitRendering(final IMouse mouse) throws InterruptedException {
        log.info("waiting for options popup rendering");
        final Stamp checkingStamp = stamps.targetBaseApp().optionsPopupCaption();
        final Waiting.EmptyConsumer action = () -> {
            log.info("try open options popup");
            mouse.move(settings.targetBaseAppSettings().openOptionsButtonPoint()).leftClick();
        };
        final Stamp foundStamp = new Waiting()
                .timeout(settings.targetBaseAppSettings().optionsDialogRenderingTimeout(), TimeUnit.MILLISECONDS)
                .withDelay(settings.targetBaseAppSettings().checkOptionsDialogRenderingDelay(), TimeUnit.MILLISECONDS)
                .startFrom(action)
                .latency(1, TimeUnit.SECONDS)
                .doOnEveryFailedIteration(i -> {
                    log.debug("{}: options popup has not rendered yet", new DurationFormatter(i.fromStart()).toStringWithoutZeroParts());
                    action.accept();
                })
                .waitFor(() -> commonWindowService.getStampValidator().validateStampWholeData(this.window, checkingStamp))
                .orElseThrow(() -> getBrokenWindowException("options popup not found", checkingStamp));
        log.debug("found stamp {}", foundStamp.key().internal().groupName() + "." + foundStamp.key().name());
    }

    private Optional<Stamp> waitNeedRestartPopupRendering() throws InterruptedException {
        log.info("waiting for need restart popup rendering");
        final Stamp checkingStamp = stamps.targetBaseApp().needRestartPopup();
        final Optional<Stamp> maybeStamp = new Waiting()
                .timeout(settings.targetBaseAppSettings().needRestartPopupRenderingTimeout(), TimeUnit.MILLISECONDS)
                .withDelay(settings.targetBaseAppSettings().checkNeedRestartPopupRenderingDelay(), TimeUnit.MILLISECONDS)
                .doOnEveryFailedIteration(i -> log.debug("{}: need restart popup has not rendered yet", new DurationFormatter(i.fromStart()).toStringWithoutZeroParts()))
                .waitFor(() -> commonWindowService.getStampValidator().validateStampWholeData(this.window, checkingStamp));
        maybeStamp.ifPresent(stamp -> log.debug("found stamp {}", stamp.key().internal().groupName() + "." + stamp.key().name()));
        return maybeStamp;
    }

    private void waitServerLineRendering() throws InterruptedException {
        log.info("waiting for server line rendering");
        final Stamp[] checkingStamp = new Stamp[]{
                stamps.targetBaseApp().serverLineUnselected(),
                stamps.targetBaseApp().serverLineSelected()};
        final Stamp foundStamp = new Waiting()
                .timeout(settings.targetBaseAppSettings().serverLineRenderingTimeout(), TimeUnit.MILLISECONDS)
                .withDelay(settings.targetBaseAppSettings().checkServerLineRenderingDelay(), TimeUnit.MILLISECONDS)
                .doOnEveryFailedIteration(i -> log.debug("{}: server line has not rendered yet", new DurationFormatter(i.fromStart()).toStringWithoutZeroParts()))
                .waitFor(() -> commonWindowService.getStampValidator().validateStampWholeData(this.window, checkingStamp))
                .orElseThrow(() -> getBrokenWindowException("server line not found", checkingStamp));
        log.debug("found stamp {}", foundStamp.key().internal().groupName() + "." + foundStamp.key().name());
    }

    private void waitStartButtonRendering() throws InterruptedException {
        log.info("waiting for start button rendering");
        final Stamp checkingStamp = stamps.targetBaseApp().startButton();
        final Stamp foundStamp = new Waiting()
                .timeout(settings.targetBaseAppSettings().startButtonRenderingTimeout(), TimeUnit.MILLISECONDS)
                .withDelay(settings.targetBaseAppSettings().checkStartButtonRenderingDelay(), TimeUnit.MILLISECONDS)
                .doOnEveryFailedIteration(i -> log.debug("{}: start button has not rendered yet", new DurationFormatter(i.fromStart()).toStringWithoutZeroParts()))
                .waitFor(() -> commonWindowService.getStampValidator().validateStampWholeData(this.window, checkingStamp))
                .orElseThrow(() -> getBrokenWindowException("start button not rendered", checkingStamp));
        log.debug("found stamp {}", foundStamp.key().internal().groupName() + "." + foundStamp.key().name());
    }

    private boolean openDailyTrackerPopupAndCheckRendering(final IMouse mouse) throws InterruptedException {
        log.info("waiting for daily tracker popup");
        final Stamp checkingStamp = stamps.targetBaseApp().dailyTrackerPopupCaption();
        final Waiting.EmptyConsumer openPopupAction = () -> mouse.move(settings.targetBaseAppSettings().dailyTrackerButtonPoint()).leftClick();
        final boolean rendered = new Waiting()
                .timeout(settings.targetBaseAppSettings().dailyTrackerPopupRenderingTimeout(), TimeUnit.MILLISECONDS)
                .withDelay(settings.targetBaseAppSettings().checkDailyTrackerPopupRenderingDelay(), TimeUnit.MILLISECONDS)
                .startFrom(openPopupAction)
                .doOnEveryFailedIteration(i -> {
                    log.debug("{}: daily tracker popup has not rendered yet", new DurationFormatter(i.fromStart()).toStringWithoutZeroParts());
                    openPopupAction.accept();
                })
                .waitForBoolean(() -> commonWindowService.getStampValidator().validateStampWholeData(this.window, checkingStamp).isPresent());
        if (!rendered)
            log.warn("daily tracker popup has not rendered (marker={})", commonWindowService.logFailedStamps(this.window, checkingStamp));
        else log.info("daily tracker popup has rendered");
        return rendered;
    }

    private boolean closeDailyTrackerPopupAndCheckRendering(final IMouse mouse) throws InterruptedException {
        log.info("closing daily tracker popup");
        final Stamp checkingStamp = stamps.targetBaseApp().dailyTrackerPopupCaption();
        final Waiting.EmptyConsumer closePopupAction = () -> mouse.move(settings.targetBaseAppSettings().closeDailyTrackerPopupButtonPoint()).leftClick();
        ;
        final boolean notRendered = new Waiting()
                .timeout(settings.targetBaseAppSettings().dailyTrackerPopupRenderingTimeout(), TimeUnit.MILLISECONDS)
                .withDelay(settings.targetBaseAppSettings().checkDailyTrackerPopupRenderingDelay(), TimeUnit.MILLISECONDS)
                .startFrom(closePopupAction)
                .doOnEveryFailedIteration(i -> {
                    log.debug("{}: daily tracker popup still rendered", new DurationFormatter(i.fromStart()).toStringWithoutZeroParts());
                    closePopupAction.accept();
                })
                .waitForBoolean(() -> commonWindowService.getStampValidator().validateStampWholeData(this.window, checkingStamp).isEmpty());
        if (notRendered) log.info("daily tracker popup closed");
        else
            log.warn("daily tracker popup has not closed (marker={})", commonWindowService.logFailedStamps(this.window, checkingStamp));
        return notRendered;
    }

    private boolean checkAccountInfoRendering() throws InterruptedException {
        log.info("check account info rendering");
        final Optional<Stamp> maybe = openAccountInfoPopup();
        if (maybe.isPresent()) {
            window.getKeyboard().enterHotKey(settings.targetBaseAppSettings().openAccountInfoHotkey().toHotKey());
            return true;
        }
        return false;
    }

    private Optional<Stamp> openAccountInfoPopup() throws InterruptedException {
        log.info("wait for account info popup rendering");
        final IKeyboard keyboard = window.getKeyboard();
        final Stamp checkingStamp = stamps.targetBaseApp().accountInfoPopupCaption();
        final HotKey hotKey = settings.targetBaseAppSettings().openAccountInfoHotkey().toHotKey();
        final Optional<Stamp> maybeStamp = new Waiting()
                .timeout(settings.targetBaseAppSettings().accountInfoPopupRenderingTimeout(), TimeUnit.MILLISECONDS)
                .withDelay(settings.targetBaseAppSettings().accountInfoPopupRenderingDelay(), TimeUnit.MILLISECONDS)
                .doOnEveryFailedIteration(i -> {
                    log.debug("{}: account info popup has not rendered yet", new DurationFormatter(i.fromStart()).toStringWithoutZeroParts());
                    keyboard.enterHotKey(hotKey);
                })
                .waitFor(() -> commonWindowService.getStampValidator().validateStampWholeData(this.window, checkingStamp));
        maybeStamp.ifPresent(stamp -> log.debug("found stamp {}", stamp.key().internal().groupName() + "." + stamp.key().name()));
        return maybeStamp;
    }

    private BrokenWindowException getBrokenWindowException(final String message, Stamp... stamps) {
        final String marker = Long.toString(System.currentTimeMillis());
        final BrokenWindowException exception = new BrokenWindowException(message + " (marker=" + marker + ")");
        //log failed stamps
        try {
            commonWindowService.getStampValidator().logFailedStamps(marker, this.window, stamps);
        } catch (InterruptedException e) {
            exception.addSuppressed(e);
        }
        return exception;
    }

    public class RestoredBaseAppWindowImpl implements RestoredBaseAppWindow {

        private RestoredBaseAppWindowImpl() {
        }

        @Override
        public void close() throws InterruptedException {
            if (!closeWindowViaMenu()) throw new BrokenWindowException("cannot close base window via menu");
        }

        @Override
        public void selectServer() throws InterruptedException {
            final Stamp foundStamp = waitServerPageRendering();
            log.info("server page rendered successfully");

            checkIfNotDisconnected();

            final IMouse mouse = getMouse();
            //проверяем нужно ли менять масштаб (масштаб не равен базовому)
            if (!foundStamp.key().equals(StampKeys.TargetBaseApp.optionsButtonBaseScale)) {
                log.info("try change settings");
                changeUISizeToBaseAndMute(mouse);
            }

            waitServerLineRendering();

            //TODO здесь проверять на то, что сервер под обслуживанием и прерывать работу
            checkIfNotDisconnected();

            //select server
            log.info("selecting server");
            mouse.move(settings.targetBaseAppSettings().selectServerButtonPoint()).leftClick();
            //connect server
            log.info("connecting server");
            mouse.move(settings.targetBaseAppSettings().connectServerButtonPoint()).leftClick().leftClick();
            log.info("processing select server page done");
        }

        @Override
        public void chooseCharacter() throws InterruptedException {
            waitStartButtonRendering();
            log.info("start button has rendered successfully");

            checkIfNotDisconnected();

            //select character
            final IMouse mouse = getMouse();
            log.info("selecting character");
            mouse.move(settings.targetBaseAppSettings().selectCharacterButtonPoint()).leftClick();

            new Delay(settings.application().speedFactor()).sleep(500, TimeUnit.MILLISECONDS);

            //start game
            log.info("starting game");
            mouse.move(settings.targetBaseAppSettings().startButtonPoint()).leftClick();

            log.info("character has been selected");
        }

        @Override
        public void checkInGameWindowRendered() throws InterruptedException {
            checkIfNotDisconnected();
            checkIfInGameWindowRendered();
        }
    }

    private class InGameBaseAppWindowImpl implements InGameBaseAppWindow {
        @Override
        public void checkInLoginTracker() throws InterruptedException {
            log.info("check in login tracker");
            final IMouse mouse = getMouse();
            if (!openDailyTrackerPopupAndCheckRendering(mouse)) {
                log.warn("skip daily tracking, because tracker popup has not rendered");
                return;
            }
            final Delay delay = new Delay(settings.application().speedFactor());
            mouse.move(settings.targetBaseAppSettings().dailyTrackerTabPoint()).leftClick();
            delay.sleep(1, TimeUnit.SECONDS); //FIXME вместо задержки проверять отрисовку
            mouse.move(settings.targetBaseAppSettings().trackLoginButtonPoint()).leftClick();
            log.info("login tacker has clicked");
            delay.sleep(1, TimeUnit.SECONDS); //FIXME вместо задержки проверять отрисовку
            closeDailyTrackerPopupAndCheckRendering(mouse);
            log.info("checked in login tracker");
        }

        @Override
        public void checkInAction() throws InterruptedException {
            final int actionsCount = settings.application().actionsCount();
            if (actionsCount < 1) {
                log.info("checking in action disabled");
                return;
            }
            final IMouse mouse = getMouse();
            log.info("try check in action (actions count = {})", actionsCount);
            int index = actionsCount;
            while (index-- > 0) {
                final PointFloat actionButtonPoint = settings.targetBaseAppSettings().actionButtonPoint();
                final PointFloat actionSecondButtonPoint = settings.targetBaseAppSettings().actionSecondButtonPoint();
                final float xOffset = actionSecondButtonPoint.x() - actionButtonPoint.x();
                final float buttonOffset = xOffset * index;
                mouse.move(actionButtonPoint.x() + buttonOffset, actionButtonPoint.y())
                        .leftClick()
                        .leftClick(); //FIXME один клик почему то не отрабатывает
            }
            new Delay(settings.application().speedFactor()).sleep(1, TimeUnit.SECONDS); //FIXME вместо задержки проверять отрисовку
            log.info("checked in action");
        }
    }

    private static class BrokenWindowException extends RuntimeException {
        public BrokenWindowException(final String message) {
            super(message);
        }

    }

    private static class DisconnectedWindowException extends RuntimeException {
        public DisconnectedWindowException(final String message) {
            super(message);
        }

    }
}
