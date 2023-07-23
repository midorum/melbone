package midorum.melbone.window.internal.common;

import com.midorum.win32api.facade.*;
import com.midorum.win32api.facade.exception.Win32ApiException;
import com.midorum.win32api.struct.PointFloat;
import com.midorum.win32api.win32.IWinUser;
import dma.flow.Waiting;
import dma.function.VoidAction;
import dma.util.DurationFormatter;
import dma.validation.Validator;
import midorum.melbone.model.exception.CannotGetUserInputException;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.model.settings.stamp.Stamp;
import midorum.melbone.window.internal.util.StaticResources;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ForegroundWindow {

    private static final String WIN_32_LONG_FORMAT = "0x%08x";
    private static final Function<Integer, String> WIN_32_LONG_FORMATTER = i -> String.format(WIN_32_LONG_FORMAT, i);
    private final Logger logger = StaticResources.LOGGER;
    private final IWindow window;
    private final Settings settings;
    private final Mouse mouse;
    private final boolean closeOverlappingWindows;
    private final boolean shotOverlappingWindows;
    private final Set<String> overlappingWindowsToSkip;
    private final Set<String> overlappingWindowsToClose;
    private final int bringWindowForegroundTimeout;
    private final int bringWindowForegroundDelay;
    private final Win32System win32System;
    private final StampValidator stampValidator;
    private final String windowId;

    ForegroundWindow(final IWindow window, final Settings settings, final Win32System win32System, final StampValidator stampValidator) {
        this.window = window;
        this.settings = settings;
        this.win32System = win32System;
        this.stampValidator = stampValidator;
        this.mouse = new Mouse(settings.application(), window);
        this.closeOverlappingWindows = settings.application().closeOverlappingWindows();
        this.shotOverlappingWindows = settings.application().shotOverlappingWindows();
        this.overlappingWindowsToSkip = Set.of(settings.application().overlappingWindowsToSkip());
        this.overlappingWindowsToClose = Set.of(settings.application().overlappingWindowsToClose());
        this.bringWindowForegroundTimeout = settings.application().bringWindowForegroundTimeout();
        this.bringWindowForegroundDelay = settings.application().bringWindowForegroundDelay();
        this.windowId = window.getSystemId();
    }

    public Mouse getMouse() throws InterruptedException, CannotGetUserInputException {
        final String logMarker = getLogMarker();
        try {
            if (!bringWindowForeground(logMarker)) throw getCannotGetUserInputException(logMarker);
            return mouse;
        } catch (Win32ApiException e) {
            throw new CannotGetUserInputException(e.getMessage(), e);
        }
    }

    public IKeyboard getKeyboard() throws InterruptedException, CannotGetUserInputException {
        final String logMarker = getLogMarker();
        try {
            if (!bringWindowForeground(logMarker)) throw getCannotGetUserInputException(logMarker);
            return window.getKeyboard();
        } catch (Win32ApiException e) {
            throw new CannotGetUserInputException(e.getMessage(), e);
        }
    }

    public StateWaiting waiting() {
        return new StateWaiting();
    }

    private boolean bringWindowForeground(final String marker) throws InterruptedException, Win32ApiException {
        try {
            final boolean windowIsForeground = new Waiting()
                    .timeout(bringWindowForegroundTimeout, TimeUnit.MILLISECONDS)
                    .withDelay(bringWindowForegroundDelay, TimeUnit.MILLISECONDS)
                    .waitForBoolean(() -> {
                        try {
                            return window.bringForeground();
                        } catch (Win32ApiException e) {
                            throw new ControlledWin32ApiException(e);
                        }
                    });
            if (windowIsForeground) {
                findPossibleTopmostAndCloseIfNecessary(marker);
            } else {
                logger.warn("Can not bring window {} foreground. Maybe another window lays over and has user input.", windowId);
                findPossibleOverlay(marker).ifPresent(IWindow::close);
            }
            return windowIsForeground;
        } catch (ControlledWin32ApiException e) {
            throw (Win32ApiException) e.getCause();
        }
    }

    private boolean areRectanglesOverlay(final Rectangle r1, final Rectangle r2) {
        return r1.left() <= r2.right() && r2.left() <= r1.right() && r1.top() <= r2.bottom() && r2.top() <= r1.bottom();
    }

    private void findPossibleTopmostAndCloseIfNecessary(final String marker) throws Win32ApiException {
        final String topmostMarker = "topmost_" + marker;
        final Function<Rectangle, Boolean> rectangleChecker = window.getWindowRectangle()
                .map(wr -> (Function<Rectangle, Boolean>) r -> areRectanglesOverlay(wr, r))
                .getOrThrow();
        final Predicate<IWindow> isNotSame = window.getProcessId()
                .map(processId -> (Predicate<IWindow>) w -> w.getProcessId().map(pid -> !pid.equals(processId))
                        .getOrHandleError(e -> {
                            logger.warn("cannot get window attributes (" + getWindowInfo(w) + ") - skip", e);
                            return false;
                        })).getOrThrow();
        win32System.listAllWindows().stream()
                .filter(isNotSame)
                .filter(isTopmost(rectangleChecker))
                .forEach(processTopmost(topmostMarker));
    }

    private Optional<IWindow> findPossibleOverlay(final String marker) {
        final String overlayMarker = "overlay_" + marker;
        final Optional<IWindow> foregroundWindow = win32System.getForegroundWindow();
        foregroundWindow.ifPresentOrElse(window -> {
            logger.warn("found overlay window: marker={}, {}", overlayMarker, getWindowExtendedInfo(window));
            stampValidator.takeAndSaveWholeScreenShot(overlayMarker);
        }, () -> win32System.listAllWindows().forEach(window -> {
            if (window.isVisible()) {
                logger.debug("found non-foreground but visible window: marker={}, {}", overlayMarker, getWindowExtendedInfo(window));
            }
        }));
        return foregroundWindow;
    }

    private Function<Boolean, Boolean> and(final Boolean b) {
        return b1 -> b && b1;
    }

    private Predicate<IWindow> isTopmost(final Function<Rectangle, Boolean> rectangleChecker) {
        return w -> w.getProcess().map(IProcess::name).map(name -> name.map(s -> !overlappingWindowsToSkip.contains(s)).orElse(true))
                .flatMap(result -> w.hasStyles(IWinUser.WS_VISIBLE).map(and(result)))
                .flatMap(result -> w.hasExtendedStyles(IWinUser.WS_EX_TOPMOST).map(and(result)))
                .flatMap(result -> w.getWindowRectangle().map(rectangleChecker).map(and(result)))
                .getOrHandleError(e -> {
                    logger.warn("cannot get window attributes (" + getWindowInfo(w) + ") - skip", e);
                    return false;
                });
    }

    private Consumer<IWindow> processTopmost(final String topmostMarker) {
        return topmost -> {
            final String topmostId = topmost.getSystemId();
            logger.warn("Target window was brought to the foreground and has user input. But there is a topmost window which may overlap it: marker={}, id={}, {}",
                    topmostMarker, topmostId, getWindowExtendedInfo(topmost));
            if (shotOverlappingWindows) stampValidator.takeAndSaveWholeScreenShot(topmostMarker);
            if (closeOverlappingWindows && topmost.getProcess().map(IProcess::name).map(name -> name.map(overlappingWindowsToClose::contains).orElse(false)).getOrHandleError(e -> {
                logger.error("cannot get process info for window " + topmostId, e);
                return false;
            })) {
                logger.warn("setting \"closeOverlappingWindows\" is on and process is in the blacklist - close the topmost window {}", topmostId);
                topmost.close();
            } else {
                logger.warn("setting \"closeOverlappingWindows\" is off or process is not in the blacklist - the topmost window {} won't be closed but can hinder the target one", topmostId);
            }
        };
    }

    private String getLogMarker() {
        return Long.toString(System.currentTimeMillis());
    }

    private String getWindowInfo(final IWindow window) {
        return "pid=" + window.getProcessId()
                + ", process=" + window.getProcess().map(IProcess::name)
                + ", title=" + window.getText()
                + ", class=" + window.getClassName();
    }

    private String getWindowExtendedInfo(final IWindow window) {
        return getWindowInfo(window)
                + ", style=" + window.getStyle().map(WIN_32_LONG_FORMATTER)
                + ", extendedStyle=" + window.getExtendedStyle().map(WIN_32_LONG_FORMATTER)
                + ", windowRect=" + window.getWindowRectangle();
    }

    private CannotGetUserInputException getCannotGetUserInputException(final String logMarker) {
        stampValidator.takeAndSaveWholeScreenShot(logMarker);
        return new CannotGetUserInputException("cannot get user input in target widow " + windowId + " (marker=" + logMarker + ")");
    }

    public class StateWaiting {

        private int timeout;
        private int delay;
        private int latency;
        private PointFloat mousePosition;
        private Waiting.EmptyConsumer startFromConsumer;
        private Waiting.IterationConsumer failedIterationConsumer;
        private PointFloat mouseClickPoint;
        private HotKey hotKey;
        private boolean encloseWithHotKey = false;
        private boolean logFailedStamps = false;
        private String logMarker;

        public StateWaiting withTimeout(final int timeout) {
            this.timeout = timeout;
            return this;
        }

        public StateWaiting withDelay(final int delay) {
            this.delay = delay;
            return this;
        }

        public StateWaiting withLatency(final int latency) {
            this.latency = latency;
            return this;
        }

        public StateWaiting withMousePosition(final PointFloat mousePosition) {
            this.mousePosition = mousePosition;
            return this;
        }

        public StateWaiting startFromConsumer(final Waiting.EmptyConsumer startFromConsumer) {
            this.startFromConsumer = startFromConsumer;
            return this;
        }

        public StateWaiting doOnEveryFailedIteration(final Waiting.IterationConsumer failedIterationConsumer) {
            this.failedIterationConsumer = failedIterationConsumer;
            return this;
        }

        public StateWaiting logFailedStamps() {
            this.logFailedStamps = true;
            return this;
        }

        public StateWaiting logFailedStampsWithMarker(final String marker) {
            this.logFailedStamps = true;
            this.logMarker = Validator.checkNotNull(marker).orThrowForSymbol("marker");
            return this;
        }

        public StateWaiting usingHotKey(final HotKey hotKey) {
            this.hotKey = hotKey;
            return this;
        }

        public StateWaiting usingHotKeyEnclose(final HotKey hotKey) {
            this.hotKey = hotKey;
            this.encloseWithHotKey = true;
            return this;
        }

        public StateWaiting usingMouseClickAt(final PointFloat point) {
            this.mouseClickPoint = point;
            return this;
        }

        public Optional<Stamp> forStamp(final Stamp stamp) throws InterruptedException, CannotGetUserInputException {
            return new Instance(timeout, delay, latency, startFromConsumer, failedIterationConsumer, mouseClickPoint, hotKey, encloseWithHotKey, logFailedStamps, logMarker, new AdjustingMouse(settings.application(), win32System, window, mousePosition))
                    .waitFor(stamp);
        }

        public Optional<Stamp> forAnyStamp(final Stamp... stamps) throws CannotGetUserInputException, InterruptedException {
            return new Instance(timeout, delay, latency, startFromConsumer, failedIterationConsumer, mouseClickPoint, hotKey, encloseWithHotKey, logFailedStamps, logMarker, new AdjustingMouse(settings.application(), win32System, window, mousePosition))
                    .waitForAny(stamps);
        }

        public boolean forStampDisappearing(final Stamp stamp) throws CannotGetUserInputException, InterruptedException {
            return new Instance(timeout, delay, latency, startFromConsumer, failedIterationConsumer, mouseClickPoint, hotKey, encloseWithHotKey, logFailedStamps, logMarker, new AdjustingMouse(settings.application(), win32System, window, mousePosition))
                    .waitForStampDisappearing(stamp);
        }

        private class Instance {

            private final int timeout;
            private final int delay;
            private final int latency;
            private final Waiting.EmptyConsumer startFromConsumer;
            private final Waiting.IterationConsumer failedIterationConsumer;
            private final PointFloat mouseClickPoint;
            private final HotKey hotKey;
            private final boolean encloseWithHotKey;
            private final boolean logFailedStamps;
            private final String logMarker;
            private final AdjustingMouse mouse;

            private Instance(final int timeout,
                             final int delay,
                             final int latency,
                             final Waiting.EmptyConsumer startFromConsumer,
                             final Waiting.IterationConsumer failedIterationConsumer,
                             final PointFloat mouseClickPoint,
                             final HotKey hotKey,
                             final boolean encloseWithHotKey,
                             final boolean logFailedStamps,
                             final String logMarker,
                             final AdjustingMouse mouse) {
                this.timeout = timeout;
                this.delay = delay;
                this.latency = latency;
                this.startFromConsumer = startFromConsumer;
                this.failedIterationConsumer = failedIterationConsumer;
                this.mouseClickPoint = mouseClickPoint;
                this.hotKey = hotKey;
                this.encloseWithHotKey = encloseWithHotKey;
                this.logFailedStamps = logFailedStamps;
                this.logMarker = Validator.checkNotNull(logMarker).orDefault(getLogMarker());
                this.mouse = mouse;
            }

            public Optional<Stamp> waitFor(final Stamp stamp) throws InterruptedException, CannotGetUserInputException {
                final Stamp stampChecked = Validator.checkNotNull(stamp).orThrowForSymbol("stamp");
                final String stampsString = stringifyStamp(stampChecked);
                final Rectangle windowRectShouldBe = stampChecked.windowRect();
                final Supplier<Optional<Stamp>> stampSupplier = () -> stampValidator.validateStamp(stampChecked);
                final VoidAction stampsMismatchingLogger = () -> stampValidator.logStampsMismatching(logMarker, stampChecked);
                return waitForStamp(stampsString, windowRectShouldBe, stampSupplier, stampsMismatchingLogger);
            }

            public Optional<Stamp> waitForAny(final Stamp... stamps) throws InterruptedException, CannotGetUserInputException {
                final Stamp[] stampsChecked = Validator.checkNotNull(stamps).andCheckNot(ss -> ss.length == 0).orThrowForSymbol("stamps");
                final String stampsString = stringifyStamps(stampsChecked);
                final Rectangle windowRectShouldBe = stampsChecked[0].windowRect();
                final Supplier<Optional<Stamp>> stampSupplier = () -> stampValidator.findFirstValidStamp(stampsChecked);
                final VoidAction stampsMismatchingLogger = () -> stampValidator.logStampsMismatching(logMarker, stampsChecked);
                return waitForStamp(stampsString, windowRectShouldBe, stampSupplier, stampsMismatchingLogger);
            }

            public boolean waitForStampDisappearing(final Stamp stamp) throws CannotGetUserInputException, InterruptedException {
                final Stamp stampChecked = Validator.checkNotNull(stamp).orThrowForSymbol("stamp");
                final String stampsString = stringifyStamp(stampChecked);
                final Rectangle windowRectShouldBe = stampChecked.windowRect();
                final Supplier<Optional<Stamp>> stampSupplier = () -> stampValidator.validateStamp(stampChecked);
                return waitForStampDisappearing(stampsString, windowRectShouldBe, stampSupplier);
            }

            private Optional<Stamp> waitForStamp(final String stampsString, final Rectangle windowRectShouldBe, final Supplier<Optional<Stamp>> stampSupplier, final VoidAction stampsMismatchingLogger) throws InterruptedException, CannotGetUserInputException {
                logger.debug("[{}] wait for stamp(s) {}", windowId, stampsString);
                final IKeyboard keyboard = window.getKeyboard();
                try {
                    final Optional<Stamp> foundStamp = new Waiting()
                            .timeout(timeout, TimeUnit.MILLISECONDS)
                            .withDelay(delay, TimeUnit.MILLISECONDS)
                            .startFrom(Validator.checkNotNull(startFromConsumer).orDefault(() -> {
                            }))
                            .latency(latency, TimeUnit.MILLISECONDS)
                            .doOnEveryFailedIteration(i -> {
                                logger.debug("[{}] {}: stamp(s) {} not found yet", windowId, new DurationFormatter(i.fromStart()).toStringWithoutZeroParts(), stampsString);
                                if (failedIterationConsumer != null) failedIterationConsumer.accept(i);
                                try {
                                    if (hotKey != null) keyboard.enterHotKey(hotKey);
                                    if (mouseClickPoint != null) mouse.clickAtPoint(mouseClickPoint);
                                } catch (Win32ApiException e) {
                                    throw new ControlledWin32ApiException(e);
                                }
                            })
                            .waitFor(() -> {
                                try {
                                    if (!adjustWindow(windowRectShouldBe.left(), windowRectShouldBe.top()))
                                        return Optional.empty();
                                    if (!window.getWindowRectangle().map(r -> rectanglesAreEquals(r, windowRectShouldBe)).getOrThrow())
                                        return Optional.empty();
                                    mouse.adjust();
                                    return stampSupplier.get();
                                } catch (Win32ApiException e) {
                                    throw new ControlledWin32ApiException(e);
                                }
                            });
                    if (!window.isForeground()) throw getCannotGetUserInputException(logMarker);
                    foundStamp.ifPresentOrElse(s -> {
                                try {
                                    logger.debug("[{}] found stamp {}", windowId, stringifyStamp(s));
                                    if (encloseWithHotKey) {
                                        keyboard.enterHotKey(hotKey);
                                    }
                                } catch (Win32ApiException e) {
                                    throw new ControlledWin32ApiException(e);
                                }
                            },
                            () -> {
                                try {
                                    if (logFailedStamps) {
                                        mouse.adjust();
                                        stampsMismatchingLogger.perform();
                                        logger.warn("[{}] stamp(s) {} not found (marker={})", windowId, stampsString, logMarker);
                                    } else
                                        logger.debug("[{}] stamp(s) {} not found", windowId, stampsString);
                                } catch (Win32ApiException e) {
                                    throw new ControlledWin32ApiException(e);
                                }
                            });
                    return foundStamp;
                } catch (ControlledWin32ApiException e) {
                    final Throwable cause = e.getCause();
                    throw new CannotGetUserInputException(cause.getMessage(), cause);
                }
            }

            private boolean waitForStampDisappearing(final String stampsString, final Rectangle windowRectShouldBe, final Supplier<Optional<Stamp>> stampSupplier) throws InterruptedException, CannotGetUserInputException {
                logger.debug("[{}] wait for stamp(s) {}", windowId, stampsString);
                final IKeyboard keyboard = window.getKeyboard();
                try {
                    final boolean result = new Waiting()
                            .timeout(timeout, TimeUnit.MILLISECONDS)
                            .withDelay(delay, TimeUnit.MILLISECONDS)
                            .startFrom(Validator.checkNotNull(startFromConsumer).orDefault(() -> {
                            }))
                            .latency(latency, TimeUnit.MILLISECONDS)
                            .doOnEveryFailedIteration(i -> {
                                logger.debug("[{}] {}: stamp(s) {} still present", windowId, new DurationFormatter(i.fromStart()).toStringWithoutZeroParts(), stampsString);
                                if (failedIterationConsumer != null) failedIterationConsumer.accept(i);
                                try {
                                    if (hotKey != null) keyboard.enterHotKey(hotKey);
                                    if (mouseClickPoint != null) mouse.clickAtPoint(mouseClickPoint);
                                } catch (Win32ApiException e) {
                                    throw new ControlledWin32ApiException(e);
                                }
                            })
                            .waitForBoolean(() -> {
                                try {
                                    if (!adjustWindow(windowRectShouldBe.left(), windowRectShouldBe.top()))
                                        return false;
                                    if (!window.getWindowRectangle().map(r -> rectanglesAreEquals(r, windowRectShouldBe)).getOrThrow())
                                        return false;
                                    mouse.adjust();
                                    return stampSupplier.get().isEmpty();
                                } catch (Win32ApiException e) {
                                    throw new ControlledWin32ApiException(e);
                                }
                            });
                    if (!window.isForeground()) throw getCannotGetUserInputException(logMarker);
                    if (result) logger.debug("[{}] stamp has disappeared {}", windowId, stampsString);
                    else logger.warn("[{}] stamp has not disappeared {}", windowId, stampsString);
                    return result;
                } catch (ControlledWin32ApiException e) {
                    final Throwable cause = e.getCause();
                    throw new CannotGetUserInputException(cause.getMessage(), cause);
                }
            }

            private String stringifyStamp(final Stamp stamp) {
                return stamp.key().internal().groupName() + "." + stamp.key().name();
            }

            private String stringifyStamps(final Stamp[] stamps) {
                return Arrays.stream(stamps).map(this::stringifyStamp).collect(Collectors.joining(",", "[", "]"));
            }

            private boolean rectanglesAreEquals(final Rectangle windowRectangle, final Rectangle stampRectangle) {
                return Validator.checkNotNull(windowRectangle)
                        .andMap(r -> r.equals(stampRectangle))
                        .orThrow("window rectangle is null");
            }

            private boolean adjustWindow(final int x, final int y) throws InterruptedException, Win32ApiException {
                if (!bringWindowForeground(logMarker)) return false;
                window.moveWindow(x, y);
                return true;
            }
        }

    }

    private static class ControlledWin32ApiException extends RuntimeException {

        public ControlledWin32ApiException(final Win32ApiException cause) {
            super(cause);
        }
    }

}
