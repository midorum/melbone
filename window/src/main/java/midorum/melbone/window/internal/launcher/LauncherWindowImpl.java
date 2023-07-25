package midorum.melbone.window.internal.launcher;

import com.midorum.win32api.facade.*;
import com.midorum.win32api.facade.exception.Win32ApiException;
import com.midorum.win32api.win32.MsLcid;
import com.midorum.win32api.win32.Win32VirtualKey;
import dma.flow.Waiting;
import dma.function.EmptyPredicateThrowing;
import dma.util.DurationFormatter;
import midorum.melbone.model.dto.Account;
import midorum.melbone.model.exception.CannotGetUserInputException;
import midorum.melbone.model.settings.stamp.Stamp;
import midorum.melbone.model.window.WindowConsumer;
import midorum.melbone.settings.StampKeys;
import midorum.melbone.model.exception.ControlledInterruptedException;
import midorum.melbone.model.exception.CriticalErrorException;
import midorum.melbone.model.exception.NeedRetryException;
import midorum.melbone.model.settings.stamp.Stamps;
import midorum.melbone.model.window.launcher.LauncherWindow;
import midorum.melbone.model.window.launcher.RestoredLauncherWindow;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.window.internal.common.CommonWindowService;
import midorum.melbone.window.internal.common.ForegroundWindow;
import midorum.melbone.window.internal.common.Mouse;
import midorum.melbone.window.internal.util.Log;
import midorum.melbone.window.internal.util.StaticResources;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class LauncherWindowImpl implements LauncherWindow {

    private final IWindow window;
    private final Log log;
    private final CommonWindowService commonWindowService;
    private final Settings settings;
    private final Stamps stamps;

    public LauncherWindowImpl(final IWindow window, final CommonWindowService commonWindowService, final Settings settings, final Stamps stamps) {
        this.window = window;
        this.commonWindowService = commonWindowService;
        this.settings = settings;
        this.stamps = stamps;
        this.log = new Log(StaticResources.LOGGER, "launcher [" + window.getSystemId() + "]");
    }

    @Override
    public void restoreAndDo(final WindowConsumer<RestoredLauncherWindow> consumer) throws InterruptedException, Win32ApiException {
        if (!this.window.isExists().getOrThrow()) {
            log.warn("window not found - skip");
            return;
        }
        try {
            commonWindowService.bringForeground(window).andDo(foregroundWindow -> {
                try {
                    consumer.accept(new RestoredLauncherWindowImpl(foregroundWindow, log));
                } catch (BrokenLauncherException e) {
                    log.warn("launcher is broken - " + e.getMessage() + " - close:", e);
                    try {
                        tryCloseWindowAndCheckIfClosedNormally(foregroundWindow);
                    } catch (BrokenLauncherException ex) {
                        e.addSuppressed(ex);
                    }
                    throw e;
                }
            });
        } catch (BrokenLauncherException | CannotGetUserInputException e) {
            final String message = "launcher is broken - " + e.getMessage() + " - need retry";
            log.warn(message + ":", e);
            if (this.window.isExists().getOrThrow()) killProcess();
            throw new NeedRetryException(message, e);
        }
    }

    private String getLogMarker() {
        return Long.toString(System.currentTimeMillis());
    }

    private Stamp waitLauncherRendering(final ForegroundWindow foregroundWindow) throws InterruptedException, CannotGetUserInputException {
        log.info("wait for launcher rendering");
        final String logMarker = getLogMarker();
        return foregroundWindow.waiting()
                .withTimeout(settings.targetLauncher().windowRenderingTimeout())
                .withDelay(settings.targetLauncher().windowRenderingDelay())
                .logFailedStampsWithMarker(logMarker)
                .forAnyStamp(stamps.targetLauncher().loginButtonNoErrorInactive(),
                        stamps.targetLauncher().loginButtonWithErrorInactive(),
                        stamps.targetLauncher().loginButtonNoErrorActive(),
                        stamps.targetLauncher().loginButtonWithErrorActive(),
                        stamps.targetLauncher().startButtonInactive(),
                        stamps.targetLauncher().startButtonActive())
                .orElseThrow(() -> getBrokenLauncherException("launcher not rendered properly", logMarker));
    }

    private Stamp waitStartButtonRendering(final ForegroundWindow foregroundWindow) throws InterruptedException, CannotGetUserInputException {
        log.info("wait for start button rendering");
        final String logMarker = getLogMarker();
        return foregroundWindow.waiting()
                .withTimeout(settings.targetLauncher().searchStartButtonTimeout())
                .withDelay(settings.targetLauncher().searchStartButtonDelay())
                .logFailedStampsWithMarker(logMarker)
                .forStamp(stamps.targetLauncher().startButtonActive())
                .orElseThrow(() -> getBrokenLauncherException("not ready to start", logMarker));
    }

    private Stamp logOutAndWaitLoginForm(final ForegroundWindow foregroundWindow) throws InterruptedException, CannotGetUserInputException, Win32ApiException {
        log.info("try log out");
        final Mouse mouse = foregroundWindow.getMouse();
        mouse.clickAtPoint(settings.targetLauncher().accountDropListPoint());
        mouse.clickAtPoint(settings.targetLauncher().accountLogoutPoint());

        return checkLoginFormRendered(foregroundWindow);
    }

    private Stamp checkLoginFormRendered(final ForegroundWindow foregroundWindow) throws InterruptedException, CannotGetUserInputException {
        log.info("waiting login form rendering");
        final String logMarker = getLogMarker();
        return foregroundWindow.waiting()
                .withTimeout(settings.targetLauncher().windowRenderingTimeout())
                .withDelay(settings.targetLauncher().windowRenderingDelay())
                .logFailedStampsWithMarker(logMarker)
                .forAnyStamp(stamps.targetLauncher().loginButtonNoErrorInactive(),
                        stamps.targetLauncher().loginButtonNoErrorActive())
                .orElseThrow(() -> getBrokenLauncherException("login form not rendered properly - abort", logMarker));
    }

    private void enterLoginAndPasswordAndLoginOnErroneousForm(final ForegroundWindow foregroundWindow, final Account account) throws InterruptedException, CannotGetUserInputException, Win32ApiException {
        enterLoginAndPasswordAndLogin(foregroundWindow, account);
    }

    private void enterLoginAndPasswordAndLogin(final ForegroundWindow foregroundWindow, final Account account) throws InterruptedException, CannotGetUserInputException, Win32ApiException {
        log.info("try log in for {}", account.login());

        final Mouse mouse = foregroundWindow.getMouse();
        final IKeyboard keyboard = foregroundWindow.getKeyboard();
        final HotKey hotKeyCtrlA = new HotKey.Builder().withControl().code(Win32VirtualKey.VK_A).build();
        final HotKey hotKeyDelete = new HotKey.Builder().code(Win32VirtualKey.VK_DELETE).build();

        //preset layout and capital
        window.setKeyboardLayout(MsLcid.enUs);
        keyboard.setCapital(false);

        //input login
        mouse.clickAtPoint(settings.targetLauncher().loginInputPoint());
        keyboard.enterHotKey(hotKeyCtrlA)
                .enterHotKey(hotKeyDelete)
                .type(account.login());
        log.info("entered login for {}", account.login());
        //input password
        mouse.clickAtPoint(settings.targetLauncher().passwordInputPoint());
        keyboard.enterHotKey(hotKeyCtrlA)
                .enterHotKey(hotKeyDelete)
                .type(account.password());
        log.info("entered password for {}", account.login());
        //press login button
        mouse.clickAtPoint(settings.targetLauncher().loginButtonPoint());
        log.info("pressed login button");

        checkLoginError(foregroundWindow);

        //TODO select game type
        //TODO select server type

        log.info("{} logged in successfully", account.login());
    }

    private void checkLoginError(final ForegroundWindow foregroundWindow) throws InterruptedException, CannotGetUserInputException {
        log.info("checking for login error");
        foregroundWindow.waiting()
                .withTimeout(settings.targetLauncher().loginTimeout())
                .withDelay(settings.targetLauncher().checkLoginDelay())
                .forStamp(stamps.targetLauncher().errorExclamationSign())
                .ifPresent(stamp -> {
                    throw new CriticalErrorException("login error occurred - abort");
                });
    }

    private void tryCloseWindowAndCheckIfClosedNormally(final ForegroundWindow foregroundWindow) throws InterruptedException {
        try {
            if (!windowIsVisibleAndHasNormalMetrics()) return;
        } catch (Win32ApiException e) {
            throw new BrokenLauncherException("Cannot check launcher window attributes", e);
        }
        final Waiting.EmptyConsumer closeWindowAction = () -> {
            try {
                if (windowIsVisibleAndHasNormalMetrics()) closeWindow(foregroundWindow);
            } catch (CannotGetUserInputException | Win32ApiException e) {
                throw new BrokenLauncherException("Cannot close launcher window", e);
            }
        };
        final EmptyPredicateThrowing<InterruptedException> windowExistingChecker = () -> {
            try {
                return !this.window.isExists().getOrThrow();
            } catch (Win32ApiException e) {
                throw new BrokenLauncherException("Cannot check existing launcher window", e);
            }
        };
        final boolean windowClosed = new Waiting()
                .startFrom(closeWindowAction)
                .latency(settings.targetLauncher().closingWindowDelay(), TimeUnit.MILLISECONDS)
                .timeout(settings.targetLauncher().closingWindowTimeout(), TimeUnit.MILLISECONDS)
                .withDelay(settings.targetLauncher().closingWindowDelay(), TimeUnit.MILLISECONDS)
                .doOnEveryFailedIteration(i -> {
                    log.debug("{}: launcher hasn't closed yet", new DurationFormatter(i.fromStart()).toStringWithoutZeroParts());
                    closeWindowAction.accept();
                })
                .waitForBoolean(windowExistingChecker);
        log.info("launcher{}closed normally", windowClosed ? " " : " hasn't ");
    }

    private boolean windowIsVisibleAndHasNormalMetrics() throws Win32ApiException {
        if (!this.window.isVisible()) {
            log.warn("window is not visible");
            return false;
        }
        final Rectangle windowDimensions = settings.targetLauncher().windowDimensions();
        return window.getWindowRectangle()
                .map(r -> {
                    final boolean e = r.width() == windowDimensions.width() && r.height() == windowDimensions.height();
                    if (!e) log.warn("window dimensions are wrong: expected {} but are {}", windowDimensions, r);
                    return e;
                })
                .getOrThrow();
    }

    private void closeWindow(final ForegroundWindow foregroundWindow) throws InterruptedException, CannotGetUserInputException, Win32ApiException {
        log.info("closing launcher window");
        foregroundWindow.getMouse().clickAtPoint(settings.targetLauncher().windowCloseButtonPoint());
        checkConfirmQuitDialogRenderedAndAcceptIt();
    }

    private void killProcess() throws Win32ApiException {
        log.info("terminating launcher window process");
        this.window.getProcess().getOrThrow().terminate();
    }

    private void checkConfirmQuitDialogRenderedAndAcceptIt() throws InterruptedException {
        log.info("wait for confirm quit dialog");
        final ConfirmQuitDialogFactory confirmQuitDialogFactory = new ConfirmQuitDialogFactory();
        new Waiting()
                .timeout(settings.targetLauncher().confirmQuitDialogRenderingTimeout(), TimeUnit.MILLISECONDS)
                .withDelay(settings.targetLauncher().confirmQuitDialogRenderingDelay(), TimeUnit.MILLISECONDS)
                .doOnEveryFailedIteration(i -> log.debug("{}: confirm quit dialog has not rendered yet", new DurationFormatter(i.fromStart()).toStringWithoutZeroParts()))
                .waitFor(confirmQuitDialogFactory::findConfirmQuitDialog)
                .ifPresent(ConfirmQuitDialog::clickConfirmButton);
    }

    private BrokenLauncherException getBrokenLauncherException(final String message, final String marker) {
        return new BrokenLauncherException(message + " (marker=" + marker + ")");
    }

    private class RestoredLauncherWindowImpl implements RestoredLauncherWindow {

        private final ForegroundWindow foregroundWindow;
        private final Log log;

        public RestoredLauncherWindowImpl(final ForegroundWindow foregroundWindow, final Log log) {
            this.foregroundWindow = foregroundWindow;
            this.log = log;
        }

        @Override
        public boolean checkClientIsAlreadyRunningWindowRendered() throws InterruptedException, CannotGetUserInputException {
            return foregroundWindow.waiting()
                    .forStamp(stamps.targetLauncher().clientIsAlreadyRunning())
                    .isPresent();
        }

        @Override
        public void login(final Account account) throws InterruptedException, CannotGetUserInputException {
            try {
                final Stamp foundStamp = waitLauncherRendering(foregroundWindow);
                //log out if needed
                if (foundStamp.key().equals(StampKeys.TargetLauncher.playButtonInactive)) {
                    log.info("play form is opened; play button is inactive");
                    logOutAndWaitLoginForm(foregroundWindow);
                } else if (foundStamp.key().equals(StampKeys.TargetLauncher.playButtonActive)) {
                    log.info("play form is opened; play button is active");
                    logOutAndWaitLoginForm(foregroundWindow);
                }
                log.info("login form has rendered");
                enterLoginAndPasswordAndLogin(foregroundWindow, account);
            } catch (Win32ApiException e) {
                throw new CannotGetUserInputException(e.getMessage(), e);
            }
        }

        @Override
        public void startGameWhenGetReady() throws InterruptedException, CannotGetUserInputException {
            try {
                waitStartButtonRendering(foregroundWindow);
                log.info("starting game");
                foregroundWindow.getMouse().clickAtPoint(settings.targetLauncher().startButtonPoint());
            } catch (Win32ApiException e) {
                throw new CannotGetUserInputException(e.getMessage(), e);
            }
        }
    }

    private class ConfirmQuitDialogFactory {

        public Optional<ConfirmQuitDialog> findConfirmQuitDialog() {
            log.info("searching confirm quit dialog");
            final Rectangle requiredDimensions = settings.targetLauncher().confirmQuitDialogDimensions();
            return commonWindowService.getWin32System()
                    .findAllWindows(settings.targetLauncher().confirmQuitDialogTitle(), null, true).stream()
                    .filter(w -> w.getWindowRectangle()
                            .map(r -> requiredDimensions.width() == r.width()
                                    && requiredDimensions.height() == r.height())
                            .getOrHandleError(e -> {
                                log.warn("cannot check window attributes (" + w.getSystemId() + ") - skip", e);
                                return false;
                            }))
                    .filter(w -> {
                        try {
                            return commonWindowService.bringForeground(w).andDo(foregroundWindow -> {
                                return foregroundWindow.waiting().forStamp(stamps.targetLauncher().quitConfirmPopup());
                            }).isPresent();
                        } catch (InterruptedException e) {
                            throw new ControlledInterruptedException(e.getMessage(), e);
                        } catch (CannotGetUserInputException e) {
                            throw new BrokenLauncherException("Cannot get user input in window " + w.getSystemId(), e);
                        }
                    })
                    .map(ConfirmQuitDialog::new)
                    .findFirst();
        }

    }

    private class ConfirmQuitDialog {
        private final IWindow window;
        private final Log log;

        private ConfirmQuitDialog(final IWindow window) {
            this.window = window;
            this.log = new Log(StaticResources.LOGGER, this.getClass().getSimpleName() + " [" + window.getSystemId() + "]");
        }

        public void clickConfirmButton() {
            log.info("accepting confirm quit dialog");
            try {
                commonWindowService.bringForeground(window).andDo(foregroundWindow -> {
                    try {
                        foregroundWindow.getMouse().clickAtPoint(settings.targetLauncher().closeQuitConfirmPopupButtonPoint());
                    } catch (Win32ApiException e) {
                        throw new CannotGetUserInputException(e.getMessage(), e);
                    }
                });
            } catch (CannotGetUserInputException e) {
                throw new BrokenLauncherException("Cannot get user input in dialog", e);
            } catch (InterruptedException e) {
                throw new ControlledInterruptedException(e.getMessage(), e);
            }
        }
    }

    private static class BrokenLauncherException extends RuntimeException {

        public BrokenLauncherException(final String message) {
            super(message);
        }

        public BrokenLauncherException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
