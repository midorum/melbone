package midorum.melbone.window.internal.launcher;

import com.midorum.win32api.facade.*;
import com.midorum.win32api.facade.exception.Win32ApiException;
import dma.flow.Waiting;
import dma.util.Delay;
import dma.util.DurationFormatter;
import midorum.melbone.model.exception.CannotGetUserInputException;
import midorum.melbone.model.exception.ControlledInterruptedException;
import midorum.melbone.model.exception.CriticalErrorException;
import midorum.melbone.model.exception.NeedRetryException;
import midorum.melbone.model.settings.stamp.Stamps;
import midorum.melbone.model.window.launcher.LauncherWindow;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.window.internal.uac.UacWindowFactory;
import midorum.melbone.window.internal.util.Log;
import midorum.melbone.window.internal.util.StaticResources;
import midorum.melbone.window.internal.common.CommonWindowService;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class LauncherWindowFactory {

    private final Logger logger = StaticResources.LOGGER;
    private final CommonWindowService commonWindowService;
    private final Win32System win32System;
    private final UacWindowFactory uacWindowFactory;
    private final Settings settings;
    private final Stamps stamps;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSS");


    public LauncherWindowFactory(final CommonWindowService commonWindowService,
                                 final Settings settings,
                                 final UacWindowFactory uacWindowFactory,
                                 final Stamps stamps) {
        this.commonWindowService = commonWindowService;
        this.win32System = commonWindowService.getWin32System();
        this.uacWindowFactory = uacWindowFactory;
        this.settings = settings;
        this.stamps = stamps;
    }

    public Optional<LauncherWindow> findWindowOrTryStartLauncher() throws InterruptedException, Win32ApiException {
        logger.info("find or start launcher");
        final String marker = Long.toString(System.currentTimeMillis());
        final Optional<LauncherWindow> maybeExistLauncherWindow = searchExistLauncherWindow(marker);
        if (maybeExistLauncherWindow.isPresent()) return maybeExistLauncherWindow;
        //TODO check network accessibility before start launcher
        final Optional<LauncherWindow> maybeNewLauncherWindow = tryStartNewLauncherWindow();
        if (maybeNewLauncherWindow.isPresent()) return maybeNewLauncherWindow;
        logger.info("launcher not started properly");
        if (detectNetworkErrorAlert(marker)) return Optional.empty();
        if (detectPossibleUac()) {
            commonWindowService.takeAndSaveWholeScreenShot("UAC window found", marker);
            throw new CriticalErrorException("UAC window found. Can not confirm UAC request programmatically.");
        }
        commonWindowService.takeAndSaveWholeScreenShot("can not start launcher or find launcher window", marker);
        return Optional.empty();
    }

    private Optional<LauncherWindow> searchExistLauncherWindow(final String marker) throws InterruptedException, Win32ApiException {
        logger.info("look for exist launcher window");
        final Optional<LauncherWindow> maybeExistLauncherWindow = findWindow();
        if (maybeExistLauncherWindow.isPresent()) return maybeExistLauncherWindow;
        logger.info("exist launcher window not found");
        detectNetworkErrorAlert(marker);
        detectBrokenLauncherProcess();
        return Optional.empty();
    }

    private void detectBrokenLauncherProcess() throws InterruptedException, Win32ApiException {
        final List<IProcess> processes = win32System.listProcessesWithName(settings.targetLauncher().processName()).getOrThrow();
        if (processes.isEmpty()) return;
        final Delay delay = new Delay(settings.application().speedFactor());
        try {
            processes.forEach(process -> {
                final String processInfo;
                try {
                    processInfo = processInfo(process).getOrThrow();
                    logger.warn("found broken launcher process: {}", processInfo);
                    if (System.currentTimeMillis() - process.getCreationTime().getOrThrow() > settings.targetLauncher().brokenProcessTimeout()) {
                        logger.warn("try terminate broken launcher process: {}", processInfo);
                        process.terminate();
                    }
                } catch (Win32ApiException e) {
                    throw new ControlledWin32ApiException(e);
                }
            });
        } catch (ControlledWin32ApiException e) {
            throw (Win32ApiException) e.getCause();
        }
        delay.sleep(10, TimeUnit.SECONDS);
        final List<IProcess> processesAfter = win32System.listProcessesWithName(settings.targetLauncher().processName()).getOrThrow();
        if (processesAfter.isEmpty()) return;
        throw new NeedRetryException("Found broken launcher process and it isn't closed - need retry");
    }

    private Either<String> processInfo(final IProcess process) {
        return process.getCreationTime().map(creationTime -> process.pid() + " - [" + process.name().orElse("no name")
                + "] created at " + dateTimeFormatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(creationTime), ZoneId.systemDefault())));
    }

    private Optional<LauncherWindow> tryStartNewLauncherWindow() throws InterruptedException {
        final int windowAppearingLatency = settings.targetLauncher().windowAppearingLatency();
        final int windowAppearingDelay = settings.targetLauncher().windowAppearingDelay();
        final int windowAppearingTimeout = settings.targetLauncher().windowAppearingTimeout();
        final String processName = settings.targetLauncher().processName();
        return new Waiting()
                .withDelay(windowAppearingDelay, TimeUnit.MILLISECONDS)
                .timeout(windowAppearingTimeout, TimeUnit.MILLISECONDS)
                .startFrom(() -> {
                    logger.info("try start launcher");
                    clickDesktopIcon();
                })
                .latency(windowAppearingLatency, TimeUnit.MILLISECONDS)
                .doOnEveryFailedIteration(i -> {
                    final String lastsFromStart = new DurationFormatter(i.fromStart()).toStringWithoutZeroParts();
                    if (i.fromLastCheckEnds().toMillis() > windowAppearingDelay) {
                        try {
                            final List<IProcess> processes = win32System.listProcessesWithName(processName).getOrThrow();
                            if (processes.isEmpty()) {
                                logger.warn("{}: launcher window and process not found yet - try start again", lastsFromStart);
                                clickDesktopIcon();
                            } else {
                                logger.info("{}: launcher window not found yet but process has started", lastsFromStart);
                            }
                        } catch (Win32ApiException e) {
                            logger.error("cannot get process with name " + processName + " - skip", e);
                        }
                    } else {
                        logger.debug("{}: launcher window not found yet", lastsFromStart);
                    }
                })
                .waitFor(this::findWindow);
    }

    private void clickDesktopIcon() throws InterruptedException {
        win32System.minimizeAllWindows();
        clickLauncherDesktopIcon();
    }

    public Optional<LauncherWindow> findWindow() {
        //TODO need regexp based search
        final Rectangle requiredDimensions = settings.targetLauncher().windowDimensions();
        final List<IWindow> allWindows = win32System.findAllWindows(settings.targetLauncher().windowTitle(), null, true);
        return allWindows.stream()
                .filter(window -> window.getWindowRectangle()
                        .map(r -> requiredDimensions.width() == r.width()
                                && requiredDimensions.height() == r.height())
                        .getOrHandleError(e -> {
                            logger.warn("cannot check window attributes (" + window.getSystemId() + ") - skip", e);
                            return false;
                        }))
                .peek(window -> logger.info("found launcher window {}", window.getSystemId()))
                .map(window -> new LauncherWindowImpl(window, commonWindowService, settings, stamps))
                .map(LauncherWindow.class::cast)
                .findFirst();
    }

    private void clickLauncherDesktopIcon() throws InterruptedException {
        win32System.getScreenMouse(settings.application().speedFactor())
                .move(settings.targetLauncher().desktopShortcutLocationPoint())
                .leftClick()
                .leftClick();
    }

    private boolean detectNetworkErrorAlert(final String marker) {
        logger.info("look for network error alert");
        final Optional<NetworkErrorAlertWindow> networkErrorAlertWindow = findNetworkErrorAlertWindow();
        networkErrorAlertWindow.ifPresentOrElse(alert -> {
            logger.warn("found network error alert: close it");
            try {
                if (!closeNetworkErrorAlert())
                    commonWindowService.takeAndSaveWholeScreenShot("cannot close network error alert", marker);
            } catch (InterruptedException e) {
                throw new ControlledInterruptedException(e);
            }
        }, () -> logger.info("network error alert not found"));
        return networkErrorAlertWindow.isPresent();
    }

    private boolean closeNetworkErrorAlert() throws InterruptedException {
        return new Waiting()
                .timeout(settings.targetLauncher().networkErrorDialogTimeout(), TimeUnit.MILLISECONDS)
                .withDelay(settings.targetLauncher().networkErrorDialogDelay(), TimeUnit.MILLISECONDS)
                .waitForBoolean(() -> {
                    final Optional<NetworkErrorAlertWindow> maybeNetworkErrorDialog = findNetworkErrorAlertWindow();
                    maybeNetworkErrorDialog.ifPresent(networkErrorAlertWindow -> {
                        try {
                            networkErrorAlertWindow.clickConfirmButton();
                        } catch (InterruptedException e) {
                            throw new ControlledInterruptedException(e);
                        } catch (CannotGetUserInputException e) {
                            logger.error("cannot close network error alert because cannot get user input on it - skip:", e);
                        }
                    });
                    return maybeNetworkErrorDialog.isEmpty();
                });
    }

    private Optional<NetworkErrorAlertWindow> findNetworkErrorAlertWindow() {
        final Rectangle requiredDimensions = settings.targetLauncher().networkErrorDialogDimensions();
        final List<IWindow> allWindows = win32System.findAllWindows(settings.targetLauncher().networkErrorDialogTitle(), null, true);
        return allWindows.stream()
                .filter(w -> w.getWindowRectangle()
                        .map(r -> requiredDimensions.width() == r.width()
                                && requiredDimensions.height() == r.height())
                        .getOrHandleError(e -> {
                            logger.warn("cannot check window attributes (" + w.getSystemId() + ") - skip", e);
                            return false;
                        }))
                //FIXME проблема с получением снимков для topmost-окон: неправильные координаты снимка, соответственно валидация не проходит
                //TODO после исправления включить
//                    .filter(w -> {
//                        try {
//                            return StampValidator.INSTANCE.validateStampWholeData(w, Stamps.TargetLauncher.networkErrorDialog()).isPresent();
//                        } catch (InterruptedException e) {
//                            throw new ControlledInterruptedException(e.getMessage(), e);
//                        }
//                    })
                .map((IWindow window) -> new NetworkErrorAlertWindow(window, settings))
                .findFirst();
    }

    private boolean detectPossibleUac() {
        return uacWindowFactory.findUacOverlayWindow().isPresent();
    }

    public class NetworkErrorAlertWindow {

        private final IWindow window;
        private final Settings settings;
        private final Log log;

        public NetworkErrorAlertWindow(final IWindow window, final Settings settings) {
            this.window = window;
            this.settings = settings;
            this.log = new Log(StaticResources.LOGGER, window.getSystemId());
        }

        public void clickConfirmButton() throws InterruptedException, CannotGetUserInputException {
            log.debug("close network error alert");
            commonWindowService.bringForeground(window).andDo(foregroundWindow -> {
                try {
                    foregroundWindow.getMouse().clickAtPoint(settings.targetLauncher().closeNetworkErrorDialogButtonPoint());
                } catch (Win32ApiException e) {
                    throw new CannotGetUserInputException(e.getMessage(), e);
                }
            });
        }

    }

    private static class ControlledWin32ApiException extends RuntimeException {

        public ControlledWin32ApiException(final Win32ApiException exception) {
            super(exception);
        }
    }

}