package midorum.melbone.window.internal.common;

import com.midorum.win32api.facade.IWindow;
import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.facade.Win32System;
import com.midorum.win32api.win32.IWinUser;
import midorum.melbone.model.settings.PropertiesProvider;
import midorum.melbone.model.settings.stamp.Stamp;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.window.internal.util.StaticResources;
import org.apache.logging.log4j.Logger;

public class CommonWindowService {

    private final Logger logger;
    private final Win32System win32System;
    private final StampValidator stampValidator;

    public CommonWindowService(final Settings settings, final PropertiesProvider propertiesProvider) {
        this.logger = StaticResources.LOGGER;
        this.win32System = Win32System.getInstance();
        this.stampValidator = new StampValidator(win32System, settings, propertiesProvider);
    }

    public Win32System getWin32System() {
        return win32System;
    }

    public StampValidator getStampValidator() {
        return stampValidator;
    }

    public String getUID(final IWindow window) {
        return window.getProcessId() + "_" + window.getProcess().getCreationTime();
    }

    public boolean bringWindowForeground(final IWindow window) throws InterruptedException {
        return stampValidator.bringWindowForeground(window);
    }

    public boolean checkIfWindowRendered(final IWindow window) {
        final boolean s = window.hasAndHasNotStyles(IWinUser.WS_VISIBLE, IWinUser.WS_DISABLED);
        final boolean m = window.hasStyles(IWinUser.WS_MINIMIZE);
        final Rectangle clientRect = window.getClientRectangle();
        final boolean r = clientRect.height() > 0 && clientRect.width() > 0;
        logger.debug("base window {} attributes:\n\t> window is visible and not disabled - {};\n\t> window has properly client rect - {}", window.getSystemId(), s, r);
        return s && (m || r);
    }

    public void logPossibleOverlay() {
        stampValidator.logPossibleOverlay();
    }

    public String logFailedStamps(final IWindow window, final Stamp... stamps) throws InterruptedException {
        return stampValidator.logFailedStamps(window, stamps);
    }

    public void fixResult(final Result result) {
        logger.debug("fixed result: {}", result);
    }

    public enum Result {
        baseAppWindowDisappeared
    }
}
