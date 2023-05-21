package midorum.melbone.window.internal.appcountcontrol;

import com.midorum.win32api.facade.Win32System;
import midorum.melbone.model.window.appcountcontrol.ApplicationsCountControlWindow;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.window.internal.common.CommonWindowService;
import midorum.melbone.window.internal.util.StaticResources;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class ApplicationsCountControlWindowFactory {

    private final CommonWindowService commonWindowService;
    private final Win32System win32System;
    private final Settings settings;
    private final Logger logger = StaticResources.LOGGER;

    public ApplicationsCountControlWindowFactory(final CommonWindowService commonWindowService, final Settings settings) {
        this.commonWindowService = commonWindowService;
        this.win32System = commonWindowService.getWin32System();
        this.settings = settings;
    }

    public Optional<ApplicationsCountControlWindow> findWindow() {
        return win32System.findWindow(settings.targetCountControl().windowTitle())
                .map(window -> {
                    logger.info("found window \"{}\" ({})", settings.targetCountControl().windowTitle(), window.getSystemId());
                    return new ApplicationsCountControlWindowImpl(window, commonWindowService, settings);
                });
    }
}
