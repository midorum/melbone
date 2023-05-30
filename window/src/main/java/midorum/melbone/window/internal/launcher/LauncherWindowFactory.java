package midorum.melbone.window.internal.launcher;

import com.midorum.win32api.facade.*;
import com.midorum.win32api.struct.PointInt;
import dma.flow.Waiting;
import dma.util.Delay;
import dma.util.DurationFormatter;
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

    public Optional<LauncherWindow> findWindowOrTryStartLauncher() throws InterruptedException {
        logger.info("find or start launcher");
        final Optional<LauncherWindow> maybeExistLauncherWindow = searchExistLauncherWindow();
        if (maybeExistLauncherWindow.isPresent()) return maybeExistLauncherWindow;
        detectLauncherInitializationError(); //TODO check network accessibility before start launcher to prevent this error
        final Optional<LauncherWindow> maybeNewLauncherWindow = tryStartNewLauncherWindow();
        if (maybeNewLauncherWindow.isPresent()) return maybeNewLauncherWindow;
        detectLauncherInitializationError(); //TODO check network accessibility before start launcher to prevent this error
        detectPossibleOverlay();
        logger.warn("can not start launcher or find launcher window");
        return Optional.empty();
    }

    private Optional<LauncherWindow> searchExistLauncherWindow() throws InterruptedException {
        final Optional<LauncherWindow> maybeExistLauncherWindow = findWindow();
        if (maybeExistLauncherWindow.isPresent()) return maybeExistLauncherWindow;
        final List<IProcess> processes = win32System.listProcessesWithName(settings.targetLauncher().processName());
        if (processes.isEmpty()) return Optional.empty();
        final Delay delay = new Delay(settings.application().speedFactor());
        processes.forEach(process -> {
            final String processInfo = processInfo(process);
            logger.warn("found broken launcher process: {}", processInfo);
            if (System.currentTimeMillis() - process.getCreationTime() > settings.targetLauncher().brokenProcessTimeout()) {
                logger.warn("try terminate broken launcher process: {}", processInfo);
                process.terminate();
            }
        });
        delay.sleep(10, TimeUnit.SECONDS);
        final List<IProcess> processesAfter = win32System.listProcessesWithName(settings.targetLauncher().processName());
        if (processesAfter.isEmpty()) return Optional.empty();
        throw new NeedRetryException("Found broken launcher process and it isn't closed - need retry");
    }

    private String processInfo(final IProcess process) {
        final long creationTime = process.getCreationTime();
        return process.pid() + " - [" + process.name().orElse("no name")
                + "] created at " + dateTimeFormatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(creationTime), ZoneId.systemDefault()));
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
                    //startLauncherProcess();
                    clickDesktopIcon();
                })
                .latency(windowAppearingLatency, TimeUnit.MILLISECONDS)
                .doOnEveryFailedIteration(i -> {
                    final String lastsFromStart = new DurationFormatter(i.fromStart()).toStringWithoutZeroParts();
                    if (i.fromLastCheckEnds().toMillis() > windowAppearingDelay) {
                        final List<IProcess> processes = win32System.listProcessesWithName(processName);
                        if (processes.isEmpty()) {
                            logger.warn("{}: launcher window and process not found yet - try start again", lastsFromStart);
                            clickDesktopIcon();
                        } else {
                            logger.info("{}: launcher window not found yet but process has started", lastsFromStart);
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
        logger.info("searching launcher");
        //TODO need regexp based search
        final List<IWindow> allWindows = win32System.findAllWindows(settings.targetLauncher().windowTitle(), null, true);
        return allWindows.stream()
                .filter(window -> {
                    final Rectangle requiredDimensions = settings.targetLauncher().windowDimensions();
                    final Rectangle windowRectangle = window.getWindowRectangle();
                    return requiredDimensions.width() == windowRectangle.width()
                            && requiredDimensions.height() == windowRectangle.height();
                })
                .peek(window -> logger.info("found launcher window {}", window.getSystemId()))
                .map(window -> new LauncherWindowImpl(window, commonWindowService, settings, stamps))
                .map(LauncherWindow.class::cast)
                .findFirst();
    }

    // unsafe operation because of possible anti-cheat detection
    private void startLauncherProcess() {
//        try {
//            ProcessBuilder processBuilder = new ProcessBuilder()
//                    .command(Settings.TargetLauncher.path())
//                    .directory(new File(Settings.TargetLauncher.workingDirectory()));
//            Process process = processBuilder.start();
//            long pid = process.pid();
//            logger.info("process {} started with pid:{}", process.info().toString(), pid);
//        } catch (IOException ex) {
//            throw new CriticalErrorException(ex);
//        }
    }

    private void clickLauncherDesktopIcon() throws InterruptedException {
        win32System.getScreenMouse(settings.application().speedFactor())
                .move(settings.targetLauncher().desktopShortcutLocationPoint())
                .leftClick()
                .leftClick();
    }

    private void detectLauncherInitializationError() {
        if (detectAndCloseInitializationErrorDialog())
            logger.warn("launcher started with error and closed");
    }

    private boolean detectAndCloseInitializationErrorDialog() {
        final Optional<InitializationErrorDialog> maybeInitializationErrorDialog = findInitializationErrorDialog();
        maybeInitializationErrorDialog.ifPresent(initializationErrorDialog -> {
            logger.warn("initialization error dialog found: close it");
            try {
                initializationErrorDialog.clickConfirmButton();
            } catch (InterruptedException e) {
                throw new ControlledInterruptedException(e);
            }
        });
        return maybeInitializationErrorDialog.isPresent();
    }

    private Optional<InitializationErrorDialog> findInitializationErrorDialog() {
        logger.info("searching launcher initialization error dialog");
        final List<IWindow> allWindows = win32System.findAllWindows(settings.targetLauncher().initializationErrorDialogTitle(), null, true);
        return allWindows.stream()
                .filter(w -> {
                    final Rectangle requiredDimensions = settings.targetLauncher().initializationErrorDialogDimensions();
                    final Rectangle windowRect = w.getWindowRectangle();
                    return requiredDimensions.width() == windowRect.width()
                            && requiredDimensions.height() == windowRect.height();
                })
                //FIXME проблема с получением снимков для topmost-окон: неправильные координаты снимка, соответственно валидация не проходит
                //TODO после исправления включить
//                    .filter(w -> {
//                        try {
//                            final boolean present = StampValidator.INSTANCE.validateStampWholeData(w, Stamps.TargetLauncher.initializationErrorDialog()).isPresent();
//                            logger.debug("validating stamp {}", present);
//                            return present;
//                        } catch (InterruptedException e) {
//                            throw new ControlledInterruptedException(e.getMessage(), e);
//                        }
//                    })
                .map((IWindow window) -> new InitializationErrorDialog(window, settings))
                .findFirst();
    }

    private void detectPossibleOverlay() {
        if (uacWindowFactory.findUacOverlayWindow().isPresent())
            throw new CriticalErrorException("UAC window found. Can not confirm UAC request programmatically.");
        commonWindowService.logPossibleOverlay();
    }

    public static class InitializationErrorDialog {

        private final IWindow window;
        private final Settings settings;
        private final Log log;


        public InitializationErrorDialog(final IWindow window, final Settings settings) {
            this.window = window;
            this.settings = settings;
            this.log = new Log(StaticResources.LOGGER, window.getSystemId());
        }

        public void clickConfirmButton() throws InterruptedException {
            log.info("close initialization error dialog");
            getMouse().move(settings.targetLauncher().closeInitializationErrorDialogButtonPoint()).leftClick();
        }

        private IMouse getMouse() {
            return window.getWindowMouse(settings.application().speedFactor());
        }
    }

}