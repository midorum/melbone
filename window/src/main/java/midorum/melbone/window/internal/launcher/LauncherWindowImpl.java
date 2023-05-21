package midorum.melbone.window.internal.launcher;

import com.midorum.win32api.facade.IKeyboard;
import com.midorum.win32api.facade.IMouse;
import com.midorum.win32api.facade.IWindow;
import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.win32.MsLcid;
import com.midorum.win32api.win32.Win32VirtualKey;
import dma.flow.Waiting;
import dma.function.ConsumerThrowing;
import dma.util.DurationFormatter;
import midorum.melbone.model.dto.Account;
import midorum.melbone.model.settings.stamp.Stamp;
import midorum.melbone.settings.StampKeys;
import midorum.melbone.model.exception.ControlledInterruptedException;
import midorum.melbone.model.exception.CriticalErrorException;
import midorum.melbone.model.exception.NeedRetryException;
import midorum.melbone.model.settings.stamp.Stamps;
import midorum.melbone.model.window.launcher.LauncherWindow;
import midorum.melbone.model.window.launcher.RestoredLauncherWindow;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.window.internal.common.CommonWindowService;
import midorum.melbone.window.internal.util.Log;
import midorum.melbone.window.internal.util.StaticResources;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class LauncherWindowImpl implements LauncherWindow {

    private final IWindow window;
    private final Log log;
    private final CommonWindowService commonWindowService;
    private final Settings settings;
    private final Stamps stamps;

    public LauncherWindowImpl(final IWindow window, final CommonWindowService commonWindowService, final Settings settings, final Stamps stamps) {
        this.commonWindowService = commonWindowService;
        this.settings = settings;
        this.stamps = stamps;
        Objects.requireNonNull(window);
        this.window = window;
        this.log = new Log(StaticResources.LOGGER, "launcher [" + window.getSystemId() + "]");
    }

    @Override
    public void restoreAndDo(final ConsumerThrowing<RestoredLauncherWindow, InterruptedException> consumer) throws InterruptedException {
        commonWindowService.bringWindowForeground(window);
        try {
            consumer.accept(new RestoredLauncherWindowImpl(this.window, this.log));
        } catch (BrokenLauncherException e) {
            try {
                log.warn("launcher has broken - need retry");
                if (this.window.isExists() && !tryCloseWindowAndCheckIfClosedNormally()) killProcess();
                throw new NeedRetryException(e.getMessage(), e);
            } catch (Throwable t) {
                t.addSuppressed(e);
                throw t;
            }
        }
    }

    private IMouse getMouse() {
        return window.getWindowMouse(settings.application().speedFactor());
    }

    private Stamp waitLauncherRendering() throws InterruptedException {
        log.info("waiting launcher rendering");
        final Stamp[] checkingStamps = {stamps.targetLauncher().loginButtonNoErrorInactive(),
                stamps.targetLauncher().loginButtonWithErrorInactive(),
                stamps.targetLauncher().loginButtonNoErrorActive(),
                stamps.targetLauncher().loginButtonWithErrorActive(),
                stamps.targetLauncher().startButtonInactive(),
                stamps.targetLauncher().startButtonActive()};
        final Stamp foundStamp = new Waiting()
                .withDelay(settings.targetLauncher().windowRenderingDelay(), TimeUnit.SECONDS)
                .maxTimes(settings.targetLauncher().attemptsToWindowRendering())
                .doOnEveryFailedIteration(i -> log.debug("{}: launcher not rendered properly yet", new DurationFormatter(i.fromStart()).toStringWithoutZeroParts()))
                .waitFor(() -> commonWindowService.getStampValidator().validateStampWholeData(this.window, checkingStamps))
                .orElseThrow(() -> getBrokenLauncherException("launcher not rendered properly", checkingStamps));
        log.debug("found stamp {}", foundStamp.key().internal().groupName() + "." + foundStamp.key().name());
        return foundStamp;
    }

    private void waitStartButtonRendering() throws InterruptedException {
        log.info("waiting for start button");
        final Stamp[] checkingStamps = {stamps.targetLauncher().startButtonActive()};
        final Stamp foundStamp = new Waiting()
                .withDelay(settings.targetLauncher().searchStartButtonDelay(), TimeUnit.SECONDS)
                .maxTimes(settings.targetLauncher().attemptToFindStartButton())
                .doOnEveryFailedIteration(i -> log.debug("{}: not ready to start yet", new DurationFormatter(i.fromStart()).toStringWithoutZeroParts()))
                .waitFor(() -> commonWindowService.getStampValidator().validateStampWholeData(this.window, checkingStamps))
                .orElseThrow(() -> getBrokenLauncherException("not ready to start", checkingStamps));
        log.debug("found stamp {}", foundStamp.key().internal().groupName() + "." + foundStamp.key().name());
    }

    private void logOutAndWaitLoginForm(final IMouse mouse) throws InterruptedException {
        log.info("try log out");
        mouse.move(settings.targetLauncher().accountDropListPoint()).leftClick();
        mouse.move(settings.targetLauncher().accountLogoutPoint()).leftClick();

        checkLoginFormRendered();
    }

    private void checkLoginFormRendered() throws InterruptedException {
        log.info("waiting login form rendering");
        final Stamp[] checkingStamps = {stamps.targetLauncher().loginButtonNoErrorInactive(),
                stamps.targetLauncher().loginButtonNoErrorActive()};
        final Stamp foundStamp = new Waiting()
                .withDelay(1, TimeUnit.SECONDS)
                .maxTimes(60)
                .doOnEveryFailedIteration(i -> log.debug("{}: login form not rendered properly yet", new DurationFormatter(i.fromStart()).toStringWithoutZeroParts()))
                .waitFor(() -> commonWindowService.getStampValidator().validateStampWholeData(this.window, checkingStamps))
                .orElseThrow(() -> getBrokenLauncherException("login form not rendered properly - abort", checkingStamps));
        log.debug("found stamp {}", foundStamp.key().internal().groupName() + "." + foundStamp.key().name());
    }

    private void enterLoginAndPasswordAndLoginOnErroneousForm(final Account account, final IMouse mouse) throws InterruptedException {
        enterLoginAndPasswordAndLogin(account, mouse);
    }

    private void enterLoginAndPasswordAndLogin(final Account account, final IMouse mouse) throws InterruptedException {
        log.info("try log in for {}", account.login());
        final IKeyboard keyboard = window.getKeyboard();

        //preset layout and capital
        window.setKeyboardLayout(MsLcid.enUs);
        keyboard.setCapital(false);

        //input login
        mouse.move(settings.targetLauncher().loginInputPoint()).leftClick();
        keyboard.typeControlled('A')
                .pressAndRelease(Win32VirtualKey.VK_DELETE)
                .type(account.login());
        log.info("entered login for {}", account.login());
        //input password
        mouse.move(settings.targetLauncher().passwordInputPoint()).leftClick();
        keyboard.typeControlled('A')
                .pressAndRelease(Win32VirtualKey.VK_DELETE)
                .type(account.password());
        log.info("entered password for {}", account.login());
        //press login button
        mouse.move(settings.targetLauncher().loginButtonPoint()).leftClick();
        log.info("pressed login button");

        checkLoginError();

        //TODO select game type
        //TODO select server type

        log.info("{} logged in successfully", account.login());
    }

    private void checkLoginError() throws InterruptedException {
        log.info("checking for login error");
        new Waiting()
                .withDelay(1, TimeUnit.SECONDS)
                .maxTimes(2)
                .doOnEveryFailedIteration(i -> log.debug("{}: check for login error", new DurationFormatter(i.fromStart()).toStringWithoutZeroParts()))
                .waitFor(() -> commonWindowService.getStampValidator().validateStampWholeData(this.window, stamps.targetLauncher().errorExclamationSign()))
                .ifPresent(stamp -> {
                    throw new CriticalErrorException("login error occurred - abort");
                });
    }

    private boolean tryCloseWindowAndCheckIfClosedNormally() throws InterruptedException {
        if (!windowIsVisibleAndHasNormalMetrics()) return false;
        log.info("try close broken launcher");
        final Waiting.EmptyConsumer closeWindowAction = () -> {
            if (windowIsVisibleAndHasNormalMetrics()) closeWindow();
        };
        final boolean windowClosed = new Waiting()
                .startFrom(closeWindowAction)
                .latency(settings.targetLauncher().closingWindowDelay(), TimeUnit.SECONDS)
                .maxTimes(5)
                .withDelay(settings.targetLauncher().closingWindowDelay(), TimeUnit.SECONDS)
                .doOnEveryFailedIteration(i -> {
                    log.debug("{}: launcher hasn't closed yet", new DurationFormatter(i.fromStart()).toStringWithoutZeroParts());
                    closeWindowAction.accept();
                })
                .waitForBoolean(() -> !this.window.isExists());
        log.info("launcher{}closed normally", windowClosed ? " " : " hasn't ");
        return windowClosed;
    }

    private boolean windowIsVisibleAndHasNormalMetrics() {
        if (!this.window.isVisible()) {
            log.warn("window is not visible");
            return false;
        }
        final Rectangle windowRectangle = this.window.getWindowRectangle();
        final Rectangle windowDimensions = settings.targetLauncher().windowDimensions();
        final boolean equals = windowRectangle.width() == windowDimensions.width() && windowRectangle.height() == windowDimensions.height();
        if (!equals) log.warn("window dimensions are wrong: expected {} but are {}", windowDimensions, windowRectangle);
        return equals;
    }

    private void closeWindow() throws InterruptedException {
        log.info("closing launcher window");
        final IMouse mouse = getMouse();
        mouse.move(settings.targetLauncher().windowCloseButtonPoint()).leftClick();
        checkConfirmQuitDialogRenderedAndAcceptIt();
    }

    private void killProcess() {
        log.info("terminating launcher window process");
        this.window.getProcess().terminate();
    }

    private void checkConfirmQuitDialogRenderedAndAcceptIt() throws InterruptedException {
        log.info("waiting for confirm quit dialog");
        new Waiting()
                .withDelay(500, TimeUnit.MILLISECONDS)
                .maxTimes(5)
                .doOnEveryFailedIteration(i -> log.debug("{}: confirm quit dialog has not rendered yet", new DurationFormatter(i.fromStart()).toStringWithoutZeroParts()))
                .waitFor(this::findConfirmQuitDialog)
                .ifPresent(confirmQuitDialog -> {
                    log.info("accepting confirm quit dialog");
                    try {
                        confirmQuitDialog.clickConfirmButton();
                    } catch (InterruptedException e) {
                        throw new ControlledInterruptedException(e.getMessage(), e);
                    }
                });
    }

    private Optional<ConfirmQuitDialog> findConfirmQuitDialog() {
        log.info("searching confirm quit dialog");
        final Rectangle requiredDimensions = settings.targetLauncher().confirmQuitDialogDimensions();
        return commonWindowService.getWin32System()
                .findAllWindows(settings.targetLauncher().confirmQuitDialogTitle(), null, true).stream()
                .filter(w -> {
                    final Rectangle windowRectangle = w.getWindowRectangle();
                    return requiredDimensions.width() == windowRectangle.width()
                            && requiredDimensions.height() == windowRectangle.height();
                })
                .filter(w -> {
                    try {
                        return commonWindowService.getStampValidator().validateStampWholeData(w, stamps.targetLauncher().quitConfirmPopup()).isPresent();
                    } catch (InterruptedException e) {
                        throw new ControlledInterruptedException(e.getMessage(), e);
                    }
                })
                .map(ConfirmQuitDialog::new)
                .findFirst();
    }

    private BrokenLauncherException getBrokenLauncherException(final String message, Stamp... stamps) {
        final String marker = Long.toString(System.currentTimeMillis());
        final BrokenLauncherException exception = new BrokenLauncherException(message + " (marker=" + marker + ")");
        //log failed stamps
        try {
            commonWindowService.getStampValidator().logFailedStamps(marker, this.window, stamps);
        } catch (InterruptedException e) {
            exception.addSuppressed(e);
        }
        return exception;
    }

    private class RestoredLauncherWindowImpl implements RestoredLauncherWindow {

        private final IWindow window;
        private final Log log;

        public RestoredLauncherWindowImpl(final IWindow window, final Log log) {
            this.window = window;
            this.log = log;
        }

        @Override
        public boolean checkClientIsAlreadyRunningWindowRendered() throws InterruptedException {
            return commonWindowService.getStampValidator().validateStampWholeData(this.window, stamps.targetLauncher().clientIsAlreadyRunning()).isPresent();
        }

        @Override
        public void login(final Account account) throws InterruptedException {
            final IMouse mouse = getMouse();
            final Stamp foundStamp = waitLauncherRendering();
            //log out if needed
            if (foundStamp.key().equals(StampKeys.TargetLauncher.playButtonInactive)) {
                log.info("play form is opened; play button is inactive");
                logOutAndWaitLoginForm(mouse);
            } else if (foundStamp.key().equals(StampKeys.TargetLauncher.playButtonActive)) {
                log.info("play form is opened; play button is active");
                logOutAndWaitLoginForm(mouse);
            }
            log.info("login form has rendered");
            enterLoginAndPasswordAndLogin(account, mouse);
        }

        @Override
        public void startGameWhenGetReady() throws InterruptedException {
            waitStartButtonRendering();
            log.info("starting game");
            //press Start button
            getMouse().move(settings.targetLauncher().startButtonPoint()).leftClick();
        }
    }

    private class ConfirmQuitDialog {
        private final Log log;

        private ConfirmQuitDialog(final IWindow window) {
            log = new Log(StaticResources.LOGGER, this.getClass().getSimpleName() + " [" + window.getSystemId() + "]");
        }

        public void clickConfirmButton() throws InterruptedException {
            log.info("closing confirm quit dialog");
            getMouse().move(settings.targetLauncher().closeQuitConfirmPopupButtonPoint()).leftClick();
        }
    }

    private static class BrokenLauncherException extends RuntimeException {

        public BrokenLauncherException(final String message) {
            super(message);
        }

    }
}
