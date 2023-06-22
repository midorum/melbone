package midorum.melbone.window.internal.common;

import com.midorum.win32api.facade.IWindow;
import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.facade.Win32System;
import com.midorum.win32api.win32.IWinUser;
import midorum.melbone.model.exception.CannotGetUserInputException;
import midorum.melbone.model.settings.PropertiesProvider;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.window.internal.util.StaticResources;
import org.apache.logging.log4j.Logger;

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
        return window.getProcessId() + "_" + window.getProcess().getCreationTime();
    }

    public boolean checkIfWindowRendered(final IWindow window) {
        final boolean s = window.hasAndHasNotStyles(IWinUser.WS_VISIBLE, IWinUser.WS_DISABLED);
        final boolean m = window.hasStyles(IWinUser.WS_MINIMIZE);
        final Rectangle clientRect = window.getClientRectangle();
        final boolean r = clientRect.height() > 0 && clientRect.width() > 0;
        logger.debug("window {} attributes are:\n\t> window is visible and not disabled - {};\n\t> window has properly client rect - {}", window.getSystemId(), s, r);
        return s && (m || r);
    }

    public void fixResult(final Result result) {
        logger.debug("fixed result: {}", result);
    }

    public ForegroundWindowSupplier bringForeground(final IWindow window) {
        return new ForegroundWindowSupplier(window);
    }

    public void takeAndSaveWholeScreenShot(final String marker) {
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
