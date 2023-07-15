package midorum.melbone.window.internal.common;

import com.midorum.win32api.facade.*;
import com.midorum.win32api.win32.IWinUser;
import midorum.melbone.model.exception.CannotGetUserInputException;
import midorum.melbone.model.settings.PropertiesProvider;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.window.internal.util.StaticResources;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class CommonWindowService {

    private final Logger logger;
    private final Settings settings;
    private final Win32System win32System;
    private final StampValidator stampValidator;

    public CommonWindowService(final Settings settings, final PropertiesProvider propertiesProvider) {
        this.logger = StaticResources.LOGGER;
        this.settings = settings;
        this.win32System = Win32System.getInstance();
        this.stampValidator = new StampValidator(win32System, settings, propertiesProvider, new FileServiceProvider());
    }

    public Win32System getWin32System() {
        return win32System;
    }

    public String getUID(final IWindow window) {
        return window.getProcessId() + "_" + window.getProcess().flatMap(IProcess::getCreationTime).getOrHandleError(e -> {
            logger.error("cannot get window process info (window " + window.getSystemId() + ")");
            return System.currentTimeMillis();
        });
    }

    public Either<Boolean> checkIfWindowRendered(final IWindow window) {
        record Result(boolean s, boolean m, boolean r) {
        }
        return window.hasAndHasNotStyles(IWinUser.WS_VISIBLE, IWinUser.WS_DISABLED)
                .flatMap(s -> window.hasStyles(IWinUser.WS_MINIMIZE)
                        .flatMap(m -> window.getClientRectangle().map(rectangle -> rectangle.height() > 0 && rectangle.width() > 0)
                                .map(r -> new Result(s, m, r))))
                .map(result -> {
                    logger.debug("window {} attributes are:\n\t> window is visible and not disabled - {};\n\t> window has properly client rect - {}", window.getSystemId(), result.s, result.r);
                    return result.s && (result.m || result.r);
                });
    }

    public void fixResult(final Result result) {
        logger.debug("fixed result: {}", result);
    }

    public ForegroundWindowSupplier bringForeground(final IWindow window) {
        return new ForegroundWindowSupplier(window);
    }

    public void takeAndSaveWholeScreenShot(final String cause, final String marker) {
        logger.warn("whole screenshot requested (cause={}, marker={})", Objects.requireNonNull(cause), Objects.requireNonNull(marker));
        stampValidator.takeAndSaveWholeScreenShot(marker);
    }

    public enum Result {
        baseAppWindowDisappeared
    }

    public class ForegroundWindowSupplier {

        private final IWindow window;

        ForegroundWindowSupplier(final IWindow window) {
            this.window = window;
        }

        public void andDo(final ForegroundWindowConsumer action) throws InterruptedException, CannotGetUserInputException {
            action.accept(new ForegroundWindow(window, settings, win32System, stampValidator));
        }

        public <R> R andDo(final ForegroundWindowFunction<R> action) throws InterruptedException, CannotGetUserInputException {
            return action.apply(new ForegroundWindow(window, settings, win32System, stampValidator));
        }

        @FunctionalInterface
        public interface ForegroundWindowConsumer {
            void accept(ForegroundWindow t) throws InterruptedException, CannotGetUserInputException;
        }

        @FunctionalInterface
        public interface ForegroundWindowFunction<R> {
            R apply(ForegroundWindow t) throws InterruptedException, CannotGetUserInputException;
        }
    }
}
